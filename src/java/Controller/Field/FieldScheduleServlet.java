package Controller.Field;

import DAO.FieldDAO;
import DAO.ScheduleDAO;
import Models.Field;
import Models.Schedule;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/field-schedule")
public class FieldScheduleServlet extends HttpServlet {

    // 🔥 HARD CODE FIELD ID (đã chắc chắn có schedule)
    private static final UUID FIELD_ID =
            UUID.fromString("0AA32471-F9F8-41D3-A901-25590CF90E51");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FieldDAO fieldDAO = new FieldDAO();
        ScheduleDAO scheduleDAO = new ScheduleDAO();

        Field field = fieldDAO.getById(FIELD_ID);
        if (field == null) {
    response.getWriter().println("FIELD NOT FOUND: " + FIELD_ID);
    return;
}


      List<Schedule> schedules = scheduleDAO.getScheduleByField(FIELD_ID);

        request.setAttribute("field", field);
        request.setAttribute("schedules", schedules);

        request.getRequestDispatcher("/View/Field/FieldSchedule.jsp")
               .forward(request, response);
    }
}
