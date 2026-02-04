package Controller.Staff;

import DAO.AuthDAO;
import DAO.LocationDAO;
import Models.Location;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

public class AddStaffServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // // allow only admin users
        // HttpSession session = request.getSession(false);
        // if (session == null || session.getAttribute("user") == null) {
        //     response.sendError(HttpServletResponse.SC_FORBIDDEN);
        //     return;
        // }
        // User user = (User) session.getAttribute("user");
        // boolean isAdmin = false;
        // if (user.getRole() != null && user.getRole().getRoleName() != null) {
        //     isAdmin = "admin".equalsIgnoreCase(user.getRole().getRoleName());
        // }
        // if (!isAdmin) {
        //     response.sendError(HttpServletResponse.SC_FORBIDDEN);
        //     return;
        // }

         try {
             LocationDAO ldao = new LocationDAO();
             List<Location> locations = ldao.getAllLocations();
             request.setAttribute("locations", locations);
         } catch (Exception e) {
             request.setAttribute("error", "Không thể tải danh sách location: " + e.getMessage());
         }
         request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");

        String employeeCode = request.getParameter("employeeCode");
        String hireDateStr = request.getParameter("hireDate"); // yyyy-MM-dd
        String status = request.getParameter("status");
        String locationId = request.getParameter("locationId");

        if (fullName == null || fullName.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin bắt buộc.");
            request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
            return;
        }

        Date hireDate = null;
        try {
            if (hireDateStr != null && !hireDateStr.isBlank()) {
                hireDate = Date.valueOf(hireDateStr);
            }
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", "Ngày thuê không hợp lệ.");
            request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
            return;
        }

        try {
            AuthDAO dao = new AuthDAO();
            if (employeeCode != null && !employeeCode.isBlank() && dao.employeeCodeExists(employeeCode)) {
                request.setAttribute("error", "Mã nhân viên đã tồn tại. Vui lòng chọn mã khác.");
                request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
                return;
            }

            if (dao.emailExists(email)) {
                request.setAttribute("error", "Email đã tồn tại.");
                request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
                return;
            }

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
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            request.getRequestDispatcher("/View/Staff/AddStaff.jsp").forward(request, response);
        }
    }
}
