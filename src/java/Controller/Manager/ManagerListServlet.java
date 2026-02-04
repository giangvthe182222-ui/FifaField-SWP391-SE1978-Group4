package Controller.Manager;

import DAO.ManagerDAO;
import Models.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/manager-list")
public class ManagerListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            ManagerDAO managerDAO = new ManagerDAO();
            List<Manager> managers = managerDAO.getAllManagers();
            request.setAttribute("managers", managers);
            request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách quản lý: " + e.getMessage());
            request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
