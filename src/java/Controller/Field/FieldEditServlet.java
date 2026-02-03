package Controller.Field;



import DAO.FieldDAO;
import Models.Field;
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
import java.sql.PreparedStatement;
import java.util.UUID;

@WebServlet(name = "FieldEditServlet", urlPatterns = {"/fields/edit"})
@MultipartConfig
public class FieldEditServlet extends HttpServlet {
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
            if (f == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            request.setAttribute("field", f);
            request.getRequestDispatcher("/View/Field/field-edit.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String id = request.getParameter("field_id");
        if (id == null || id.isBlank()) id = request.getParameter("fieldId");
        if (id == null || id.isBlank()) { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing field_id"); return; }

        try {
            UUID uuid = UUID.fromString(id);
            String name = request.getParameter("fieldName");
            String type = request.getParameter("fieldType");
            String status = request.getParameter("status");
            String condition = request.getParameter("condition");
            String oldImage = request.getParameter("old_image");

            String imageUrl = oldImage;
            Part img = request.getPart("image");
            if (img != null && img.getSize() > 0) {
                String fileName = Paths.get(img.getSubmittedFileName()).getFileName().toString();
                String uploadDir = getServletContext().getRealPath("/uploads"); new File(uploadDir).mkdirs();
                imageUrl = "uploads/" + UUID.randomUUID() + "_" + fileName;
                img.write(getServletContext().getRealPath("/") + imageUrl);
            }

            Field f = new Field();
f.setFieldId(uuid);
            f.setFieldName(name);
            f.setFieldType(type);
            f.setImageUrl(imageUrl);
            f.setStatus(status);
            f.setFieldCondition(condition);

            // update via DAO: there is no update method — do simple inline update
            java.sql.Connection con = null;
            try {
                con = Utils.DBConnection.getConnection();
                String sql = "UPDATE Field SET field_name=?, field_type=?, image_url=?, status=?, condition=? WHERE field_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setNString(1, f.getFieldName());
                    ps.setNString(2, f.getFieldType());
                    ps.setString(3, f.getImageUrl());
                    ps.setNString(4, f.getStatus());
                    ps.setNString(5, f.getFieldCondition());
                    ps.setString(6, f.getFieldId().toString());
                    ps.executeUpdate();
                }
            } finally {
                if (con != null) try { con.close(); } catch (Exception ex) {}
            }

            response.sendRedirect(request.getContextPath() + "/fields/view?field_id=" + id);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
