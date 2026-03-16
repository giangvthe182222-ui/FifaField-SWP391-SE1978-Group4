package Controller.Manager;

import DAO.LocationDAO;
import DAO.ManagerDAO;
import Models.Location;
import Models.Manager;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class ManagerLocationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/location");
            return;
        }

        String flashSuccess = (String) session.getAttribute("flash_success");
        if (flashSuccess != null) {
            request.setAttribute("flashSuccess", flashSuccess);
            session.removeAttribute("flash_success");
        }

        User user = (User) session.getAttribute("user");
        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                request.setAttribute("error", "Bạn chưa được gán cơ sở.");
                request.getRequestDispatcher("/View/Manager/manager-location.jsp").forward(request, response);
                return;
            }

            Location location = new LocationDAO().getLocationById(manager.getLocationId());
            request.setAttribute("manager", manager);
            request.setAttribute("location", location);
            request.getRequestDispatcher("/View/Manager/manager-location.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load manager location", e);
        }
    }
}
