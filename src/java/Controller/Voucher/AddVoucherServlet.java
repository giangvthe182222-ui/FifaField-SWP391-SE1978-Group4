package Controller.Voucher;

import DAO.LocationDAO;
import DAO.VoucherDAO;
import Models.Location;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet("/voucher/add")
public class AddVoucherServlet extends HttpServlet {

    private final VoucherDAO voucherDAO = new VoucherDAO();
    private final LocationDAO locationDAO = new LocationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Location> locations = locationDAO.getAllLocations();
            request.setAttribute("locations", locations);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Không thể tải danh sách cụm sân: " + e.getMessage());
        }
        request.getRequestDispatcher("/View/Voucher/location-voucher-add.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String loc = request.getParameter("location_id");
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String discount = request.getParameter("discount");
        String start = request.getParameter("start_date");
        String end = request.getParameter("end_date");

        if (name == null || name.isBlank() || code == null || code.isBlank()) {
            request.setAttribute("error", "Tên và mã voucher không được để trống");
            doGet(request, response);
            return;
        }

        try {
            UUID locationId = loc == null || loc.isBlank() ? null : UUID.fromString(loc);
            BigDecimal disc = new BigDecimal(discount);
            LocalDate sd = LocalDate.parse(start);
            LocalDate ed = LocalDate.parse(end);

            if (!ed.isAfter(sd)) {
                request.setAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu");
                doGet(request, response);
                return;
            }

            boolean ok = voucherDAO.addVoucherToLocation(locationId, name.trim(), code.trim(), disc, sd, ed);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/voucher/list");
            } else {
                request.setAttribute("error", "Thêm voucher thất bại");
                doGet(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            doGet(request, response);
        }
    }
}
