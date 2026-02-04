package Controller.Staff;

import DAO.StaffDAO;
import DAO.LocationDAO;
import Models.Location;
import Models.StaffViewModel;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class EditStaffServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null || id.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/staff/list");
            return;
        }
        StaffDAO dao = new StaffDAO();
        LocationDAO ldao = new LocationDAO();
        try {
            StaffViewModel vm = dao.getStaffById(id);
            if (vm == null) { response.sendRedirect(request.getContextPath() + "/staff/list"); return; }
            java.util.List<Location> locations = ldao.getAllLocations();
            request.setAttribute("locations", locations);
            request.setAttribute("staff", vm);
            request.getRequestDispatcher("/View/Staff/EditStaff.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String userId = request.getParameter("userId");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");
        String employeeCode = request.getParameter("employeeCode");
        String hireDateStr = request.getParameter("hireDate");
        String status = request.getParameter("status");
        String locationId = request.getParameter("locationId");

        // basic validation
        StaffDAO dao = new StaffDAO();
        LocationDAO ldao = new LocationDAO();
        if (fullName == null || fullName.isBlank() || employeeCode == null || employeeCode.isBlank() || status == null || status.isBlank() || locationId == null || locationId.isBlank()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ các trường bắt buộc.");
            try {
                request.setAttribute("staff", dao.getStaffById(userId));
                request.setAttribute("locations", ldao.getAllLocations());
            } catch (SQLException ex) {
                throw new ServletException(ex);
            }
            request.getRequestDispatcher("/View/Staff/EditStaff.jsp").forward(request, response);
            return;
        }

        Date hireDate = null;
        try {
            if (hireDateStr != null && !hireDateStr.isBlank()) hireDate = Date.valueOf(hireDateStr);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", "Ngày không hợp lệ.");
            try {
                request.setAttribute("staff", dao.getStaffById(userId));
                request.setAttribute("locations", ldao.getAllLocations());
            } catch (SQLException e) {
                throw new ServletException(e);
            }
            request.getRequestDispatcher("/View/Staff/EditStaff.jsp").forward(request, response);
            return;
        }

        try {
            boolean ok = dao.updateStaff(userId, fullName, phone, address, gender, employeeCode, hireDate, status, locationId);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/staff/list");
            } else {
                request.setAttribute("error", "Cập nhật không thành công.");
                request.setAttribute("staff", dao.getStaffById(userId));
                request.setAttribute("locations", ldao.getAllLocations());
                request.getRequestDispatcher("/View/Staff/EditStaff.jsp").forward(request, response);
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
