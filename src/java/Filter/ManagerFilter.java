package Filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import Models.User;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {
    "/manager/*",
    "/shifts",
    "/shifts/*"
})
public class ManagerFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        applyNoCacheHeaders(res);

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            String path = req.getRequestURI().substring(req.getContextPath().length());
            String redirectPath = path.startsWith("/") ? path.substring(1) : path;
            res.sendRedirect(req.getContextPath() + "/login?redirect=" + redirectPath);
            return;
        }

        User user = (User) session.getAttribute("user");

        if (user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole().getRoleName())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Manager role required.");
            return;
        }

        chain.doFilter(request, response);
    }

    private void applyNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
