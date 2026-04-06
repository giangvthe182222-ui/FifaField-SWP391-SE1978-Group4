<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi Tiết Đặt Sân (Staff) - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2.5rem; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="flex-grow max-w-4xl mx-auto px-6 py-12 w-full space-y-8">
    
    <!-- Header Section -->
    <div class="space-y-2">
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/staff/locationBookings" class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 hover:text-[#008751] transition-colors shadow-sm border border-gray-100">
                <i data-lucide="chevron-left" class="w-5 h-5"></i>
            </a>
            <h1 class="text-3xl font-black text-gray-900 tracking-tight uppercase">
                CHI TIẾT <span class="text-[#008751]">ĐIỀU PHỐI</span>
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
        <c:set var="normalizedPlayStatus" value="${empty currentPlayStatus ? fn:toLowerCase(fn:trim(booking.playStatus)) : currentPlayStatus}" />
        <c:set var="normalizedPaymentStatus" value="${empty currentPaymentStatus ? fn:toLowerCase(fn:trim(booking.paymentStatus)) : currentPaymentStatus}" />
        <c:set var="normalizedExtraPaymentStatus" value="${empty currentExtraPaymentStatus ? fn:toLowerCase(fn:trim(booking.extraPaymentStatus)) : currentExtraPaymentStatus}" />
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
            
            <!-- Main Info Card -->
            <div class="md:col-span-2 space-y-8">
                <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center gap-4">
                            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Thông tin trận đấu</h2>
                        </div>
                        <div class="flex flex-wrap items-center justify-end gap-2">
                            <span class="px-3 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest ${normalizedPlayStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : normalizedPlayStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border border-sky-100' : normalizedPlayStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border border-orange-200' : normalizedPlayStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border border-indigo-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">
                                play: ${normalizedPlayStatus}
                            </span>
                            <span class="px-3 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest ${normalizedPaymentStatus == 'failed' || normalizedPaymentStatus == 'refunded' ? 'bg-rose-50 text-rose-600 border border-rose-100' : normalizedPaymentStatus == 'pending refund' ? 'bg-amber-50 text-amber-600 border border-amber-100' : normalizedPaymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border border-yellow-200' : normalizedPaymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">
                                payment: ${normalizedPaymentStatus}
                            </span>
                            <span class="px-3 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest ${normalizedExtraPaymentStatus == 'pending extra' ? 'bg-orange-50 text-orange-700 border border-orange-200' : normalizedExtraPaymentStatus == 'paid extra' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">
                                extra: ${empty normalizedExtraPaymentStatus ? 'none' : normalizedExtraPaymentStatus}
                            </span>
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
                        <div class="space-y-1">
                            <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Số tiền chưa trả</p>
                            <p class="font-black text-rose-600"><fmt:formatNumber value="${empty outstandingAmount ? 0 : outstandingAmount}" pattern="#,##0"/> đ</p>
                        </div>
                    </div>

                    <div class="pt-8 border-t border-gray-50 space-y-6">
                        <div class="flex items-center gap-4">
                            <div class="w-8 h-1 bg-gray-200 rounded-full"></div>
                            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Vật tư đi kèm</h2>
                        </div>

                        <c:choose>
                            <c:when test="${empty equipments}">
                                <div class="py-6 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100">
                                    <p class="text-[9px] font-black text-gray-300 uppercase tracking-widest">Không có vật tư thuê kèm</p>
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
                                                <fmt:formatNumber value="${e.rentalPrice}" pattern="#,##0"/> đ
                                            </p>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="pt-8 border-t border-gray-50 space-y-6">
                        <div class="flex items-center gap-4">
                            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Thêm vật tư cho khách</h2>
                        </div>

                        <c:choose>
                            <c:when test="${canAddEquipment and not empty availableEquipments}">
                                <div class="bg-emerald-50 border border-emerald-100 rounded-3xl p-5">
                                    <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[#008751]">Chỉ áp dụng khi booking đã checked in và đang trong khung giờ sử dụng</p>
                                    <p class="text-sm font-semibold text-emerald-800 mt-2">Tiền dụng cụ phát sinh sẽ được cộng vào phần còn lại để khách thanh toán nốt sau.</p>
                                </div>

                                <form method="post" action="${pageContext.request.contextPath}/staff/bookingDetail" class="space-y-4">
                                    <input type="hidden" name="action" value="addEquipment" />
                                    <input type="hidden" name="id" value="${booking.bookingId}" />

                                    <div class="space-y-4">
                                        <c:forEach var="item" items="${availableEquipments}">
                                            <div class="grid grid-cols-1 md:grid-cols-[1fr_auto] gap-4 items-center p-4 bg-gray-50 rounded-2xl border border-gray-100">
                                                <div class="space-y-1">
                                                    <p class="text-sm font-black text-gray-900 uppercase tracking-tight">${item.name}</p>
                                                    <div class="flex flex-wrap items-center gap-3 text-[10px] font-black uppercase tracking-widest text-gray-400">
                                                        <span>Còn lại: ${item.quantity}</span>
                                                        <span>Đơn giá: <fmt:formatNumber value="${item.rentalPrice}" pattern="#,##0"/> đ</span>
                                                    </div>
                                                </div>
                                                <div class="flex items-center gap-3 justify-end">
                                                    <label for="equipment_${item.equipmentId}" class="text-[9px] font-black uppercase tracking-widest text-gray-400">Số lượng</label>
                                                    <input id="equipment_${item.equipmentId}" type="number" min="0" max="${item.quantity}" value="0" name="equipment_${item.equipmentId}" class="w-24 rounded-2xl border border-gray-200 px-4 py-3 text-sm font-black text-gray-900 focus:border-[#008751] focus:ring focus:ring-[#008751]/10 outline-none" />
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>

                                    <button type="submit" class="w-full md:w-auto bg-[#008751] hover:bg-[#006b40] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[#008751]/20 text-white px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all hover:-translate-y-0.5 active:scale-[0.99] inline-flex items-center justify-center gap-2 shadow-lg shadow-emerald-200">
                                        <i data-lucide="package-plus" class="w-4 h-4"></i>
                                        THÊM DỤNG CỤ VÀO BOOKING
                                    </button>
                                </form>
                            </c:when>
                            <c:when test="${canAddEquipment}">
                                <div class="py-6 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100">
                                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Không còn equipment khả dụng tại location này</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="py-6 px-6 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100 space-y-2">
                                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Chưa đủ điều kiện thêm equipment</p>
                                    <p class="text-sm font-semibold text-gray-500">Booking phải ở trạng thái checked in và thời điểm hiện tại nằm trong khung giờ ${booking.startTime} - ${booking.endTime}.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
            </div>

            <!-- Summary & Status Update Card -->
            <div class="space-y-6">
                <div class="bg-[#166534] text-white elite-card shadow-2xl shadow-[#166534]/20 p-8 space-y-8 relative overflow-hidden">
                    <div class="absolute -top-4 -right-4 opacity-10">
                        <i data-lucide="settings" class="w-32 h-32"></i>
                    </div>
                    
                    <div class="relative z-10 space-y-6">
                        <h2 class="text-sm font-black uppercase tracking-widest flex items-center gap-2">
                            <i data-lucide="refresh-cw" class="w-4 h-4 text-emerald-300"></i>
                            Cập nhật trạng thái
                        </h2>

                        <form id="statusUpdateForm" method="post" action="${pageContext.request.contextPath}/staff/bookingDetail" class="space-y-6">
                            <input type="hidden" name="id" value="${booking.bookingId}" />
                            
                            <div class="space-y-2">
                                <label for="playStatus" class="text-[9px] font-black uppercase tracking-widest opacity-60">Play status</label>
                                <c:set var="canUpdate" value="${canMarkPaid || canCheckIn || canCheckOut || canMarkPendingRefund || canRefund || canMarkExtraPaid}"/>
                                <select name="playStatus" id="playStatus" ${!canUpdate ? 'disabled' : ''} class="w-full bg-white/10 border border-white/20 rounded-2xl py-4 px-6 text-xs font-black uppercase tracking-widest text-white outline-none focus:ring-2 focus:ring-emerald-400/50 appearance-none cursor-pointer ${!canUpdate ? 'opacity-60 cursor-not-allowed' : ''}">
                                    <option value="${normalizedPlayStatus}" selected class="text-gray-900">${normalizedPlayStatus}</option>
                                    <c:if test="${canCheckIn && normalizedPlayStatus != 'checked in'}">
                                        <option value="checked in" class="text-gray-900">checked in</option>
                                    </c:if>
                                    <c:if test="${canCheckOut && normalizedPlayStatus != 'checked out'}">
                                        <option value="checked out" class="text-gray-900">checked out</option>
                                    </c:if>
                                </select>
                            </div>

                            <div class="space-y-2">
                                <label for="paymentStatus" class="text-[9px] font-black uppercase tracking-widest opacity-60">Payment status</label>
                                <select name="paymentStatus" id="paymentStatus" data-current="${normalizedPaymentStatus}" ${!canUpdate ? 'disabled' : ''} class="w-full bg-white/10 border border-white/20 rounded-2xl py-4 px-6 text-xs font-black uppercase tracking-widest text-white outline-none focus:ring-2 focus:ring-emerald-400/50 appearance-none cursor-pointer ${!canUpdate ? 'opacity-60 cursor-not-allowed' : ''}">
                                    <option value="${normalizedPaymentStatus}" selected class="text-gray-900">${normalizedPaymentStatus}</option>
                                    <c:if test="${canMarkPaid && normalizedPaymentStatus != 'paid'}">
                                        <option value="paid" class="text-gray-900">paid</option>
                                    </c:if>
                                    <c:if test="${canMarkPendingRefund && normalizedPaymentStatus != 'pending refund'}">
                                        <option value="pending refund" class="text-gray-900">pending refund</option>
                                    </c:if>
                                    <c:if test="${canRefund && normalizedPaymentStatus != 'refunded'}">
                                        <option value="refunded" class="text-gray-900">refunded</option>
                                    </c:if>
                                </select>
                            </div>

                            <div class="space-y-2">
                                <label for="extraPaymentStatus" class="text-[9px] font-black uppercase tracking-widest opacity-60">Extra payment status</label>
                                <select name="extraPaymentStatus" id="extraPaymentStatus" data-current="${empty normalizedExtraPaymentStatus ? 'none' : normalizedExtraPaymentStatus}" ${!canMarkExtraPaid ? 'disabled' : ''} class="w-full bg-white/10 border border-white/20 rounded-2xl py-4 px-6 text-xs font-black uppercase tracking-widest text-white ${!canMarkExtraPaid ? 'opacity-60 cursor-not-allowed' : ''}">
                                    <option value="${empty normalizedExtraPaymentStatus ? 'none' : normalizedExtraPaymentStatus}" selected class="text-gray-900">${empty normalizedExtraPaymentStatus ? 'none' : normalizedExtraPaymentStatus}</option>
                                    <c:if test="${canMarkExtraPaid && normalizedExtraPaymentStatus != 'paid extra'}">
                                        <option value="paid extra" class="text-gray-900">paid extra</option>
                                    </c:if>
                                </select>
                                <input type="hidden" name="extraPaymentStatus" value="${empty normalizedExtraPaymentStatus ? 'none' : normalizedExtraPaymentStatus}" />
                            </div>

                            <button type="submit" ${!canUpdate ? 'disabled' : ''} class="w-full bg-[#008751] hover:bg-emerald-400 text-white py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-2 shadow-xl shadow-black/20 ${!canUpdate ? 'opacity-60 cursor-not-allowed hover:bg-[#008751] hover:translate-y-0 active:scale-100' : ''}">
                                <i data-lucide="save" class="w-4 h-4"></i>
                                LƯU THAY ĐỔI
                            </button>
                        </form>

                        <div class="pt-6 border-t border-white/10">
                            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-emerald-400 mb-1">TỔNG THANH TOÁN</p>
                            <p class="text-2xl font-black tracking-tighter leading-none">
                                <fmt:formatNumber value="${booking.totalPrice}" pattern="#,##0"/> đ
                            </p>
                            <p class="text-sm font-black text-rose-300 mt-2">Chưa trả: <fmt:formatNumber value="${empty outstandingAmount ? 0 : outstandingAmount}" pattern="#,##0"/> đ</p>
                        </div>
                    </div>
                </div>

                <div class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-8 space-y-4">
                    <h3 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Hoàn thành đơn</h3>
                    <c:set var="hasOutstandingAmount" value="${not empty outstandingAmount and outstandingAmount > 0}" />
                    <c:if test="${hasOutstandingAmount}">
                        <form method="get" action="${pageContext.request.contextPath}/payment" class="space-y-3">
                            <input type="hidden" name="bookingId" value="${booking.bookingId}" />
                            <input type="hidden" name="source" value="remaining" />
                            <button type="submit" ${normalizedPlayStatus == 'checked in' ? 'disabled' : ''} class="w-full bg-[#008751] hover:bg-emerald-500 text-white py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-2 ${normalizedPlayStatus == 'checked in' ? 'opacity-60 cursor-not-allowed hover:bg-[#008751] hover:translate-y-0 active:scale-100' : ''}">
                                <i data-lucide="credit-card" class="w-4 h-4"></i>
                                Trả nốt đơn
                            </button>
                        </form>
                    </c:if>
                    <c:if test="${normalizedPlayStatus == 'checked in'}">
                        <p class="text-xs font-semibold text-amber-700">Không thể thanh toán khi booking đang checked in. Vui lòng thanh toán trước giờ chơi hoặc sau khi checked out.</p>
                    </c:if>
                    <c:if test="${!hasOutstandingAmount}">
                        <p class="text-xs font-semibold text-emerald-700">Đơn này đã thanh toán đủ, không còn công nợ.</p>
                    </c:if>
                </div>

                <a href="${pageContext.request.contextPath}/staff/locationBookings" class="w-10 h-10 bg-white rounded-xl border border-gray-100 text-gray-400 hover:text-[#008751] hover:border-[#008751] transition-all flex items-center justify-center" aria-label="Quay lại" title="Quay lại">
                    <i data-lucide="arrow-left" class="w-5 h-5"></i>
                </a>
            </div>
        </div>
    </c:if>

</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();

    (function () {
        var form = document.getElementById('statusUpdateForm');
        if (!form) {
            return;
        }

        form.addEventListener('submit', function (e) {
            var paymentSelect = document.getElementById('paymentStatus');
            var extraSelect = document.getElementById('extraPaymentStatus');

            var currentPayment = paymentSelect ? (paymentSelect.getAttribute('data-current') || '').toLowerCase() : '';
            var nextPayment = paymentSelect ? (paymentSelect.value || '').toLowerCase() : '';
            var currentExtra = extraSelect ? (extraSelect.getAttribute('data-current') || '').toLowerCase() : '';
            var nextExtra = extraSelect ? (extraSelect.value || '').toLowerCase() : '';

            if ((currentPayment === 'deposited' && nextPayment === 'paid')
                    || (currentExtra === 'pending extra' && nextExtra === 'paid extra')) {
                if (!confirm('Xác nhận thanh toán tiền mặt?')) {
                    e.preventDefault();
                }
            }
        });
    })();
</script>
</body>
</html>
