package Controller.Location;

import DAO.VoucherDAO;
import Models.Voucher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "LocationVoucherServlet", urlPatterns = {"/locations/vouchers"})
public class LocationVoucherServlet extends HttpServlet {

    private VoucherDAO voucherDAO = new VoucherDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String locId = request.getParameter("location_id");
        if (locId == null) {
            response.sendRedirect(request.getContextPath() + "/locations");
            return;
        }

        try {
            UUID locationId = UUID.fromString(locId);
            List<Voucher> vouchers = voucherDAO.getByLocation(locationId);
            request.setAttribute("vouchers", vouchers);
            request.setAttribute("locationId", locId);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách voucher: " + e.getMessage());
        }
        request.getRequestDispatcher("/View/Location/location-vouchers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String locId = request.getParameter("location_id");
        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String discount = request.getParameter("discount");
        String start = request.getParameter("start_date");
        String end = request.getParameter("end_date");

        // Validate required fields
        if (locId == null || locId.trim().isEmpty()) {
            request.setAttribute("error", "ID cụm sân không hợp lệ");
            doGet(request, response);
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "Tên voucher không được để trống");
            doGet(request, response);
            return;
        }

        if (code == null || code.trim().isEmpty()) {
            request.setAttribute("error", "Mã voucher không được để trống");
            doGet(request, response);
            return;
        }

        if (discount == null || discount.trim().isEmpty()) {
            request.setAttribute("error", "Phần trăm giảm giá không được để trống");
            doGet(request, response);
            return;
        }

        if (start == null || start.trim().isEmpty()) {
            request.setAttribute("error", "Ngày bắt đầu không được để trống");
            doGet(request, response);
            return;
        }

        if (end == null || end.trim().isEmpty()) {
            request.setAttribute("error", "Ngày kết thúc không được để trống");
            doGet(request, response);
            return;
        }

        try {
            UUID locationId = UUID.fromString(locId);
            BigDecimal disc = new BigDecimal(discount);

            // Validate discount percentage
            if (disc.compareTo(BigDecimal.ZERO) <= 0 || disc.compareTo(new BigDecimal(100)) > 0) {
                request.setAttribute("error", "Phần trăm giảm giá phải từ 1 đến 100");
                doGet(request, response);
                return;
            }

            LocalDate sd = LocalDate.parse(start);
            LocalDate ed = LocalDate.parse(end);

            // Validate end_date > start_date
            if (!ed.isAfter(sd)) {
                request.setAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu");
                doGet(request, response);
                return;
            }

            boolean success = voucherDAO.addVoucherToLocation(locationId, name.trim(), code.trim(), disc, sd, ed);
            
            if (success) {
                request.setAttribute("success", "Thêm voucher thành công");
            } else {
                request.setAttribute("error", "Thêm voucher thất bại");
            }
            doGet(request, response);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("error", "Phần trăm giảm giá phải là số: " + e.getMessage());
            doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            doGet(request, response);
        }
    }
}
