package Controller.Equipment;

import DAO.LocationEquipmentDAO;
import Models.LocationEquipmentViewModel;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/location-equipment-list")
public class LocationEquipmentListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String locationIdRaw = request.getParameter("locationId");
        if (locationIdRaw == null || locationIdRaw.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing locationId");
            return;
        }

        UUID locationId;
        try {
            locationId = UUID.fromString(locationIdRaw);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid locationId");
            return;
        }

        int page = 1;
        int pageSize = 8;

        try {
            page = Integer.parseInt(request.getParameter("page"));
            if (page < 1) page = 1;
        } catch (Exception ignored) {}

        String search = request.getParameter("search");
        String type   = request.getParameter("type");
        String status = request.getParameter("status");
        String sort   = request.getParameter("sort");

        // ===== 4. DAO =====
        LocationEquipmentDAO dao = new LocationEquipmentDAO(new DBConnection());

        List<LocationEquipmentViewModel> list =
                dao.getFiltered(
                        locationId,
                        search,
                        type,
                        status,
                        sort,
                        page,
                        pageSize
                );

        int totalItems = dao.countFiltered(
                locationId,
                search,
                type,
                status
        );

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // ===== 5. SET ATTRIBUTE CHO JSP =====
        request.setAttribute("locationEquipmentList", list);
        request.setAttribute("locationId", locationId);

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.setAttribute("search", search);
        request.setAttribute("type", type);
        request.setAttribute("status", status);
        request.setAttribute("sort", sort);

        request.getRequestDispatcher(
                "/View/Equipment/LocationEquipmentList.jsp"
        ).forward(request, response);
    }
}
