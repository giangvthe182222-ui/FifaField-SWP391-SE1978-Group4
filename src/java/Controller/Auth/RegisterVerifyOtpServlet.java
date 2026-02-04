package Controller.Auth;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;

import java.io.IOException;

public class RegisterVerifyOtpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.getWriter().write("{\"ok\":false,\"msg\":\"Phiên xác minh đã hết. Vui lòng gửi lại mã.\"}");
            return;
        }

        String email = req.getParameter("email");
        String code = req.getParameter("code");

        String sEmail = (String) session.getAttribute("reg_email");
        String otp = (String) session.getAttribute("reg_otp");
        Long expire = (Long) session.getAttribute("reg_expire");

        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            resp.getWriter().write("{\"ok\":false,\"msg\":\"Vui lòng nhập email và mã.\"}");
            return;
        }

        if (sEmail == null || otp == null || expire == null) {
            resp.getWriter().write("{\"ok\":false,\"msg\":\"Vui lòng bấm GỬI MÃ trước.\"}");
            return;
        }

        if (!email.trim().equalsIgnoreCase(sEmail.trim())) {
            resp.getWriter().write("{\"ok\":false,\"msg\":\"Email không khớp email đã gửi mã.\"}");
            return;
        }

        if (System.currentTimeMillis() > expire) {
            resp.getWriter().write("{\"ok\":false,\"msg\":\"Mã đã hết hạn. Vui lòng gửi lại.\"}");
            return;
        }

        if (!code.trim().equals(otp)) {
            resp.getWriter().write("{\"ok\":false,\"msg\":\"Mã xác minh không đúng.\"}");
            return;
        }

        // sau khi verify đúng:
        session.setAttribute("reg_verified", true);

// tạo verifyKey
        String verifyKey = java.util.UUID.randomUUID().toString();
        session.setAttribute("reg_verify_key", verifyKey);

// trả về JSON có verifyKey
        resp.getWriter().write("{\"ok\":true,\"msg\":\"Xác minh thành công!\",\"verifyKey\":\"" + verifyKey + "\"}");

    }
}
