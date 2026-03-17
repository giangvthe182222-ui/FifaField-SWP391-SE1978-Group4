package Controller.Manager;

import DAO.FieldDAO;
import DAO.ManagerDAO;
import Models.Field;
import Models.Manager;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/manager/field/status")
public class ManagerFieldStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/fields");
            return;
        }

        User user = (User) session.getAttribute("user");
        String fieldIdRaw = request.getParameter("fieldId");
        String status = request.getParameter("status");
        if (status != null) {
            status = status.trim().toLowerCase();
        }

        if (!"available".equals(status) && !"unavailable".equals(status)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid status");
            return;
        }

        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager has no assigned location");
                return;
            }

            UUID fieldId = UUID.fromString(fieldIdRaw);
            FieldDAO fieldDAO = new FieldDAO();
            Field field = fieldDAO.getById(fieldId);
            if (field == null || !manager.getLocationId().equals(field.getLocationId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Field is outside manager location");
                return;
            }

            fieldDAO.updateFieldStatus(fieldId, status);
            session.setAttribute("flash_success", "Đã cập nhật trạng thái sân.");
            response.sendRedirect(request.getContextPath() + "/manager/location");
        } catch (Exception e) {
            throw new ServletException("Cannot update field status", e);
        }
    }
}
