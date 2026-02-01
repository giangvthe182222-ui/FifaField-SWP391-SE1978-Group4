package Controller.Auth;

import DAO.AuthDAO;
import jakarta.servlet.ServletException; 
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/View/Auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String gender = request.getParameter("gender");
        String confirmPassword = request.getParameter("confirmPassword");

        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            request.setAttribute("fullNameValue", fullName);
            request.setAttribute("emailValue", email);
            request.getRequestDispatcher("/View/Auth/register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("error", "Mật khẩu tối thiểu 6 ký tự.");
            request.setAttribute("fullNameValue", fullName);
            request.setAttribute("emailValue", email);
            request.getRequestDispatcher("/View/Auth/register.jsp").forward(request, response);
            return;
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập lại mật khẩu.");
            request.setAttribute("fullNameValue", fullName);
            request.setAttribute("emailValue", email);
            request.setAttribute("phoneValue", phone);
            request.setAttribute("addressValue", address);
            request.setAttribute("genderValue", gender);
            request.getRequestDispatcher("/View/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu nhập lại không khớp.");
            request.setAttribute("fullNameValue", fullName);
            request.setAttribute("emailValue", email);
            request.setAttribute("phoneValue", phone);
            request.setAttribute("addressValue", address);
            request.setAttribute("genderValue", gender);
            request.getRequestDispatcher("/View/Auth/register.jsp").forward(request, response);
            return;
        }

        try {
            AuthDAO dao = new AuthDAO();

            if (dao.emailExists(email)) {
                request.setAttribute("error", "Email đã tồn tại.");
                request.setAttribute("fullNameValue", fullName);
                request.setAttribute("emailValue", email);
                request.getRequestDispatcher("/View/Auth/register.jsp").forward(request, response);
                return;
            }

            dao.registerCustomer(
                    fullName.trim(),
                    email.trim(),
                    password,
                    phone,
                    address,
                    gender
            );

            // dang ky xong -> ve login de dang nhap
            request.getSession().setAttribute(
                    "success",
                    "Tạo tài khoản thành công 🎉"
            );
            response.sendRedirect(request.getContextPath() + "/login");

        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            request.setAttribute("fullNameValue", fullName);
            request.setAttribute("emailValue", email);
            request.setAttribute("phoneValue", phone);
            request.setAttribute("addressValue", address);
            request.setAttribute("genderValue", gender);

            request.getRequestDispatcher("/View/Auth/register.jsp").forward(request, response);
        }
    }
}
