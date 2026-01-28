package Controller.Equipment;

import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
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

        String name = req.getParameter("name");
        String type = req.getParameter("equipment_type");
        String status = req.getParameter("status");
        String desc = req.getParameter("description");

        float rental = Float.parseFloat(req.getParameter("rental_price"));
        float damage = Float.parseFloat(req.getParameter("damage_fee"));

        // IMAGE
        Part img = req.getPart("image");
        String fileName = Paths.get(img.getSubmittedFileName()).getFileName().toString();

        String uploadDir = getServletContext().getRealPath("/uploads");
        new File(uploadDir).mkdirs();

        String imagePath = "uploads/" + UUID.randomUUID() + "_" + fileName;
        img.write(getServletContext().getRealPath("/") + imagePath);

        // UUID
        String id = UUID.randomUUID().toString();

        Equipment e = new Equipment(
                id, name, type, imagePath, rental, damage, status, desc
        );

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());

        if (dao.addEquipment(e)) {
            // 👉 CHUYỂN SANG DETAIL
            resp.sendRedirect(req.getContextPath()
                    + "/equipment-detail?id=" + id);
        } else {
            req.setAttribute("error", "Add failed");
            req.getRequestDispatcher("View/AddEquipment.jsp").forward(req, resp);
        }
    }
}
