<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch và Booking - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">

<c:choose>
    <c:when test="${customerView}">
        <jsp:include page="/View/Layout/HeaderCustomer.jsp"/>
    </c:when>
    <c:otherwise>
        <jsp:include page="/View/Layout/HeaderAdmin.jsp"/>
    </c:otherwise>
</c:choose>

<main class="max-w-7xl mx-auto px-6 py-10 space-y-8">
    <div class="space-y-3">
        <c:choose>
            <c:when test="${customerView}">
                <a href="${pageContext.request.contextPath}/booking?locationId=${field.locationId}&fieldId=${field.fieldId}"
                   class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-100 rounded-xl text-[10px] font-black uppercase tracking-widest text-gray-500 hover:text-[#008751] hover:border-[#008751] transition-all">
                    Quay lại chọn sân
                </a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/field-schedule?fieldId=${field.fieldId}&date=${weekStart}&status=${status}"
                   class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-100 rounded-xl text-[10px] font-black uppercase tracking-widest text-gray-500 hover:text-[#008751] hover:border-[#008751] transition-all">
                    Quay lại lịch tiêu chuẩn
                </a>
            </c:otherwise>
        </c:choose>
        <h1 class="text-3xl md:text-4xl font-black uppercase tracking-tight">Lịch kèm booking: <span class="text-[#008751]">${field.fieldName}</span></h1>
        <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">Hiển thị booking đã đặt, đã thanh toán và đã hoàn thành theo từng khung giờ</p>
    </div>

    <section class="bg-white border border-gray-100 rounded-3xl p-6 md:p-8 shadow-sm space-y-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div class="flex items-center gap-3 text-[10px] font-black uppercase tracking-widest">
                <a href="${pageContext.request.contextPath}/field-booking-schedule?fieldId=${field.fieldId}&date=${prevWeek}&status=${status}"
                   class="px-4 py-2 rounded-xl border border-gray-200 text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Tuần trước</a>
                <span class="px-4 py-2 rounded-xl bg-slate-50 border border-slate-200 text-gray-500">${weekStart} - ${weekEnd}</span>
                <a href="${pageContext.request.contextPath}/field-booking-schedule?fieldId=${field.fieldId}&date=${nextWeek}&status=${status}"
                   class="px-4 py-2 rounded-xl border border-gray-200 text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Tuần sau</a>
            </div>

            <form method="get" action="${pageContext.request.contextPath}/field-booking-schedule" class="flex items-center gap-3">
                <input type="hidden" name="fieldId" value="${field.fieldId}">
                <input type="hidden" name="date" value="${weekStart}">
                <select name="status" onchange="this.form.submit()" class="px-3 py-2 rounded-xl border border-gray-200 text-xs font-black uppercase tracking-widest text-gray-500">
                    <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái lịch</option>
                    <option value="available" ${status == 'available' ? 'selected' : ''}>available</option>
                    <option value="unavailable" ${status == 'unavailable' ? 'selected' : ''}>unavailable</option>
                </select>
            </form>
        </div>

        <div class="overflow-x-auto pb-4">
            <div class="flex gap-5 min-w-max">
                <c:forEach var="entry" items="${schedulesByDate}">
                    <div class="min-w-[300px] space-y-4">
                        <div class="bg-white p-4 rounded-2xl border-2 border-gray-50 text-center shadow-sm">
                            <p class="text-[10px] font-black text-[#008751] uppercase tracking-[0.2em] opacity-70">${displayDateMap[entry.key]}</p>
                        </div>

                        <c:choose>
                            <c:when test="${empty entry.value}">
                                <div class="bg-gray-50 border-2 border-dashed border-gray-100 rounded-[1.5rem] p-8 text-center">
                                    <p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Không có lịch trong ngày này</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="space-y-3">
                                    <c:forEach var="s" items="${entry.value}">
                                        <c:set var="booked" value="${bookingBySchedule[s.scheduleId]}"/>
                                        <div class="group border-2 rounded-[1.5rem] p-4 transition-all ${s.status == 'available' ? 'border-gray-50 bg-white hover:border-[#008751] hover:shadow-md' : 'border-gray-100 bg-gray-50/70'}">
                                            <div class="flex items-start justify-between gap-3 mb-3">
                                                <p class="text-sm font-black text-gray-900 tracking-tight">${s.startTime} - ${s.endTime}</p>
                                                <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${s.status == 'available' ? 'bg-emerald-50 text-[#008751]' : 'bg-amber-50 text-amber-500'}">${s.status}</span>
                                            </div>

                                            <div class="flex items-center justify-between mb-3">
                                                <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest"><fmt:formatNumber value="${s.price}" pattern="#,##0"/> đ</span>
                                                <span class="text-[10px] font-black uppercase tracking-widest ${not empty booked ? 'text-blue-600' : 'text-gray-300'}">${not empty booked ? 'có booking' : 'trống'}</span>
                                            </div>

                                            <c:choose>
                                                <c:when test="${not empty booked}">
                                                    <div class="space-y-2 text-[10px] font-black uppercase tracking-widest text-gray-500">
                                                        <p>Khách hàng: <span class="text-gray-800">${booked.customerName}</span></p>
                                                        <p>Số điện thoại: <span class="text-gray-800">${booked.customerPhone}</span></p>
                                                        <p>Trạng thái booking:
                                                            <span class="px-2 py-1 rounded-full text-[9px] ${booked.status == 'completed' ? 'bg-emerald-100 text-emerald-700' : (booked.status == 'paid' ? 'bg-blue-100 text-blue-700' : 'bg-amber-100 text-amber-700')}">
                                                                ${booked.status}
                                                            </span>
                                                        </p>
                                                        <p>Mã booking: <span class="text-gray-800">${booked.bookingId}</span></p>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="block text-center px-3 py-2 rounded-xl border border-gray-200 text-gray-400 text-[10px] font-black uppercase tracking-widest">Chưa có booking</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:forEach>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>
