<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch cá nhân - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">
<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="max-w-7xl mx-auto px-6 py-10 space-y-8">
    <jsp:include page="/View/Layout/CustomerTopBanner.jsp"/>

    <div class="space-y-2">
        <h1 class="text-4xl font-black uppercase tracking-tight">Lịch chơi <span class="text-[#008751]">cá nhân</span></h1>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.25em]">Lịch chung hiển thị booking của bạn ở trạng thái pending, paid, completed</p>
    </div>

    <section class="bg-white border border-gray-100 rounded-3xl p-6 md:p-8 shadow-sm space-y-6">
        <form method="get" action="${pageContext.request.contextPath}/customer/my-calendar" class="flex flex-col md:flex-row md:items-end gap-4">
            <div>
                <label class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Lọc theo ngày</label>
                <input type="date" name="date" value="${selectedDate}" class="px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
            </div>
            <div>
                <label class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Lọc theo cơ sở</label>
                <select name="locationId" class="px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none min-w-[220px]">
                    <option value="">Tất cả cơ sở</option>
                    <c:forEach var="loc" items="${locationOptions}">
                        <option value="${loc.locationId}" ${selectedLocationId == loc.locationId ? 'selected' : ''}>${loc.locationName}</option>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Lọc theo sân</label>
                <select name="fieldId" class="px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none min-w-[220px]">
                    <option value="">Tất cả sân</option>
                    <c:forEach var="f" items="${fieldOptions}">
                        <option value="${f.fieldId}" ${selectedFieldId == f.fieldId ? 'selected' : ''}>${f.fieldName}</option>
                    </c:forEach>
                </select>
            </div>
            <button type="submit" class="px-6 py-3 rounded-2xl bg-[#008751] text-white font-black text-[10px] uppercase tracking-widest hover:bg-emerald-400 transition-all">Lọc</button>
            <a href="${pageContext.request.contextPath}/customer/my-calendar" class="px-6 py-3 rounded-2xl border border-gray-200 text-gray-500 font-black text-[10px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all text-center">Xóa lọc</a>
        </form>

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div class="flex items-center gap-3 text-[10px] font-black uppercase tracking-widest">
                <a href="${pageContext.request.contextPath}/customer/my-calendar?weekDate=${prevWeek}<c:if test='${not empty selectedLocationId}'>&locationId=${selectedLocationId}</c:if><c:if test='${not empty selectedFieldId}'>&fieldId=${selectedFieldId}</c:if>"
                   class="px-4 py-2 rounded-xl border border-gray-200 text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Tuần trước</a>
                <span class="px-4 py-2 rounded-xl bg-slate-50 border border-slate-200 text-gray-500">${weekStart} - ${weekEnd}</span>
                <a href="${pageContext.request.contextPath}/customer/my-calendar?weekDate=${nextWeek}<c:if test='${not empty selectedLocationId}'>&locationId=${selectedLocationId}</c:if><c:if test='${not empty selectedFieldId}'>&fieldId=${selectedFieldId}</c:if>"
                   class="px-4 py-2 rounded-xl border border-gray-200 text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Tuần sau</a>
            </div>
        </div>

        <div class="overflow-x-auto pb-4">
            <div class="flex gap-5 min-w-max">
                <c:forEach var="entry" items="${bookingsByDate}">
                    <div class="min-w-[300px] space-y-4">
                        <div class="bg-white p-4 rounded-2xl border-2 border-gray-50 text-center shadow-sm">
                            <p class="text-[10px] font-black text-[#008751] uppercase tracking-[0.2em] opacity-70">${displayDateMap[entry.key]}</p>
                        </div>

                        <c:choose>
                            <c:when test="${empty entry.value}">
                                <div class="bg-gray-50 border-2 border-dashed border-gray-100 rounded-[1.5rem] p-8 text-center">
                                    <p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Không có booking cá nhân</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="space-y-3">
                                    <c:forEach var="b" items="${entry.value}">
                                        <div class="border-2 rounded-[1.5rem] p-4 transition-all border-gray-50 bg-white hover:border-[#008751] hover:shadow-md">
                                            <div class="flex items-start justify-between gap-3 mb-3">
                                                <p class="text-sm font-black text-gray-900 tracking-tight">${b.startTime} - ${b.endTime}</p>
                                                <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.status == 'completed' ? 'bg-emerald-50 text-emerald-700' : (b.status == 'paid' ? 'bg-blue-50 text-blue-700' : (b.status == 'checked in' ? 'bg-purple-50 text-purple-700' : 'bg-amber-50 text-amber-700'))}">${b.status}</span>
                                            </div>

                                            <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Cơ sở</p>
                                            <p class="text-sm font-black text-gray-700 uppercase tracking-tight mt-1">${empty b.locationName ? '--' : b.locationName}</p>

                                            <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Sân</p>
                                            <p class="text-sm font-black text-gray-800 uppercase tracking-tight mt-1">${b.fieldName}</p>

                                            <div class="flex items-center justify-between mt-4">
                                                <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest"><fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> đ</span>
                                                <a href="${pageContext.request.contextPath}/customer/bookingDetail?id=${b.bookingId}" class="px-4 py-2 rounded-xl bg-gray-900 text-white text-[10px] font-black uppercase tracking-widest hover:bg-[#008751] transition-all">Chi tiết</a>
                                            </div>
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
