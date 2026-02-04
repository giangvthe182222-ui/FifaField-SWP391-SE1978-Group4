package Controller.Staff;

import DAO.StaffDAO;
import Models.StaffViewModel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StaffListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StaffDAO dao = new StaffDAO();
        try {
            List<StaffViewModel> list = dao.getAllStaff();
            request.setAttribute("staffs", list);
            request.getRequestDispatcher("/View/Staff/StaffList.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
