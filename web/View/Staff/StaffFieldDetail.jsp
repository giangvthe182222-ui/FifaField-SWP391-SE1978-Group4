<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết sân - Staff</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="max-w-7xl mx-auto px-6 py-10 space-y-8">
    <div class="space-y-5">
        <a href="${pageContext.request.contextPath}/staff/fields" class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-100 rounded-xl text-[10px] font-black uppercase tracking-widest text-gray-500 hover:text-[#008751] hover:border-[#008751] transition-all">Quay lại danh sách sân</a>

        <section class="bg-white border border-gray-100 rounded-3xl p-6 md:p-8 shadow-sm">
            <div class="flex flex-col md:flex-row gap-6">
                <div class="w-full md:w-56 h-44 rounded-2xl overflow-hidden bg-slate-100 shrink-0">
                    <c:choose>
                        <c:when test="${not empty field.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${field.imageUrl}" alt="Ảnh sân" class="w-full h-full object-cover"/>
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/assets/img/default_field.jpg" alt="Ảnh sân" class="w-full h-full object-cover"/>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="flex-1 space-y-2">
                    <h1 class="text-3xl font-black uppercase tracking-tight">${field.fieldName}</h1>
                    <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">Chi nhánh: ${locationName}</p>
                    <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">Loại sân: ${field.fieldType}</p>
                    <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">Tình trạng: ${field.fieldCondition}</p>
                    <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">Trạng thái: ${field.status}</p>
                </div>
            </div>
        </section>
    </div>

    <section class="bg-white border border-gray-100 rounded-3xl p-6 md:p-8 shadow-sm space-y-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <h2 class="text-2xl font-black uppercase tracking-tight">Lịch sân theo tuần</h2>
            <div class="flex items-center gap-3 text-[10px] font-black uppercase tracking-widest">
                <a href="${pageContext.request.contextPath}/staff/fields/detail?fieldId=${field.fieldId}&date=${prevWeek}" class="px-4 py-2 rounded-xl border border-gray-200 text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Tuần trước</a>
                <span class="px-4 py-2 rounded-xl bg-slate-50 border border-slate-200 text-gray-500">${weekStart} - ${weekEnd}</span>
                <a href="${pageContext.request.contextPath}/staff/fields/detail?fieldId=${field.fieldId}&date=${nextWeek}" class="px-4 py-2 rounded-xl border border-gray-200 text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Tuần sau</a>
            </div>
        </div>

        <div class="overflow-x-auto pb-4">
            <div class="flex gap-5 min-w-max">
                <c:forEach var="entry" items="${schedulesByDate}">
                    <div class="min-w-[280px] space-y-4">
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
                                                <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${s.status == 'available' ? 'bg-emerald-50 text-[#008751]' : 'bg-amber-50 text-amber-500'}">${s.status == 'available' ? 'available' : 'unavailable'}</span>
                                            </div>

                                            <div class="flex items-center justify-between mb-3">
                                                <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest"><fmt:formatNumber value="${s.price}" pattern="#,##0"/> đ</span>
                                                <span class="text-[10px] font-black uppercase tracking-widest ${not empty booked ? 'text-blue-600' : 'text-gray-300'}">${not empty booked ? 'đã đặt' : 'trống'}</span>
                                            </div>

                                            <c:choose>
                                                <c:when test="${not empty booked}">
                                                    <div class="space-y-2">
                                                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Khách hàng: <span class="text-gray-700">${booked.customerName}</span></p>
                                                        <a href="${pageContext.request.contextPath}/staff/bookingDetail?id=${booked.bookingId}" class="block text-center px-3 py-2 rounded-xl bg-gray-900 text-white text-[10px] font-black uppercase tracking-widest hover:bg-[#008751] transition-all">Booking detail</a>
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
