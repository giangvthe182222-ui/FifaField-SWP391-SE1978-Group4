package Controller.Manager;

import DAO.FieldDAO;
import DAO.LocationDAO;
import DAO.ManagerDAO;
import Models.Field;
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
import java.util.List;

@WebServlet("/manager/fields")
public class ManagerFieldListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=manager/fields");
            return;
        }

        User user = (User) session.getAttribute("user");
        try {
            Manager manager = new ManagerDAO().getManagerById(user.getUserId());
            if (manager == null || manager.getLocationId() == null) {
                request.setAttribute("error", "Bạn chưa được gán cơ sở.");
                request.getRequestDispatcher("/View/Manager/manager-fields.jsp").forward(request, response);
                return;
            }

            List<Field> fields = new FieldDAO().getByLocation(manager.getLocationId());
            Location location = new LocationDAO().getLocationById(manager.getLocationId());
            request.setAttribute("fields", fields);
            request.setAttribute("locationName", manager.getLocationName());
            request.setAttribute("location", location);
            request.getRequestDispatcher("/View/Manager/manager-fields.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load manager fields", e);
        }
    }
}
