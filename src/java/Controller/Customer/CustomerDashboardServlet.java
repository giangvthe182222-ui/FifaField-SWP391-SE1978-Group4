package Controller.Customer;

import DAO.LocationDAO;
import Models.Location;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "CustomerDashboardServlet", urlPatterns = {"/customer/dashboard"})
public class CustomerDashboardServlet extends HttpServlet {
    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=customer/dashboard");
            return;
        }

        try {
            LocationDAO locationDAO = new LocationDAO();
            List<Location> allLocations = locationDAO.getAllLocations();
            
            // Filter out inactive locations
            List<Location> activeLocations = allLocations.stream()
                    .filter(loc -> loc.getStatus() != null && "ACTIVE".equalsIgnoreCase(loc.getStatus()))
                    .collect(Collectors.toList());

            String searchName = request.getParameter("searchName");
            String searchAddress = request.getParameter("searchAddress");
                String selectedLocationName = request.getParameter("locationName");

                List<String> locationNameOptions = activeLocations.stream()
                    .map(Location::getLocationName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
            
                List<Location> filteredLocations = activeLocations.stream()
                    .filter(loc -> {
                    if (selectedLocationName != null && !selectedLocationName.isBlank()) {
                        return selectedLocationName.equalsIgnoreCase(loc.getLocationName());
                    }
                    return true;
                    })
                    .filter(loc -> {
                        if (searchName != null && !searchName.isBlank()) {
                            return loc.getLocationName() != null && loc.getLocationName().toLowerCase()
                                    .contains(searchName.toLowerCase());
                        }
                        return true;
                    })
                    .filter(loc -> {
                        if (searchAddress != null && !searchAddress.isBlank()) {
                            return loc.getAddress() != null && loc.getAddress().toLowerCase()
                                    .contains(searchAddress.toLowerCase());
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            
            // Pagination
            int pageNum = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isBlank()) {
                try {
                    pageNum = Integer.parseInt(pageParam);
                    if (pageNum < 1) pageNum = 1;
                } catch (NumberFormatException e) {
                    pageNum = 1;
                }
            }
            
            int totalItems = filteredLocations.size();
            int totalPages = (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;
            if (pageNum > totalPages && totalPages > 0) pageNum = totalPages;
            
            int startIdx = (pageNum - 1) * PAGE_SIZE;
            int endIdx = Math.min(startIdx + PAGE_SIZE, totalItems);
            List<Location> pageLocations = new ArrayList<>(filteredLocations.subList(startIdx, endIdx));
            
            request.setAttribute("locations", pageLocations);
            request.setAttribute("currentPage", pageNum);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalItems", totalItems);
            request.setAttribute("searchName", searchName);
            request.setAttribute("searchAddress", searchAddress);
            request.setAttribute("locationName", selectedLocationName);
            request.setAttribute("locationNameOptions", locationNameOptions);
            request.getRequestDispatcher("/View/Customer/dashboard.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("error", "Khong the tai danh sach cum san. Vui long thu lai sau.");
            request.getRequestDispatcher("/View/Customer/dashboard.jsp").forward(request, response);
        }
    }
}
