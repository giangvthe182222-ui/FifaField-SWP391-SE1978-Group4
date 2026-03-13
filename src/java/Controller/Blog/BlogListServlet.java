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
import java.util.List;
import java.util.UUID;

@WebServlet(name = "BlogListServlet", urlPatterns = {"/blogs", "/staff/blogs", "/manager/blogs"})
public class BlogListServlet extends HttpServlet {

    private static final int PAGE_SIZE = 9;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=blogs");
            return;
        }

        String role = getRole(user);
        String uri = request.getRequestURI();

        if (uri.contains("/manager/") && !"manager".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager role required.");
            return;
        }

        if (uri.contains("/staff/") && !"staff".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Staff role required.");
            return;
        }

        int page = parseInt(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        String status = trimToNull(request.getParameter("status"));
        String keyword = trimToNull(request.getParameter("q"));

        BlogDAO dao = new BlogDAO();
        try {
            int totalItems = dao.countBlogsForRole(user.getUserId(), role, status, keyword);
            int totalPages = Math.max(1, (totalItems + PAGE_SIZE - 1) / PAGE_SIZE);
            if (page > totalPages) {
                page = totalPages;
            }

            List<Blog> blogs = dao.getBlogsForRole(user.getUserId(), role, page, PAGE_SIZE, status, keyword);

            request.setAttribute("blogs", blogs);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", totalItems);
            request.setAttribute("statusFilter", status == null ? "all" : status);
            request.setAttribute("keyword", keyword == null ? "" : keyword);
            request.setAttribute("listPath", request.getContextPath() + request.getServletPath());
            request.setAttribute("roleName", role);
            request.setAttribute("canCreate", "staff".equals(role) || "manager".equals(role));
            request.setAttribute("canManage", "manager".equals(role));

            request.getRequestDispatcher("/View/Blog/blog-list.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Cannot load blog list", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/blogs");
            return;
        }

        String role = getRole(user);
        if (!"manager".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager role required.");
            return;
        }

        String action = trimToNull(request.getParameter("action"));
        UUID blogId = toUuid(request.getParameter("blogId"));
        if (action == null || blogId == null) {
            response.sendRedirect(request.getContextPath() + "/manager/blogs");
            return;
        }

        BlogDAO dao = new BlogDAO();
        try {
            if ("approve".equalsIgnoreCase(action)) {
                dao.approveBlog(blogId, user.getUserId());
            } else if ("reject".equalsIgnoreCase(action)) {
                dao.rejectBlog(blogId, user.getUserId());
            } else if ("delete".equalsIgnoreCase(action)) {
                dao.deleteBlog(blogId);
            }
            response.sendRedirect(request.getContextPath() + "/manager/blogs");
        } catch (SQLException ex) {
            throw new ServletException("Cannot process manager blog action", ex);
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

    private int parseInt(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
