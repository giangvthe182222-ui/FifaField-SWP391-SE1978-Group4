package Controller.Equipment;

import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.UUID;

@MultipartConfig
@WebServlet(name = "EditEquipmentServlet", urlPatterns = {"/edit-equipment"})
public class EditEquipmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        UUID id;
        try {
            id = UUID.fromString(idParam);
        } catch (IllegalArgumentException ex) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());
        Equipment equipment = dao.getById(id);

        if (equipment == null) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        request.setAttribute("equipment", equipment);
        request.getRequestDispatcher("View/EditEquipment.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // ===== ID =====
        UUID id;
        try {
            id = UUID.fromString(request.getParameter("equipment_id"));
        } catch (Exception ex) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        String name = request.getParameter("name");
        String equipmentType = request.getParameter("equipment_type");
        String status = request.getParameter("status");
        String description = request.getParameter("description");

        BigDecimal rentalPrice;
        BigDecimal damageFee;

        try {
            rentalPrice = new BigDecimal(request.getParameter("rental_price"));
            damageFee = new BigDecimal(request.getParameter("damage_fee"));
        } catch (Exception e) {
            request.setAttribute("error", "Giá không hợp lệ");
            doGet(request, response);
            return;
        }

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());
        Equipment old = dao.getById(id);

        if (old == null) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        // ===== IMAGE HANDLING =====
        Part imagePart = request.getPart("image");
        String imageUrl = old.getImageUrl(); // mặc định giữ ảnh cũ

        if (imagePart != null && imagePart.getSize() > 0) {
            String fileName = Paths.get(imagePart.getSubmittedFileName())
                                   .getFileName().toString();

            String uploadPath = getServletContext().getRealPath("/uploads");
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            imageUrl = "uploads/" + UUID.randomUUID() + "_" + fileName;
            imagePart.write(getServletContext().getRealPath("/") + imageUrl);
        }

        // ===== UPDATE MODEL =====
        Equipment updated = new Equipment();
        updated.setEquipmentId(id);
        updated.setName(name);
        updated.setEquipmentType(equipmentType);
        updated.setImageUrl(imageUrl);
        updated.setRentalPrice(rentalPrice);
        updated.setDamageFee(damageFee);
        updated.setStatus(status);
        updated.setDescription(description);
        // createdAt giữ nguyên trong DB

        boolean success = dao.update(updated);

        if (success) {
            response.sendRedirect(
                request.getContextPath() + "/equipment-detail?id=" + id
            );
        } else {
            request.setAttribute("error", "Cập nhật thất bại");
            doGet(request, response);
        }
    }
}
