<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh Toán - Thiết Bị Bổ Sung</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="bg-emerald-50">
    <div class="min-h-screen flex items-center justify-center py-8 px-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-6xl w-full">
            <!-- Left: Payment Info -->
            <div class="bg-white rounded-lg shadow p-6">
                <h2 class="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                    <i class="fas fa-info-circle text-blue-600"></i>
                    Chi Tiết Thanh Toán
                </h2>

                <c:if test="${not empty rental}">
                    <div class="space-y-4 mb-8">
                        <div class="flex justify-between pb-2 border-b">
                            <span class="text-gray-700">Mã Rental:</span>
                            <span class="font-mono font-bold text-gray-900">${rental.rentalId}</span>
                        </div>
                        <div class="flex justify-between pb-2 border-b">
                            <span class="text-gray-700">Sân:</span>
                            <span class="font-semibold text-gray-900">${rental.fieldId}</span>
                        </div>
                        <div class="flex justify-between pb-2 border-b">
                            <span class="text-gray-700">Số Dụng Cụ:</span>
                            <span class="text-2xl font-bold text-blue-600">${equipmentCount}</span>
                        </div>
                        <div class="flex justify-between pb-2 border-b">
                            <span class="text-gray-700">Ngày Tạo:</span>
                            <span class="font-semibold text-gray-900">
                                <fmt:formatDate value="${createdTime}" pattern="HH:mm:ss dd/MM/yyyy" />
                            </span>
                        </div>
                    </div>

                    <!-- Amount -->
                    <div class="bg-blue-50 rounded-lg p-6 mb-8">
                        <p class="text-sm text-gray-600 mb-2">Tổng Tiền Thanh Toán</p>
                        <p class="text-4xl font-bold text-blue-600">
                            <fmt:formatNumber value="${rental.totalPrice}" pattern="#,##0" /> đ
                        </p>
                    </div>

                    <!-- Info Box -->
                    <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
                        <h4 class="font-bold text-yellow-900 mb-2 flex items-center gap-2">
                            <i class="fas fa-exclamation-triangle text-yellow-600"></i>
                            Thông Tin Quan Trọng
                        </h4>
                        <ul class="text-sm text-yellow-800 space-y-1">
                            <li><strong>Thời hạn:</strong> 15 phút từ lúc tạo đơn</li>
                            <li><strong>Phương thức:</strong> Chuyển khoản ngân hàng</li>
                            <li><strong>Hết hạn:</strong> Tự động hủy nếu không thanh toán</li>
                        </ul>
                    </div>

                    <!-- Back Button -->
                    <a href="${pageContext.request.contextPath}/staff/locationBookings" 
                       class="inline-flex items-center gap-2 px-4 py-2 text-gray-700 hover:text-gray-900">
                        <i class="fas fa-arrow-left"></i>
                        Quay Lại
                    </a>
                </c:if>
            </div>

            <!-- Right: QR Code -->
            <div class="bg-gradient-to-br from-emerald-500 to-emerald-700 rounded-lg shadow p-8 flex flex-col justify-between min-h-96">
                <div class="text-center mb-8">
                    <p class="text-emerald-100 text-sm font-semibold tracking-widest mb-2">BANK TRANSFER QR</p>
                    <h1 class="text-3xl font-black text-white mb-2">Quét QR Để Thanh Toán</h1>
                    <p class="text-emerald-100 text-sm">Sử dụng ứng dụng ngân hàng để quét mã QR</p>
                </div>

                <!-- QR Code Frame -->
                <div class="bg-white rounded-lg p-6 flex justify-center mb-8">
                    <c:if test="${not empty qrCodeURL}">
                        <img src="${qrCodeURL}" alt="VietQR Code" class="w-64 h-64 object-contain">
                    </c:if>
                </div>

                <!-- Bank Info -->
                <div class="text-center text-white">
                    <p class="text-sm opacity-90 mb-1">Ngân Hàng: <strong>${bankCode}</strong></p>
                    <p class="text-sm opacity-90 mb-1">Tài Khoản: <strong>${accountNumber}</strong></p>
                    <p class="text-sm opacity-90">Chủ TK: <strong>${accountName}</strong></p>
                </div>

                <!-- Timer -->
                <div id="timerWrapper" class="mt-8 p-4 bg-white/20 rounded-lg text-center">
                    <p class="text-white text-sm font-semibold mb-3">Đơn hàng sẽ hết hạn sau:</p>
                    <div class="flex justify-center gap-2">
                        <div class="bg-white/30 px-3 py-2 rounded">
                            <p class="text-2xl font-bold text-white" id="minutesBox">15</p>
                            <p class="text-xs text-white/80">phút</p>
                        </div>
                        <div class="bg-white/30 px-3 py-2 rounded">
                            <p class="text-2xl font-bold text-white" id="secondsBox">00</p>
                            <p class="text-xs text-white/80">giây</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        let rentalId = '<c:out value="${rentalId}"/>';
        let initialSeconds = Number('<c:out value="${remainingSeconds}" default="0"/>');
        if (Number.isNaN(initialSeconds)) initialSeconds = 0;
        let timeRemaining = initialSeconds;
        let paymentChecked = false;

        function updateTimerDisplay() {
            let safeTime = Math.max(0, timeRemaining);
            let minutes = Math.floor(safeTime / 60);
            let seconds = safeTime % 60;

            document.getElementById('minutesBox').textContent = String(minutes).padStart(2, '0');
            document.getElementById('secondsBox').textContent = String(seconds).padStart(2, '0');

            const timerWrapper = document.getElementById('timerWrapper');
            if (safeTime < 180) {
                timerWrapper.classList.add('bg-red-500/30');
            }
        }

        function checkPaymentStatus() {
            if (paymentChecked) return;

            fetch('${pageContext.request.contextPath}/supplementaryEquipmentPaymentQR', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({
                    'action': 'check_payment',
                    'rentalId': rentalId
                })
            })
            .then(response => response.text())
            .then(text => {
                const lines = text.split('\n');
                const data = {};
                lines.forEach(line => {
                    const [key, ...valueParts] = line.split('=');
                    data[key] = valueParts.join('=');
                });

                if ('SUCCESS' === data.paymentStatus) {
                    paymentChecked = true;
                    showPaymentSuccess();
                } else if (data.expired === 'true' || 'FAILED' === data.paymentStatus) {
                    paymentChecked = true;
                    showPaymentExpired();
                }
            })
            .catch(err => console.error('Payment check error:', err));
        }

        function showPaymentSuccess() {
            alert('Thanh toán thành công!');
            window.location.href = '${pageContext.request.contextPath}/staff/locationBookings';
        }

        function showPaymentExpired() {
            alert('Hết thời gian thanh toán. Đơn đã bị hủy tự động.');
            window.location.href = '${pageContext.request.contextPath}/staff/locationBookings';
        }

        document.addEventListener('DOMContentLoaded', () => {
            updateTimerDisplay();

            setInterval(() => {
                timeRemaining = Math.max(0, timeRemaining - 1);
                updateTimerDisplay();

                if (!paymentChecked && timeRemaining === 0) {
                    paymentChecked = true;
                    showPaymentExpired();
                }
            }, 1000);

            setInterval(() => {
                checkPaymentStatus();
            }, 3000);
        });
    </script>
</body>
</html>
