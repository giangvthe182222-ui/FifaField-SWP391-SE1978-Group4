<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng Điều Khiển - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2.5rem; }
        .fifa-green { color: #008751; }
        .bg-fifa-green { background-color: #008751; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="flex-grow max-w-7xl mx-auto w-full px-6 py-12 space-y-12">
    
    <!-- Welcome Section -->
    <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 relative overflow-hidden group">
        <!-- Decoration -->
        <div class="absolute -top-10 -right-10 opacity-[0.03] group-hover:scale-110 transition-transform duration-700">
            <i data-lucide="shield" class="w-64 h-64"></i>
        </div>

        <div class="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-8">
            <div class="space-y-2">
                <p class="text-[10px] font-black tracking-[0.3em] text-gray-400 uppercase">Hệ thống FIFAFIELD 2026</p>
                <h1 class="text-4xl font-black text-gray-900 uppercase tracking-tight">
                    Xin chào, <span class="text-[#008751]"><%= currentUser.getFullName() %></span>
                </h1>
                <p class="text-gray-500 font-bold text-sm uppercase tracking-widest opacity-60">Sẵn sàng cho trận đấu tiếp theo của bạn?</p>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full md:w-auto">
                <a href="${pageContext.request.contextPath}/booking?bookingMode=normal" class="px-8 py-5 rounded-[1.8rem] bg-[#008751] text-white font-black uppercase text-xs tracking-[0.2em] hover:bg-emerald-400 transition-all hover:-translate-y-1 shadow-2xl shadow-[#008751]/20 flex items-center justify-center gap-3">
                    <i data-lucide="zap" class="w-4 h-4"></i>
                    Đặt sân thường
                </a>
                <a href="${pageContext.request.contextPath}/booking/weekly" class="px-8 py-5 rounded-[1.8rem] border-2 border-[#008751] text-[#008751] bg-white font-black uppercase text-xs tracking-[0.2em] hover:bg-emerald-50 transition-all hover:-translate-y-1 flex items-center justify-center gap-3">
                    <i data-lucide="calendar-range" class="w-4 h-4"></i>
                    Đặt sân theo tuần
                </a>
            </div>
        </div>

        <div class="mt-12">
            <jsp:include page="/View/Layout/CustomerQuickPanel.jsp"/>
        </div>
    </section>

    <c:if test="${not empty refundNotifications}">
        <section class="bg-emerald-50 rounded-[2rem] border border-emerald-100 p-6 md:p-8 shadow-sm space-y-4">
            <div class="flex items-center justify-between gap-4">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-white text-emerald-600 border border-emerald-200 flex items-center justify-center">
                        <i data-lucide="badge-dollar-sign" class="w-5 h-5"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black uppercase tracking-[0.2em] text-emerald-500">Thông báo hoàn tiền</p>
                        <h2 class="text-xl font-black text-emerald-900 uppercase tracking-tight">Bạn có ${refundNotificationCount} đơn đã refunded</h2>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <c:forEach var="refund" items="${refundNotifications}">
                    <article class="bg-white rounded-2xl border border-emerald-100 p-5 space-y-3">
                        <div class="flex items-center justify-between gap-3">
                            <p class="text-[10px] font-black uppercase tracking-widest text-emerald-600">
                                Đơn #${fn:toUpperCase(fn:substring(refund.bookingId, 0, 8))}
                            </p>
                            <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest bg-emerald-100 text-emerald-700">
                                Refunded
                            </span>
                        </div>

                        <p class="text-sm font-black text-gray-900 uppercase tracking-tight">${refund.fieldName}</p>
                        <p class="text-xs font-bold text-gray-500">
                            ${refund.bookingDate} | ${refund.startTime} - ${refund.endTime}
                        </p>
                        <p class="text-xs font-black text-emerald-700 uppercase tracking-wider">
                            Số tiền hoàn: <fmt:formatNumber value="${refund.totalPrice}" pattern="#,##0"/> đ
                        </p>

                        <a href="${pageContext.request.contextPath}/customer/bookingDetail?id=${refund.bookingId}"
                           class="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-gray-900 text-white text-[10px] font-black uppercase tracking-widest hover:bg-[#008751] transition-all">
                            Xem chi tiết
                        </a>
                    </article>
                </c:forEach>
            </div>
        </section>
    </c:if>

    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <i data-lucide="alert-circle" class="w-5 h-5 text-rose-500"></i>
            <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${error}</p>
        </div>
    </c:if>

    <section class="space-y-8">
        <div class="flex items-center gap-4">
            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Tổng quan tài khoản</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm space-y-2">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400">Tổng số đơn đã đặt</p>
                <p class="text-3xl font-black text-gray-900 tracking-tighter">${totalBookings}</p>
            </article>
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm space-y-2">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400">Số tiền đã chi</p>
                <p class="text-3xl font-black text-[#008751] tracking-tighter"><fmt:formatNumber value="${totalSpent}" pattern="#,##0"/> đ</p>
            </article>
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm space-y-2">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400">Công nợ còn lại</p>
                <p class="text-3xl font-black text-rose-600 tracking-tighter"><fmt:formatNumber value="${totalOutstanding}" pattern="#,##0"/> đ</p>
            </article>
            <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm space-y-2">
                <p class="text-[9px] font-black uppercase tracking-widest text-gray-400">Lịch sắp tới</p>
                <p class="text-3xl font-black text-gray-900 tracking-tighter">${upcomingBookings}</p>
            </article>
        </div>
    </section>

    <section class="space-y-8">
        <div class="flex items-center justify-between gap-4 flex-wrap">
            <div class="flex items-center gap-4">
                <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Đơn gần đây</h2>
            </div>
            <div class="flex items-center gap-3">
                <a href="${pageContext.request.contextPath}/customer/bookings" class="px-5 py-3 rounded-2xl bg-gray-900 text-white font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-all">Lịch sử đặt sân</a>
                <a href="${pageContext.request.contextPath}/customer/my-calendar" class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-black text-[10px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all">Lịch chơi của tôi</a>
                <a href="${pageContext.request.contextPath}/customer/locations" class="px-5 py-3 rounded-2xl border border-gray-200 text-gray-600 font-black text-[10px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all">Danh sách cơ sở</a>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty recentBookings}">
                <div class="bg-white rounded-[2.5rem] border-2 border-dashed border-gray-100 p-16 text-center">
                    <i data-lucide="calendar-x" class="w-12 h-12 text-gray-200 mx-auto mb-4"></i>
                    <p class="text-gray-300 font-black uppercase tracking-widest text-[10px]">Bạn chưa có đơn đặt sân nào</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <c:forEach var="b" items="${recentBookings}">
                        <article class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm space-y-4">
                            <div class="flex items-start justify-between gap-4">
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">${b.bookingDate} | ${b.startTime} - ${b.endTime}</p>
                                    <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight mt-1">${b.fieldName}</h3>
                                </div>
                                <a href="${pageContext.request.contextPath}/customer/bookingDetail?id=${b.bookingId}" class="px-4 py-2 rounded-xl bg-gray-900 text-white font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-all">Chi tiết</a>
                            </div>

                            <div class="flex flex-wrap gap-2">
                                <span class="px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest ${b.playStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border border-sky-100' : b.playStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border border-orange-200' : b.playStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border border-indigo-100' : b.playStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">Play: ${empty b.playStatus ? 'booked' : b.playStatus}</span>
                                <span class="px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest ${b.paymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : b.paymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border border-yellow-200' : b.paymentStatus == 'pending refund' || b.paymentStatus == 'pending refund confirm' ? 'bg-amber-50 text-amber-600 border border-amber-100' : b.paymentStatus == 'refunded' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-gray-100 text-gray-600 border border-gray-200'}">Payment: ${empty b.paymentStatus ? 'pending' : b.paymentStatus}</span>
                                <span class="px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest ${b.extraPaymentStatus == 'paid extra' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : b.extraPaymentStatus == 'pending extra' ? 'bg-orange-50 text-orange-700 border border-orange-200' : 'bg-gray-100 text-gray-600 border border-gray-200'}">Extra: ${empty b.extraPaymentStatus ? 'none' : b.extraPaymentStatus}</span>
                            </div>

                            <div class="flex items-center justify-between border-t border-gray-100 pt-3">
                                <p class="text-sm font-black text-[#008751]"><fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> đ</p>
                                <p class="text-xs font-black text-rose-600 uppercase tracking-widest">Còn lại: <fmt:formatNumber value="${empty b.outstandingAmount ? 0 : b.outstandingAmount}" pattern="#,##0"/> đ</p>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>
</body>
</html>


