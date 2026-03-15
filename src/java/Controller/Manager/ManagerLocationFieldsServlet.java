package Controller.Manager;

import DAO.ManagerDAO;
import DAO.FieldDAO;
import Models.Manager;
import Models.Field;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ManagerLocationFieldsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(user.getUserId());

            if (manager == null || manager.getLocationId() == null) {
                request.setAttribute("error", "Bạn chưa được gán cụm sân. Vui lòng liên hệ Admin.");
                request.getRequestDispatcher("/View/Manager/manager-location-fields.jsp").forward(request, response);
                return;
            }

            FieldDAO fieldDAO = new FieldDAO();
            List<Field> fields = fieldDAO.getByLocation(manager.getLocationId());

            request.setAttribute("fields", fields);
            request.setAttribute("locationName", manager.getLocationName());
            request.setAttribute("locationId", manager.getLocationId().toString());
            request.getRequestDispatcher("/View/Manager/manager-location-fields.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách sân: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-location-fields.jsp").forward(request, response);
        }
    }
}
