package Controller.Staff;

import DAO.StaffDAO;
import Models.StaffViewModel;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StaffDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null || id.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/staff/list");
            return;
        }
        StaffDAO dao = new StaffDAO();
        try {
            StaffViewModel vm = dao.getStaffById(id);
            if (vm == null) {
                response.sendRedirect(request.getContextPath() + "/staff/list");
                return;
            }
            request.setAttribute("staff", vm);
            request.getRequestDispatcher("/View/Staff/StaffDetail.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
