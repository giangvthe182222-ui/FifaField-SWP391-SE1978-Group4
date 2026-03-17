package Controller.Manager;

import DAO.LocationEquipmentDAO;
import DAO.ManagerDAO;
import DAO.EquipmentDAO;
import Models.LocationEquipmentViewModel;
import Models.Manager;
import Models.User;
import Utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/manager/location-equipment")
public class ManagerLocationEquipmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/location-equipment");
            return;
        }

        String flashSuccess = (String) session.getAttribute("flash_success");
        if (flashSuccess != null) {
            request.setAttribute("flashSuccess", flashSuccess);
            session.removeAttribute("flash_success");
        }
        String flashError = (String) session.getAttribute("flash_error");
        if (flashError != null) {
            request.setAttribute("flashError", flashError);
            session.removeAttribute("flash_error");
        }

        User user = (User) session.getAttribute("user");
        String search = trimToNull(request.getParameter("search"));
        String type = trimToNull(request.getParameter("type"));
        String status = trimToNull(request.getParameter("status"));

        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                request.setAttribute("error", "Bạn chưa được gán cơ sở.");
                request.getRequestDispatcher("/View/Manager/manager-location-equipment.jsp").forward(request, response);
                return;
            }

            LocationEquipmentDAO locationEquipmentDAO = new LocationEquipmentDAO(new DBConnection());
            List<LocationEquipmentViewModel> equipments = locationEquipmentDAO.getFiltered(
                    manager.getLocationId(),
                    search,
                    type,
                    status,
                    null,
                    1,
                    1000
            );
            EquipmentDAO equipmentDAO = new EquipmentDAO(new DBConnection());

            request.setAttribute("locationId", manager.getLocationId());
            request.setAttribute("locationName", manager.getLocationName());
            request.setAttribute("equipments", equipments);
            request.setAttribute("typeList", equipmentDAO.getAllTypes());
            request.setAttribute("search", search);
            request.setAttribute("type", type);
            request.setAttribute("status", status);
            request.getRequestDispatcher("/View/Manager/manager-location-equipment.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load location equipment for manager", e);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
