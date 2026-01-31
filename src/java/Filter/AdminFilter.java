package filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import Models.User;
import jakarta.servlet.annotation.WebFilter;
//Hardcode
import java.util.UUID;

@WebFilter(servletNames = {
    "EquipmentListServlet",
    "AddEquipmentServlet",
    "EquipmentDetailServlet",
    "EditEquipmentServlet"
})
//public class AdminFilter implements Filter {
//
//    @Override
//    public void doFilter(
//            ServletRequest request,
//            ServletResponse response,
//            FilterChain chain
//    ) throws IOException, ServletException {
//
//        HttpServletRequest req = (HttpServletRequest) request;
//        HttpServletResponse res = (HttpServletResponse) response;
//
//        HttpSession session = req.getSession(false);
//
//        if (session == null || session.getAttribute("user") == null) {
//            res.sendRedirect(req.getContextPath() + "/login.jsp");
//            return;
//        }
//
//        User user = (User) session.getAttribute("user");
//
//        if (!"ADMIN".equalsIgnoreCase(user.getRole().getRoleName())) {
//            res.sendError(HttpServletResponse.SC_FORBIDDEN); // 
//            return;
//        }
//
//        chain.doFilter(request, response);
//    }
//}

//Hardcode
public class AdminFilter implements Filter {

    // hardcode roleId của ADMIN
    private static final UUID ADMIN_ROLE_ID =
        UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);

        // HARD CODE USER nếu chưa có
        if (session.getAttribute("user") == null) {
            User admin = new User();
            admin.setUserId(UUID.randomUUID());
            admin.setFullName("Hardcode Admin");
            admin.setRoleId(ADMIN_ROLE_ID);

            session.setAttribute("user", admin);
        }

        User user = (User) session.getAttribute("user");

        // check role
        if (!ADMIN_ROLE_ID.equals(user.getRoleId())) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }
}