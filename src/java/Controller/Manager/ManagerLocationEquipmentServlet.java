package Controller.Manager;

import DAO.ManagerDAO;
import DAO.LocationEquipmentDAO;
import Models.Manager;
import Models.LocationEquipmentViewModel;
import Models.User;
import Utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ManagerLocationEquipmentServlet extends HttpServlet {

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
                request.getRequestDispatcher("/View/Manager/manager-location-equipment.jsp").forward(request, response);
                return;
            }

            DBConnection dbConn = new DBConnection();
            LocationEquipmentDAO leDAO = new LocationEquipmentDAO(dbConn);
            List<LocationEquipmentViewModel> equipments = leDAO.getByLocation(manager.getLocationId());

            request.setAttribute("equipments", equipments);
            request.setAttribute("locationName", manager.getLocationName());
            request.getRequestDispatcher("/View/Manager/manager-location-equipment.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách dụng cụ: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-location-equipment.jsp").forward(request, response);
        }
    }
}
