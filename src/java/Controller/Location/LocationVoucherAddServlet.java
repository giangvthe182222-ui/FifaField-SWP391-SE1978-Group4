package Controller.Location;

import DAO.VoucherDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@WebServlet(name = "LocationVoucherAddServlet", urlPatterns = {"/locations/vouchers/add"})
public class LocationVoucherAddServlet extends HttpServlet {

    private final VoucherDAO voucherDAO = new VoucherDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // show add form; location_id may be passed as param
        request.getRequestDispatcher("/View/Voucher/location-voucher-add.jsp").forward(request, response);
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
        if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty() || discount == null || discount.trim().isEmpty() || start == null || start.trim().isEmpty() || end == null || end.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng điền đầy đủ các trường bắt buộc");
            doGet(request, response);
            return;
        }

        try {
            UUID locationId = locId == null || locId.isBlank() ? null : UUID.fromString(locId);
            BigDecimal disc = new BigDecimal(discount);
            LocalDate sd = LocalDate.parse(start);
            LocalDate ed = LocalDate.parse(end);

            if (disc.compareTo(BigDecimal.ZERO) <= 0 || disc.compareTo(new BigDecimal(100)) > 0) {
                request.setAttribute("error", "Phần trăm giảm giá phải từ 1 đến 100");
                doGet(request, response);
                return;
            }

            if (!ed.isAfter(sd)) {
                request.setAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu");
                doGet(request, response);
                return;
            }

            boolean success = voucherDAO.addVoucherToLocation(locationId, name.trim(), code.trim(), disc, sd, ed);
            if (success) {
                // redirect back to list for the location
                String target = request.getContextPath() + "/locations/vouchers?location_id=" + (locationId == null ? "" : locationId.toString());
                response.sendRedirect(target);
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
