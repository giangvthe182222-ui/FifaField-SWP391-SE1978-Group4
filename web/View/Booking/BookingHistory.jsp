<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch Sử Đặt Sân - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2.5rem; }
        .custom-scrollbar::-webkit-scrollbar { height: 6px; width: 6px; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #008751; border-radius: 10px; }
        .custom-scrollbar::-webkit-scrollbar-track { background: #f1f5f9; border-radius: 10px; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<c:choose>
    <c:when test="${viewMode == 'manager'}">
        <jsp:include page="/View/Layout/HeaderManager.jsp"/>
    </c:when>
    <c:when test="${viewMode == 'staff'}">
        <jsp:include page="/View/Layout/HeaderStaff.jsp"/>
    </c:when>
    <c:otherwise>
        <jsp:include page="/View/Layout/HeaderCustomer.jsp"/>
    </c:otherwise>
</c:choose>

<main class="flex-grow max-w-7xl mx-auto px-6 py-12 w-full space-y-12">

    <c:if test="${viewMode == 'customer'}">
        <jsp:include page="/View/Layout/CustomerTopBanner.jsp"/>
    </c:if>
    
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div class="space-y-2">
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                <c:choose>
                    <c:when test="${viewMode == 'staff'}">ĐẶT SÂN <span class="text-[#008751]">${locationName}</span></c:when>
                    <c:otherwise>LỊCH SỬ <span class="text-[#008751]">ĐẶT SÂN</span></c:otherwise>
                </c:choose>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">
                <c:choose>
                    <c:when test="${viewMode == 'staff'}">Quản lý điều phối chi tiết tại chi nhánh</c:when>
                    <c:otherwise>Hành trình thi đấu của bạn tại hệ thống FIFAFIELD</c:otherwise>
                </c:choose>
            </p>
        </div>

        <form method="get" action="${pageContext.request.contextPath}${viewMode == 'manager' ? '/manager/locationBookings' : (viewMode == 'staff' ? '/staff/locationBookings' : '/customer/bookings')}" class="flex flex-wrap items-center gap-4">
            <div>
                <input type="date" name="date" value="${param.date}" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
            </div>
            <div>
                <input type="time" name="time" value="${param.time}" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
            </div>
            <c:choose>
                <c:when test="${viewMode == 'staff'}">
                    <div>
                        <select name="playStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Play status</option>
                            <option value="booked" ${param.playStatus == 'booked' ? 'selected' : ''}>booked</option>
                            <option value="checked in" ${param.playStatus == 'checked in' ? 'selected' : ''}>checked in</option>
                            <option value="checked out" ${param.playStatus == 'checked out' ? 'selected' : ''}>checked out</option>
                            <option value="completed" ${param.playStatus == 'completed' ? 'selected' : ''}>completed</option>
                            <option value="cancelled" ${param.playStatus == 'cancelled' ? 'selected' : ''}>cancelled</option>
                        </select>
                    </div>
                    <div>
                        <select name="paymentStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Payment status</option>
                            <option value="pending" ${param.paymentStatus == 'pending' ? 'selected' : ''}>pending</option>
                            <option value="deposited" ${param.paymentStatus == 'deposited' ? 'selected' : ''}>deposited</option>
                            <option value="paid" ${param.paymentStatus == 'paid' ? 'selected' : ''}>paid</option>
                            <option value="pending refund" ${param.paymentStatus == 'pending refund' ? 'selected' : ''}>pending refund</option>
                            <option value="refunded" ${param.paymentStatus == 'refunded' ? 'selected' : ''}>refunded</option>
                            <option value="failed" ${param.paymentStatus == 'failed' ? 'selected' : ''}>failed</option>
                        </select>
                    </div>
                    <div>
                        <select name="extraPaymentStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Extra status</option>
                            <option value="none" ${param.extraPaymentStatus == 'none' ? 'selected' : ''}>none</option>
                            <option value="pending extra" ${param.extraPaymentStatus == 'pending extra' ? 'selected' : ''}>pending extra</option>
                            <option value="paid extra" ${param.extraPaymentStatus == 'paid extra' ? 'selected' : ''}>paid extra</option>
                        </select>
                    </div>
                </c:when>
                <c:when test="${viewMode == 'customer'}">
                    <div>
                        <select name="playStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Play status</option>
                            <option value="booked" ${param.playStatus == 'booked' ? 'selected' : ''}>booked</option>
                            <option value="checked in" ${param.playStatus == 'checked in' ? 'selected' : ''}>checked in</option>
                            <option value="checked out" ${param.playStatus == 'checked out' ? 'selected' : ''}>checked out</option>
                            <option value="completed" ${param.playStatus == 'completed' ? 'selected' : ''}>completed</option>
                            <option value="cancelled" ${param.playStatus == 'cancelled' ? 'selected' : ''}>cancelled</option>
                        </select>
                    </div>
                    <div>
                        <select name="paymentStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Payment status</option>
                            <option value="pending" ${param.paymentStatus == 'pending' ? 'selected' : ''}>pending</option>
                            <option value="deposited" ${param.paymentStatus == 'deposited' ? 'selected' : ''}>deposited</option>
                            <option value="paid" ${param.paymentStatus == 'paid' ? 'selected' : ''}>paid</option>
                            <option value="pending refund" ${param.paymentStatus == 'pending refund' ? 'selected' : ''}>pending refund</option>
                            <option value="refunded" ${param.paymentStatus == 'refunded' ? 'selected' : ''}>refunded</option>
                            <option value="failed" ${param.paymentStatus == 'failed' ? 'selected' : ''}>failed</option>
                        </select>
                    </div>
                    <div>
                        <select name="extraPaymentStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Extra status</option>
                            <option value="none" ${param.extraPaymentStatus == 'none' ? 'selected' : ''}>none</option>
                            <option value="pending extra" ${param.extraPaymentStatus == 'pending extra' ? 'selected' : ''}>pending extra</option>
                            <option value="paid extra" ${param.extraPaymentStatus == 'paid extra' ? 'selected' : ''}>paid extra</option>
                        </select>
                    </div>
                </c:when>
                <c:otherwise>
                    <div>
                        <select name="playStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Play status</option>
                            <option value="booked" ${param.playStatus == 'booked' ? 'selected' : ''}>booked</option>
                            <option value="checked in" ${param.playStatus == 'checked in' ? 'selected' : ''}>checked in</option>
                            <option value="checked out" ${param.playStatus == 'checked out' ? 'selected' : ''}>checked out</option>
                            <option value="completed" ${param.playStatus == 'completed' ? 'selected' : ''}>completed</option>
                            <option value="cancelled" ${param.playStatus == 'cancelled' ? 'selected' : ''}>cancelled</option>
                        </select>
                    </div>
                    <div>
                        <select name="paymentStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Payment status</option>
                            <option value="pending" ${param.paymentStatus == 'pending' ? 'selected' : ''}>pending</option>
                            <option value="deposited" ${param.paymentStatus == 'deposited' ? 'selected' : ''}>deposited</option>
                            <option value="paid" ${param.paymentStatus == 'paid' ? 'selected' : ''}>paid</option>
                            <option value="pending refund" ${param.paymentStatus == 'pending refund' ? 'selected' : ''}>pending refund</option>
                            <option value="refunded" ${param.paymentStatus == 'refunded' ? 'selected' : ''}>refunded</option>
                            <option value="failed" ${param.paymentStatus == 'failed' ? 'selected' : ''}>failed</option>
                        </select>
                    </div>
                    <div>
                        <select name="extraPaymentStatus" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                            <option value="">Extra status</option>
                            <option value="none" ${param.extraPaymentStatus == 'none' ? 'selected' : ''}>none</option>
                            <option value="pending extra" ${param.extraPaymentStatus == 'pending extra' ? 'selected' : ''}>pending extra</option>
                            <option value="paid extra" ${param.extraPaymentStatus == 'paid extra' ? 'selected' : ''}>paid extra</option>
                        </select>
                    </div>
                </c:otherwise>
            </c:choose>
            <c:if test="${viewMode == 'staff' || viewMode == 'manager'}">
                <div>
                    <input type="text" name="customerKeyword" placeholder="Tên hoặc số điện thoại..." value="${param.customerKeyword}" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                </div>
            </c:if>
            <button type="submit" class="bg-[#008751] text-white px-6 py-3 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-emerald-400 transition-all">Lọc</button>
            <a href="${pageContext.request.contextPath}${viewMode == 'manager' ? '/manager/locationBookings' : (viewMode == 'staff' ? '/staff/locationBookings' : '/customer/bookings')}" class="text-[10px] font-black text-gray-400 uppercase tracking-widest hover:text-[#008751]">Xóa</a>
        </form>
    </div>

    <!-- Flash Messages -->
    <c:if test="${not empty flashSuccess}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-[#008751] text-white rounded-xl flex items-center justify-center shadow-lg shadow-[#008751]/20">
            </div>
            <p class="text-sm font-bold text-[#008751] uppercase tracking-tight">${flashSuccess}</p>
        </div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
            </div>
            <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${flashError}</p>
        </div>
    </c:if>

    <c:if test="${viewMode == 'staff'}">
        <div class="bg-white elite-card border border-amber-100 shadow-sm p-6 md:p-8 space-y-5">
            <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-[0.25em]">Refund alerts</p>
                    <h2 class="text-2xl font-black text-gray-900 uppercase tracking-tight">Thông báo đơn cần refund</h2>
                </div>
                <div class="px-4 py-2 rounded-xl bg-amber-50 border border-amber-100 text-amber-700 text-[11px] font-black uppercase tracking-widest">
                    Còn ${refundPendingCount} đơn chưa refund
                </div>
            </div>

            <c:choose>
                <c:when test="${refundPendingCount > 0}">
                    <div class="space-y-3 max-h-64 overflow-y-auto pr-2 custom-scrollbar">
                        <c:forEach var="refundBooking" items="${refundPendingBookings}">
                            <a href="${pageContext.request.contextPath}/staff/bookingDetail?id=${refundBooking.bookingId}" class="block p-4 rounded-2xl border border-amber-100 bg-amber-50/40 hover:bg-amber-50 transition-colors">
                                <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                                    <div>
                                        <p class="text-sm font-black text-gray-900 uppercase tracking-tight">${refundBooking.fieldName}</p>
                                        <p class="text-[10px] font-black text-gray-500 uppercase tracking-widest mt-1">${refundBooking.bookingDate} | ${refundBooking.startTime} - ${refundBooking.endTime}</p>
                                        <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mt-1">KH: ${refundBooking.customerName} - ${refundBooking.customerPhone}</p>
                                    </div>
                                    <span class="inline-flex items-center justify-center px-4 py-2 rounded-xl bg-white border border-amber-200 text-amber-700 text-[10px] font-black uppercase tracking-widest">
                                        Chi tiết đơn
                                    </span>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="p-4 rounded-2xl border border-emerald-100 bg-emerald-50 text-emerald-700 text-sm font-bold">
                        Hiện chưa có đơn pending refund tại location bạn phụ trách.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <!-- Vertical List of Horizontal Bars -->
    <div class="space-y-6">
        <div class="flex items-center gap-4">
            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Danh sách đơn đặt gần đây</h2>
        </div>

        <c:choose>
            <c:when test="${empty bookings}">
                <div class="py-20 text-center bg-white elite-card border-2 border-dashed border-gray-100 shadow-sm">
                    <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6">
                        </div>
                    <h3 class="text-xl font-black text-gray-900 uppercase tracking-tight">Chưa có dữ liệu đặt sân</h3>
                    <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-2">
                        <c:choose>
                            <c:when test="${viewMode == 'staff'}">Không có đơn đặt nào phù hợp bộ lọc tại chi nhánh</c:when>
                            <c:when test="${viewMode == 'manager'}">Không có đơn đặt nào phù hợp bộ lọc tại location được gán</c:when>
                            <c:otherwise>Hãy bắt đầu trận đấu đầu tiên của bạn ngay hôm nay!</c:otherwise>
                        </c:choose>
                    </p>
                    <c:if test="${viewMode == null || viewMode == 'customer'}">
                        <a href="${pageContext.request.contextPath}/booking" class="inline-flex items-center gap-2 mt-8 px-8 py-4 bg-[#008751] text-white rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-emerald-400 transition-all hover:-translate-y-1">
                            ĐẶT SÂN NGAY
                        </a>
                    </c:if>
                </div>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${viewMode == 'staff' || viewMode == 'manager'}">
                        <!-- Staff view: horizontal rows -->
                        <div class="space-y-4 pb-8">
                            <c:forEach var="b" items="${bookings}">
                                <div class="bg-white rounded-[2rem] border border-gray-100 shadow-lg shadow-gray-200/30 hover:shadow-xl hover:shadow-[#008751]/10 transition-all group relative overflow-hidden p-6 md:p-8 flex flex-col md:flex-row items-center gap-6 md:gap-10">
                            
                                    <!-- Left: Icon & Status -->
                                    <div class="flex items-center gap-6 w-full md:w-auto">
                                
                                        <div class="md:hidden flex-grow">
                                            <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight">${b.fieldName}</h3>
                                            <c:if test="${viewMode != 'staff'}">
                                                <div class="flex items-center gap-2">
                                                    <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.playStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : b.playStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border border-sky-100' : b.playStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border border-orange-200' : b.playStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border border-indigo-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">
                                                        Play: ${empty b.playStatus ? 'booked' : b.playStatus}
                                                    </span>
                                                    <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.paymentStatus == 'refunded' ? 'bg-rose-50 text-rose-600 border border-rose-100' : b.paymentStatus == 'pending refund' || b.paymentStatus == 'pending refund confirm' ? 'bg-amber-50 text-amber-600 border border-amber-100' : b.paymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border border-yellow-200' : b.paymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : 'bg-gray-100 text-gray-600 border border-gray-200'}">
                                                        Payment: ${empty b.paymentStatus ? 'pending' : b.paymentStatus}
                                                    </span>
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>

                                    <!-- Middle: Info -->
                                    <div class="hidden md:block flex-grow space-y-2">
                                        <div class="flex items-center gap-4">
                                            <h3 class="text-xl font-black text-gray-900 uppercase tracking-tight group-hover:text-[#008751] transition-colors">${b.fieldName}</h3>
                                            <c:if test="${viewMode != 'staff'}">
                                                <div class="flex items-center gap-2">
                                                    <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.playStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : b.playStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border border-sky-100' : b.playStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border border-orange-200' : b.playStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border border-indigo-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">
                                                        Play: ${empty b.playStatus ? 'booked' : b.playStatus}
                                                    </span>
                                                    <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.paymentStatus == 'refunded' ? 'bg-rose-50 text-rose-600 border border-rose-100' : b.paymentStatus == 'pending refund' || b.paymentStatus == 'pending refund confirm' ? 'bg-amber-50 text-amber-600 border border-amber-100' : b.paymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border border-yellow-200' : b.paymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : 'bg-gray-100 text-gray-600 border border-gray-200'}">
                                                        Payment: ${empty b.paymentStatus ? 'pending' : b.paymentStatus}
                                                    </span>
                                                </div>
                                            </c:if>
                                        </div>
                                        <div class="flex items-center gap-6">
                                            <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">${b.bookingDate}</p>
                                            <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">${b.startTime} - ${b.endTime}</p>
                                            <c:if test="${viewMode == 'staff' || viewMode == 'manager'}">
                       
                                                <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">SĐT: ${b.customerPhone}</p>
                                            </c:if>
                                        </div>
                                </div>

                                    <!-- Mobile Info (Visible only on small screens) -->
                                    <div class="md:hidden w-full grid grid-cols-2 gap-4 pt-4 border-t border-gray-50">
                                        <div class="space-y-1">
                                            <p class="text-[8px] font-black text-gray-400 uppercase tracking-widest">Ngày & Giờ</p>
                                            <p class="text-xs font-bold text-gray-700">${b.bookingDate} | ${b.startTime}</p>
                                        </div>
                                        <div class="space-y-1 text-right">
                                            <p class="text-[8px] font-black text-gray-400 uppercase tracking-widest">Tổng cộng</p>
                                            <p class="text-xs font-black text-[#008751]"><fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> VND</p>
                                            <p class="text-[9px] font-bold text-rose-600">Chưa trả: <fmt:formatNumber value="${empty b.outstandingAmount ? 0 : b.outstandingAmount}" pattern="#,##0"/> VND</p>
                                        </div>
                                    </div>

                                    <!-- Right: Price & Action -->
                                    <div class="flex items-center justify-between md:justify-end gap-8 w-full md:w-auto shrink-0">
                                        <div class="hidden md:block text-right">
                                            <p class="text-[8px] font-black text-gray-400 uppercase tracking-widest mb-1">Tổng cộng</p>
                                            <p class="text-2xl font-black text-[#008751] tracking-tighter">
                                                <fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> VND
                                            </p>
                                            <p class="text-[10px] font-bold text-rose-600 mt-1">Chưa trả: <fmt:formatNumber value="${empty b.outstandingAmount ? 0 : b.outstandingAmount}" pattern="#,##0"/> VND</p>
                                        </div>
                                        <div class="flex items-center gap-2 bg-gray-50 p-2 rounded-2xl border border-gray-100 w-full sm:w-auto">
                                            <c:choose>
                                                <c:when test="${viewMode == 'staff'}">
                                                    <c:set var="canStaffUpdate" value="${staffCanCheckInMap[b.bookingId] || staffCanCheckOutMap[b.bookingId] || staffCanPendingRefundMap[b.bookingId] || staffCanRefundMap[b.bookingId] || staffCanMarkPaidMap[b.bookingId] || staffCanMarkExtraPaidMap[b.bookingId]}"/>
                                                    <div class="flex flex-col items-stretch gap-2">
                                                        <select name="playStatus_${b.bookingId}" data-current="${empty b.playStatus ? 'booked' : b.playStatus}" ${!canStaffUpdate ? 'disabled' : ''} class="px-2 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border ${!canStaffUpdate ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer'} ${b.playStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border-rose-100' : b.playStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border-sky-100' : b.playStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border-orange-200' : b.playStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border-indigo-100' : 'bg-slate-100 text-slate-600 border-slate-200'}">
                                                            <option value="${empty b.playStatus ? 'booked' : b.playStatus}" selected>${empty b.playStatus ? 'booked' : b.playStatus}</option>
                                                            <c:if test="${staffCanCheckInMap[b.bookingId] && b.playStatus != 'checked in'}">
                                                                <option value="checked in">checked in</option>
                                                            </c:if>
                                                            <c:if test="${staffCanCheckOutMap[b.bookingId] && b.playStatus != 'checked out'}">
                                                                <option value="checked out">checked out</option>
                                                            </c:if>
                                                        </select>

                                                        <select name="paymentStatus_${b.bookingId}" data-current="${empty b.paymentStatus ? 'paid' : b.paymentStatus}" ${!canStaffUpdate ? 'disabled' : ''} class="px-2 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border ${!canStaffUpdate ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer'} ${b.paymentStatus == 'failed' ? 'bg-rose-50 text-rose-600 border-rose-100' : b.paymentStatus == 'pending refund' || b.paymentStatus == 'pending refund confirm' ? 'bg-amber-50 text-amber-600 border-amber-100' : b.paymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border-yellow-200' : b.paymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border-emerald-100' : 'bg-slate-100 text-slate-600 border-slate-200'}">
                                                            <option value="${empty b.paymentStatus ? 'paid' : b.paymentStatus}" selected>${empty b.paymentStatus ? 'paid' : b.paymentStatus}</option>
                                                            <c:if test="${staffCanMarkPaidMap[b.bookingId] && b.paymentStatus != 'paid'}">
                                                                <option value="paid">paid</option>
                                                            </c:if>
                                                            <c:if test="${staffCanPendingRefundMap[b.bookingId] && b.paymentStatus != 'pending refund'}">
                                                                <option value="pending refund">pending refund</option>
                                                            </c:if>
                                                            <c:if test="${staffCanRefundMap[b.bookingId] && b.paymentStatus != 'refunded'}">
                                                                <option value="refunded">refunded</option>
                                                            </c:if>
                                                        </select>

                                                        <select name="extraPaymentStatus_${b.bookingId}" data-current="${empty b.extraPaymentStatus ? 'none' : b.extraPaymentStatus}" ${!staffCanMarkExtraPaidMap[b.bookingId] ? 'disabled' : ''} class="px-2 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border ${!staffCanMarkExtraPaidMap[b.bookingId] ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer'} ${b.extraPaymentStatus == 'pending extra' ? 'bg-orange-50 text-orange-700 border-orange-200' : b.extraPaymentStatus == 'paid extra' ? 'bg-emerald-50 text-[#008751] border-emerald-100' : 'bg-gray-100 text-gray-600 border-gray-200'}">
                                                            <option value="${empty b.extraPaymentStatus ? 'none' : b.extraPaymentStatus}" selected>${empty b.extraPaymentStatus ? 'none' : b.extraPaymentStatus}</option>
                                                            <c:if test="${staffCanMarkExtraPaidMap[b.bookingId] && b.extraPaymentStatus != 'paid extra'}">
                                                                <option value="paid extra">paid extra</option>
                                                            </c:if>
                                                        </select>
                                                    </div>
                                                    <button type="button" onclick="updateStatus('${b.bookingId}')" ${!canStaffUpdate ? 'disabled' : ''} class="bg-gray-900 text-white p-2 rounded-xl hover:bg-[#008751] transition-colors ${!canStaffUpdate ? 'opacity-50 cursor-not-allowed hover:bg-gray-900' : ''}">Update</button>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="flex flex-col items-stretch gap-2">
                                                        <select name="playStatus_${b.bookingId}" data-current="${empty b.playStatus ? 'booked' : b.playStatus}" class="px-2 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border cursor-pointer ${b.playStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border-rose-100' : b.playStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border-sky-100' : b.playStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border-orange-200' : b.playStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border-indigo-100' : 'bg-slate-100 text-slate-600 border-slate-200'}">
                                                            <option value="booked" ${empty b.playStatus || b.playStatus == 'booked' ? 'selected' : ''}>booked</option>
                                                            <option value="checked in" ${b.playStatus == 'checked in' ? 'selected' : ''}>checked in</option>
                                                            <option value="checked out" ${b.playStatus == 'checked out' ? 'selected' : ''}>checked out</option>
                                                            <option value="completed" ${b.playStatus == 'completed' ? 'selected' : ''}>completed</option>
                                                            <option value="cancelled" ${b.playStatus == 'cancelled' ? 'selected' : ''}>cancelled</option>
                                                        </select>

                                                        <select name="paymentStatus_${b.bookingId}" data-current="${empty b.paymentStatus ? 'pending' : b.paymentStatus}" class="px-2 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border cursor-pointer ${b.paymentStatus == 'failed' ? 'bg-rose-50 text-rose-600 border-rose-100' : b.paymentStatus == 'pending refund' || b.paymentStatus == 'pending refund confirm' ? 'bg-amber-50 text-amber-600 border-amber-100' : b.paymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border-yellow-200' : b.paymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border-emerald-100' : 'bg-slate-100 text-slate-600 border-slate-200'}">
                                                            <option value="pending" ${empty b.paymentStatus || b.paymentStatus == 'pending' ? 'selected' : ''}>pending</option>
                                                            <option value="deposited" ${b.paymentStatus == 'deposited' ? 'selected' : ''}>deposited</option>
                                                            <option value="paid" ${b.paymentStatus == 'paid' ? 'selected' : ''}>paid</option>
                                                            <option value="pending refund" ${b.paymentStatus == 'pending refund' ? 'selected' : ''}>pending refund</option>
                                                            <option value="refunded" ${b.paymentStatus == 'refunded' ? 'selected' : ''}>refunded</option>
                                                            <option value="failed" ${b.paymentStatus == 'failed' ? 'selected' : ''}>failed</option>
                                                        </select>

                                                        <select name="extraPaymentStatus_${b.bookingId}" data-current="${empty b.extraPaymentStatus ? 'none' : b.extraPaymentStatus}" class="px-2 py-1 rounded-full text-[8px] font-black uppercase tracking-widest border cursor-pointer ${b.extraPaymentStatus == 'pending extra' ? 'bg-orange-50 text-orange-700 border-orange-200' : b.extraPaymentStatus == 'paid extra' ? 'bg-emerald-50 text-[#008751] border-emerald-100' : 'bg-gray-100 text-gray-600 border-gray-200'}">
                                                            <option value="none" ${empty b.extraPaymentStatus || b.extraPaymentStatus == 'none' ? 'selected' : ''}>none</option>
                                                            <option value="pending extra" ${b.extraPaymentStatus == 'pending extra' ? 'selected' : ''}>pending extra</option>
                                                            <option value="paid extra" ${b.extraPaymentStatus == 'paid extra' ? 'selected' : ''}>paid extra</option>
                                                        </select>
                                                    </div>
                                                    <button type="button" onclick="updateStatus('${b.bookingId}')" class="bg-gray-900 text-white p-2 rounded-xl hover:bg-[#008751] transition-colors">Update</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="flex flex-wrap items-center gap-2 w-full md:w-auto">
                                            <c:choose>
                                                <c:when test="${viewMode == 'staff'}">
                                                    <a href="${pageContext.request.contextPath}/staff/bookingDetail?id=${b.bookingId}" class="w-full md:w-auto bg-gray-900 text-white px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest flex items-center justify-center gap-2 hover:bg-[#008751] transition-all hover:scale-[1.05] active:scale-95 shadow-lg shadow-gray-200">Chi tiết</a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="w-full md:w-auto bg-gray-100 text-gray-500 px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest flex items-center justify-center gap-2">Theo dõi tại lịch sân</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                </div>
                            </c:forEach>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <!-- Customer view: square cards -->
                        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 pb-8">
                            <c:forEach var="b" items="${bookings}">
                                <article class="bg-white rounded-3xl border border-gray-100 shadow-lg shadow-gray-200/30 hover:shadow-xl hover:shadow-[#008751]/10 transition-all p-6 flex flex-col gap-4 min-h-[320px]">
                                    <div class="flex items-start justify-between gap-3">
                                        <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight leading-tight">${b.fieldName}</h3>
                                        <div class="flex flex-col gap-1 items-end shrink-0">
                                            <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.playStatus == 'checked in' ? 'bg-sky-50 text-sky-600 border border-sky-100' : b.playStatus == 'checked out' ? 'bg-orange-50 text-orange-700 border border-orange-200' : b.playStatus == 'completed' ? 'bg-indigo-50 text-indigo-600 border border-indigo-100' : b.playStatus == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-slate-100 text-slate-600 border border-slate-200'}">Play: ${empty b.playStatus ? 'booked' : b.playStatus}</span>
                                            <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.paymentStatus == 'paid' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : b.paymentStatus == 'deposited' ? 'bg-yellow-50 text-yellow-700 border border-yellow-200' : b.paymentStatus == 'pending refund' || b.paymentStatus == 'pending refund confirm' ? 'bg-amber-50 text-amber-600 border border-amber-100' : b.paymentStatus == 'refunded' ? 'bg-rose-50 text-rose-600 border border-rose-100' : b.paymentStatus == 'failed' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-gray-100 text-gray-600 border border-gray-200'}">Payment: ${empty b.paymentStatus ? 'pending' : b.paymentStatus}</span>
                                            <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.extraPaymentStatus == 'paid extra' ? 'bg-emerald-50 text-[#008751] border border-emerald-100' : b.extraPaymentStatus == 'pending extra' ? 'bg-orange-50 text-orange-700 border border-orange-200' : 'bg-gray-100 text-gray-600 border border-gray-200'}">Extra: ${empty b.extraPaymentStatus ? 'none' : b.extraPaymentStatus}</span>
                                        </div>
                                    </div>

                                    <div class="space-y-2 bg-gray-50 border border-gray-100 rounded-2xl p-4">
                                        <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Ngày đặt</p>
                                        <p class="text-sm font-bold text-gray-800">${b.bookingDate}</p>
                                        <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest pt-1">Khung giờ</p>
                                        <p class="text-sm font-bold text-gray-800">${b.startTime} - ${b.endTime}</p>
                                    </div>

                                    <div class="mt-auto space-y-4">
                                        <div>
                                            <p class="text-[8px] font-black text-gray-400 uppercase tracking-widest mb-1">Tổng cộng</p>
                                            <p class="text-2xl font-black text-[#008751] tracking-tighter"><fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> VND</p>
                                            <p class="text-[10px] font-bold text-rose-600 mt-1">Chưa trả: <fmt:formatNumber value="${empty b.outstandingAmount ? 0 : b.outstandingAmount}" pattern="#,##0"/> VND</p>
                                        </div>

                                        <div class="flex flex-wrap items-center gap-2">
                                            <a href="${pageContext.request.contextPath}/customer/bookingDetail?id=${b.bookingId}" class="bg-gray-900 text-white px-5 py-3 rounded-xl font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-colors">Chi tiết</a>
                                            <c:if test="${b.paymentStatus == 'refunded'}">
                                                <form method="post" action="${pageContext.request.contextPath}/customer/bookingDetail" onsubmit="return confirm('Xác nhận báo cáo chưa nhận được tiền hoàn?');">
                                                    <input type="hidden" name="id" value="${b.bookingId}">
                                                    <input type="hidden" name="action" value="report_refund_issue">
                                                    <button type="submit" class="bg-rose-600 text-white px-4 py-3 rounded-xl font-black text-[10px] uppercase tracking-widest hover:bg-rose-500 transition-colors">Báo cáo chưa hoàn tiền</button>
                                                </form>
                                            </c:if>
                                            <c:if test="${reviewableBookingMap[b.bookingId]}">
                                                <c:choose>
                                                    <c:when test="${feedbackBookingMap[b.bookingId]}">
                                                        <c:set var="fb" value="${feedbackMap[b.bookingId]}" />
                                                        <div class="w-full rounded-2xl border border-emerald-100 bg-emerald-50/80 p-4 space-y-3">
                                                            <div class="flex items-center justify-between gap-3">
                                                                <div class="flex items-center gap-1 text-xl leading-none">
                                                                    <c:forEach begin="1" end="5" var="star">
                                                                        <span class="${star <= fb.rating ? 'text-amber-400' : 'text-gray-200'}">★</span>
                                                                    </c:forEach>
                                                                </div>
                                                                <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest bg-white text-emerald-700 border border-emerald-100">Đã đánh giá</span>
                                                            </div>
                                                            <c:if test="${not empty fb.comment}">
                                                                <p class="text-sm font-semibold text-emerald-900/80">${fb.comment}</p>
                                                            </c:if>
                                                            <div class="flex flex-wrap gap-2">
                                                                <a href="${pageContext.request.contextPath}/customer/feedback?bookingId=${b.bookingId}" class="bg-emerald-600 text-white px-4 py-3 rounded-xl font-black text-[10px] uppercase tracking-widest hover:bg-emerald-500 transition-colors">Sửa đánh giá</a>
                                                                <form method="post" action="${pageContext.request.contextPath}/customer/feedback" onsubmit="return confirm('Bạn có muốn xóa đánh giá này không?');">
                                                                    <input type="hidden" name="bookingId" value="${b.bookingId}">
                                                                    <input type="hidden" name="action" value="delete">
                                                                    <button type="submit" class="bg-white text-rose-600 border border-rose-200 px-4 py-3 rounded-xl font-black text-[10px] uppercase tracking-widest hover:bg-rose-50 transition-colors">Xóa</button>
                                                                </form>
                                                            </div>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a href="${pageContext.request.contextPath}/customer/feedback?bookingId=${b.bookingId}" class="bg-amber-500 text-white px-4 py-3 rounded-xl font-black text-[10px] uppercase tracking-widest hover:bg-amber-400 transition-colors">Gửi đánh giá</a>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:if>
                                        </div>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Pagination -->
    <c:if test="${totalPages > 1}">
        <div class="flex items-center justify-center gap-2 py-6">
            <c:if test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}${viewMode == 'manager' ? '/manager/locationBookings' : (viewMode == 'staff' ? '/staff/locationBookings' : '/customer/bookings')}?page=1<c:if test="${not empty param.date}">&date=${param.date}</c:if><c:if test="${not empty param.time}">&time=${param.time}</c:if><c:if test="${viewMode == 'staff' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${not empty param.customerKeyword}">&customerKeyword=${param.customerKeyword}</c:if>" 
                   class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                    ⟨⟨ First
                </a>
                    <a href="${pageContext.request.contextPath}${viewMode == 'manager' ? '/manager/locationBookings' : (viewMode == 'staff' ? '/staff/locationBookings' : '/customer/bookings')}?page=${currentPage - 1}<c:if test="${not empty param.date}">&date=${param.date}</c:if><c:if test="${not empty param.time}">&time=${param.time}</c:if><c:if test="${viewMode == 'staff' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${not empty param.customerKeyword}">&customerKeyword=${param.customerKeyword}</c:if>" 
                   class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                    ⟨ Prev
                </a>
            </c:if>
            
            <c:forEach begin="1" end="${totalPages}" var="i">
                <c:choose>
                    <c:when test="${i == currentPage}">
                        <span class="px-3 py-2 rounded-lg bg-[#008751] text-white font-bold text-sm">${i}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}${viewMode == 'manager' ? '/manager/locationBookings' : (viewMode == 'staff' ? '/staff/locationBookings' : '/customer/bookings')}?page=${i}<c:if test="${not empty param.date}">&date=${param.date}</c:if><c:if test="${not empty param.time}">&time=${param.time}</c:if><c:if test="${viewMode == 'staff' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${not empty param.customerKeyword}">&customerKeyword=${param.customerKeyword}</c:if>" 
                           class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                            ${i}
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:if test="${currentPage < totalPages}">
                <a href="${pageContext.request.contextPath}${viewMode == 'manager' ? '/manager/locationBookings' : (viewMode == 'staff' ? '/staff/locationBookings' : '/customer/bookings')}?page=${currentPage + 1}<c:if test="${not empty param.date}">&date=${param.date}</c:if><c:if test="${not empty param.time}">&time=${param.time}</c:if><c:if test="${viewMode == 'staff' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${not empty param.customerKeyword}">&customerKeyword=${param.customerKeyword}</c:if>" 
                   class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                    Next ⟩
                </a>
                <a href="${pageContext.request.contextPath}${viewMode == 'manager' ? '/manager/locationBookings' : (viewMode == 'staff' ? '/staff/locationBookings' : '/customer/bookings')}?page=${totalPages}<c:if test="${not empty param.date}">&date=${param.date}</c:if><c:if test="${not empty param.time}">&time=${param.time}</c:if><c:if test="${viewMode == 'staff' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'staff' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'manager' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.playStatus}">&playStatus=${param.playStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.paymentStatus}">&paymentStatus=${param.paymentStatus}</c:if><c:if test="${viewMode == 'customer' && not empty param.extraPaymentStatus}">&extraPaymentStatus=${param.extraPaymentStatus}</c:if><c:if test="${not empty param.customerKeyword}">&customerKeyword=${param.customerKeyword}</c:if>" 
                   class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                    Last ⟩⟩
                </a>
            </c:if>
        </div>
        <p class="text-center text-sm font-semibold text-slate-500">Trang <span class="text-[#008751] font-bold">${currentPage}</span> / ${totalPages} (Tổng: ${totalItems} đơn đặt)</p>
    </c:if>
    <c:if test="${not empty bookings and totalItems > 0}">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div class="bg-white elite-card p-8 border border-gray-100 shadow-sm flex items-center gap-6">
                    <div class="w-14 h-14 bg-emerald-50 rounded-2xl flex items-center justify-center text-[#008751]">
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tổng đơn đặt</p>
                        <p class="text-3xl font-black text-gray-900 tracking-tighter">${totalItems}</p>
                    </div>
                </div>
            <div class="bg-white elite-card p-8 border border-gray-100 shadow-sm flex items-center gap-6">
                <div class="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-600">
                </div>
                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Trang hiện tại</p>
                    <p class="text-3xl font-black text-gray-900 tracking-tighter">${currentPage} / ${totalPages}</p>
                </div>
            </div>
            <div class="bg-white elite-card p-8 border border-gray-100 shadow-sm flex items-center gap-6">
                <div class="w-14 h-14 bg-amber-50 rounded-2xl flex items-center justify-center text-amber-600">
                </div>
                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Hiển thị</p>
                    <p class="text-3xl font-black text-gray-900 tracking-tighter">${bookings.size()}</p>
                </div>
            </div>
        </div>
    </c:if>

</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    function updateStatus(bookingId) {
        var playStatus = document.getElementsByName('playStatus_' + bookingId)[0];
        var paymentStatus = document.getElementsByName('paymentStatus_' + bookingId)[0];
        var extraPaymentStatus = document.getElementsByName('extraPaymentStatus_' + bookingId)[0];

        if (playStatus && paymentStatus && extraPaymentStatus) {
            var currentPaymentStatus = (paymentStatus.getAttribute('data-current') || '').toLowerCase();
            var nextPaymentStatus = (paymentStatus.value || '').toLowerCase();
            var currentExtraPaymentStatus = (extraPaymentStatus.getAttribute('data-current') || '').toLowerCase();
            var nextExtraPaymentStatus = (extraPaymentStatus.value || '').toLowerCase();
            if ((currentPaymentStatus === 'deposited' && nextPaymentStatus === 'paid')
                    || (currentExtraPaymentStatus === 'pending extra' && nextExtraPaymentStatus === 'paid extra')) {
                if (!confirm('Xác nhận thanh toán tiền mặt?')) {
                    return;
                }
            }

            if ((currentPaymentStatus === 'pending refund' || currentPaymentStatus === 'pending refund confirm')
                    && nextPaymentStatus === 'refunded') {
                if (!confirm('Xác nhận đã hoàn tiền?')) {
                    return;
                }
            }

            var form = document.createElement('form');
            form.method = 'post';
            form.action = '${pageContext.request.contextPath}${viewMode == "manager" ? "/manager/locationBookings" : "/staff/locationBookings"}';

            var bookingInput = document.createElement('input');
            bookingInput.name = 'bookingId';
            bookingInput.value = bookingId;
            form.appendChild(bookingInput);

            var playInput = document.createElement('input');
            playInput.name = 'playStatus';
            playInput.value = playStatus.value;
            form.appendChild(playInput);

            var paymentInput = document.createElement('input');
            paymentInput.name = 'paymentStatus';
            paymentInput.value = paymentStatus.value;
            form.appendChild(paymentInput);

            var extraInput = document.createElement('input');
            extraInput.name = 'extraPaymentStatus';
            extraInput.value = extraPaymentStatus.value;
            form.appendChild(extraInput);

            document.body.appendChild(form);
            form.submit();
            return;
        }

        return;
    }
</script>

    
</body>
</html>
