package Filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import Models.User;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {
    "/admin-dashboard",
    "/locations",
    "/locations/*",
    "/fields",
    "/fields/*",
    "/field-schedule",
    "/field-booking-schedule",
    "/schedule-edit",
    "/equipment-list",
    "/equipment-detail",
    "/edit-equipment",
    "/voucher/*",
    "/manager-list",
    "/manager-detail",
    "/manager-delete",
    "/add-manager",
    "/manager-edit",
    "/staff-list",
    "/staff/add",
    "/staff/list",
    "/staff/detail",
    "/staff/edit"
})
public class AdminFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            String path = req.getRequestURI().substring(req.getContextPath().length());
            String redirectPath = path.startsWith("/") ? path.substring(1) : path;
            res.sendRedirect(req.getContextPath() + "/login?redirect=" + redirectPath);
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Admin role required.");
            return;
        }

        chain.doFilter(request, response);
    }
}