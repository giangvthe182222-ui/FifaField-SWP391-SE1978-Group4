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
                    <input type="hidden" id="verifyKey" name="verifyKey" value="">
                    <input type="hidden" name="action" value="create">

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
                            <input id="email" class="input" type="email" name="email"
                                   placeholder="Email" value="<%=emailValue%>" required>
                        </div>
                    </div>

                    <div>
                        <div class="input-wrap otp-wrap">
                            <input id="otp" class="input otp-input" type="text"
                                   placeholder="Mã Xác Nhận" autocomplete="one-time-code" inputmode="numeric">
                            <button id="btnSendOtp" class="otp-send" type="button">Gửi</button>
                        </div>

                        <div id="sendMsg" class="hint"></div>
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
                            <input id="password" class="input" type="password" name="password" disabled required>
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
                            <input id="confirmPassword" class="input" type="password" name="confirmPassword" disabled required>
                        </div>
                    </div>

                    <button id="btnSubmit" class="btn" type="submit" disabled>TẠO TÀI KHOẢN</button>

                    <div class="footer">
                        Đã có tài khoản? <a href="<%=request.getContextPath()%>/login">Đăng nhập</a>
                    </div>

                </form>
            </div>
        </div>

        <script>
            const ctx = "<%=request.getContextPath()%>";

            const emailEl = document.getElementById("email");
            const otpEl = document.getElementById("otp");
            const btnSend = document.getElementById("btnSendOtp");
            const sendMsg = document.getElementById("sendMsg");

            const passEl = document.getElementById("password");
            const cpassEl = document.getElementById("confirmPassword");
            const btnSubmit = document.getElementById("btnSubmit");

            let cooldownTimer = null;
            let cooldownLeft = 0;

            function setHint(msg, ok) {
                sendMsg.textContent = msg || "";
                sendMsg.style.color = ok ? "#0b8a58" : "#b91c1c";
                sendMsg.style.fontWeight = "700";
                sendMsg.style.marginTop = "8px";
                sendMsg.style.fontSize = "13px";
            }

            function lockRegister() {
                passEl.disabled = true;
                cpassEl.disabled = true;
                btnSubmit.disabled = true;
            }

            function unlockRegister() {
                passEl.disabled = false;
                cpassEl.disabled = false;
                btnSubmit.disabled = false;
            }

            function startCooldown(seconds) {
                cooldownLeft = seconds;
                btnSend.disabled = true;
                btnSend.textContent = cooldownLeft + "s";

                if (cooldownTimer)
                    clearInterval(cooldownTimer);

                cooldownTimer = setInterval(() => {
                    cooldownLeft--;
                    if (cooldownLeft <= 0) {
                        clearInterval(cooldownTimer);
                        cooldownTimer = null;
                        btnSend.disabled = false;
                        btnSend.textContent = "Gửi lại";
                    } else {
                        btnSend.textContent = cooldownLeft + "s";
                    }
                }, 1000);
            }

            async function sendOtp() {
                const email = emailEl.value.trim();
                if (!email) {
                    setHint("Vui lòng nhập email.", false);
                    return;
                }

                // mỗi lần gửi lại -> khóa phần mật khẩu cho chắc
                lockRegister();

                btnSend.disabled = true;
                btnSend.textContent = "Đang gửi...";
                setHint("", true);

                const form = new URLSearchParams();
                form.append("email", email);

                try {
                    const res = await fetch(ctx + "/register/send-otp", {
                        method: "POST",
                        headers: {"Content-Type": "application/x-www-form-urlencoded"},
                        body: form.toString()
                    });

                    const data = await res.json();
                    setHint(data.msg, data.ok);

                    if (data.ok) {
                        // bắt đầu countdown 60s
                        startCooldown(60);

                        // focus vào ô OTP
                        otpEl.value = "";
                        otpEl.focus();
                    } else {
                        btnSend.disabled = false;
                        btnSend.textContent = "Gửi";
                    }

                } catch (e) {
                    btnSend.disabled = false;
                    btnSend.textContent = "Gửi";
                    setHint("Lỗi mạng. Vui lòng thử lại.", false);
                }
            }

            // auto verify khi otp đủ 6 số
            let verifyDebounce = null;

            async function verifyOtpAuto() {
                const email = emailEl.value.trim();
                const code = otpEl.value.trim();

                // chỉ verify khi đủ 6 ký tự số (OTP)
                if (!/^\d{6}$/.test(code))
                    return;

                // debounce để tránh spam request
                if (verifyDebounce)
                    clearTimeout(verifyDebounce);
                verifyDebounce = setTimeout(async () => {
                    const form = new URLSearchParams();
                    form.append("email", email);
                    form.append("code", code);

                    try {
                        const res = await fetch(ctx + "/register/verify-otp", {
                            method: "POST",
                            headers: {"Content-Type": "application/x-www-form-urlencoded"},
                            body: form.toString()
                        });

                        const data = await res.json();
                        setHint(data.msg, data.ok);

                        if (data.ok) {
                            // ✅ set verifyKey để RegisterServlet check
                            document.getElementById("verifyKey").value = data.verifyKey || "";

                            // ✅ khóa email + OTP
                            emailEl.readOnly = true;
                            otpEl.readOnly = true;

                            // ✅ mở khóa password + submit
                            passEl.disabled = false;
                            cpassEl.disabled = false;
                            btnSubmit.disabled = false;

                            // ✅ KHÓA NÚT GỬI để không gửi thêm
                            btnSend.disabled = true;
                            btnSend.textContent = "Đã xác minh";

                            // ✅ nếu đang countdown thì stop luôn
                            if (cooldownTimer) {
                                clearInterval(cooldownTimer);
                                cooldownTimer = null;
                            }
                        } else {
                            lockRegister();
                        }
                    } catch (e) {
                        setHint("Không xác minh được. Vui lòng thử lại.", false);
                    }
                }, 250);
            }

            // ===== events =====
            btnSend.addEventListener("click", sendOtp);

            otpEl.addEventListener("input", () => {
                // chỉ cho nhập số, tự xóa ký tự khác
                otpEl.value = otpEl.value.replace(/[^\d]/g, "");
                verifyOtpAuto();
            });

            // khi đổi email -> reset trạng thái
            emailEl.addEventListener("input", () => {
                // nếu user sửa email thì mở lại readOnly (nếu đang bị khóa)
                document.getElementById("verifyKey").value = "";

                if (emailEl.readOnly)
                    return;

                otpEl.readOnly = false;
                otpEl.value = "";
                lockRegister();
                setHint("", true);

                // reset nút gửi nếu đang không countdown
                if (!cooldownTimer) {
                    btnSend.textContent = "Gửi";
                    btnSend.disabled = false;
                }
            });

            // initial
            lockRegister();
        </script>

    </body>
</html>
