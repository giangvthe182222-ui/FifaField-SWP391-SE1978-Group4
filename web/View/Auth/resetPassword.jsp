<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Đặt lại mật khẩu</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/auth.css">
    </head>
    <body>
        <div class="auth-wrap">
            <div class="card">
                <div class="brand">
                    <div class="logo">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                        <path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="white" stroke-width="2"/>
                        <path d="M6 11h12v10H6V11Z" stroke="white" stroke-width="2"/>
                        </svg>
                    </div>
                    <h1 class="title">MẬT KHẨU MỚI</h1>
                    <p class="subtitle">ĐẶT LẠI MẬT KHẨU</p>
                </div>

                <% if (error != null) { %><div class="alert"><%=error%></div><% } %>

                <form class="form" method="post" action="<%=request.getContextPath()%>/reset-password">
                    <div>
                        <div class="label">Mật khẩu mới</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="2"/>
                                <path d="M6 11h12v10H6V11Z" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="password" name="newPassword" placeholder="Tối thiểu 6 ký tự" required>
                        </div>
                    </div>

                    <div>
                        <div class="label">Nhập lại mật khẩu mới</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M7 11V8a5 5 0 0 1 10 0v3" stroke="currentColor" stroke-width="2"/>
                                <path d="M6 11h12v10H6V11Z" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="password" name="confirmPassword" placeholder="Nhập lại mật khẩu" required>
                        </div>
                    </div>

                    <button class="btn" type="submit">CẬP NHẬT</button>
                </form>
            </div>
        </div>
    </body>
</html>
