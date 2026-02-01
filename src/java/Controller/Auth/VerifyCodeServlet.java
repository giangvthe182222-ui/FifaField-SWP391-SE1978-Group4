package Controller.Auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;

public class VerifyCodeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/View/Auth/verifyCode.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String code = request.getParameter("code");
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String otp = (String) session.getAttribute("reset_otp");
        Long expire = (Long) session.getAttribute("reset_expire");

        if (code == null || code.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập mã xác minh.");
            request.getRequestDispatcher("/View/Auth/verifyCode.jsp").forward(request, response);
            return;
        }

        if (otp == null || expire == null || System.currentTimeMillis() > expire) {
            request.setAttribute("error", "Mã đã hết hạn. Vui lòng gửi lại mã.");
            request.getRequestDispatcher("/View/Auth/verifyCode.jsp").forward(request, response);
            return;
        }

        if (!code.trim().equals(otp)) {
            request.setAttribute("error", "Mã xác minh không đúng.");
            request.getRequestDispatcher("/View/Auth/verifyCode.jsp").forward(request, response);
            return;
        }

        session.setAttribute("reset_verified", true);
        response.sendRedirect(request.getContextPath() + "/reset-password");
    }
}
