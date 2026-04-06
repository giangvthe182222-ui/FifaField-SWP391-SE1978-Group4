<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.Booking, Models.Payment, Models.BookingViewModel, java.math.BigDecimal" %>
<%
    Booking booking = (Booking) request.getAttribute("booking");
    BookingViewModel bookingVM = (BookingViewModel) request.getAttribute("bookingVM");
    Payment payment = (Payment) request.getAttribute("payment");
    String qrCodeURL = (String) request.getAttribute("qrCodeURL");
    Long timeRemainingAttr = (Long) request.getAttribute("timeRemaining");
    String bankCode = (String) request.getAttribute("bankCode");
    String accountNumber = (String) request.getAttribute("accountNumber");
    String accountName = (String) request.getAttribute("accountName");
    String checkoutUrl = (String) request.getAttribute("checkoutUrl");
    String bookingDetailPath = (String) request.getAttribute("bookingDetailPath");
    String bookingHistoryPath = (String) request.getAttribute("bookingHistoryPath");
    String paymentSource = (String) request.getAttribute("paymentSource");
    String supplementaryRentalId = (String) request.getAttribute("supplementaryRentalId");
    String paymentDescription = (String) request.getAttribute("paymentDescription");
    String paymentMethodLabel = (String) request.getAttribute("paymentMethodLabel");
    String remainingPaymentMethodLabel = (String) request.getAttribute("remainingPaymentMethodLabel");
    BigDecimal bookingTotalAmount = (BigDecimal) request.getAttribute("bookingTotalAmount");
    BigDecimal remainingAmount = (BigDecimal) request.getAttribute("remainingAmount");
    Boolean isDepositPayment = (Boolean) request.getAttribute("isDepositPayment");
    Boolean isWeeklyGroupPayment = (Boolean) request.getAttribute("isWeeklyGroupPayment");
    Integer weeklySessionCount = (Integer) request.getAttribute("weeklySessionCount");
    String weeklyGroupId = (String) request.getAttribute("weeklyGroupId");
        Models.User viewUser = (Models.User) session.getAttribute("user");
        boolean staffUser = viewUser != null
            && viewUser.getRole() != null
            && viewUser.getRole().getRoleName() != null
            && "STAFF".equalsIgnoreCase(viewUser.getRole().getRoleName());

        String paymentLocationId = (bookingVM != null && bookingVM.getLocationId() != null)
            ? bookingVM.getLocationId().toString()
            : "";
        String paymentFieldId = (booking != null && booking.getFieldId() != null)
            ? booking.getFieldId().toString()
            : "";
        String paymentPhone = (booking != null && booking.getPhoneNumber() != null)
            ? booking.getPhoneNumber()
            : "";
        String staffPaymentBackPath = request.getContextPath() + "/booking";
        if (staffUser) {
            StringBuilder sb = new StringBuilder(staffPaymentBackPath);
            boolean hasQuery = false;

            if (paymentLocationId != null && !paymentLocationId.trim().isEmpty()) {
                sb.append(hasQuery ? '&' : '?').append("locationId=").append(paymentLocationId.trim());
                hasQuery = true;
            }
            if (paymentFieldId != null && !paymentFieldId.trim().isEmpty()) {
                sb.append(hasQuery ? '&' : '?').append("fieldId=").append(paymentFieldId.trim());
                hasQuery = true;
            }
            if (paymentPhone != null && !paymentPhone.trim().isEmpty()) {
                sb.append(hasQuery ? '&' : '?').append("bookingPhone=")
                        .append(java.net.URLEncoder.encode(paymentPhone.trim(), java.nio.charset.StandardCharsets.UTF_8.name()));
            }

            staffPaymentBackPath = sb.toString();
        }

    if (paymentSource == null || paymentSource.isBlank()) {
        paymentSource = "booking";
    }
    if (paymentDescription == null || paymentDescription.isBlank()) {
        paymentDescription = Boolean.TRUE.equals(isWeeklyGroupPayment) ? "Thanh toán lịch tuần" : "Thanh toán đặt sân";
    }
    if (bookingDetailPath == null || bookingDetailPath.isBlank()) {
        bookingDetailPath = "/customer/bookingDetail";
    }
    if (bookingHistoryPath == null || bookingHistoryPath.isBlank()) {
        bookingHistoryPath = "/customer/bookings";
    }

    if (booking == null || payment == null) {
        response.sendRedirect(request.getContextPath() + "/View/Booking/Booking.jsp?error=payment_not_found");
        return;
    }

    if (paymentMethodLabel == null || paymentMethodLabel.isBlank()) {
        paymentMethodLabel = payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "--";
    }
    if (bookingTotalAmount == null) {
        bookingTotalAmount = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
    }
    if (remainingAmount == null) {
        remainingAmount = BigDecimal.ZERO;
    }
    if (isDepositPayment == null) {
        isDepositPayment = Boolean.FALSE;
    }
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Thanh Toan - FIFAFIELD</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <script src="https://unpkg.com/lucide@latest"></script>
        <style>
            :root {
                --brand-green: #008751;
                --brand-green-dark: #006b40;
                --brand-green-soft: #effaf4;
                --brand-border: #bfe9d5;
                --panel-shadow: 0 18px 42px rgba(4, 65, 38, 0.14);
            }

            body {
                background: radial-gradient(circle at 10% 10%, #f5fff9 0%, #ecf8f3 36%, #e4f4ec 100%);
                color: #183026;
            }

            .payment-shell {
                max-width: 1180px;
                margin: 0 auto;
                transform: scale(0.88);
                transform-origin: top center;
            }

            .left-card,
            .right-card {
                border: 1px solid var(--brand-border);
                border-radius: 20px;
                background: #ffffff;
                box-shadow: var(--panel-shadow);
            }

            .left-card {
                overflow: hidden;
            }

            .timer-box {
                border-radius: 16px;
                border: 1px solid #f9c9d9;
                background: linear-gradient(180deg, #ffeef4 0%, #ffdfe9 100%);
            }

            .timer-pill {
                min-width: 74px;
                border-radius: 12px;
                border: 1px solid #f4b7ca;
                background: #fff6f9;
                color: #cc3e76;
            }

            .qr-stage {
                border-radius: 22px;
                background: linear-gradient(160deg, var(--brand-green) 0%, #00a461 100%);
                padding: 30px;
                color: #ffffff;
            }

            .qr-frame {
                border-radius: 10px;
                background: #ffffff;
                border: 1px solid #cdeedc;
                width: fit-content;
                margin: 0 auto;
                padding: 12px;
            }

            .qr-image {
                width: 320px;
                height: 320px;
                margin: 0 auto;
                border-radius: 0;
                object-fit: contain;
            }

            .status-card {
                border: 1px solid #d4f4e4;
                border-radius: 14px;
                background: #f6fffa;
            }

            .timer-warning {
                animation: flashPulse 1s infinite;
            }

            @keyframes flashPulse {
                0%,
                100% {
                    opacity: 1;
                }
                50% {
                    opacity: 0.74;
                }
            }

            @media (max-width: 1280px) {
                .payment-shell {
                    transform: scale(0.93);
                }
            }

            @media (max-width: 1024px) {
                .payment-shell {
                    transform: none;
                }

                .qr-stage {
                    padding: 22px;
                }
            }
        </style>
    </head>
    <body>


        <main class="payment-shell px-4 pb-6 pt-4">
            <div class="grid grid-cols-1 lg:grid-cols-[340px_1fr] gap-6 items-start">

                <aside class="left-card p-5 space-y-5">
                    <div class="pb-4 border-b border-slate-200">
                        <h2 class="text-2xl font-black text-slate-900">Thông tin đơn hàng</h2>
                        <p class="text-sm text-slate-500 mt-1">Thanh toán qua chuyển khoản ngân hàng</p>
                    </div>

                    <div class="space-y-4 text-slate-800">
                        <div>
                            <p class="text-sm text-slate-500">Nhà cung cấp</p>
                            <p class="font-bold text-lg leading-7 mt-1">FIFAFIELD</p>
                        </div>

                        <div class="pt-3 border-t border-slate-200">
                            <p class="text-sm text-slate-500">Mã đơn hàng</p>
                            <p class="font-black text-2xl tracking-wide mt-1"><%= booking.getBookingId().toString().substring(0, 8).toUpperCase() %></p>
                        </div>

                        <div class="pt-3 border-t border-slate-200">
                            <p class="text-sm text-slate-500">Mô tả</p>
                            <p class="font-semibold text-lg mt-1"><%= paymentDescription %></p>
                        </div>

                        <div class="pt-3 border-t border-slate-200">
                            <p class="text-sm text-slate-500">Số tiền</p>
                            <p class="font-black text-4xl text-[var(--brand-green)] mt-1"><%= String.format("%,d", payment.getAmount().longValue()) %>d</p>
                        </div>

                        <div class="pt-3 border-t border-slate-200">
                            <p class="text-sm text-slate-500">Thông tin đặt sân</p>
                            <p class="font-semibold text-base mt-1"><%= bookingVM != null ? bookingVM.getFieldName() : "--" %></p>
                            <p class="text-sm text-slate-600 mt-1">
                                <%= (bookingVM != null && bookingVM.getBookingDate() != null) ? bookingVM.getBookingDate().toString() : "--" %>
                                |
                                <%= (bookingVM != null && bookingVM.getStartTime() != null && bookingVM.getEndTime() != null)
                                        ? bookingVM.getStartTime().toString() + " - " + bookingVM.getEndTime().toString()
                                        : "--" %>
                            </p>
                            <p class="text-sm text-slate-600 mt-2">Trạng thái booking: <span class="font-semibold"><%= booking.getStatus() != null ? booking.getStatus() : "--" %></span></p>
                            <p class="text-sm text-slate-600 mt-1">Mã lịch: <span class="font-semibold"><%= booking.getScheduleId() != null ? booking.getScheduleId() : "--" %></span></p>
                            <% if (Boolean.TRUE.equals(isWeeklyGroupPayment)) { %>
                            <p class="text-sm text-slate-600 mt-1">Số ca trong tuần: <span class="font-semibold"><%= weeklySessionCount != null ? weeklySessionCount : 0 %></span></p>
                            <% } %>
                            <p class="text-sm text-slate-600 mt-1">Mã giao dịch: <span class="font-semibold"><%= payment.getTransactionCode() != null ? payment.getTransactionCode() : "--" %></span></p>
                            <p class="text-sm text-slate-600 mt-1">Phương thức thanh toán: <span class="font-semibold"><%= paymentMethodLabel %></span></p>
                            <% if (Boolean.TRUE.equals(isDepositPayment)) { %>
                            <p class="text-sm text-slate-600 mt-1">Giá trị booking: <span class="font-semibold"><%= String.format("%,d", bookingTotalAmount.longValue()) %>d</span></p>
                            <p class="text-sm text-slate-600 mt-1">Đã thanh toán online: <span class="font-semibold"><%= String.format("%,d", payment.getAmount().longValue()) %>d</span></p>
                            <p class="text-sm text-slate-600 mt-1">Còn lại tại sân: <span class="font-semibold"><%= String.format("%,d", remainingAmount.longValue()) %>d</span></p>
                            <p class="text-sm text-slate-600 mt-1">Phần còn lại: <span class="font-semibold"><%= remainingPaymentMethodLabel != null ? remainingPaymentMethodLabel : "--" %></span></p>
                            <% } %>
                        </div>
                    </div>

                    <div class="timer-box p-4 text-center">
                        <p class="text-[19px] font-semibold text-[#d0467d]">
                            <%= ("supplementary".equalsIgnoreCase(paymentSource) || "remaining".equalsIgnoreCase(paymentSource))
                                    ? "Thanh toán không giới hạn thời gian:"
                                    : "Đơn hàng sẽ hết hạn sau:" %>
                        </p>
                        <div class="mt-4 flex justify-center gap-4" id="timerWrapper">
                            <div class="timer-pill py-3 px-3">
                                <p class="text-4xl font-black leading-none" id="minutesBox">1</p>
                                <p class="text-sm mt-2 font-medium">Phút</p>
                            </div>
                            <div class="timer-pill py-3 px-3">
                                <p class="text-4xl font-black leading-none" id="secondsBox">00</p>
                                <p class="text-sm mt-2 font-medium">Giây</p>
                            </div>
                        </div>
                    </div>

                    <div>
                        <% if ("supplementary".equalsIgnoreCase(paymentSource) || "remaining".equalsIgnoreCase(paymentSource)) { %>
                            <% String backPath = staffUser ? staffPaymentBackPath : (request.getContextPath() + bookingHistoryPath); %>
                            <a href="<%= backPath %>" class="block w-full text-center rounded-xl py-3 border border-[var(--brand-border)] bg-[var(--brand-green-soft)] text-[var(--brand-green-dark)] font-semibold hover:bg-[#def4e8] transition">
                                Quay lại
                            </a>
                        <% } else { %>
                            <form action="${pageContext.request.contextPath}/payment-cancel" method="post" onsubmit="return confirm('Bạn chắc chắn muốn huỷ đặt sân này?');">
                                <input type="hidden" name="bookingId" value="<%= booking.getBookingId() %>" />
                                <% if (Boolean.TRUE.equals(isWeeklyGroupPayment) && weeklyGroupId != null) { %>
                                <input type="hidden" name="weeklyGroupId" value="<%= weeklyGroupId %>" />
                                <% } %>
                                <% if (staffUser) { %>
                                <input type="hidden" name="redirectToBooking" value="1" />
                                <input type="hidden" name="locationId" value="<%= paymentLocationId %>" />
                                <input type="hidden" name="fieldId" value="<%= paymentFieldId %>" />
                                <input type="hidden" name="bookingPhone" value="<%= paymentPhone %>" />
                                <% } %>
                                <button type="submit" class="w-full rounded-xl py-3 border border-[var(--brand-border)] bg-[var(--brand-green-soft)] text-[var(--brand-green-dark)] font-semibold hover:bg-[#def4e8] transition">
                                    Quay lại
                                </button>
                            </form>
                        <% } %>
                    </div>
                </aside>

                <section class="right-card p-5 md:p-6">
                    <div class="qr-stage space-y-5">
                        <div class="text-center">
                            <p class="text-xs uppercase tracking-[0.35em] text-emerald-100 font-semibold">Bank Transfer QR</p>
                            <h1 class="text-3xl md:text-4xl font-black mt-2">Thanh toán qua ngân hàng</h1>
                            <p class="text-sm md:text-base text-emerald-100 mt-2">Quét QR hoặc mở trang payOS Checkout để hoàn tất thanh toán.</p>
                        </div>

                        <div class="qr-frame">
                            <img src="<%= qrCodeURL %>" alt="VietQR" class="qr-image">
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                            <div class="bg-white/20 rounded-xl p-3">
                                <p class="text-emerald-100">Ngân hàng</p>
                                <p class="font-bold text-white text-lg mt-1"><%= bankCode %> Bank</p>
                            </div>
                            <div class="bg-white/20 rounded-xl p-3">
                                <p class="text-emerald-100">Số tài khoản</p>
                                <p class="font-bold text-white text-lg mt-1"><%= accountNumber %></p>
                            </div>
                            <div class="bg-white/20 rounded-xl p-3 md:col-span-2">
                                <p class="text-emerald-100">Tên tài khoản</p>
                                <p class="font-bold text-white text-lg mt-1"><%= accountName %></p>
                            </div>
                            <div class="bg-white/20 rounded-xl p-3 md:col-span-2">
                                <p class="text-emerald-100">Nội dung chuyển khoản</p>
                                <p class="font-black text-white text-xl mt-1 tracking-wide"><%= payment.getTransactionCode() %></p>
                            </div>
                        </div>

                        <% if (checkoutUrl != null && !checkoutUrl.trim().isEmpty()) { %>
                            <div class="pt-1 text-center">
                                <a href="<%= checkoutUrl %>" target="_blank" rel="noopener"
                                   class="inline-flex items-center justify-center rounded-xl bg-white text-[var(--brand-green)] font-bold px-5 py-3 hover:bg-emerald-50 transition">
                                    Mở payOS Checkout
                                </a>
                            </div>
                        <% } %>
                    </div>

                    <div id="paymentStatusDiv" class="hidden status-card mt-5 p-4 text-center">
                        <div class="flex items-center justify-center gap-3 mb-1">
                            <svg class="w-7 h-7 text-emerald-600 animate-spin" viewBox="0 0 50 50"><circle cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="3"></circle></svg>
                            <p class="text-lg font-bold text-emerald-700">Đang xác nhận thanh toán...</p>
                        </div>
                        <p class="text-sm text-emerald-600">Hệ thống đang kiểm tra giao dịch</p>
                    </div>

                    <div class="mt-5 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-900">
                        <p class="font-semibold mb-2">Lưu ý:</p>
                        <ul class="space-y-1 list-disc list-inside">
                            <li>Hệ thống tự động kiểm tra trạng thái thanh toán mỗi 10 giây.</li>
                            <li>Vui lòng chuyển đúng số tiền và đúng nội dung để được xác nhận.</li>
                            <li><%= ("supplementary".equalsIgnoreCase(paymentSource) || "remaining".equalsIgnoreCase(paymentSource)) ? "Bạn có thể quay lại thanh toán bất cứ lúc nào." : "Khi hết thời gian, hệ thống sẽ tự động hủy giữ chỗ." %></li>
                        </ul>
                    </div>
                </section>
            </div>
        </main>

        <input type="hidden" id="timeRemainingVal" value="<%= (timeRemainingAttr != null ? timeRemainingAttr.longValue() : 0L) %>" />

        <script>
            const bookingId = '<%= booking.getBookingId() %>';
            const paymentSource = '<%= paymentSource %>';
            const supplementaryRentalId = '<%= supplementaryRentalId != null ? supplementaryRentalId : "" %>';
            const successMessage = paymentSource === 'supplementary'
                ? 'Thanh toán equipment bổ sung đã được xác nhận.'
                : 'Đơn đặt sân đã được xác nhận.';
            const successLink = paymentSource === 'supplementary'
                ? '${pageContext.request.contextPath}<%= bookingHistoryPath %>'
                : (paymentSource === 'remaining'
                    ? '${pageContext.request.contextPath}<%= bookingHistoryPath %>'
                    : '${pageContext.request.contextPath}<%= bookingDetailPath %>?id=' + bookingId);
            const successLinkLabel = paymentSource === 'supplementary'
                ? 'Quay về danh sách booking'
                : (paymentSource === 'remaining' ? 'Quay về danh sách booking' : 'Xem chi tiết booking');
            const failedMessage = paymentSource === 'supplementary'
                ? 'Đơn equipment bổ sung đã bị hủy.'
                : (paymentSource === 'remaining' ? 'Thanh toán phần còn lại chưa thành công.' : 'Đơn đặt sân đã bị hủy.');
            const failedLink = paymentSource === 'supplementary'
                ? '${pageContext.request.contextPath}<%= bookingHistoryPath %>'
                : (paymentSource === 'remaining'
                    ? '${pageContext.request.contextPath}<%= bookingHistoryPath %>'
                    : '${pageContext.request.contextPath}/booking');
            const failedLinkLabel = paymentSource === 'supplementary'
                ? 'Quay về danh sách booking'
                : (paymentSource === 'remaining' ? 'Quay về danh sách booking' : 'Quay lại đặt sân');
            const expiredMessage = paymentSource === 'supplementary'
                ? 'Hết thời gian thanh toán. Đơn equipment bổ sung đã bị hủy tự động.'
                : (paymentSource === 'remaining'
                    ? 'Thanh toán phần còn lại không giới hạn thời gian.'
                    : 'Hết thời gian thanh toán. Đơn đặt sân đã bị hủy tự động.');
            const hasPaymentDeadline = paymentSource !== 'supplementary' && paymentSource !== 'remaining';
            const initialTimeRemaining = parseInt(document.getElementById('timeRemainingVal').value || '0', 10);
            let timeRemaining = initialTimeRemaining;
            const checkIntervalMs = 10000;
            const timerInterval = 1000;
            let paymentChecked = false;
            let paymentExpiredHandled = false;

            function parseKeyValueResponse(rawText) {
                const data = {};
                if (!rawText) {
                    return data;
                }

                rawText.split(/\r?\n/).forEach((line) => {
                    const idx = line.indexOf('=');
                    if (idx > 0) {
                        const key = line.substring(0, idx).trim();
                        const value = line.substring(idx + 1).trim();
                        data[key] = value;
                    }
                });
                return data;
            }

            function updateTimerDisplay() {
                if (!hasPaymentDeadline) {
                    document.getElementById('minutesBox').textContent = '--';
                    document.getElementById('secondsBox').textContent = '--';
                    document.getElementById('timerWrapper').classList.remove('timer-warning');
                    return;
                }

                const safeTime = Math.max(0, timeRemaining);
                const minutes = Math.floor(safeTime / 60);
                const seconds = safeTime % 60;

                document.getElementById('minutesBox').textContent = String(minutes).padStart(2, '0');
                document.getElementById('secondsBox').textContent = String(seconds).padStart(2, '0');

                const timerWrapper = document.getElementById('timerWrapper');
                if (safeTime < 180) {
                    timerWrapper.classList.add('timer-warning');
                } else {
                    timerWrapper.classList.remove('timer-warning');
                }
            }

            function checkPaymentStatus() {
                if (paymentChecked) {
                    return;
                }

                fetch('${pageContext.request.contextPath}/payment', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: new URLSearchParams({
                        'action': 'check_payment',
                        'bookingId': bookingId,
                        'source': paymentSource,
                        'rentalId': supplementaryRentalId
                    })
                })
                .then(response => response.text())
                .then(rawText => {
                    const data = parseKeyValueResponse(rawText);
                    const paymentStatus = (data.paymentStatus || '').toUpperCase();
                    const expired = (data.expired || '').toLowerCase() === 'true';
                    const serverTimeRemaining = parseInt(data.timeRemaining || '0', 10);

                    if (!Number.isNaN(serverTimeRemaining)) {
                        timeRemaining = Math.max(0, serverTimeRemaining);
                    }

                    if (paymentStatus === 'SUCCESS') {
                        paymentChecked = true;
                        showPaymentSuccess();
                    } else if (paymentStatus === 'FAILED') {
                        paymentChecked = true;
                        if (hasPaymentDeadline && (expired || timeRemaining <= 0)) {
                            showPaymentExpired();
                        } else {
                            showPaymentFailed();
                        }
                    } else if (hasPaymentDeadline && (expired || timeRemaining <= 0)) {
                        paymentChecked = true;
                        showPaymentExpired();
                    }
                })
                .catch(() => {
                    // Ignore polling errors and continue polling on next cycle.
                });
            }

            function showPaymentSuccess() {
                const statusDiv = document.getElementById('paymentStatusDiv');
                statusDiv.innerHTML = `
                    <div class="flex items-center justify-center gap-3 mb-1">
                        <svg class="w-7 h-7 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                        </svg>
                        <p class="text-lg font-bold text-green-700">Thanh toán thành công!</p>
                    </div>
                    <p class="text-sm text-green-600 mb-3">` + successMessage + `</p>
                    <a href="` + successLink + `" class="inline-block bg-green-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-green-700 transition">` + successLinkLabel + `</a>
                `;
                statusDiv.classList.remove('hidden');

                setTimeout(() => {
                    window.location.href = '${pageContext.request.contextPath}<%= bookingHistoryPath %>';
                }, 1800);
            }

            function showPaymentFailed() {
                const statusDiv = document.getElementById('paymentStatusDiv');
                statusDiv.innerHTML = `
                    <div class="flex items-center justify-center gap-3 mb-1">
                        <svg class="w-7 h-7 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                        </svg>
                        <p class="text-lg font-bold text-red-700">Thanh toán thất bại</p>
                    </div>
                    <p class="text-sm text-red-600 mb-3">` + failedMessage + `</p>
                    <a href="` + failedLink + `" class="inline-block bg-red-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-red-700 transition">` + failedLinkLabel + `</a>
                `;
                statusDiv.classList.remove('hidden');
                statusDiv.classList.remove('status-card');
                statusDiv.classList.add('bg-red-50', 'border', 'border-red-200', 'rounded-xl');
            }

            async function showPaymentExpired() {
                if (paymentExpiredHandled) {
                    return;
                }
                paymentExpiredHandled = true;

                try {
                    if (paymentSource === 'supplementary' || paymentSource === 'remaining') {
                        await fetch('${pageContext.request.contextPath}/payment', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            body: new URLSearchParams({
                                action: 'check_payment',
                                bookingId: bookingId,
                                source: paymentSource,
                                rentalId: supplementaryRentalId
                            })
                        });
                    } else {
                        await fetch('${pageContext.request.contextPath}/payment-cancel', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            body: new URLSearchParams({
                                bookingId: bookingId
                            })
                        });
                    }
                } catch (error) {
                    // Ignore cancellation request errors; redirect still occurs below.
                }

                alert(expiredMessage);
                window.location.href = '${pageContext.request.contextPath}<%= bookingHistoryPath %>';
            }

            document.addEventListener('DOMContentLoaded', () => {
                lucide.createIcons();
                updateTimerDisplay();

                if (!hasPaymentDeadline) {
                    const checkIntervalId = setInterval(() => {
                        checkPaymentStatus();
                        if (paymentChecked) {
                            clearInterval(checkIntervalId);
                        }
                    }, checkIntervalMs);
                    return;
                }

                setInterval(() => {
                    timeRemaining = Math.max(0, timeRemaining - 1);
                    updateTimerDisplay();

                    if (!paymentChecked && timeRemaining === 0) {
                        paymentChecked = true;
                        showPaymentExpired();
                    }
                }, timerInterval);

                const checkIntervalId = setInterval(() => {
                    checkPaymentStatus();
                    if (paymentChecked) {
                        clearInterval(checkIntervalId);
                    }
                }, checkIntervalMs);
            });
        </script>

    </body>
</html>
