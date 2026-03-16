package Controller.Manager;

import DAO.LocationDAO;
import DAO.ManagerDAO;
import Models.Location;
import Models.Manager;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/manager/location/status")
public class ManagerLocationStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/location");
            return;
        }

        User user = (User) session.getAttribute("user");
        String status = request.getParameter("status");

        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager has no assigned location");
                return;
            }

            LocationDAO locationDAO = new LocationDAO();
            Location location = locationDAO.getLocationById(manager.getLocationId());
            if (location == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Location not found");
                return;
            }

            location.setStatus(status);
            locationDAO.updateLocation(location);
            session.setAttribute("flash_success", "Đã cập nhật trạng thái cơ sở.");
            response.sendRedirect(request.getContextPath() + "/manager/location");
        } catch (Exception e) {
            throw new ServletException("Cannot update manager location status", e);
        }
    }
}
