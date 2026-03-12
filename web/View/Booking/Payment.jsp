<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.Booking, Models.Payment" %>
<%
    Booking booking = (Booking) request.getAttribute("booking");
    Payment payment = (Payment) request.getAttribute("payment");
    String qrCodeURL = (String) request.getAttribute("qrCodeURL");
    Long timeRemainingAttr = (Long) request.getAttribute("timeRemaining");
    String bankCode = (String) request.getAttribute("bankCode");
    String accountNumber = (String) request.getAttribute("accountNumber");
    String accountName = (String) request.getAttribute("accountName");

    if (booking == null || payment == null) {
        response.sendRedirect(request.getContextPath() + "/View/Booking/Booking.jsp?error=payment_not_found");
        return;
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

        <jsp:include page="/View/Layout/Header.jsp" />

        <main class="payment-shell px-4 pb-6 pt-4">
            <div class="grid grid-cols-1 lg:grid-cols-[340px_1fr] gap-6 items-start">

                <aside class="left-card p-5 space-y-5">
                    <div class="pb-4 border-b border-slate-200">
                        <h2 class="text-2xl font-black text-slate-900">Thong tin don hang</h2>
                        <p class="text-sm text-slate-500 mt-1">Thanh toan qua chuyen khoan ngan hang</p>
                    </div>

                    <div class="space-y-4 text-slate-800">
                        <div>
                            <p class="text-sm text-slate-500">Nha cung cap</p>
                            <p class="font-bold text-lg leading-7 mt-1">FIFAFIELD</p>
                        </div>

                        <div class="pt-3 border-t border-slate-200">
                            <p class="text-sm text-slate-500">Ma don hang</p>
                            <p class="font-black text-2xl tracking-wide mt-1"><%= booking.getBookingId().toString().substring(0, 8).toUpperCase() %></p>
                        </div>

                        <div class="pt-3 border-t border-slate-200">
                            <p class="text-sm text-slate-500">Mo ta</p>
                            <p class="font-semibold text-lg mt-1">Thanh toan dat san</p>
                        </div>

                        <div class="pt-3 border-t border-slate-200">
                            <p class="text-sm text-slate-500">So tien</p>
                            <p class="font-black text-4xl text-[var(--brand-green)] mt-1"><%= String.format("%,d", booking.getTotalPrice().longValue()) %>d</p>
                        </div>
                    </div>

                    <div class="timer-box p-4 text-center">
                        <p class="text-[19px] font-semibold text-[#d0467d]">Don hang se het han sau:</p>
                        <div class="mt-4 flex justify-center gap-4" id="timerWrapper">
                            <div class="timer-pill py-3 px-3">
                                <p class="text-4xl font-black leading-none" id="minutesBox">1</p>
                                <p class="text-sm mt-2 font-medium">Phut</p>
                            </div>
                            <div class="timer-pill py-3 px-3">
                                <p class="text-4xl font-black leading-none" id="secondsBox">00</p>
                                <p class="text-sm mt-2 font-medium">Giay</p>
                            </div>
                        </div>
                    </div>

                    <div>
                        <form action="${pageContext.request.contextPath}/payment-cancel" method="post" onsubmit="return confirm('Bạn chắc chắn muốn huỷ đặt sân này?');">
                            <input type="hidden" name="bookingId" value="<%= booking.getBookingId() %>" />
                            <button type="submit" class="w-full rounded-xl py-3 border border-[var(--brand-border)] bg-[var(--brand-green-soft)] text-[var(--brand-green-dark)] font-semibold hover:bg-[#def4e8] transition">
                                Quay lai
                            </button>
                        </form>
                    </div>
                </aside>

                <section class="right-card p-5 md:p-6">
                    <div class="qr-stage space-y-5">
                        <div class="text-center">
                            <p class="text-xs uppercase tracking-[0.35em] text-emerald-100 font-semibold">Bank Transfer QR</p>
                            <h1 class="text-3xl md:text-4xl font-black mt-2">Thanh toan qua ngan hang</h1>
                            <p class="text-sm md:text-base text-emerald-100 mt-2">Mo app ngan hang va quet ma QR de hoan tat thanh toan.</p>
                        </div>

                        <div class="qr-frame">
                            <img src="<%= qrCodeURL %>" alt="VietQR" class="qr-image">
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                            <div class="bg-white/20 rounded-xl p-3">
                                <p class="text-emerald-100">Ngan hang</p>
                                <p class="font-bold text-white text-lg mt-1"><%= bankCode %> Bank</p>
                            </div>
                            <div class="bg-white/20 rounded-xl p-3">
                                <p class="text-emerald-100">So tai khoan</p>
                                <p class="font-bold text-white text-lg mt-1"><%= accountNumber %></p>
                            </div>
                            <div class="bg-white/20 rounded-xl p-3 md:col-span-2">
                                <p class="text-emerald-100">Ten tai khoan</p>
                                <p class="font-bold text-white text-lg mt-1"><%= accountName %></p>
                            </div>
                            <div class="bg-white/20 rounded-xl p-3 md:col-span-2">
                                <p class="text-emerald-100">Noi dung chuyen khoan</p>
                                <p class="font-black text-white text-xl mt-1 tracking-wide">FFF_BOOKING_<%= booking.getBookingId().toString().replace("-", "").toUpperCase().substring(0, 8) %></p>
                            </div>
                        </div>
                    </div>

                    <div id="paymentStatusDiv" class="hidden status-card mt-5 p-4 text-center">
                        <div class="flex items-center justify-center gap-3 mb-1">
                            <svg class="w-7 h-7 text-emerald-600 animate-spin" viewBox="0 0 50 50"><circle cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="3"></circle></svg>
                            <p class="text-lg font-bold text-emerald-700">Dang xac nhan thanh toan...</p>
                        </div>
                        <p class="text-sm text-emerald-600">He thong dang kiem tra giao dich</p>
                    </div>

                    <div class="mt-5 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-900">
                        <p class="font-semibold mb-2">Luu y:</p>
                        <ul class="space-y-1 list-disc list-inside">
                            <li>He thong tu dong kiem tra trang thai thanh toan moi 10 giay.</li>
                            <li>Vui long chuyen dung so tien va dung noi dung de duoc xac nhan.</li>
                            <li>Khi het thoi gian, he thong se tu dong huy giu cho.</li>
                        </ul>
                    </div>
                </section>
            </div>
        </main>

        <input type="hidden" id="timeRemainingVal" value="<%= (timeRemainingAttr != null ? timeRemainingAttr.longValue() : 0L) %>" />

        <script>
            const bookingId = '<%= booking.getBookingId() %>';
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
                        'bookingId': bookingId
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
                        if (expired || timeRemaining <= 0) {
                            showPaymentExpired();
                        } else {
                            showPaymentFailed();
                        }
                    } else if (expired || timeRemaining <= 0) {
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
                        <p class="text-lg font-bold text-green-700">Thanh toan thanh cong!</p>
                    </div>
                    <p class="text-sm text-green-600 mb-3">Don dat san da duoc xac nhan.</p>
                    <a href="${pageContext.request.contextPath}/customer/bookings" class="inline-block bg-green-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-green-700 transition">Xem danh sach booking</a>
                `;
                statusDiv.classList.remove('hidden');

                setTimeout(() => {
                    window.location.href = '${pageContext.request.contextPath}/customer/bookings';
                }, 1800);
            }

            function showPaymentFailed() {
                const statusDiv = document.getElementById('paymentStatusDiv');
                statusDiv.innerHTML = `
                    <div class="flex items-center justify-center gap-3 mb-1">
                        <svg class="w-7 h-7 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                        </svg>
                        <p class="text-lg font-bold text-red-700">Thanh toan that bai</p>
                    </div>
                    <p class="text-sm text-red-600 mb-3">Don dat san da bi huy.</p>
                    <a href="${pageContext.request.contextPath}/booking" class="inline-block bg-red-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-red-700 transition">Quay lai dat san</a>
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
                    await fetch('${pageContext.request.contextPath}/payment-cancel', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        body: new URLSearchParams({
                            bookingId: bookingId
                        })
                    });
                } catch (error) {
                    // Ignore cancellation request errors; redirect still occurs below.
                }

                alert('Hết thời gian thanh toán. Đơn đặt sân đã bị hủy tự động.');
                window.location.href = '${pageContext.request.contextPath}/customer/bookings';
            }

            document.addEventListener('DOMContentLoaded', () => {
                lucide.createIcons();
                updateTimerDisplay();

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
