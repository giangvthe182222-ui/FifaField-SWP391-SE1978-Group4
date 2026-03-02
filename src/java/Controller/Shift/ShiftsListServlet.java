package Controller.Shift;

import DAO.ShiftDAO;
import Models.Shift;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ShiftsListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ShiftDAO dao = new ShiftDAO();
            List<Shift> shifts = dao.getAllShifts();
            request.setAttribute("shifts", shifts);
            request.getRequestDispatcher("/View/Shift/shifts-list.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải ca: " + ex.getMessage());
            request.getRequestDispatcher("/View/Shift/shifts-list.jsp").forward(request, response);
        }
    }
}
