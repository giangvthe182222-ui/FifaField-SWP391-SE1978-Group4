package Controller.Auth;

import DAO.AuthDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
 
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/View/Auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập email và mật khẩu.");
            request.getRequestDispatcher("/View/Auth/login.jsp").forward(request, response);
            return;
        }

        AuthDAO dao = new AuthDAO();
        String userId = dao.login(email, password);

        if (userId == null) {
            request.setAttribute("error", "Email hoặc mật khẩu không đúng.");
            request.setAttribute("emailValue", email);
            request.getRequestDispatcher("/View/Auth/login.jsp").forward(request, response);
            return;
        }

        request.getSession(true).setAttribute("userId", userId);

        response.sendRedirect(request.getContextPath() + "/View/Auth/homepage.jsp");
    }
}
