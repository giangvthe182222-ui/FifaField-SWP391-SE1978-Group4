package Controller.Auth;

import DAO.PasswordResetDAO;
import Utils.EmailUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Random;

public class ForgotPasswordServlet extends HttpServlet {

    private String generateOtp() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code); 
    }
 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/View/Auth/forgotPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập email.");
            request.getRequestDispatcher("/View/Auth/forgotPassword.jsp").forward(request, response);
            return;
        }

        try {
            PasswordResetDAO dao = new PasswordResetDAO();

            // Bao mat: du email ko ton tai cung bao "da gui" de tranh do email
            boolean exists = dao.emailExists(email);

            String otp = generateOtp();
            long expireAt = System.currentTimeMillis() + 5 * 60 * 1000; // 5 phut

            HttpSession session = request.getSession(true);
            session.setAttribute("reset_email", email.trim());
            session.setAttribute("reset_otp", otp);
            session.setAttribute("reset_expire", expireAt);
            session.setAttribute("reset_verified", false);

            if (exists) {
                EmailUtil.sendOtp(email.trim(), otp);
            }

            // chuyen sang nhap code
            response.sendRedirect(request.getContextPath() + "/verify-code");

        } catch (Exception e) {
            request.setAttribute("error", "Không gửi được email. Kiểm tra SMTP/App Password.");
            request.getRequestDispatcher("/View/Auth/forgotPassword.jsp").forward(request, response);
        }
    }
}
