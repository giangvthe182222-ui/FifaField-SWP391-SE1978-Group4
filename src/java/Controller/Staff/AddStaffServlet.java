package Controller.Staff;

import DAO.AuthDAO;
import DAO.LocationDAO;
import Models.Location;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

public class AddStaffServlet extends HttpServlet {

    // ===== COMMON METHOD =====
    private void loadLocations(HttpServletRequest request) {
        try {
            LocationDAO ldao = new LocationDAO();
            List<Location> locations = ldao.getAllLocations();
            request.setAttribute("locations", locations);
        } catch (Exception e) {
            request.setAttribute("error", "Không thể tải danh sách location");
        }
    }

    // ===== GET =====
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        loadLocations(request);
        request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
    }

    // ===== POST =====
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ===== GET PARAM =====
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");

        String employeeCode = request.getParameter("employeeCode");
        String hireDateStr = request.getParameter("hireDate");
        String status = request.getParameter("status");
        String locationId = request.getParameter("locationId");

        // ===== SET BACK DATA (FOR RE-FILL FORM) =====
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("address", address);
        request.setAttribute("gender", gender);
        request.setAttribute("employeeCode", employeeCode);
        request.setAttribute("hireDate", hireDateStr);
        request.setAttribute("status", status);
        request.setAttribute("locationId", locationId);

        // ===== BASIC VALIDATE =====
        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {

            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin bắt buộc.");
            loadLocations(request);
            request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
            return;
        }

        // ===== PARSE DATE =====
        Date hireDate = null;
        try {
            if (hireDateStr != null && !hireDateStr.isBlank()) {
                hireDate = Date.valueOf(hireDateStr);
            }
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Ngày thuê không hợp lệ.");
            loadLocations(request);
            request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
            return;
        }

        // ===== BUSINESS LOGIC =====
        try {
            AuthDAO dao = new AuthDAO();

            // check employee code
            if (employeeCode != null && !employeeCode.isBlank()
                    && dao.employeeCodeExists(employeeCode)) {

                request.setAttribute("error", "Mã nhân viên đã tồn tại.");
                loadLocations(request);
                request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
                return;
            }

            // check email
            if (dao.emailExists(email)) {
                request.setAttribute("error", "Email đã tồn tại.");
                loadLocations(request);
                request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
                return;
            }

            // ===== INSERT =====
            dao.registerStaff(
                    fullName.trim(),
                    email.trim(),
                    password,
                    phone,
                    address,
                    gender,
                    employeeCode,
                    hireDate,
                    status,
                    locationId
            );

            request.getSession().setAttribute("success", "Tạo nhân viên thành công");
            response.sendRedirect(request.getContextPath() + "/staff/list");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            loadLocations(request);
            request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
        }
    }
}
