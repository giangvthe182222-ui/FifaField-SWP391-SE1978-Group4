<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi Tiết Đặt Sân - FIFAFIELD</title>
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

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="flex-grow max-w-4xl mx-auto px-6 py-12 w-full space-y-8">
    
    <!-- Header Section -->
    <div class="space-y-2">
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/customer/bookings" class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 hover:text-[#008751] transition-colors shadow-sm border border-gray-100">
                <i data-lucide="chevron-left" class="w-5 h-5"></i>
            </a>
            <h1 class="text-3xl font-black text-gray-900 tracking-tight uppercase">
                CHI TIẾT <span class="text-[#008751]">ĐƠN ĐẶT</span>
            </h1>
        </div>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em] ml-14">Thông tin xác thực hệ thống FIFAFIELD 2026</p>
    </div>

    <!-- Flash Messages -->
    <c:if test="${not empty flashSuccess}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-[#008751] text-white rounded-xl flex items-center justify-center shadow-lg shadow-[#008751]/20">
                <i data-lucide="check" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-[#008751] uppercase tracking-tight">${flashSuccess}</p>
        </div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${flashError}</p>
        </div>
    </c:if>

    <c:if test="${not empty booking}">
        <c:set var="normalizedPlayStatus" value="${fn:toLowerCase(fn:trim(booking.playStatus))}" />
        <c:set var="normalizedPaymentStatus" value="${fn:toLowerCase(fn:trim(booking.paymentStatus))}" />
        <c:set var="normalizedExtraPaymentStatus" value="${fn:toLowerCase(fn:trim(booking.extraPaymentStatus))}" />
        <c:set var="hasOutstandingAmount" value="${not empty booking.outstandingAmount and booking.outstandingAmount > 0}" />
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
            
            <!-- Main Info Card -->
            <div class="md:col-span-2 space-y-8">
                <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center gap-4">
                            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Thông tin trận đấu</h2>
                        </div>
                        <div class="flex flex-col items-end gap-1">
                            <span class="px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest ${normalizedPlayStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border border-sky-100' : normalizedPlayStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border border-orange-200' : normalizedPlayStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border border-indigo-100' : normalizedPlayStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">Play: ${empty booking.playStatus ? 'booked' : booking.playStatus}</span>
                            <span class="px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest ${normalizedPaymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : normalizedPaymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border border-yellow-200' : normalizedPaymentStatus == 'pending refund' || normalizedPaymentStatus == 'pending refund confirm' ? 'bg-amber-50 text-amber-600 border border-amber-100' : normalizedPaymentStatus == 'refunded' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-gray-100 text-gray-600 border border-gray-200'}">Payment: ${empty booking.paymentStatus ? 'pending' : booking.paymentStatus}</span>
                            <span class="px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest ${normalizedExtraPaymentStatus == 'paid extra' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : normalizedExtraPaymentStatus == 'pending extra' ? 'bg-orange-50 text-orange-700 border border-orange-200' : 'bg-gray-100 text-gray-600 border border-gray-200'}">Extra: ${empty booking.extraPaymentStatus ? 'none' : booking.extraPaymentStatus}</span>
                        </div>
                    </div>

                    <div class="grid grid-cols-2 gap-y-8 gap-x-4">
                        <div class="space-y-1">
                            <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Mã đặt sân</p>
                            <p class="font-mono font-bold text-gray-900 text-sm">${booking.bookingId}</p>
                        </div>
                        <div class="space-y-1">
                            <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Sân thi đấu</p>
                            <p class="font-black text-gray-900 uppercase tracking-tight">${booking.fieldName}</p>
                        </div>
                        <div class="space-y-1">
                            <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Ngày thi đấu</p>
                            <p class="font-bold text-gray-700">${booking.bookingDate}</p>
                        </div>
                        <div class="space-y-1">
                            <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Khung giờ</p>
                            <p class="font-bold text-gray-700">${booking.startTime} - ${booking.endTime}</p>
                        </div>
                    </div>

                    <div class="pt-8 border-t border-gray-50 space-y-6">
                        <div class="flex items-center gap-4">
                            <div class="w-8 h-1 bg-gray-200 rounded-full"></div>
                            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Dụng cụ đi kèm</h2>
                        </div>

                        <c:choose>
                            <c:when test="${empty equipments}">
                                <div class="py-6 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100">
                                    <p class="text-[9px] font-black text-gray-300 uppercase tracking-widest">Không có dụng cụ thuê kèm</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="space-y-3">
                                    <c:forEach var="e" items="${equipments}">
                                        <div class="flex items-center justify-between p-4 bg-gray-50 rounded-2xl border border-gray-100">
                                            <div class="flex items-center gap-3">
                                                <div class="w-8 h-8 bg-white rounded-lg flex items-center justify-center text-[#008751] shadow-sm">
                                                    <i data-lucide="package" class="w-4 h-4"></i>
                                                </div>
                                                <div>
                                                    <p class="text-xs font-black text-gray-900 uppercase tracking-tight">${e.name}</p>
                                                    <p class="text-[9px] font-bold text-gray-400 uppercase tracking-widest">Số lượng: ${e.quantity}</p>
                                                </div>
                                            </div>
                                            <p class="text-xs font-black text-[#008751]">
                                                <fmt:formatNumber value="${e.rentalPrice}" pattern="#,##0"/> VND
                                            </p>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
            </div>

            <!-- Summary Card -->
            <div class="space-y-6">
                <div class="bg-[#166534] text-white elite-card shadow-2xl shadow-[#166534]/20 p-8 space-y-8 relative overflow-hidden">
                    <div class="absolute -top-4 -right-4 opacity-10">
                        <i data-lucide="receipt" class="w-32 h-32"></i>
                    </div>
                    
                    <div class="relative z-10 space-y-6">
                        <h2 class="text-sm font-black uppercase tracking-widest flex items-center gap-2">
                            <i data-lucide="credit-card" class="w-4 h-4 text-emerald-300"></i>
                            Tổng thanh toán
                        </h2>

                        <div class="space-y-4">
                            <div class="flex justify-between text-[10px] font-black uppercase tracking-widest opacity-60">
                                <span>Play</span>
                                <span class="text-emerald-300">${empty booking.playStatus ? 'booked' : booking.playStatus}</span>
                            </div>
                            <div class="flex justify-between text-[10px] font-black uppercase tracking-widest opacity-60">
                                <span>Payment</span>
                                <span class="text-emerald-300">${empty booking.paymentStatus ? 'pending' : booking.paymentStatus}</span>
                            </div>
                            <div class="flex justify-between text-[10px] font-black uppercase tracking-widest opacity-60">
                                <span>Công nợ còn lại</span>
                                <span class="text-rose-200"><fmt:formatNumber value="${empty booking.outstandingAmount ? 0 : booking.outstandingAmount}" pattern="#,##0"/> VND</span>
                            </div>
                            <div class="pt-4 border-t border-white/10">
                                <p class="text-[10px] font-black uppercase tracking-[0.2em] text-emerald-400 mb-1">TỔNG CỘNG</p>
                                <p class="text-3xl font-black tracking-tighter leading-none">
                                    <fmt:formatNumber value="${booking.totalPrice}" pattern="#,##0"/> VND
                                </p>
                            </div>
                        </div>

                        <c:if test="${hasOutstandingAmount}">
                            <c:choose>
                                <c:when test="${normalizedPaymentStatus == 'pending'}">
                                    <a href="${pageContext.request.contextPath}/payment?bookingId=${booking.bookingId}" class="w-full bg-[#008751] hover:bg-emerald-500 text-white py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-2">
                                        <i data-lucide="credit-card" class="w-4 h-4"></i>
                                        TIẾP TỤC THANH TOÁN
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/payment?bookingId=${booking.bookingId}&source=remaining" class="w-full bg-[#008751] hover:bg-emerald-500 text-white py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-2">
                                        <i data-lucide="credit-card" class="w-4 h-4"></i>
                                        TRẢ NỐT ĐƠN
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </c:if>

                        <c:if test="${(normalizedPaymentStatus == 'paid' || normalizedPaymentStatus == 'deposited') && canRequestRefund}">
                            <form method="post" action="${pageContext.request.contextPath}/customer/bookingDetail">
                                <input type="hidden" name="id" value="${booking.bookingId}" />
                                <input type="hidden" name="action" value="cancel" />
                                <button type="submit" class="w-full bg-rose-600 hover:bg-rose-500 text-white py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-2">
                                    <i data-lucide="x-circle" class="w-4 h-4"></i>
                                    YÊU CẦU HOÀN TIỀN
                                </button>
                            </form>
                            <p class="text-[8px] text-center font-bold text-emerald-200/50 uppercase tracking-widest leading-relaxed">
                                * Hủy trước 48h để chuyển đơn sang pending refund theo chính sách FIFAFIELD
                            </p>
                        </c:if>

                        <c:if test="${refundBlockedByPolicy}">
                            <div class="w-full bg-amber-50 border border-amber-200 rounded-2xl p-4">
                                <p class="text-[10px] font-black text-amber-700 uppercase tracking-widest leading-relaxed">
                                    Đơn không được refund vì lịch đấu còn 2 ngày hoặc ít hơn.
                                </p>
                            </div>
                        </c:if>
                    </div>
                </div>

                <a href="${pageContext.request.contextPath}/customer/bookings" class="w-10 h-10 bg-white rounded-xl border border-gray-100 text-gray-400 hover:text-[#008751] hover:border-[#008751] transition-all flex items-center justify-center" aria-label="Quay lại" title="Quay lại">
                    <i data-lucide="arrow-left" class="w-5 h-5"></i>
                </a>
            </div>
        </div>
    </c:if>

</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>
</body>
</html>
