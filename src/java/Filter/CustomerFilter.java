package Filter;

import Models.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {
    "/customer/*"
})
public class CustomerFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
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
        if (user.getRole() == null || !"CUSTOMER".equalsIgnoreCase(user.getRole().getRoleName())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Customer role required.");
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
    }
}
