package Controller.Field;



import DAO.FieldDAO;
import Models.Field;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

@WebServlet(name = "FieldDetailServlet", urlPatterns = {"/fields/view"})
public class FieldDetailServlet extends HttpServlet {
    private final FieldDAO fieldDAO = new FieldDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("field_id");
        if (id == null || id.isBlank()) id = request.getParameter("fieldId");
        if (id == null || id.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing field_id");
            return;
        }

        try {
            UUID uuid = UUID.fromString(id);
            Field f = fieldDAO.getById(uuid);
            if (f == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Field not found");
                return;
            }
            request.setAttribute("field", f);
            request.getRequestDispatcher("/View/Field/field-detail.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID format");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
