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
        request.getRequestDispatcher("View/Equipment/EditEquipment.jsp")
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
        String rentalRaw = request.getParameter("rental_price");
        String damageRaw = request.getParameter("damage_fee");
        String error = null;
        BigDecimal rentalPrice = null;
        BigDecimal damageFee = null;

        // Validate status
        if (!"available".equals(status) && !"unavailable".equals(status)) {
            error = "Trạng thái không hợp lệ (chỉ 'available' hoặc 'unavailable')";
        }

        // Validate numbers
        try {
            rentalPrice = new BigDecimal(rentalRaw);
            damageFee = new BigDecimal(damageRaw);
            if (rentalPrice.compareTo(BigDecimal.ZERO) <= 0 || damageFee.compareTo(BigDecimal.ZERO) <= 0) {
                error = "Giá thuê và phí hỏng hóc phải lớn hơn 0";
            }
        } catch (Exception e) {
            error = "Giá không hợp lệ";
        }

        // Validate required fields
        if (name == null || name.isBlank() || equipmentType == null || equipmentType.isBlank()) {
            error = "Vui lòng nhập đầy đủ tên và loại thiết bị";
        }

        if (error != null) {
            request.setAttribute("error", error);
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
                request.getContextPath() + "/equipment-list"
            );
        } else {
            request.setAttribute("error", "Cập nhật thất bại");
            doGet(request, response);
        }
    }
}
