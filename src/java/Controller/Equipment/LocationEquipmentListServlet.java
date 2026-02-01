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

    // tạm hardcode (sau này có thể lấy từ URL / session)
    private static final UUID LOCATION_ID =
            UUID.fromString("E41577BF-A373-4389-ADC6-44B6E132AF66");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = 1;
        int pageSize = 8;

        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {}

        String search = request.getParameter("search");
        String type = request.getParameter("type");
        String status = request.getParameter("status");
        String sort = request.getParameter("sort");

        LocationEquipmentDAO dao = new LocationEquipmentDAO(new DBConnection());

        List<LocationEquipmentViewModel> list =
                dao.getFiltered(LOCATION_ID, search, type, status, sort, page, pageSize);

        int totalItems = dao.countFiltered(LOCATION_ID, search, type, status);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // 🔥 QUAN TRỌNG
        request.setAttribute("locationEquipmentList", list);
        request.setAttribute("locationId", LOCATION_ID);

        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher(
                "/View/Equipment/LocationEquipmentList.jsp"
        ).forward(request, response);
    }
}
