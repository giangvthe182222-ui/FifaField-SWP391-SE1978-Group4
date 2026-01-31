package Controller.Equipment;

import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(
    name = "EquipmentListServlet",
    urlPatterns = "/equipment-list"
)
public class EquipmentListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String type = request.getParameter("type");
        if (keyword != null && keyword.isBlank()) keyword = null;
        if (status != null && status.isBlank()) status = null;
        if (type != null && type.isBlank()) type = null;

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());

//        List<Equipment> equipmentList = dao.filter(keyword, status, type);
        List<Equipment> equipmentList = dao.getAll();
        List<String> typeList = dao.getAllTypes();

        request.setAttribute("equipmentList", equipmentList);
        request.setAttribute("typeList", typeList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("type", type);

        request.getRequestDispatcher("/View/Equipment/EquipmentList.jsp")
               .forward(request, response);
        
        System.out.println("Equipment size = " + equipmentList.size());
        System.out.println(">>> EquipmentListServlet DOGET CALLED");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
