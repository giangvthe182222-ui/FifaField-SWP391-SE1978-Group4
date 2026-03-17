package Controller.Shift;

import DAO.ShiftDAO;
import Models.Shift;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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
            LocalTime startTime = LocalTime.parse(start);
            LocalTime endTime = LocalTime.parse(end);

            if (!endTime.isAfter(startTime)) {
                request.setAttribute("error", "Giờ kết thúc phải sau giờ bắt đầu.");
                request.setAttribute("shiftName", name);
                request.setAttribute("startTime", start);
                request.setAttribute("endTime", end);
                request.getRequestDispatcher("/View/Shift/shift-add.jsp").forward(request, response);
                return;
            }

            Shift s = new Shift();
            s.setShiftId(UUID.randomUUID());
            s.setShiftName(name);
            s.setStartTime(startTime);
            s.setEndTime(endTime);
            ShiftDAO dao = new ShiftDAO();
            dao.addShift(s);
            response.sendRedirect(request.getContextPath() + "/manager/shifts");
        } catch (Exception ex) {
            ex.printStackTrace();
            request.setAttribute("shiftName", name);
            request.setAttribute("startTime", start);
            request.setAttribute("endTime", end);
            request.setAttribute("error", "Lỗi khi tạo ca: " + ex.getMessage());
            request.getRequestDispatcher("/View/Shift/shift-add.jsp").forward(request, response);
        }
    }
}
