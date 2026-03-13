package Controller.Auth;

import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class ProfileViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        request.setAttribute("dashboardPath", resolveDashboardPath(request, user));
        request.getRequestDispatcher("/View/Auth/ProfileView.jsp").forward(request, response);
    }

    private String resolveDashboardPath(HttpServletRequest request, User user) {
        String contextPath = request.getContextPath();
        if (user == null || user.getRole() == null || user.getRole().getRoleName() == null) {
            return contextPath + "/";
        }

        String role = user.getRole().getRoleName().trim().toLowerCase();
        if ("admin".equals(role)) {
            return contextPath + "/admin-dashboard";
        }
        if ("manager".equals(role)) {
            return contextPath + "/manager/dashboard";
        }
        if ("staff".equals(role)) {
            return contextPath + "/staff/dashboard";
        }
        return contextPath + "/customer/dashboard";
    }
}
