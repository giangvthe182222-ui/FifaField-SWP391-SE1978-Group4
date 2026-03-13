package Controller.Blog;

import DAO.BlogDAO;
import Models.Blog;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(name = "BlogCommentServlet", urlPatterns = {"/blog/comment/add", "/blog/comment/delete"})
public class BlogCommentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=blogs");
            return;
        }

        String role = getRole(user);
        if (!"customer".equals(role) && !"staff".equals(role) && !"manager".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Role not allowed.");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String uri = request.getRequestURI();
        UUID blogId = toUuid(request.getParameter("blogId"));
        if (blogId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid blog id.");
            return;
        }

        BlogDAO dao = new BlogDAO();

        try {
            Blog visibleBlog = dao.getBlogDetailForRole(blogId, user.getUserId(), role);
            if (visibleBlog == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Blog not accessible.");
                return;
            }

            if (uri.endsWith("/add")) {
                String content = safeTrim(request.getParameter("content"));
                if (content != null) {
                    if (content.length() > 1000) {
                        content = content.substring(0, 1000);
                    }
                    UUID parentCommentId = toUuid(request.getParameter("parentCommentId"));
                    if (parentCommentId != null && !dao.commentBelongsToBlog(parentCommentId, blogId)) {
                        parentCommentId = null;
                    }
                    dao.addComment(blogId, user.getUserId(), parentCommentId, content);
                }
            } else if (uri.endsWith("/delete")) {
                UUID commentId = toUuid(request.getParameter("commentId"));
                if (commentId != null) {
                    boolean isManager = "manager".equals(role);
                    boolean isOwner = dao.isCommentOwner(commentId, user.getUserId());
                    if (!isManager && !isOwner) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only manager or comment owner can delete this comment.");
                        return;
                    }
                    dao.deleteComment(commentId);
                }
            }

            response.sendRedirect(request.getContextPath() + "/blog/detail?id=" + blogId);
        } catch (SQLException ex) {
            throw new ServletException("Cannot handle blog comment action", ex);
        }
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
}
