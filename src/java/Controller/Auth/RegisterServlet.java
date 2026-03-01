package Controller.Auth;

import DAO.AuthDAO;
import Utils.EmailUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Random;

public class RegisterServlet extends HttpServlet {

    private String generateOtp() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/View/Auth/register.jsp").forward(req, resp);
    }

    private void keepForm(HttpServletRequest req) {
        req.setAttribute("emailValue", req.getParameter("email"));
        req.setAttribute("fullNameValue", req.getParameter("fullName"));
        req.setAttribute("phoneValue", req.getParameter("phone"));
        req.setAttribute("addressValue", req.getParameter("address"));
        req.setAttribute("genderValue", req.getParameter("gender"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            // Neu user bam submit form tao tai khoan ma khong co action
            // thi mac dinh coi nhu "create"
            action = "create";
        }

        HttpSession session = req.getSession(true);

        switch (action) {
            case "send": {
                keepForm(req);

                String email = req.getParameter("email");
                if (email == null || email.isBlank()) {
                    req.setAttribute("error", "Vui lòng nhập email để nhận mã.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                    return;
                }

                try {
                    // Neu email da ton tai => khong cho dang ky
                    AuthDAO dao = new AuthDAO();
                    if (dao.emailExists(email.trim())) {
                        req.setAttribute("error", "Email đã tồn tại. Vui lòng dùng email khác hoặc đăng nhập.");
                        req.setAttribute("verified", false);
                        forward(req, resp);
                        return;
                    }

                    String otp = generateOtp();
                    long expireAt = System.currentTimeMillis() + 5 * 60 * 1000;

                    session.setAttribute("reg_email", email.trim());
                    session.setAttribute("reg_otp", otp);
                    session.setAttribute("reg_expire", expireAt);
                    session.setAttribute("reg_verified", false);

                    EmailUtil.sendOtp(email.trim(), otp);

                    req.setAttribute("success", "Đã gửi mã xác nhận. Vui lòng kiểm tra email.");
                    req.setAttribute("verified", false);
                    forward(req, resp);

                } catch (Exception e) {
                    e.printStackTrace();
                    req.setAttribute("error", "Gửi mã thất bại. Kiểm tra SMTP/App Password.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                }
                return;
            }

            case "verify": {
                String verifyKey = java.util.UUID.randomUUID().toString();
                session.setAttribute("reg_verify_key", verifyKey);
                req.setAttribute("verifyKey", verifyKey);

                keepForm(req);

                String code = req.getParameter("code");
                String se = (String) session.getAttribute("reg_email");
                String otp = (String) session.getAttribute("reg_otp");
                Long expire = (Long) session.getAttribute("reg_expire");

                if (se == null || otp == null || expire == null) {
                    req.setAttribute("error", "Bạn chưa gửi mã. Vui lòng bấm Gửi trước.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                    return;
                }

                if (System.currentTimeMillis() > expire) {
                    req.setAttribute("error", "Mã đã hết hạn. Vui lòng bấm Gửi để nhận mã mới.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                    return;
                }

                if (code == null || code.isBlank()) {
                    req.setAttribute("error", "Vui lòng nhập mã xác nhận.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                    return;
                }

                if (!code.trim().equals(otp)) {
                    req.setAttribute("error", "Mã xác nhận không đúng.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                    return;
                }

                session.setAttribute("reg_verified", true);
                req.setAttribute("success", "Xác minh email thành công. Bạn có thể đặt mật khẩu.");
                req.setAttribute("verified", true);
                forward(req, resp);
                return;
            }

            case "create": {
                // Chi cho create neu da verified
                Boolean v = (Boolean) session.getAttribute("reg_verified");
                boolean verified = (v != null && v);
                String verifyKeyForm = req.getParameter("verifyKey");
                String verifyKeySession = (String) session.getAttribute("reg_verify_key");
                if (verifyKeySession == null || verifyKeyForm == null || !verifyKeySession.equals(verifyKeyForm)) {
                    req.setAttribute("error", "Hành động không hợp lệ. Vui lòng xác minh email trước khi tạo tài khoản.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                    return;
                }

                keepForm(req);

                if (!verified) {
                    req.setAttribute("error", "Vui lòng xác minh email trước khi tạo tài khoản.");
                    req.setAttribute("verified", false);
                    forward(req, resp);
                    return;
                }

                String emailSession = (String) session.getAttribute("reg_email");
                String emailForm = req.getParameter("email");
                if (emailSession == null || emailForm == null || !emailSession.equalsIgnoreCase(emailForm.trim())) {
                    req.setAttribute("error", "Email không khớp với email đã xác minh. Vui lòng gửi/verify lại.");
                    req.setAttribute("verified", false);
                    session.setAttribute("reg_verified", false);
                    forward(req, resp);
                    return;
                }

                String fullName = req.getParameter("fullName");
                String phone = req.getParameter("phone");
                String address = req.getParameter("address");
                String gender = req.getParameter("gender");
                String password = req.getParameter("password");
                String confirm = req.getParameter("confirmPassword");

                // regex cho phep
                String PASSWORD_REGEX = "^[A-Za-z0-9!@#$%^&*()]+$";

                if (fullName == null || fullName.isBlank()) {
                    req.setAttribute("error", "Vui lòng nhập họ và tên.");
                    req.setAttribute("verified", true);
                    forward(req, resp);
                    return;
                }

                if (password == null || password.isBlank() || confirm == null || confirm.isBlank()) {
                    req.setAttribute("error", "Vui lòng nhập mật khẩu và nhập lại mật khẩu.");
                    req.setAttribute("verified", true);
                    forward(req, resp);
                    return;
                }

                if (password.length() < 6) {
                    req.setAttribute("error", "Mật khẩu tối thiểu 6 ký tự.");
                    req.setAttribute("verified", true);
                    forward(req, resp);
                    return;
                }

                if (password.length() > 20) { // vì DB NVARCHAR(20)
                    req.setAttribute("error", "Mật khẩu tối đa 20 ký tự (do DB hiện tại).");
                    req.setAttribute("verified", true);
                    forward(req, resp);
                    return;
                }

                if (!password.equals(confirm)) {
                    req.setAttribute("error", "Nhập lại mật khẩu không khớp.");
                    req.setAttribute("verified", true);
                    forward(req, resp);
                    return;
                }

                if (!password.matches(PASSWORD_REGEX)) {
                    req.setAttribute("error",
                            "Mật khẩu chỉ được chứa chữ không dấu, số và các ký tự !@#$%^&*(). Không chứa khoảng trắng hoặc ký tự đặc biệt khác.");
                    req.setAttribute("verified", true); // giu trang thai da xac minh email
                    forward(req, resp);
                    return;
                }

                try {
                    AuthDAO dao = new AuthDAO();

                    // check lai email ton tai (tranh race)
                    if (dao.emailExists(emailForm.trim())) {
                        req.setAttribute("error", "Email đã tồn tại. Vui lòng dùng email khác.");
                        req.setAttribute("verified", false);
                        session.setAttribute("reg_verified", false);
                        forward(req, resp);
                        return;
                    }

                    dao.registerCustomer(fullName.trim(), emailForm.trim(), password, phone, address, gender);

                    // don session dang ky
                    session.removeAttribute("reg_email");
                    session.removeAttribute("reg_otp");
                    session.removeAttribute("reg_expire");
                    session.removeAttribute("reg_verified");
                    session.removeAttribute("reg_verify_key");

                    // flash success sang login
                    session.setAttribute("success", "🎉 Tạo tài khoản thành công! Vui lòng đăng nhập.");
                    resp.sendRedirect(req.getContextPath() + "/login");

                } catch (Exception e) {
                    e.printStackTrace();
                    req.setAttribute("error", "Tạo tài khoản thất bại: " + e.getMessage());
                    req.setAttribute("verified", true);
                    forward(req, resp);
                }
                return;
            }

            default: {
                // Neu user submit khong co action
                req.setAttribute("error", "Hành động không hợp lệ.");
                forward(req, resp);
            }
        }
    }
}
