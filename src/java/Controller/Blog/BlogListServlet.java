package Controller.Blog;

import DAO.BlogDAO;
import Models.Blog;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "BlogListServlet", urlPatterns = {"/blogs", "/staff/blogs", "/manager/blogs"})
public class BlogListServlet extends BaseBlogServlet {

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

        if (uri.contains("/manager/") && !ROLE_MANAGER.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager role required.");
            return;
        }

        if (uri.contains("/staff/") && !ROLE_STAFF.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Staff role required.");
            return;
        }

        int page = parseInt(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        String status = safeTrim(request.getParameter("status"));
        String keyword = safeTrim(request.getParameter("q"));

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
            request.setAttribute("canCreate", ROLE_STAFF.equals(role) || ROLE_MANAGER.equals(role));
            request.setAttribute("canManage", ROLE_MANAGER.equals(role));

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
        if (!ROLE_MANAGER.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager role required.");
            return;
        }

        String action = safeTrim(request.getParameter("action"));
        UUID blogId = toUuid(request.getParameter("blogId"));
        if (action == null || blogId == null) {
            response.sendRedirect(request.getContextPath() + "/manager/blogs");
            return;
        }

        BlogDAO dao = new BlogDAO();
        try {
            // Luong manager: xu ly action tren bai viet roi quay ve danh sach.
            handleManagerAction(dao, action, blogId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/manager/blogs");
        } catch (SQLException ex) {
            throw new ServletException("Cannot process manager blog action", ex);
        }
    }

    private void handleManagerAction(BlogDAO dao, String action, UUID blogId, UUID managerId) throws SQLException {
        if ("approve".equalsIgnoreCase(action)) {
            dao.approveBlog(blogId, managerId);
            return;
        }

        if ("reject".equalsIgnoreCase(action)) {
            dao.rejectBlog(blogId, managerId);
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            dao.deleteBlog(blogId);
        }
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
}
