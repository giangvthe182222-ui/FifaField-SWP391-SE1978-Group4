<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- Tailwind & Google Fonts -->
<script src="https://cdn.tailwindcss.com"></script>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
<script src="https://unpkg.com/lucide@latest"></script>

<style>
    body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
    .fifa-green { color: #008751; }
    .bg-fifa-green { background-color: #008751; }
    .rounded-elite { border-radius: 2rem; }
    
    /* Animation cho phần Refund */
    #refund-details {
        max-height: 0;
        overflow: hidden;
        transition: max-height 0.4s ease-out, opacity 0.3s ease;
        opacity: 0;
    }
    #refund-details.show {
        max-height: 1000px; /* Đủ lớn để chứa nội dung */
        opacity: 1;
        margin-top: 1.5rem;
    }
</style>
<jsp:include page="/View/Layout/HeaderCustomer.jsp" />

<main class="max-w-7xl mx-auto w-full px-4 py-8 space-y-10">
    <jsp:include page="/View/Layout/CustomerTopBanner.jsp" />

    <!-- 1. THÔNG BÁO HOÀN TIỀN (COLLAPSIBLE) -->
    <c:if test="${not empty refundNotifications}">
        <section class="bg-emerald-50 rounded-elite border border-emerald-100 shadow-sm overflow-hidden">
            <!-- Thanh tiêu đề bấm được -->
            <button onclick="toggleRefunds()" class="w-full flex items-center justify-between p-6 hover:bg-emerald-100/50 transition-all text-left group">
                <div class="flex items-center gap-4">
                    <div class="w-12 h-12 rounded-2xl bg-white text-emerald-600 border border-emerald-200 flex items-center justify-center shadow-sm">
                        <i data-lucide="badge-dollar-sign" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black uppercase tracking-[0.2em] text-emerald-500">Thông báo hệ thống</p>
                        <h2 class="text-lg font-black text-emerald-900 uppercase tracking-tight">
                            Bạn có <span class="text-emerald-600">${refundNotificationCount}</span> đơn đã được hoàn tiền
                        </h2>
                    </div>
                </div>
                <div class="flex items-center gap-2 text-emerald-600 font-black text-[10px] uppercase tracking-widest bg-white px-4 py-2 rounded-xl shadow-sm group-hover:bg-emerald-600 group-hover:text-white transition-all">
                    <span id="refund-btn-text">Xem chi tiết</span>
                    <i data-lucide="chevron-down" id="refund-icon" class="w-4 h-4 transition-transform duration-300"></i>
                </div>
            </button>

            <!-- Nội dung chi tiết (Bị ẩn mặc định) -->
            <div id="refund-details" class="px-6 pb-6">
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
                    <c:forEach var="refund" items="${refundNotifications}">
                        <article class="bg-white rounded-2xl border border-emerald-100 p-5 space-y-3 shadow-sm hover:border-emerald-300 transition-colors">
                            <div class="flex items-center justify-between gap-3">
                                <p class="text-[10px] font-black uppercase tracking-widest text-emerald-600">
                                    Mã đơn: #${fn:toUpperCase(fn:substring(refund.bookingId, 0, 8))}
                                </p>
                                <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest bg-emerald-100 text-emerald-700 border border-emerald-200">
                                    Đã hoàn tiền
                                </span>
                            </div>

                            <div class="space-y-1">
                                <p class="text-sm font-black text-gray-900 uppercase tracking-tight">${refund.fieldName}</p>
                                <p class="text-[11px] font-bold text-gray-400">
                                    ${refund.bookingDate} | ${refund.startTime} - ${refund.endTime}
                                </p>
                            </div>

                            <div class="flex items-center justify-between pt-2 border-t border-emerald-50">
                                <p class="text-xs font-black text-emerald-700 uppercase tracking-wider">
                                    Số tiền: <span class="text-sm"><fmt:formatNumber value="${refund.totalPrice}" pattern="#,##0"/> đ</span>
                                </p>
                                <a href="${pageContext.request.contextPath}/customer/bookingDetail?id=${refund.bookingId}"
                                   class="px-4 py-2 rounded-xl bg-gray-900 text-white text-[9px] font-black uppercase tracking-widest hover:bg-[#008751] transition-all">
                                    Chi tiết
                                </a>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </div>
        </section>
    </c:if>

    <!-- 2. TỔNG QUAN TÀI KHOẢN -->
    <section class="space-y-6">
        <div class="flex items-center gap-3">
            <div class="w-2 h-6 bg-[#008751] rounded-full"></div>
            <h2 class="text-[11px] font-black text-gray-400 uppercase tracking-[0.3em]">Tổng quan tài khoản</h2>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm hover:shadow-md transition-shadow">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400 mb-2">Đã đặt</p>
                <p class="text-2xl font-black text-gray-900 tracking-tighter">${totalBookings} <span class="text-xs text-gray-300">đơn</span></p>
            </article>
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm hover:shadow-md transition-shadow">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400 mb-2">Đã chi</p>
                <p class="text-2xl font-black text-[#008751] tracking-tighter"><fmt:formatNumber value="${totalSpent}" pattern="#,##0"/> <span class="text-xs">đ</span></p>
            </article>
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm hover:shadow-md transition-shadow">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400 mb-2">Công nợ</p>
                <p class="text-2xl font-black text-rose-600 tracking-tighter"><fmt:formatNumber value="${totalOutstanding}" pattern="#,##0"/> <span class="text-xs">đ</span></p>
            </article>
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm hover:shadow-md transition-shadow">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400 mb-2">Sắp tới</p>
                <p class="text-2xl font-black text-indigo-600 tracking-tighter">${upcomingBookings} <span class="text-xs text-gray-300">trận</span></p>
            </article>
        </div>
    </section>

    <!-- 3. ĐƠN GẦN ĐÂY -->
    <section class="space-y-6">
        <div class="flex items-center justify-between gap-4 flex-wrap">
            <div class="flex items-center gap-3">
                <div class="w-2 h-6 bg-[#008751] rounded-full"></div>
                <h2 class="text-[11px] font-black text-gray-400 uppercase tracking-[0.3em]">Đơn gần đây</h2>
            </div>
            <div class="flex items-center gap-2">
                <a href="${pageContext.request.contextPath}/customer/bookings" class="px-4 py-2.5 rounded-xl bg-gray-900 text-white font-black text-[9px] uppercase tracking-widest hover:bg-[#008751] transition-all">Lịch sử</a>
                <a href="${pageContext.request.contextPath}/customer/my-calendar" class="px-4 py-2.5 rounded-xl border border-gray-200 text-gray-500 font-black text-[9px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all bg-white">Lịch của tôi</a>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty recentBookings}">
                <div class="bg-white rounded-elite border-2 border-dashed border-gray-100 p-12 text-center">
                    <i data-lucide="calendar-x" class="w-10 h-10 text-gray-200 mx-auto mb-3"></i>
                    <p class="text-gray-300 font-black uppercase tracking-widest text-[9px]">Chưa có dữ liệu đặt sân</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
                    <c:forEach var="b" items="${recentBookings}">
                        <article class="bg-white border border-gray-100 rounded-[2rem] p-6 shadow-sm hover:shadow-xl hover:shadow-gray-200/40 transition-all group">
                            <div class="flex items-start justify-between gap-4">
                                <div class="space-y-1">
                                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">
                                        <i data-lucide="calendar" class="w-3 h-3 inline mr-1"></i> ${b.bookingDate} | ${b.startTime} - ${b.endTime}
                                    </p>
                                    <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight group-hover:text-[#008751] transition-colors">${b.fieldName}</h3>
                                </div>
                                <a href="${pageContext.request.contextPath}/customer/bookingDetail?id=${b.bookingId}" 
                                   class="w-10 h-10 rounded-xl bg-gray-50 text-gray-400 flex items-center justify-center hover:bg-[#008751] hover:text-white transition-all">
                                    <i data-lucide="arrow-right" class="w-4 h-4"></i>
                                </a>
                            </div>

                            <div class="flex flex-wrap gap-2 my-4">
                                <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border ${b.playStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border-sky-100' : b.playStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border-indigo-100' : 'bg-gray-50 text-gray-400 border-gray-100'}">
                                    Play: ${empty b.playStatus ? 'booked' : b.playStatus}
                                </span>
                                <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border ${b.paymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border-emerald-100' : 'bg-amber-50 text-amber-600 border-amber-100'}">
                                    Pay: ${empty b.paymentStatus ? 'pending' : b.paymentStatus}
                                </span>
                            </div>

                            <div class="flex items-center justify-between border-t border-gray-50 pt-4">
                                <div>
                                    <p class="text-[8px] font-black text-gray-400 uppercase tracking-widest mb-1">Tổng tiền</p>
                                    <p class="text-base font-black text-[#008751]"><fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> đ</p>
                                </div>
                                <div class="text-right">
                                    <p class="text-[8px] font-black text-gray-400 uppercase tracking-widest mb-1">Còn nợ</p>
                                    <p class="text-base font-black text-rose-600"><fmt:formatNumber value="${empty b.outstandingAmount ? 0 : b.outstandingAmount}" pattern="#,##0"/> đ</p>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<script>
    // Khởi tạo icons
    lucide.createIcons();

    // Hàm đóng mở phần Refund
    function toggleRefunds() {
        const details = document.getElementById('refund-details');
        const icon = document.getElementById('refund-icon');
        const btnText = document.getElementById('refund-btn-text');
        
        if (details.classList.contains('show')) {
            details.classList.remove('show');
            icon.style.transform = 'rotate(0deg)';
            btnText.innerText = 'Xem chi tiết';
        } else {
            details.classList.add('show');
            icon.style.transform = 'rotate(180deg)';
            btnText.innerText = 'Thu gọn';
        }
    }
</script>