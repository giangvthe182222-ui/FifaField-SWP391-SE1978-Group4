package Controller.Blog;

import DAO.BlogDAO;
import Models.Blog;
import Models.BlogComment;
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

@WebServlet(name = "BlogDetailServlet", urlPatterns = {"/blog/detail"})
public class BlogDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=blogs");
            return;
        }

        UUID blogId = toUuid(request.getParameter("id"));
        if (blogId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid blog id.");
            return;
        }

        String role = getRole(user);
        BlogDAO dao = new BlogDAO();

        try {
            Blog blog = dao.getBlogDetailForRole(blogId, user.getUserId(), role);
            if (blog == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Blog not found or inaccessible.");
                return;
            }

            List<BlogComment> comments = dao.getCommentsByBlogId(blogId, user.getUserId());
            request.setAttribute("blog", blog);
            request.setAttribute("comments", comments);
            request.setAttribute("roleName", role);
            request.setAttribute("isManager", "manager".equals(role));
            request.getRequestDispatcher("/View/Blog/blog-detail.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("Cannot load blog detail", ex);
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
}
