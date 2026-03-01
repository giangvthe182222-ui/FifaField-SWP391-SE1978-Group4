package Controller.Shift;

import DAO.ShiftDAO;
import Models.Shift;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.UUID;

public class AddShiftServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/View/Shift/shift-add.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("shiftName");
        String start = request.getParameter("startTime");
        String end = request.getParameter("endTime");
        try {
            Shift s = new Shift();
            s.setShiftId(UUID.randomUUID());
            s.setShiftName(name);
            s.setStartTime(LocalTime.parse(start));
            s.setEndTime(LocalTime.parse(end));
            ShiftDAO dao = new ShiftDAO();
            dao.addShift(s);
            response.sendRedirect(request.getContextPath() + "/shifts");
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tạo ca: " + ex.getMessage());
            request.getRequestDispatcher("/View/Shift/shift-add.jsp").forward(request, response);
        }
    }
}
