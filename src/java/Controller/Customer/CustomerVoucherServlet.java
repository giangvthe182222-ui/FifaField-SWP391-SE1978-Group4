package Controller.Customer;

import DAO.LocationDAO;
import DAO.VoucherDAO;
import Models.Location;
import Models.User;
import Models.Voucher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(name = "CustomerVoucherServlet", urlPatterns = {"/customer/vouchers"})
public class CustomerVoucherServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/vouchers");
            return;
        }

        try {
            VoucherDAO voucherDAO = new VoucherDAO();
            LocationDAO locationDAO = new LocationDAO();
            List<Voucher> vouchers = voucherDAO.getAllVouchers();
            List<Location> locations = locationDAO.getAllLocations();
            LocalDate today = LocalDate.now();
            String locationIdRaw = request.getParameter("locationId");
            String locationNameKeyword = request.getParameter("locationName");
            final String normalizedLocationNameKeyword = locationNameKeyword == null ? "" : locationNameKeyword.trim().toLowerCase();
            final UUID selectedLocationId;

            if (locationIdRaw != null && !locationIdRaw.isBlank()) {
                UUID temp;
                try {
                    temp = UUID.fromString(locationIdRaw);
                } catch (IllegalArgumentException ignored) {
                    temp = null;
                }
                selectedLocationId = temp;
            } else {
                selectedLocationId = null;
            }

            List<Voucher> activeVouchers = vouchers.stream()
                    .filter(v -> v.getStatus() != null && "active".equalsIgnoreCase(v.getStatus()))
                    .filter(v -> (v.getStartDate() == null || !v.getStartDate().isAfter(today))
                    && (v.getEndDate() == null || !v.getEndDate().isBefore(today)))
                    .filter(v -> selectedLocationId == null || selectedLocationId.equals(v.getLocationId()))
                    .filter(v -> {
                    if (normalizedLocationNameKeyword.isEmpty()) {
                        return true;
                    }
                    Location matched = locations.stream()
                        .filter(loc -> loc.getLocationId().equals(v.getLocationId()))
                        .findFirst()
                        .orElse(null);
                    return matched != null
                        && matched.getLocationName() != null
                        && matched.getLocationName().toLowerCase().contains(normalizedLocationNameKeyword);
                    })
                    .collect(Collectors.toList());

            // Pagination
            int pageNum = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isBlank()) {
                try {
                    pageNum = Integer.parseInt(pageParam);
                    if (pageNum < 1) {
                        pageNum = 1;
                    }
                } catch (NumberFormatException e) {
                    pageNum = 1;
                }
            }

            int totalItems = activeVouchers.size();
            int totalPages = (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;
            if (pageNum > totalPages && totalPages > 0) {
                pageNum = totalPages;
            }

            int startIdx = (pageNum - 1) * PAGE_SIZE;
            int endIdx = Math.min(startIdx + PAGE_SIZE, totalItems);
            List<Voucher> pageVouchers = new ArrayList<>(activeVouchers.subList(startIdx, endIdx));

            Map<UUID, String> locationNames = new HashMap<>();
            for (Location location : locations) {
                locationNames.put(location.getLocationId(), location.getLocationName());
            }

            request.setAttribute("vouchers", pageVouchers);
            request.setAttribute("locationNames", locationNames);
            request.setAttribute("currentPage", pageNum);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", totalItems);
            request.setAttribute("locations", locations);
            request.setAttribute("selectedLocationId", selectedLocationId == null ? "" : selectedLocationId.toString());
            request.setAttribute("locationName", locationNameKeyword == null ? "" : locationNameKeyword.trim());
            request.getRequestDispatcher("/View/Customer/vouchers.jsp").forward(request, response);
        } catch (Exception ex) {
            request.setAttribute("error", "Không thể tải danh sách voucher. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/View/Customer/vouchers.jsp").forward(request, response);
        }
    }
}
