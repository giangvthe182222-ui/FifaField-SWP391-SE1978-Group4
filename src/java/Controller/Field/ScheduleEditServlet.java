package Controller.Field;

import DAO.ScheduleDAO;
import Models.Schedule;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@WebServlet("/schedule-edit")
public class ScheduleEditServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sid = request.getParameter("scheduleId");
        if (sid == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            UUID scheduleId = UUID.fromString(sid);
            ScheduleDAO dao = new ScheduleDAO();
            Schedule s = dao.getById(scheduleId);
            if (s == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("schedule", s);
            request.getRequestDispatcher("/View/Field/schedule-edit.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String sid = request.getParameter("scheduleId");
        String priceStr = request.getParameter("price");
        String status = request.getParameter("status");
        String fieldId = request.getParameter("fieldId");

        if (sid == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            UUID scheduleId = UUID.fromString(sid);
            BigDecimal price = priceStr == null || priceStr.isBlank() ? BigDecimal.ZERO : new BigDecimal(priceStr);

            ScheduleDAO dao = new ScheduleDAO();
            boolean ok = dao.updateSchedule(scheduleId, price, status);
            if (!ok) {
                request.setAttribute("error", "Không thể cập nhật lịch");
            }
            // redirect back to field schedule view
            if (fieldId != null && !fieldId.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/field-schedule?fieldId=" + fieldId);
            } else {
                response.sendRedirect(request.getContextPath() + "/field-schedule");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
