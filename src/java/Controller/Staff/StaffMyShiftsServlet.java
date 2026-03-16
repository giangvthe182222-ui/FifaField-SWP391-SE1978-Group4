package Controller.Staff;

import DAO.StaffShiftDAO;
import Models.StaffShiftViewModel;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "StaffMyShiftsServlet", urlPatterns = {"/staff/my-shifts"})
public class StaffMyShiftsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect=staff/my-shifts");
            return;
        }

        User user = (User) session.getAttribute("user");
        try {
            UUID staffId = user.getUserId();
            StaffShiftDAO dao = new StaffShiftDAO();
            List<StaffShiftViewModel> shifts = dao.getShiftsForStaff(staffId);

            request.setAttribute("staffShifts", shifts);
            request.getRequestDispatcher("/View/Staff/StaffMyShifts.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash_error", "Error loading staff shifts: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
        }
    }
}
