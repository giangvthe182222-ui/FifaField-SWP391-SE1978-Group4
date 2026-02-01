<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String error = (String) request.getAttribute("error");
    String fullNameValue = (String) request.getAttribute("fullNameValue");
    String emailValue = (String) request.getAttribute("emailValue");
    String phoneValue = (String) request.getAttribute("phoneValue");
    String addressValue = (String) request.getAttribute("addressValue");
    String genderValue = (String) request.getAttribute("genderValue");

    if (fullNameValue == null) fullNameValue = "";
    if (emailValue == null) emailValue = "";
    if (phoneValue == null) phoneValue = "";
    if (addressValue == null) addressValue = "";
    if (genderValue == null) genderValue = "";
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>FIFA FIELD - Register</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/auth.css">
    </head>
    <body>
        <div class="auth-wrap">
            <div class="card">
                <div class="brand">
                    <div class="logo">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                        <path d="M7 4h10v2H7V4Zm-2 4h14v5a7 7 0 0 1-14 0V8Zm4 12h6v2H9v-2Z"
                              stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <h1 class="title">ĐĂNG KÝ</h1>
                    <p class="subtitle">TẠO TÀI KHOẢN MỚI</p>
                </div>

                <% if (error != null) { %>
                <div class="alert"><%=error%></div>
                <% } %>

                <form class="form" action="<%=request.getContextPath()%>/register" method="post">

                    <div>
                        <div class="label">Họ và tên</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M20 21a8 8 0 1 0-16 0" stroke="currentColor" stroke-width="2"/>
                                <path d="M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="text" name="fullName" placeholder="Nguyễn Văn A"
                                   value="<%=fullNameValue%>" required>
                        </div>
                    </div>

                    <div>
                        <div class="label">Email</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M4 6h16v12H4V6Z" stroke="currentColor" stroke-width="2"/>
                                <path d="m4 7 8 6 8-6" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="email" name="email" placeholder="name@example.com"
                                   value="<%=emailValue%>" required>
                        </div>
                    </div>

                    <div>
                        <div class="label">Số điện thoại</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <!-- phone icon -->
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M22 16.92v3a2 2 0 0 1-2.18 2
                                      19.79 19.79 0 0 1-8.63-3.07
                                      19.5 19.5 0 0 1-6-6
                                      19.79 19.79 0 0 1-3.07-8.67
                                      A2 2 0 0 1 4.11 2h3
                                      a2 2 0 0 1 2 1.72
                                      12.44 12.44 0 0 0 .7 2.81
                                      2 2 0 0 1-.45 2.11L8.09 9.91
                                      a16 16 0 0 0 6 6l1.27-1.27
                                      a2 2 0 0 1 2.11-.45
                                      12.44 12.44 0 0 0 2.81.7
                                      A2 2 0 0 1 22 16.92Z"
                                      stroke="currentColor" stroke-width="2"
                                      stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </span>
                            <input class="input" type="text" name="phone"
                                   placeholder="VD: 0901234567"
                                   value="<%= phoneValue %>">
                        </div>
                    </div>

                    <div>
                        <div class="label">Địa chỉ</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <!-- home icon -->
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M3 9.5L12 3l9 6.5V21a1 1 0 0 1-1 1h-5v-7H9v7H4
                                      a1 1 0 0 1-1-1V9.5Z"
                                      stroke="currentColor" stroke-width="2"
                                      stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </span>
                            <input class="input" type="text" name="address"
                                   placeholder="VD: Quận 1, TP.HCM"
                                   value="<%= addressValue %>">
                        </div>
                    </div>

                    <div>
                        <div class="label">Mật khẩu</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="2"/>
                                <path d="M6 11h12v10H6V11Z" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="password" name="password" placeholder="Tối thiểu 6 ký tự" required>
                        </div>
                    </div>

                    <div>
                        <div class="label">Nhập lại mật khẩu</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <!-- lock icon -->
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="2"/>
                                <path d="M6 11h12v10H6V11Z" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="password" name="confirmPassword"
                                   placeholder="Nhập lại mật khẩu" required>
                        </div>
                    </div>

                    <button class="btn" type="submit">TẠO TÀI KHOẢN</button>

                    <div class="footer">
                        Đã có tài khoản? <a href="<%=request.getContextPath()%>/login">Đăng nhập</a>
                    </div>

                </form>
            </div>
        </div>
    </body>
</html>
