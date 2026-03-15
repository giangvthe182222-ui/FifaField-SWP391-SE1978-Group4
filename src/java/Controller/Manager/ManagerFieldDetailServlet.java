package Controller.Manager;

import DAO.FieldDAO;
import DAO.ManagerDAO;
import Models.Field;
import Models.Manager;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class ManagerFieldDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        String fieldId = request.getParameter("field_id");
        if (fieldId == null || fieldId.isBlank()) fieldId = request.getParameter("fieldId");
        if (fieldId == null || fieldId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/manager/location/fields");
            return;
        }

        try {
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(user.getUserId());

            if (manager == null || manager.getLocationId() == null) {
                response.sendRedirect(request.getContextPath() + "/manager/dashboard");
                return;
            }

            FieldDAO fieldDAO = new FieldDAO();
            UUID uuid = UUID.fromString(fieldId);
            Field field = fieldDAO.getById(uuid);

            if (field == null) {
                request.setAttribute("error", "Không tìm thấy sân.");
                request.getRequestDispatcher("/View/Manager/manager-field-detail.jsp").forward(request, response);
                return;
            }

            // Ensure this field belongs to the manager's location
            if (!field.getLocationId().equals(manager.getLocationId())) {
                response.sendRedirect(request.getContextPath() + "/manager/location/fields");
                return;
            }

            request.setAttribute("field", field);
            request.setAttribute("locationName", manager.getLocationName());
            request.getRequestDispatcher("/View/Manager/manager-field-detail.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/manager/location/fields");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải thông tin sân: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-field-detail.jsp").forward(request, response);
        }
    }
}
