package Controller.Field;

import DAO.FieldDAO;
import Models.Field;
import Utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@WebServlet(name = "FieldAddServlet", urlPatterns = {"/fields/add"})
@MultipartConfig
public class FieldAddServlet extends HttpServlet {
    private final FieldDAO fieldDAO = new FieldDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Expect location_id in query; if missing, load locations for select box
        String locId = request.getParameter("location_id");
        if (locId == null || locId.isBlank()) {
            locId = request.getParameter("locationId");
        }

        request.setAttribute("locationId", locId);
        request.setAttribute("location_id", locId);

        if (locId == null || locId.isBlank()) {
            // load all locations so user can choose
            try {
                DAO.LocationDAO locationDAO = new DAO.LocationDAO();
                java.util.List<Models.Location> locations = locationDAO.getAllLocations();
                request.setAttribute("locationsList", locations);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        request.getRequestDispatcher("/View/Field/field-add.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String locationId = request.getParameter("locationId");
        if (locationId == null || locationId.isBlank()) {
            locationId = request.getParameter("location_id");
        }
        String name = request.getParameter("fieldName");
        String type = request.getParameter("fieldType");
        String status = request.getParameter("status");
        String condition = request.getParameter("condition");

        String imageUrl = null;

        try {
            Part img = request.getPart("image");
            if (img != null && img.getSize() > 0) {
                String fileName = Paths.get(img.getSubmittedFileName()).getFileName().toString();
                String uploadDir = getServletContext().getRealPath("/uploads");
                new File(uploadDir).mkdirs();
                imageUrl = "uploads/" + UUID.randomUUID() + "_" + fileName;
                img.write(getServletContext().getRealPath("/") + imageUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imageUrl == null || imageUrl.isBlank()) imageUrl = "uploads/default_field.jpg";

        // basic validation
        if (locationId == null || locationId.isBlank() || name == null || name.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (locationId == null || locationId.isBlank()) sb.append("Thiếu locationId. ");
            if (name == null || name.trim().isEmpty()) sb.append("Thiếu tên sân.");
            request.setAttribute("error", sb.toString());
            // preserve submitted params for redisplay
            request.setAttribute("locationId", locationId);
            request.setAttribute("location_id", locationId);
            request.setAttribute("fieldName", name);
            request.setAttribute("fieldType", type);
            request.setAttribute("status", status);
            request.setAttribute("condition", condition);
            if (locationId == null || locationId.isBlank()) {
                try {
                    DAO.LocationDAO locationDAO = new DAO.LocationDAO();
                    java.util.List<Models.Location> locations = locationDAO.getAllLocations();
                    request.setAttribute("locationsList", locations);
                } catch (Exception e) { e.printStackTrace(); }
            }
            request.getRequestDispatcher("/View/Field/field-add.jsp").forward(request, response);
            return;
        }

        try {
            Field f = new Field();
            f.setFieldId(UUID.randomUUID());
            f.setFieldName(name.trim());
            f.setFieldType(type == null ? "7-a-side" : type);
            f.setImageUrl(imageUrl);
            f.setStatus(status == null ? "ACTIVE" : status);
            f.setFieldCondition(condition == null ? "GOOD" : condition);
            f.setLocationId(UUID.fromString(locationId));

            boolean ok = fieldDAO.addField(f);
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/fields?location_id=" + locationId);
            } else {
                request.setAttribute("error", "Không thể thêm sân");
                doGet(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            doGet(request, response);
        }
    }
}
