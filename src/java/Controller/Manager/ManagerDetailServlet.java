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
import java.util.UUID;

@WebServlet("/manager-detail")
public class ManagerDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String managerId = request.getParameter("manager_id");

        if (managerId == null || managerId.isBlank()) {
            request.setAttribute("error", "Mã quản lý không được để trống");
            request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                    .forward(request, response);
            return;
        }

        try {
            UUID userId = UUID.fromString(managerId);
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerById(userId);

            if (manager == null) {
                request.setAttribute("error", "Không tìm thấy quản lý");
                request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                        .forward(request, response);
                return;
            }

            request.setAttribute("manager", manager);
            request.getRequestDispatcher("/View/Manager/manager-detail.jsp")
                    .forward(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Mã quản lý không hợp lệ");
            request.getRequestDispatcher("/View/Manager/manager-list.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi: " + e.getMessage());
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
