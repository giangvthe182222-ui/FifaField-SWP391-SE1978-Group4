package Controller.Manager;

import DAO.ManagerDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/manager-delete")
public class ManagerDeleteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String managerId = request.getParameter("manager_id");

        if (managerId == null || managerId.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/manager-list");
            return;
        }

        try {
            UUID userId = UUID.fromString(managerId);
            ManagerDAO managerDAO = new ManagerDAO();
            boolean success = managerDAO.deleteManager(userId);

            if (success) {
                response.sendRedirect(request.getContextPath() + "/manager-list");
            } else {
                request.setAttribute("error", "Xóa quản lý thất bại");
                request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                        .forward(request, response);
            }
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/manager-list");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                request.setAttribute("error", "Lỗi: " + e.getMessage());
                request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                        .forward(request, response);
            } catch (ServletException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
