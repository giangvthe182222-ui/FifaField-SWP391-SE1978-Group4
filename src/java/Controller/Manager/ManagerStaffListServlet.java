package Controller.Manager;

import DAO.ManagerDAO;
import DAO.StaffDAO;
import Models.Manager;
import Models.StaffViewModel;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ManagerStaffListServlet extends HttpServlet {

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
            // Get manager info to check location
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(user.getUserId());
            
            if (manager == null || manager.getLocationId() == null) {
                request.setAttribute("error", "Bạn chưa được gán cụm sân. Vui lòng liên hệ Admin.");
                request.getRequestDispatcher("/View/Manager/manager-staff-list.jsp")
                        .forward(request, response);
                return;
            }

            // Get staff in manager's location
            StaffDAO staffDAO = new StaffDAO();
            UUID locationId = manager.getLocationId();
            List<StaffViewModel> staffList = staffDAO.getAllStaffByLocation(locationId);
            
            request.setAttribute("staffList", staffList);
            request.setAttribute("locationName", manager.getLocationName());
            request.getRequestDispatcher("/View/Manager/manager-staff-list.jsp")
                    .forward(request, response);
                    
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách nhân viên: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-staff-list.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
