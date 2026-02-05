
import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@MultipartConfig
@WebServlet(name = "EquipmentDetailServlet", urlPatterns = {"/equipment-detail"})
public class EquipmentDetailServlet extends HttpServlet {

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
        
        request.setAttribute("typeList", dao.getAllTypes());
        request.setAttribute("equipment", equipment);
        request.getRequestDispatcher("View/Equipment/EquipmentDetail.jsp")
               .forward(request, response);
    }
}