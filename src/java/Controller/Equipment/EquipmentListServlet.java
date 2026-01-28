package Controller.Equipment;

import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/equipment-list")
public class EquipmentListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        String type = request.getParameter("type");

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());

        List<Equipment> equipmentList = dao.filter(keyword, status, type);
        List<String> typeList = dao.getAllTypes();

        request.setAttribute("equipmentList", equipmentList);
        request.setAttribute("typeList", typeList);

        request.getRequestDispatcher("/View/EquipmentList.jsp").forward(request, response);
    }
}
