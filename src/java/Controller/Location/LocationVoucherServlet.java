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

        UUID locationId = UUID.fromString(locId);
        List<Voucher> vouchers = voucherDAO.getByLocation(locationId);
        request.setAttribute("vouchers", vouchers);
        request.setAttribute("locationId", locId);
        request.getRequestDispatcher("/View/Location/location-vouchers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String locId = request.getParameter("location_id");
        String code = request.getParameter("code");
        String discount = request.getParameter("discount");
        String desc = request.getParameter("description");
        String start = request.getParameter("start_date");
        String end = request.getParameter("end_date");

        if (locId == null || code == null || code.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/locations/vouchers?location_id=" + locId);
            return;
        }

        UUID locationId = UUID.fromString(locId);
        BigDecimal disc = null;
        try { if (discount != null && !discount.isEmpty()) disc = new BigDecimal(discount); } catch (Exception e) { /* ignore */ }
        LocalDate sd = (start != null && !start.isEmpty()) ? LocalDate.parse(start) : null;
        LocalDate ed = (end != null && !end.isEmpty()) ? LocalDate.parse(end) : null;

        boolean ok = voucherDAO.addVoucherToLocation(locationId, code.trim(), disc, desc, sd, ed);
        response.sendRedirect(request.getContextPath() + "/locations/vouchers?location_id=" + locId);
    }
}
