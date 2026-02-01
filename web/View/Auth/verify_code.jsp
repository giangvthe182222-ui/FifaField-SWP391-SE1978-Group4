<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Nhập mã xác minh</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/auth.css">
    </head>
    <body>
        <div class="auth-wrap">
            <div class="card">
                <div class="brand">
                    <div class="logo">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
                        <path d="M12 3l8 4v6c0 5-3.5 9-8 9s-8-4-8-9V7l8-4Z"
                              stroke="white" stroke-width="2" stroke-linejoin="round"/>
                        </svg>
                    </div>
                    <h1 class="title">XÁC MINH</h1>
                    <p class="subtitle">NHẬP MÃ 6 SỐ</p>
                </div>

                <% if (error != null) { %><div class="alert"><%=error%></div><% } %>

                <form class="form" method="post" action="<%=request.getContextPath()%>/verify-code">
                    <div>
                        <div class="label">Mã xác minh</div>
                        <div class="input-wrap">
                            <span class="icon">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </span>
                            <input class="input" type="text" name="code" placeholder="VD: 123456" required>
                        </div>
                    </div>

                    <button class="btn" type="submit">XÁC NHẬN</button>
                </form>
            </div>
        </div>
    </body>
</html>
