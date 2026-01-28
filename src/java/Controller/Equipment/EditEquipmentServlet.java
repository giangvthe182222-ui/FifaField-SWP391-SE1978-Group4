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
import java.nio.file.Paths;
import java.util.UUID;

@MultipartConfig
@WebServlet(name = "EditEquipmentServlet", urlPatterns = {"/edit-equipment"})
public class EditEquipmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
        if (id == null || id.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        DBConnection db = new DBConnection();
        EquipmentDAO dao = new EquipmentDAO(db);
        Equipment equipment = dao.getEquipmentById(id);

        if (equipment == null) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        request.setAttribute("equipment", equipment);
        request.getRequestDispatcher("View/EditEquipment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String id = request.getParameter("equipment_id");
        String name = request.getParameter("name");
        String equipmentType = request.getParameter("equipment_type");
        String status = request.getParameter("status");
        String description = request.getParameter("description");

        float rentalPrice;
        float damageFee;

        try {
            rentalPrice = Float.parseFloat(request.getParameter("rental_price"));
            damageFee = Float.parseFloat(request.getParameter("damage_fee"));
        } catch (Exception e) {
            request.setAttribute("error", "Giá không hợp lệ");
            doGet(request, response);
            return;
        }

        DBConnection db = new DBConnection();
        EquipmentDAO dao = new EquipmentDAO(db);

        Equipment old = dao.getEquipmentById(id);
        if (old == null) {
            response.sendRedirect(request.getContextPath() + "/equipment-list");
            return;
        }

        // ===== IMAGE HANDLING =====
        Part imagePart = request.getPart("image");
        String imageUrl = old.getImageUrl(); // default giữ ảnh cũ

        if (imagePart != null && imagePart.getSize() > 0) {
            String fileName = Paths.get(imagePart.getSubmittedFileName())
                                  .getFileName().toString();

            String uploadPath = getServletContext().getRealPath("/uploads");
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdir();

            imageUrl = "uploads/" + UUID.randomUUID() + "_" + fileName;
            imagePart.write(getServletContext().getRealPath("/") + imageUrl);
        }

        Equipment updated = new Equipment(
                id,
                name,
                equipmentType,
                imageUrl,
                rentalPrice,
                damageFee,
                status,
                description
        );

        boolean success = dao.updateEquipment(updated);

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
