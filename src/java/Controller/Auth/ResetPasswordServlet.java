package Controller.Auth;

import DAO.PasswordResetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;

public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("reset_verified") == null
                || !(Boolean) session.getAttribute("reset_verified")) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        request.getRequestDispatcher("/View/Auth/resetPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("reset_verified") == null
                || !(Boolean) session.getAttribute("reset_verified")) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String newPass = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");

        if (newPass == null || newPass.isBlank() || confirm == null || confirm.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ mật khẩu mới.");
            request.getRequestDispatcher("/View/Auth/resetPassword.jsp").forward(request, response);
            return;
        }

        if (newPass.length() < 6) {
            request.setAttribute("error", "Mật khẩu tối thiểu 6 ký tự.");
            request.getRequestDispatcher("/View/Auth/resetPassword.jsp").forward(request, response);
            return;
        }

        if (newPass.length() > 20) {
            request.setAttribute("error", "Mật khẩu tối đa 20 ký tự (do DB hiện tại).");
            request.getRequestDispatcher("/View/Auth/resetPassword.jsp").forward(request, response);
            return;
        }

        if (!newPass.equals(confirm)) {
            request.setAttribute("error", "Nhập lại mật khẩu không khớp.");
            request.getRequestDispatcher("/View/Auth/resetPassword.jsp").forward(request, response);
            return;
        }

        String email = (String) session.getAttribute("reset_email");
        if (email == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        PasswordResetDAO dao = new PasswordResetDAO();
        boolean ok = dao.updatePasswordByEmail(email, newPass);

        // don session reset
        session.removeAttribute("reset_email");
        session.removeAttribute("reset_otp");
        session.removeAttribute("reset_expire");
        session.removeAttribute("reset_verified");

        if (ok) {
            session.setAttribute("success", "✅ Đổi mật khẩu thành công! Vui lòng đăng nhập.");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("error", "Không đổi được mật khẩu. Vui lòng thử lại.");
            request.getRequestDispatcher("/View/Auth/resetPassword.jsp").forward(request, response);
        }
    }
}
