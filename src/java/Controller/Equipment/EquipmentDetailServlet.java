package Controller.Equipment;

import DAO.EquipmentDAO;
import Models.Equipment;
import Utils.DBConnection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/equipment-detail")
public class EquipmentDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String id = req.getParameter("id");

        EquipmentDAO dao = new EquipmentDAO(new DBConnection());
        Equipment e = dao.getEquipmentById(id);

        if (e == null) {
            resp.sendRedirect(req.getContextPath() + "/equipment-list");
            return;
        }

        req.setAttribute("equipment", e);
        req.getRequestDispatcher("View/EquipmentDetail.jsp").forward(req, resp);
    }
}
