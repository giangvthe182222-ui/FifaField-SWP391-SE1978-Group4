package Controller.Field;

import DAO.FieldDAO;
import Models.Field;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "FieldListServlet", urlPatterns = {"/fields"})
public class FieldListServlet extends HttpServlet {
    private final FieldDAO fieldDAO = new FieldDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String locId = request.getParameter("location_id");
        if (locId == null || locId.isBlank()) {
            locId = request.getParameter("locationId");
        }

        if (locId == null || locId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing location_id");
            return;
        }

        try {
            UUID locationUuid = UUID.fromString(locId);
            List<Field> fields = fieldDAO.getByLocation(locationUuid);
            request.setAttribute("fields", fields);
            request.setAttribute("locationId", locId);
            request.getRequestDispatcher("/View/Field/field-list.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
