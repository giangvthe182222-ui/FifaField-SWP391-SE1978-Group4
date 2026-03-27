package Controller.Blog;

import Models.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

public abstract class BaseBlogServlet extends HttpServlet {

    protected static final String ROLE_CUSTOMER = "customer";
    protected static final String ROLE_STAFF = "staff";
    protected static final String ROLE_MANAGER = "manager";

    protected User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    protected String getRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getRoleName() == null) {
            return "";
        }
        return user.getRole().getRoleName().trim().toLowerCase();
    }

    protected boolean isRoleAllowed(String role, String... allowedRoles) {
        if (role == null || role.isEmpty() || allowedRoles == null) {
            return false;
        }

        for (String allowedRole : allowedRoles) {
            if (role.equals(allowedRole)) {
                return true;
            }
        }

        return false;
    }

    protected UUID toUuid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    protected String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}