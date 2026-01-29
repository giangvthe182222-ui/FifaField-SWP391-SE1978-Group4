package Controller.Equipment;

import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.UUID;

@MultipartConfig
@WebServlet("/add-equipment")
public class AddEquipmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("View/AddEquipment.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // ===== BASIC INFO =====
        String name = req.getParameter("name");
        String type = req.getParameter("equipment_type");
        String status = req.getParameter("status");
        String desc = req.getParameter("description");

        BigDecimal rental = new BigDecimal(req.getParameter("rental_price"));
        BigDecimal damage = new BigDecimal(req.getParameter("damage_fee"));

        // ===== IMAGE UPLOAD =====
        Part img = req.getPart("image");
        String imagePath = null;

        if (img != null && img.getSize() > 0) {
            String fileName = Paths.get(img.getSubmittedFileName())
                                   .getFileName().toString();

            String uploadDir = getServletContext().getRealPath("/uploads");
            new File(uploadDir).mkdirs();

            imagePath = "uploads/" + UUID.randomUUID() + "_" + fileName;
            img.write(getServletContext().getRealPath("/") + imagePath);
        }

        // ===== CREATE MODEL =====
        UUID equipmentId = UUID.randomUUID();

        Equipment e = new Equipment();
        e.setEquipmentId(equipmentId);
        e.setName(name);
        e.setEquipmentType(type);
        e.setImageUrl(imagePath);
        e.setRentalPrice(rental);
        e.setDamageFee(damage);
        e.setStatus(status);
        e.setDescription(desc);
        // createdAt: DB tự set

        // ===== SAVE =====
        EquipmentDAO dao = new EquipmentDAO(new DBConnection());

        if (dao.addEquipment(e)) {
            resp.sendRedirect(
                req.getContextPath() + "/equipment-detail?id=" + equipmentId
            );
        } else {
            req.setAttribute("error", "Add failed");
            req.getRequestDispatcher("View/AddEquipment.jsp")
               .forward(req, resp);
        }
    }
}
