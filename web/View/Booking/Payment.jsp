<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.Booking, Models.Payment" %>
<%
    Booking booking = (Booking) request.getAttribute("booking");
    Payment payment = (Payment) request.getAttribute("payment");
    String qrCodeURL = (String) request.getAttribute("qrCodeURL");
    Long timeRemaining = (Long) request.getAttribute("timeRemaining");
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
    <title>Payment - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; }
        .qr-container {
            animation: slideUp 0.5s ease-out;
        }
        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        .countdown-timer {
            font-variant-numeric: tabular-nums;
            letter-spacing: 0.1em;
        }
        .timer-warning {
            animation: pulse 1s infinite;
        }
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.7; }
        }
    </style>
</head>
<body class="bg-gradient-to-br from-slate-50 to-slate-100">
    
    <jsp:include page="/View/Layout/Header.jsp" />
    
    <main class="max-w-3xl mx-auto px-6 py-12">
        
        <!-- Payment Status Container -->
        <div class="bg-white rounded-3xl shadow-xl overflow-hidden">
            
            <!-- Header -->
            <div class="bg-gradient-to-r from-[#008751] to-emerald-600 text-white p-8">
                <div class="flex items-center gap-3 mb-2">
                    <i data-lucide="credit-card" class="w-6 h-6"></i>
                    <h1 class="text-3xl font-black">Thanh Toán Đặt Sân</h1>
                </div>
                <p class="text-emerald-100">Quét mã QR hoặc chuyển tiền ngay để xác nhận đặt sân</p>
            </div>
            
            <div class="p-8 space-y-8">
                
                <!-- Countdown Timer -->
                <div class="text-center">
                    <div class="inline-block bg-red-50 border-2 border-red-300 rounded-2xl px-8 py-4">
                        <p class="text-sm font-semibold text-red-600 uppercase tracking-wider mb-2">Thời gian thanh toán</p>
                        <div class="countdown-timer text-5xl font-black text-red-600" id="timerDisplay">15:00</div>
                        <p class="text-xs text-red-500 mt-2">Hết thời gian sẽ tự huỷ đơn</p>
                    </div>
                </div>
                
                <!-- Two Column Layout: QR + Info -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                    
                    <!-- QR Code Column -->
                    <div class="flex flex-col items-center">
                        <div class="qr-container bg-white border-4 border-gray-200 rounded-2xl p-6 mb-4">
                            <img src="<%= qrCodeURL %>" alt="VietQR Code" class="w-80 h-80 mx-auto">
                        </div>
                        <p class="text-xs text-gray-500 text-center max-w-xs">
                            Dùng app ngân hàng hoặc ví điện tử để quét mã QR và thanh toán
                        </p>
                    </div>
                    
                    <!-- Banking Info Column -->
                    <div class="space-y-6">
                        
                        <!-- Booking Info -->
                        <div class="bg-slate-50 rounded-xl p-6 border border-slate-200">
                            <h3 class="text-sm font-black text-gray-600 uppercase tracking-widest mb-4">Thông tin đặt sân</h3>
                            <div class="space-y-3">
                                <div class="flex justify-between items-center">
                                    <span class="text-sm text-gray-600">Mã đặt sân:</span>
                                    <span class="font-semibold text-gray-900"><%= booking.getBookingId().toString().substring(0, 8).toUpperCase() %></span>
                                </div>
                                <div class="flex justify-between items-center">
                                    <span class="text-sm text-gray-600">Ngày tạo:</span>
                                    <span class="font-semibold text-gray-900"><%= booking.getBookingTime() %></span>
                                </div>
                                <div class="border-t pt-3">
                                    <div class="flex justify-between items-center">
                                        <span class="text-sm font-semibold text-gray-600">Tổng tiền:</span>
                                        <span class="text-xl font-black text-[#008751]"><%= String.format("%,d", booking.getTotalPrice().longValue()) %> ₫</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Bank Transfer Info -->
                        <div class="bg-emerald-50 rounded-xl p-6 border border-emerald-200">
                            <h3 class="text-sm font-black text-emerald-900 uppercase tracking-widest mb-4">Chuyển khoản thủ công</h3>
                            <div class="space-y-3 text-sm">
                                <div class="flex justify-between">
                                    <span class="text-gray-600">Ngân hàng:</span>
                                    <span class="font-semibold text-gray-900">Vietcombank</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-600">Số tài khoản:</span>
                                    <span class="font-mono font-semibold text-gray-900"><%= accountNumber %></span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-600">Tên tài khoản:</span>
                                    <span class="font-semibold text-gray-900"><%= accountName %></span>
                                </div>
                                <div class="border-t pt-3">
                                    <div class="flex justify-between">
                                        <span class="text-gray-600">Nội dung:</span>
                                        <span class="font-mono font-semibold text-emerald-700">FFF_BOOKING_<%= booking.getBookingId().toString().replace("-", "").toUpperCase().substring(0, 8) %></span>
                                    </div>
                                </div>
                                <div class="bg-white rounded-lg p-3 border border-emerald-100">
                                    <span class="text-xs text-gray-600">Số tiền: </span>
                                    <span class="font-black text-[#008751]"><%= String.format("%,d", booking.getTotalPrice().longValue()) %> ₫</span>
                                </div>
                            </div>
                        </div>
                        
                    </div>
                </div>
                
                <!-- Payment Status Display -->
                <div id="paymentStatusDiv" class="hidden bg-green-50 border-2 border-green-300 rounded-2xl p-6 text-center">
                    <div class="flex items-center justify-center gap-3 mb-2">
                        <svg class="w-8 h-8 text-green-600 animate-spin" viewBox="0 0 50 50"><circle cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="3"></circle></svg>
                        <p class="text-lg font-bold text-green-700">Đang xác nhận thanh toán...</p>
                    </div>
                    <p class="text-sm text-green-600">Chúng tôi sẽ cập nhật trong vài giây</p>
                </div>
                
                <!-- Info Section -->
                <div class="bg-blue-50 rounded-xl p-6 border border-blue-200">
                    <div class="flex gap-3">
                        <i data-lucide="info" class="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5"></i>
                        <div class="text-sm text-blue-900">
                            <p class="font-semibold mb-2">Lưu ý:</p>
                            <ul class="space-y-1 list-disc list-inside">
                                <li>Hệ thống sẽ tự động kiểm tra thanh toán mỗi 10 giây</li>
                                <li>Vui lòng sử dụng đúng nội dung chuyển khoản để xác nhận</li>
                                <li>Nếu hết thời gian 15 phút, đơn sẽ tự động huỷ</li>
                                <li>Liên hệ support nếu có vấn đề thanh toán</li>
                            </ul>
                        </div>
                    </div>
                </div>
                
            </div>
        </div>
        
    </main>
    
    <script>
        const bookingId = '<%= booking.getBookingId() %>';
        const initialTimeRemaining = <%= timeRemaining %>;
        let timeRemaining = initialTimeRemaining;
        const checkInterval = 10000; // Check every 10 seconds
        const timerInterval = 1000; // Update timer every 1 second
        let paymentChecked = false;
        let paymentSucceeded = false; // True only when payment is confirmed SUCCESS
        let timeoutHandled = false; // Flag to prevent multiple timeout handling
        
        // Update timer display
        function updateTimerDisplay() {
            const minutes = Math.floor(timeRemaining / 60);
            const seconds = timeRemaining % 60;
            const display = String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
            document.getElementById('timerDisplay').textContent = display;
            
            // Add warning style if less than 3 minutes
            const timer = document.getElementById('timerDisplay');
            if (timeRemaining < 180 && timeRemaining > 0) {
                timer.closest('div').classList.add('timer-warning');
            }
            
            // Auto check when timeout reached
            if (timeRemaining <= 0 && !timeoutHandled) {
                timeoutHandled = true;
                console.log('Payment timeout reached! Checking status...');
                checkPaymentStatus(true); // Force check on timeout
            }
        }
        
        // Check payment status
        function checkPaymentStatus(forceTimeout = false) {
            if (paymentChecked && !forceTimeout) return;
            
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
            .then(response => response.json())
            .then(data => {
                console.log('Payment status:', data);
                
                if (data.paymentStatus === 'SUCCESS') {
                    paymentChecked = true;
                    paymentSucceeded = true;
                    showPaymentSuccess();
                } else if (data.paymentStatus === 'FAILED') {
                    paymentChecked = true;
                    showPaymentFailed();
                } else if (data.expired === true || data.bookingStatus === 'CANCELLED') {
                    paymentChecked = true;
                    showPaymentExpired();
                }
                
                timeRemaining = data.timeRemaining;
            })
            .catch(error => {
                console.error('Error checking payment:', error);
            });
        }
        
        // Show payment success
        function showPaymentSuccess() {
            const statusDiv = document.getElementById('paymentStatusDiv');
            statusDiv.innerHTML = `
                <div class="flex items-center justify-center gap-3 mb-2">
                    <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    <p class="text-lg font-bold text-green-700">Thanh toán thành công!</p>
                </div>
                <p class="text-sm text-green-600 mb-4">Đơn đặt sân đã được xác nhận</p>
                <a href="${pageContext.request.contextPath}/customer/bookings" 
                   class="inline-block bg-green-600 text-white px-8 py-3 rounded-xl font-semibold hover:bg-green-700 transition">
                    Xem lịch sử đặt sân
                </a>
            `;
            statusDiv.classList.remove('hidden');
            
            // Redirect after 2 seconds
            setTimeout(() => {
                window.location.href = '${pageContext.request.contextPath}/customer/bookings';
            }, 2000);
        }
        
        // Show payment failed
        function showPaymentFailed() {
            const statusDiv = document.getElementById('paymentStatusDiv');
            statusDiv.innerHTML = `
                <div class="flex items-center justify-center gap-3 mb-2">
                    <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                    <p class="text-lg font-bold text-red-700">Thanh toán thất bại</p>
                </div>
                <p class="text-sm text-red-600 mb-4">Đơn đặt sân đã bị huỷ do thanh toán thất bại</p>
                <a href="${pageContext.request.contextPath}/booking" 
                   class="inline-block bg-red-600 text-white px-8 py-3 rounded-xl font-semibold hover:bg-red-700 transition">
                    Quay lại đặt sân
                </a>
            `;
            statusDiv.classList.remove('hidden', 'bg-blue-50', 'border-blue-100');
            statusDiv.classList.add('bg-red-50', 'border-red-100');
        }
        
        // Show payment expired/timeout
        function showPaymentExpired() {
            const statusDiv = document.getElementById('paymentStatusDiv');
            statusDiv.innerHTML = `
                <div class="flex items-center justify-center gap-3 mb-4">
                    <svg class="w-10 h-10 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4v2m0 4v2M4.22 4.22a9 9 0 1112.56 12.56M4.22 4.22L12 12m0 0L4.22 19.78M12 12l7.78-7.78"></path>
                    </svg>
                    <div>
                        <p class="text-lg font-bold text-orange-700">Hết thời gian thanh toán</p>
                        <p class="text-sm text-orange-600">Đơn đặt sân đã bị huỷ tự động</p>
                    </div>
                </div>
                <div class="bg-orange-50 border border-orange-200 rounded-lg p-4 mb-4">
                    <p class="text-sm text-orange-800"><strong>Lý do:</strong> Bạn không hoàn thành thanh toán trong vòng 15 phút quy định</p>
                </div>
                <a href="${pageContext.request.contextPath}/booking" 
                   class="inline-block w-full text-center bg-orange-600 text-white px-8 py-3 rounded-xl font-semibold hover:bg-orange-700 transition">
                    Đặt lại sân
                </a>
            `;
            statusDiv.classList.remove('hidden', 'bg-blue-50', 'border-blue-100');
            statusDiv.classList.add('bg-orange-50', 'border-orange-100');
        }
        
        // Initialize
        document.addEventListener('DOMContentLoaded', () => {
            updateTimerDisplay();
            
            // Update timer every second
            const timerIntervalId = setInterval(() => {
                timeRemaining--;
                if (timeRemaining < 0) timeRemaining = 0;
                updateTimerDisplay();
            }, timerInterval);
            
            // Check payment status every 10 seconds
            const checkIntervalId = setInterval(() => {
                checkPaymentStatus();
                if (paymentChecked) {
                    clearInterval(checkIntervalId);
                    clearInterval(timerIntervalId);
                }
            }, checkInterval);
        });

        // Cancel booking immediately if user exits the page before payment succeeds.
        // sendBeacon works even during page unload unlike fetch/XHR.
        window.addEventListener('pagehide', function() {
            if (paymentSucceeded) return; // Payment done — don't cancel
            const body = new URLSearchParams({
                action: 'cancel_on_exit',
                bookingId: bookingId
            });
            navigator.sendBeacon('${pageContext.request.contextPath}/payment', body);
        });
    </script>
    
</body>
</html>
