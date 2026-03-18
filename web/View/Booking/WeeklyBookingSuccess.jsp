<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"   %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"       prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"  %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Sân Theo Tuần Thành Công - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2rem; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<c:choose>
    <c:when test="${sessionScope.user.role.roleName eq 'STAFF' or sessionScope.user.role.roleName eq 'staff'}">
        <jsp:include page="/View/Layout/HeaderStaff.jsp"/>
    </c:when>
    <c:when test="${sessionScope.user.role.roleName eq 'MANAGER' or sessionScope.user.role.roleName eq 'manager'}">
        <jsp:include page="/View/Layout/HeaderManager.jsp"/>
    </c:when>
    <c:otherwise>
        <jsp:include page="/View/Layout/HeaderCustomer.jsp"/>
    </c:otherwise>
</c:choose>

<main class="flex-grow max-w-4xl mx-auto px-6 py-16 w-full space-y-10">

    <%-- Success hero --%>
    <div class="text-center space-y-4">
        <div class="w-24 h-24 bg-emerald-100 rounded-full flex items-center justify-center mx-auto shadow-xl shadow-emerald-200/50">
            <i data-lucide="calendar-check" class="w-12 h-12 text-[#008751]"></i>
        </div>
        <h1 class="text-4xl font-black text-gray-900 uppercase tracking-tight">
            Đặt Sân <span class="text-[#008751]">Thành Công!</span>
        </h1>
        <p class="text-gray-500 font-semibold max-w-lg mx-auto">
            Các ca trong tuần đã được giữ chỗ. Vui lòng thanh toán <span class="font-black text-[#008751]">1 lần cho toàn bộ lịch tuần</span> trong vòng
            <span class="font-black text-amber-600">2 giờ</span> để xác nhận.
            Ca chưa thanh toán sẽ tự động bị hủy.
        </p>
    </div>

    <%-- Booking list --%>
    <div class="bg-white elite-card shadow-xl border border-gray-100 overflow-hidden">

        <div class="px-8 py-6 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-7 h-1 bg-[#008751] rounded-full"></div>
                <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.25em]">
                    Danh sách ca đã đặt (${fn:length(bookings)} ca)
                </h2>
            </div>
            <span class="text-xs font-bold text-amber-600 bg-amber-50 px-3 py-1.5 rounded-full border border-amber-100">
                <i data-lucide="clock" class="w-3 h-3 inline-block mr-1"></i>Chờ thanh toán
            </span>
        </div>

        <c:choose>
        <c:when test="${empty bookings}">
            <div class="p-12 text-center text-gray-400">
                <i data-lucide="inbox" class="w-8 h-8 mx-auto mb-3 opacity-40"></i>
                <p class="font-bold">Không tìm thấy thông tin đặt sân.</p>
            </div>
        </c:when>
        <c:otherwise>

        <%-- Calculate total --%>
        <c:set var="grandTotal" value="0"/>
        <c:forEach var="b" items="${bookings}">
            <c:set var="grandTotal" value="${grandTotal + b.totalPrice}"/>
        </c:forEach>

        <table class="w-full text-sm">
            <thead class="bg-gray-50">
                <tr>
                    <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">#</th>
                    <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Sân</th>
                    <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Ngày</th>
                    <th class="text-left px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Giờ</th>
                    <th class="text-right px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Giá</th>
                    <th class="text-center px-6 py-3 text-[10px] font-black text-gray-400 uppercase tracking-widest">Trạng thái</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach var="b" items="${bookings}" varStatus="vs">
                <tr class="border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                    <td class="px-6 py-4 font-black text-gray-400">${vs.count}</td>
                    <td class="px-6 py-4 font-bold text-gray-800">${b.fieldName}</td>
                    <td class="px-6 py-4">
                        <%-- bookingDate is java.time.LocalDate – toString() = yyyy-MM-dd --%>
                        <span class="font-semibold text-gray-700">${b.bookingDate}</span>
                    </td>
                    <td class="px-6 py-4 font-semibold text-gray-700">
                        ${b.startTime} – ${b.endTime}
                    </td>
                    <td class="px-6 py-4 text-right font-black text-[#008751]">
                        <fmt:formatNumber value="${b.totalPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                    </td>
                    <td class="px-6 py-4 text-center text-xs font-black uppercase tracking-wide text-amber-600">${b.status}</td>
                </tr>
            </c:forEach>
            </tbody>
            <tfoot class="bg-emerald-50/50">
                <tr>
                    <td colspan="4" class="px-6 py-4 font-black text-gray-700 uppercase tracking-wide text-sm">Tổng cộng</td>
                    <td class="px-6 py-4 text-right font-black text-2xl text-[#008751]">
                        <fmt:formatNumber value="${grandTotal}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                    </td>
                    <td></td>
                </tr>
            </tfoot>
        </table>
        </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${not empty weeklyGroupId and not empty bookings}">
        <div class="text-center">
            <a href="${pageContext.request.contextPath}/payment?weeklyGroupId=${weeklyGroupId}"
               class="inline-flex items-center justify-center gap-2 px-10 py-4 bg-[#008751] hover:bg-[#006d41] text-white font-black uppercase tracking-widest text-sm rounded-2xl transition-all shadow-lg shadow-[#008751]/20">
                <i data-lucide="credit-card" class="w-4 h-4"></i>
                Thanh toán toàn bộ lịch tuần
            </a>
        </div>
    </c:if>

    <%-- Action buttons --%>
    <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <a href="${historyPath}"
           class="flex items-center justify-center gap-2 px-8 py-4 bg-white border-2 border-gray-200 hover:border-[#008751] text-gray-700 hover:text-[#008751] font-black uppercase tracking-widest text-sm rounded-2xl transition-all">
            <i data-lucide="list" class="w-4 h-4"></i>
            Xem Lịch Sử Đặt Sân
        </a>
        <a href="${pageContext.request.contextPath}/booking/weekly"
           class="flex items-center justify-center gap-2 px-8 py-4 bg-[#008751] hover:bg-[#006d41] text-white font-black uppercase tracking-widest text-sm rounded-2xl transition-all shadow-lg shadow-[#008751]/20">
            <i data-lucide="calendar-range" class="w-4 h-4"></i>
            Đặt Tuần Khác
        </a>
    </div>

</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>
</body>
</html>
