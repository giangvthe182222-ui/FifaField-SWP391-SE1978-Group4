package Controller.Shift;

import DAO.ShiftDAO;
import Models.Shift;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.UUID;

@WebServlet("/shifts/edit")
public class EditShiftServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        try {
            ShiftDAO dao = new ShiftDAO();
            Shift s = dao.getShiftById(UUID.fromString(id));
            request.setAttribute("shift", s);
            request.getRequestDispatcher("/View/Shift/shift-edit.jsp").forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải ca: " + ex.getMessage());
            request.getRequestDispatcher("/View/Shift/shifts-list.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("shiftId");
            String name = request.getParameter("shiftName");
            String start = request.getParameter("startTime");
            String end = request.getParameter("endTime");
            Shift s = new Shift();
            s.setShiftId(UUID.fromString(id));
            s.setShiftName(name);
            s.setStartTime(LocalTime.parse(start));
            s.setEndTime(LocalTime.parse(end));
            ShiftDAO dao = new ShiftDAO();
            dao.updateShift(s);
            response.sendRedirect(request.getContextPath() + "/shifts");
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("error", "Lỗi khi cập nhật ca: " + ex.getMessage());
            request.getRequestDispatcher("/View/Shift/shift-edit.jsp").forward(request, response);
        }
    }
}
