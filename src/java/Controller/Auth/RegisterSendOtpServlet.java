package Controller.Auth;

import DAO.PasswordResetDAO;
import Utils.EmailUtil;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.Random;

public class RegisterSendOtpServlet extends HttpServlet {

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");

        String email = req.getParameter("email");
        if (email == null || email.isBlank()) {
            resp.getWriter().write("{\"ok\":false,\"msg\":\"Vui lòng nhập email.\"}");
            return;
        }

        try {
            // Neu email da ton tai -> ko cho dky (tranh bi trung)
            PasswordResetDAO dao = new PasswordResetDAO();
            if (dao.emailExists(email)) {
                resp.getWriter().write("{\"ok\":false,\"msg\":\"Email đã tồn tại.\"}");
                return;
            }

            String otp = generateOtp();
            long expireAt = System.currentTimeMillis() + 5 * 60 * 1000;

            HttpSession session = req.getSession(true);
            session.setAttribute("reg_email", email.trim());
            session.setAttribute("reg_otp", otp);
            session.setAttribute("reg_expire", expireAt);
            session.setAttribute("reg_verified", false);

            EmailUtil.sendOtp(email.trim(), otp);

            resp.getWriter().write("{\"ok\":true,\"msg\":\"Đã gửi mã. Vui lòng kiểm tra email.\"}");
        } catch (Exception e) {
            e.printStackTrace();
            String m = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
            resp.getWriter().write("{\"ok\":false,\"msg\":\"" + m.replace("\"", "'") + "\"}");
        }

    }
}
