package Controller.Auth;

import DAO.AuthDAO;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String flashError = (String) session.getAttribute("flash_error");
            if (flashError != null) {
                request.setAttribute("error", flashError);
                session.removeAttribute("flash_error");
            }

            String flashSuccess = (String) session.getAttribute("success");
            if (flashSuccess != null) {
                request.setAttribute("success", flashSuccess);
                session.removeAttribute("success");
            }
        }

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

        User user = dao.getUserById(userId);
        if (user == null) {
            request.setAttribute("error", "Lỗi hệ thống. Vui lòng thử lại.");
            request.getRequestDispatcher("/View/Auth/login.jsp").forward(request, response);
            return;
        }

        request.getSession(true).setAttribute("user", user);

        // Redirect based on role
        String roleName = user.getRole().getRoleName().toLowerCase();
        String redirectUrl;
        switch (roleName) {
            case "admin":
                redirectUrl = "/admin-dashboard";
                break;
            case "manager":
                redirectUrl = "/View/Auth/homepage.jsp"; // Assuming manager dashboard
                break;
            case "staff":
                redirectUrl = "/View/Auth/homepage.jsp"; // Assuming staff dashboard
                break;
            case "customer":
                redirectUrl = "/booking";
                break;
            default:
                redirectUrl = "/View/Auth/homepage.jsp";
                break;
        }

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}
