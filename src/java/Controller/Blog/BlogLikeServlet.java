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
import java.util.UUID;

@WebServlet(name = "BlogLikeServlet", urlPatterns = {"/blog/like/toggle"})
public class BlogLikeServlet extends BaseBlogServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=blogs");
            return;
        }

        String role = getRole(user);
        if (!isRoleAllowed(role, ROLE_CUSTOMER, ROLE_STAFF, ROLE_MANAGER)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Role not allowed.");
            return;
        }

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

            if (!"approved".equalsIgnoreCase(visibleBlog.getStatus())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chi blog da duyet moi duoc tym.");
                return;
            }

            dao.toggleLike(blogId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/blog/detail?id=" + blogId);
        } catch (SQLException ex) {
            throw new ServletException("Cannot toggle blog like", ex);
        }
    }
}
