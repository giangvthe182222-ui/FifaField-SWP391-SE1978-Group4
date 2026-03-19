package Controller.Blog;

import DAO.BlogDAO;
import Models.Blog;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

@WebServlet(name = "BlogFormServlet", urlPatterns = {"/blog/create", "/blog/edit"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class BlogFormServlet extends HttpServlet {

    private static final long MAX_IMAGE_SIZE = 5L * 1024L * 1024L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=blogs");
            return;
        }

        String role = getRole(user);
        if (!"staff".equals(role) && !"manager".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or manager can manage blogs.");
            return;
        }

        String uri = request.getRequestURI();
        boolean isEdit = uri.endsWith("/blog/edit");
        BlogDAO dao = new BlogDAO();

        try {
            if (isEdit) {
                UUID blogId = toUuid(request.getParameter("id"));
                if (blogId == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid blog id.");
                    return;
                }

                Blog blog = dao.getBlogById(blogId);
                if (blog == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Blog not found.");
                    return;
                }

                if (!canEdit(role, user.getUserId(), blog)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot edit this blog.");
                    return;
                }

                request.setAttribute("blog", blog);
                request.setAttribute("mode", "edit");
            } else {
                request.setAttribute("mode", "create");
            }

            request.setAttribute("roleName", role);
            request.getRequestDispatcher("/View/Blog/blog-form.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Cannot load blog form", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=blogs");
            return;
        }

        String role = getRole(user);
        if (!"staff".equals(role) && !"manager".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or manager can manage blogs.");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String uri = request.getRequestURI();
        boolean isEdit = uri.endsWith("/blog/edit");

        String title = safeTrim(request.getParameter("title"));
        String summary = safeTrim(request.getParameter("summary"));
        String content = safeTrim(request.getParameter("content"));
        String existingImageUrl = safeTrim(request.getParameter("existingImageUrl"));
        String submitType = safeTrim(request.getParameter("submitType"));

        if (title == null || content == null) {
            request.setAttribute("error", "Tiêu đề và nội dung là bắt buộc.");
            request.setAttribute("mode", isEdit ? "edit" : "create");
            request.setAttribute("roleName", role);
            request.setAttribute("blog", createFormBlog(request, title, summary, content, existingImageUrl));
            request.getRequestDispatcher("/View/Blog/blog-form.jsp").forward(request, response);
            return;
        }

        UploadResult uploadResult = processImageUpload(request);
        if (uploadResult.getError() != null) {
            request.setAttribute("error", uploadResult.getError());
            request.setAttribute("mode", isEdit ? "edit" : "create");
            request.setAttribute("roleName", role);
            request.setAttribute("blog", createFormBlog(request, title, summary, content, existingImageUrl));
            request.getRequestDispatcher("/View/Blog/blog-form.jsp").forward(request, response);
            return;
        }

        BlogDAO dao = new BlogDAO();

        try {
            if (!isEdit) {
                Blog blog = new Blog();
                blog.setTitle(title);
                blog.setSummary(summary);
                blog.setContent(content);
                blog.setImageUrl(uploadResult.getSavedPath());
                blog.setCreatedBy(user.getUserId());

                String status = resolveStatusForCreate(role, submitType);
                blog.setStatus(status);

                if ("approved".equals(status)) {
                    blog.setApprovedBy(user.getUserId());
                    blog.setPublishedAt(LocalDateTime.now());
                }

                UUID blogId = dao.createBlog(blog);
                response.sendRedirect(request.getContextPath() + "/blog/detail?id=" + blogId);
                return;
            }

            UUID blogId = toUuid(request.getParameter("blogId"));
            if (blogId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid blog id.");
                return;
            }

            Blog current = dao.getBlogById(blogId);
            if (current == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Blog not found.");
                return;
            }

            if (!canEdit(role, user.getUserId(), current)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot edit this blog.");
                return;
            }

            current.setTitle(title);
            current.setSummary(summary);
            current.setContent(content);
            current.setImageUrl(uploadResult.getSavedPath() != null ? uploadResult.getSavedPath() : current.getImageUrl());

            String status = resolveStatusForEdit(role, submitType);
            current.setStatus(status);

            if ("approved".equals(status)) {
                current.setApprovedBy(user.getUserId());
                if (current.getPublishedAt() == null) {
                    current.setPublishedAt(LocalDateTime.now());
                }
            } else {
                current.setApprovedBy(null);
                current.setPublishedAt(null);
            }

            dao.updateBlog(current);
            response.sendRedirect(request.getContextPath() + "/blog/detail?id=" + current.getBlogId());
        } catch (SQLException ex) {
            throw new ServletException("Cannot save blog", ex);
        }
    }

    private boolean canEdit(String role, UUID userId, Blog blog) {
        if ("manager".equals(role)) {
            return true;
        }
        if (!"staff".equals(role)) {
            return false;
        }
        if (blog.getCreatedBy() == null || !blog.getCreatedBy().equals(userId)) {
            return false;
        }
        return !"approved".equalsIgnoreCase(blog.getStatus());
    }

    private String resolveStatusForCreate(String role, String submitType) {
        if ("manager".equals(role)) {
            if ("save".equalsIgnoreCase(submitType)) {
                return "draft";
            }
            return "approved";
        }

        if ("submit".equalsIgnoreCase(submitType)) {
            return "pending";
        }
        return "draft";
    }

    private String resolveStatusForEdit(String role, String submitType) {
        if ("manager".equals(role)) {
            if ("save".equalsIgnoreCase(submitType)) {
                return "draft";
            }
            return "approved";
        }

        if ("submit".equalsIgnoreCase(submitType)) {
            return "pending";
        }
        return "draft";
    }

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    private String getRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getRoleName() == null) {
            return "";
        }
        return user.getRole().getRoleName().trim().toLowerCase();
    }

    private UUID toUuid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UploadResult processImageUpload(HttpServletRequest request) throws IOException, ServletException {
        Part imagePart = request.getPart("imageFile");
        if (imagePart == null || imagePart.getSize() <= 0) {
            return UploadResult.success(null);
        }

        if (imagePart.getSize() > MAX_IMAGE_SIZE) {
            return UploadResult.error("Ảnh vượt quá dung lượng tối đa 5MB.");
        }

        String contentType = imagePart.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            return UploadResult.error("File tải lên phải là ảnh hợp lệ.");
        }

        String submittedName = imagePart.getSubmittedFileName();
        if (submittedName == null || submittedName.trim().isEmpty()) {
            return UploadResult.error("Không đọc được tên file ảnh.");
        }

        String originalName = Paths.get(submittedName).getFileName().toString();
        int dotIndex = originalName.lastIndexOf('.');
        String extension = dotIndex >= 0 ? originalName.substring(dotIndex + 1).toLowerCase() : "";

        if (!("jpg".equals(extension)
                || "jpeg".equals(extension)
                || "png".equals(extension)
                || "gif".equals(extension)
                || "webp".equals(extension))) {
            return UploadResult.error("Định dạng ảnh không được hỗ trợ. Chỉ chấp nhận jpg, jpeg, png, gif, webp.");
        }

        String relativeFolder = "assets/img/blog";
        String absoluteFolder = getServletContext().getRealPath("/" + relativeFolder);
        if (absoluteFolder == null) {
            return UploadResult.error("Không thể xác định thư mục lưu ảnh trên máy chủ.");
        }

        File uploadDir = new File(absoluteFolder);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            return UploadResult.error("Không thể tạo thư mục lưu ảnh.");
        }

        String safeBaseName = dotIndex >= 0 ? originalName.substring(0, dotIndex) : originalName;
        safeBaseName = safeBaseName.replaceAll("[^a-zA-Z0-9-_]", "_");
        if (safeBaseName.length() > 60) {
            safeBaseName = safeBaseName.substring(0, 60);
        }

        String storedFileName = UUID.randomUUID() + "_" + safeBaseName + "." + extension;
        String relativePath = relativeFolder + "/" + storedFileName;
        imagePart.write(getServletContext().getRealPath("/") + relativePath);
        return UploadResult.success(relativePath);
    }

    private Blog createFormBlog(HttpServletRequest request, String title, String summary, String content, String imageUrl) {
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setSummary(summary);
        blog.setContent(content);
        blog.setImageUrl(imageUrl);
        blog.setBlogId(toUuid(request.getParameter("blogId")));
        return blog;
    }

    private static class UploadResult {

        private final String savedPath;
        private final String error;

        private UploadResult(String savedPath, String error) {
            this.savedPath = savedPath;
            this.error = error;
        }

        public static UploadResult success(String savedPath) {
            return new UploadResult(savedPath, null);
        }

        public static UploadResult error(String error) {
            return new UploadResult(null, error);
        }

        public String getSavedPath() {
            return savedPath;
        }

        public String getError() {
            return error;
        }
    }
}
