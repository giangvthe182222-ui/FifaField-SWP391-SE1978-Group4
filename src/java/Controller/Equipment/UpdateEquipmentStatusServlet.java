package Controller.Equipment;

import DAO.EquipmentDAO;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/update-equipment-status")
public class UpdateEquipmentStatusServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
        String newStatus = request.getParameter("newStatus");

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());
        dao.updateStatus(id, newStatus);

        response.sendRedirect(request.getContextPath() + "/equipment-list");
    }
}

