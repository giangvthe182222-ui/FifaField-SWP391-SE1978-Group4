package filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import Models.User;
import jakarta.servlet.annotation.WebFilter;

@WebFilter("/*")
public class AdminFilter implements Filter {

    // Paths (prefixes) that require ADMIN role
    private static final String[] ADMIN_PATH_PREFIXES = new String[]{
        "/admin-dashboard",
        "/staff",
        "/manager",
        "/locations",
        "/equipment",
        "/location-equipment",
        "/location-equipment-list",
        "/voucher",
        "/field"
    };

    private boolean isAdminPath(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length()).toLowerCase();
        // Block direct access to JSP admin folder as well
        if (path.contains("/view/admin") || path.contains("/view/layout/admin") || path.contains("/view/manager") ) return true;
        for (String prefix : ADMIN_PATH_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/") || path.startsWith(prefix + "?") || path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Only enforce for admin-specific paths; pass through otherwise
        if (!isAdminPath(req)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            // Not logged in -> redirect to login
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            // Logged in but not admin -> forbid access
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
            return;
        }

        // All good
        chain.doFilter(request, response);
    }
}