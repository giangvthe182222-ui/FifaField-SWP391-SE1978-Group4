package filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import Models.User;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(servletNames = {
    "EquipmentListServlet",
    "AddEquipmentServlet",
    "EquipmentDetailServlet",
    "EditEquipmentServlet",
    "LocationEquipmentDetailServlet",
    "LocationEquipmentListServlet",
    "UpdateEquipmentStatusServlet",
    "UpdateLocationEquipmentServlet",
    "FieldScheduleServlet",
    "LocationAddServlet",
    "LocationListServlet"
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
            res.sendRedirect(req.getContextPath() + "/login");
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