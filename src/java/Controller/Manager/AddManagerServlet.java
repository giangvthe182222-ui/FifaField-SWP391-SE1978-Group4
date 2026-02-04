package Controller.Manager;

import DAO.LocationDAO;
import DAO.ManagerDAO;
import Models.Location;
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

@WebServlet("/add-manager")
public class AddManagerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            LocationDAO locationDAO = new LocationDAO();
            List<Location> locations = locationDAO.getAllLocations();
            request.setAttribute("locations", locations);
            request.getRequestDispatcher("/View/Manager/add-manager.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách vị trí: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/add-manager.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");
        String startDateStr = request.getParameter("startDate");
        String locationIdStr = request.getParameter("locationId");

        // Validation
        if (fullName == null || fullName.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank() ||
                phone == null || phone.isBlank() ||
                gender == null || gender.isBlank() ||
                startDateStr == null || startDateStr.isBlank() ||
                locationIdStr == null || locationIdStr.isBlank()) {

            request.setAttribute("error", "Vui lòng điền đầy đủ thông tin");
            doGet(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp");
            doGet(request, response);
            return;
        }

        if (password.length() > 20) {
            request.setAttribute("error", "Mật khẩu tối đa 20 ký tự");
            doGet(request, response);
            return;
        }

        // Additional server-side validation
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

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            request.setAttribute("error", "Định dạng email không hợp lệ");
            doGet(request, response);
            return;
        }

        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            UUID locationId = UUID.fromString(locationIdStr);

            ManagerDAO managerDAO = new ManagerDAO();
            boolean success = managerDAO.addManager(
                    fullName,
                    email,
                    password,
                    phone,
                    address,
                    gender,
                    startDate,
                    locationId
            );

            if (success) {
                response.sendRedirect(request.getContextPath() + "/manager-list");
            } else {
                request.setAttribute("error", "Thêm quản lý thất bại");
                doGet(request, response);
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            request.setAttribute("error", "Định dạng ngày không đúng: " + e.getMessage());
            doGet(request, response);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            request.setAttribute("error", "ID vị trí không hợp lệ: " + e.getMessage());
            doGet(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            String errMsg = e.getMessage();
            
            if (errMsg.contains("Unique")) {
                request.setAttribute("error", "❌ Email đã tồn tại trong hệ thống");
            } else if (errMsg.contains("'manager'")) {
                request.setAttribute("error", "❌ Role 'manager' chưa tồn tại trong database");
            } else if (errMsg.contains("Gmail Account")) {
                request.setAttribute("error", "❌ Không thể tạo Gmail Account: " + errMsg);
            } else if (errMsg.contains("User Account")) {
                request.setAttribute("error", "❌ Không thể tạo User Account: " + errMsg);
            } else if (errMsg.contains("Manager record")) {
                request.setAttribute("error", "❌ Không thể tạo Manager record: " + errMsg);
            } else if (errMsg.contains("location")) {
                request.setAttribute("error", "❌ Vị trí không tồn tại hoặc có lỗi liên kết: " + errMsg);
            } else if (errMsg.contains("kết nối")) {
                request.setAttribute("error", "❌ Lỗi kết nối database");
            } else {
                request.setAttribute("error", "❌ Lỗi: " + errMsg);
            }
            doGet(request, response);
        }
    }
}
