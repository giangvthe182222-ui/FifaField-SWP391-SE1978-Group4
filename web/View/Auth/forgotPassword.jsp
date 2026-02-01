<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Quên mật khẩu</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/auth.css">
    </head>
    <body>
        <div class="auth-wrap">
            <div class="card">
                <div class="brand">
                    <div class="logo">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                        <path d="M4 6h16v12H4V6Z" stroke="white" stroke-width="2"/>
                        <path d="m4 7 8 6 8-6" stroke="white" stroke-width="2"/>
                        </svg>
                    </div>
                    <h1 class="title">QUÊN MẬT KHẨU</h1>
                    <p class="subtitle">NHẬP EMAIL CỦA BẠN</p>
                </div>

                <% if (error != null) { %><div class="alert"><%=error%></div><% } %>

                <form class="form" method="post" action="<%=request.getContextPath()%>/forgot-password">
                    <div>
                        <div class="label">Email</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M4 6h16v12H4V6Z" stroke="currentColor" stroke-width="2"/>
                                <path d="m4 7 8 6 8-6" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="email" name="email" placeholder="mail_cua_be@example.com" required>
                        </div>
                    </div>

                    <button class="btn" type="submit">GỬI MÃ</button>

                    <div class="footer">
                        <a href="<%=request.getContextPath()%>/login">Quay lại đăng nhập</a>
                    </div>
                </form>
            </div>
        </div>
    </body>
</html>
