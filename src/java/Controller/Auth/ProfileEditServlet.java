package Controller.Auth;

import DAO.AuthDAO;
import Models.User;
import Utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

public class ProfileEditServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/View/Auth/ProfileEdit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");

        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");

        if (fullName == null || fullName.isBlank()) {
            request.setAttribute("error", "Họ tên không được để trống.");
            doGet(request, response);
            return;
        }

        try {
            AuthDAO dao = new AuthDAO();
            dao.updateUserBasic(user.getUserId().toString(), fullName, phone, address, gender);
            // refresh session user
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setAddress(address);
            user.setGender(gender);
            session.setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/auth/profile");
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
