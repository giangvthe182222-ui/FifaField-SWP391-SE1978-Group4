<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt sân thành công</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900 flex items-center justify-center p-6">

<main class="max-w-xl w-full bg-white rounded-3xl border border-gray-100 shadow-xl p-10 text-center space-y-6">
    <div class="w-16 h-16 mx-auto rounded-2xl bg-emerald-50 text-[#008751] flex items-center justify-center">
        <i data-lucide="check-circle-2" class="w-9 h-9"></i>
    </div>

    <div class="space-y-2">
        <h1 class="text-3xl font-black uppercase tracking-tight">Đặt sân thành công</h1>
        <p class="text-sm font-semibold text-gray-500">Hệ thống đã ghi nhận đơn đặt sân của bạn.</p>
    </div>

    <c:if test="${not empty booking}">
        <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5 text-left space-y-2">
            <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Mã booking</p>
            <p class="text-sm font-black text-gray-800">${booking.bookingId}</p>
            <p class="text-[10px] font-black uppercase tracking-widest text-gray-400 pt-2">Sân</p>
            <p class="text-sm font-black text-gray-800">${booking.fieldName}</p>
            <p class="text-[10px] font-black uppercase tracking-widest text-gray-400 pt-2">Tổng tiền</p>
            <p class="text-sm font-black text-[#008751]"><fmt:formatNumber value="${booking.totalPrice}" pattern="#,##0"/> đ</p>
        </div>
    </c:if>

    <div class="flex flex-col sm:flex-row gap-3 justify-center">
        <a href="${pageContext.request.contextPath}${bookingDetailPath}?id=${booking.bookingId}" class="px-5 py-3 rounded-2xl bg-gray-900 text-white text-xs font-black uppercase tracking-widest hover:bg-[#008751] transition-all">
            Xem chi tiết
        </a>
        <a href="${pageContext.request.contextPath}/payment?bookingId=${booking.bookingId}" class="px-5 py-3 rounded-2xl border border-[#008751] text-[#008751] text-xs font-black uppercase tracking-widest hover:bg-emerald-50 transition-all">
            Thanh toán ngay
        </a>
    </div>

    <p class="text-xs font-bold text-gray-400">Tự động quay về danh sách sau <span id="countdown">5</span> giây...</p>
</main>

<script>
    lucide.createIcons();
    var seconds = 5;
    var target = '${pageContext.request.contextPath}${bookingListPath}';
    var counterEl = document.getElementById('countdown');
    var timer = setInterval(function() {
        seconds -= 1;
        if (counterEl) counterEl.textContent = String(seconds);
        if (seconds <= 0) {
            clearInterval(timer);
            window.location.href = target;
        }
    }, 1000);
</script>
</body>
</html>
