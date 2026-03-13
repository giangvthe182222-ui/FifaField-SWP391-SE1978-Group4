package Controller.Staff;

import DAO.FieldDAO;
import DAO.StaffDAO;
import Models.Field;
import Models.StaffViewModel;
import Models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "StaffFieldListServlet", urlPatterns = {"/staff/fields"})
public class StaffFieldListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/fields");
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            StaffDAO staffDAO = new StaffDAO();
            StaffViewModel staff = staffDAO.getStaffById(user.getUserId().toString());
            if (staff == null || staff.getLocationId() == null) {
                session.setAttribute("flash_error", "No location assigned to this staff.");
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            UUID locationId = UUID.fromString(staff.getLocationId());
            FieldDAO fieldDAO = new FieldDAO();
            List<Field> fields = fieldDAO.getByLocation(locationId);

            request.setAttribute("fields", fields);
            request.setAttribute("locationId", staff.getLocationId());
            request.setAttribute("locationName", staff.getLocationName());
            request.getRequestDispatcher("/View/Staff/StaffFieldList.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash_error", "Error loading fields: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
