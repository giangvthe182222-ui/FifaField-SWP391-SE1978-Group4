<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String error = (String) request.getAttribute("error");
    String emailValue = (String) request.getAttribute("emailValue");

    String success = (String) session.getAttribute("success");
    if (success != null) {
        session.removeAttribute("success"); // chỉ hiển thị 1 lần
    }

    if (emailValue == null) emailValue = "";
%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>FIFA FIELD - Login</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

        <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/auth.css">
    </head>
    <body>
        <div class="auth-wrap">
            <div class="card">
                <div class="brand">
                    <div class="logo">
                        <!-- trophy icon -->
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                        <path d="M7 4h10v2H7V4Zm-2 4h14v5a7 7 0 0 1-14 0V8Zm4 12h6v2H9v-2Z"
                              stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <h1 class="title">FIFA FIELD</h1>
                    <p class="subtitle">ĐĂNG NHẬP VÀO TÀI KHOẢN CỦA BẠN</p>
                </div>

                <% if (success != null) { %>
                <div class="success"><%= success %></div>
                <% } %>

                <% if (error != null) { %>
                <div class="alert"><%= error %></div>
                <% } %>

                <form class="form" action="<%=request.getContextPath()%>/login" method="post">
                    <div>
                        <div class="label">Địa chỉ Email</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <!-- mail icon -->
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M4 6h16v12H4V6Z" stroke="currentColor" stroke-width="2" />
                                <path d="m4 7 8 6 8-6" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="email" name="email" placeholder="name@example.com"
                                   value="<%=emailValue%>" required>
                        </div>
                    </div>

                    <div>
                        <div class="label">Mật khẩu</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <!-- lock icon -->
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="2"/>
                                <path d="M6 11h12v10H6V11Z" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="password" name="password" placeholder="••••••••" required>
                        </div>
                    </div>

                    <div class="row-right">
                        <a class="link" href="<%=request.getContextPath()%>/forgot-password">Quên mật khẩu?</a>
                    </div>

                    <button class="btn btn-login" type="submit">ĐĂNG NHẬP</button>

                    <div class="divider">HOẶC</div>
                    <button class="btn-google" type="button"
                            onclick="location.href = '<%=request.getContextPath()%>/google-login'">
                        <img src="<%=request.getContextPath()%>/assets/img/google.svg" alt="Google">
                        <span>Đăng nhập bằng Google</span>
                    </button>

                    <div class="footer">
                        Chưa có tài khoản? <a href="<%=request.getContextPath()%>/register">Đăng ký ngay</a>
                    </div>

                </form>
            </div>
        </div>
    </body>
</html>
