package Controller.Manager;

import DAO.ShiftDAO;
import Models.Shift;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class ManagerShiftsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            ShiftDAO shiftDAO = new ShiftDAO();
            List<Shift> shifts = shiftDAO.getAllShifts();
            request.setAttribute("shifts", shifts);
            request.getRequestDispatcher("/View/Manager/manager-shifts.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách ca: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-shifts.jsp").forward(request, response);
        }
    }
}
