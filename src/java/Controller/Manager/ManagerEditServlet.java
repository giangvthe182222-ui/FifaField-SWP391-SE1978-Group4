package Controller.Manager;

import DAO.LocationDAO;
import DAO.ManagerDAO;
import Models.Location;
import Models.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@WebServlet("/manager-edit")
public class ManagerEditServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String managerId = request.getParameter("manager_id");

        if (managerId == null || managerId.isBlank()) {
            request.setAttribute("error", "Mã quản lý không được để trống");
            request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                    .forward(request, response);
            return;
        }

        try {
            UUID userId = UUID.fromString(managerId);
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(userId);

            if (manager == null) {
                request.setAttribute("error", "Không tìm thấy quản lý");
                request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                        .forward(request, response);
                return;
            }

            // Load danh sách location
            LocationDAO locationDAO = new LocationDAO();
            List<Location> locations = locationDAO.getAllLocations();

            request.setAttribute("manager", manager);
            request.setAttribute("locations", locations);
            request.getRequestDispatcher("/View/Manager/manager-edit.jsp")
                    .forward(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Mã quản lý không hợp lệ");
            request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String managerId = request.getParameter("manager_id");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");
        String startDateStr = request.getParameter("startDate");
        String locationIdStr = request.getParameter("locationId");

        // Validation
        if (managerId == null || managerId.isBlank() ||
            fullName == null || fullName.isBlank() ||
            phone == null || phone.isBlank() ||
            address == null || address.isBlank() ||
            gender == null || gender.isBlank() ||
            startDateStr == null || startDateStr.isBlank() ||
            locationIdStr == null || locationIdStr.isBlank()) {

            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin");
            doGet(request, response);
            return;
        }

        try {
                UUID userId = UUID.fromString(managerId);
                LocalDate startDate = LocalDate.parse(startDateStr);
                UUID newLocationId = UUID.fromString(locationIdStr);

                // Server-side validation
                if (!fullName.matches("^[\\p{L} .'-]+$")) {
                    request.setAttribute("error", "Họ tên chỉ được chứa chữ cái, khoảng trắng và dấu hợp lệ");
                    doGet(request, response);
                    return;
                }

                if (!phone.matches("^\\d{9,15}$")) {
                    request.setAttribute("error", "Số điện thoại chỉ chứa 9-15 chữ số");
                    doGet(request, response);
                    return;
                }

                ManagerDAO managerDAO = new ManagerDAO();
                boolean success = managerDAO.updateManager(
                    userId,
                    fullName,
                    phone,
                    address,
                    gender,
                    startDate,
                    newLocationId
                );

            if (success) {
                response.sendRedirect(request.getContextPath() + "/manager-detail?manager_id=" + userId);
            } else {
                request.setAttribute("error", "Cập nhật thất bại");
                doGet(request, response);
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            request.setAttribute("error", "❌ Định dạng ngày không đúng");
            doGet(request, response);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            request.setAttribute("error", "❌ ID quản lý không hợp lệ");
            doGet(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "❌ Lỗi: " + e.getMessage());
            doGet(request, response);
        }
    }
}
