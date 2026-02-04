<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch thi đấu tuần - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .custom-scrollbar::-webkit-scrollbar { height: 6px; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #008751; }
        .day-column { min-width: 260px; }
        .filter-select {
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right 1rem center;
            background-size: 1.25rem;
            appearance: none;
        }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="flex-grow max-w-[1600px] mx-auto px-6 py-10 space-y-8 w-full">

    <!-- TOP SECTION: TITLE & BREADCRUMB -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div class="space-y-2">
            <a href="javascript:history.back()" 
               class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
                <i data-lucide="arrow-left" class="w-3 h-3"></i>
                QUAY LẠI QUẢN LÝ SÂN
            </a>
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                LỊCH TUẦN: <span class="text-[#008751]">${field.fieldName}</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Bảng điều hành khung giờ thi đấu tiêu chuẩn</p>
        </div>

        <!-- FILTER CARD -->
        <div class="bg-white p-4 rounded-3xl shadow-sm border border-gray-100">
            <form method="get" action="field-schedule" class="flex items-center gap-4">
                <input type="hidden" name="fieldId" value="${field.fieldId}">
                <div class="relative group">
                    <select name="status" onchange="this.form.submit()"
                            class="filter-select min-w-[200px] pl-5 pr-10 py-3 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:border-[#008751] font-black text-[10px] uppercase tracking-widest text-gray-500 cursor-pointer transition-all">
                        <option value="">Tất cả trạng thái</option>
                        <option value="available" ${param.status == 'available' ? 'selected' : ''}>SẴN SÀNG (Available)</option>
                        <option value="booked" ${param.status == 'booked' ? 'selected' : ''}>ĐÃ ĐẶT (Booked)</option>
                        <option value="maintenance" ${param.status == 'maintenance' ? 'selected' : ''}>BẢO TRÌ (Maintenance)</option>
                    </select>
                </div>
                <div class="p-3 bg-emerald-50 text-[#008751] rounded-2xl border border-emerald-100">
                    <i data-lucide="filter" class="w-5 h-5"></i>
                </div>
            </form>
        </div>
    </div>

    <!-- WEEK NAVIGATION BAR -->
    <div class="bg-white px-8 py-6 rounded-[2.5rem] shadow-sm border border-gray-100 flex flex-col md:flex-row items-center justify-between gap-6">
        <a href="?fieldId=${field.fieldId}&date=${prevWeek}&status=${param.status}" 
           class="flex items-center gap-3 px-6 py-3 bg-gray-50 hover:bg-[#008751] hover:text-white rounded-2xl transition-all text-gray-400 font-black text-[10px] uppercase tracking-widest shadow-sm">
            <i data-lucide="chevron-left" class="w-4 h-4 text-current"></i>
            Tuần trước
        </a>
        
        <div class="text-center group">
            <div class="text-[9px] font-black text-gray-400 uppercase tracking-[0.3em] mb-1 opacity-70">GIAI ĐOẠN HIỆN TẠI</div>
            <div class="text-lg font-black text-gray-900 uppercase tracking-tight flex items-center gap-4">
                ${weekStart} 
                <span class="w-10 h-0.5 bg-emerald-100 rounded-full group-hover:bg-[#008751] transition-all"></span> 
                ${weekEnd}
            </div>
        </div>

        <a href="?fieldId=${field.fieldId}&date=${nextWeek}&status=${param.status}" 
           class="flex items-center gap-3 px-6 py-3 bg-gray-50 hover:bg-[#008751] hover:text-white rounded-2xl transition-all text-gray-400 font-black text-[10px] uppercase tracking-widest shadow-sm">
            Tuần sau
            <i data-lucide="chevron-right" class="w-4 h-4 text-current"></i>
        </a>
    </div>

    <!-- SCHEDULE BOARD SCROLL AREA -->
    <div class="overflow-x-auto custom-scrollbar pb-10" id="scrollWrapper">
        <div class="flex gap-6 min-w-max px-2">
            
            <c:forEach items="${schedulesByDate}" var="entry">
                <!-- DAY COLUMN -->
                <div class="day-column space-y-6">
                    <!-- Day Header -->
                    <div class="bg-white p-6 rounded-[2rem] border-2 border-gray-50 shadow-sm flex flex-col items-center text-center group hover:border-[#008751] transition-all">
                        <span class="text-[10px] font-black text-[#008751] uppercase tracking-[0.25em] mb-1 opacity-60">
                            ${displayDateMap[entry.key].split(',')[0]}
                        </span>
                        <h3 class="text-xl font-black text-gray-900 uppercase tracking-tight">
                            ${displayDateMap[entry.key].split(',')[1]}
                        </h3>
                    </div>

                    <!-- Slots List -->
                    <div class="space-y-4">
                        <c:if test="${empty entry.value}">
                            <div class="bg-gray-50/50 border-2 border-dashed border-gray-100 rounded-[2rem] p-12 flex flex-col items-center justify-center text-center opacity-60">
                                <i data-lucide="calendar-x" class="w-10 h-10 text-gray-200 mb-3"></i>
                                <span class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Không có lịch</span>
                            </div>
                        </c:if>

                        <c:forEach items="${entry.value}" var="s">
                            <div class="group bg-white border-2 border-gray-50 rounded-[2.2rem] p-6 shadow-sm hover:shadow-xl transition-all hover:border-[#008751] hover:-translate-y-1 relative overflow-hidden">
                                <!-- Status Line Decor -->
                                <div class="absolute top-0 left-0 w-1.5 h-full transition-all
                                    ${s.status == 'available' ? 'bg-[#008751]' : 
                                      s.status == 'booked' ? 'bg-blue-600' : 'bg-amber-400'}">
                                </div>

                                <div class="flex justify-between items-start mb-6">
                                    <div class="text-lg font-black text-gray-900 group-hover:text-[#008751] transition-colors leading-none tracking-tight">
                                        ${s.startTime} <span class="text-gray-200 mx-1">-</span> ${s.endTime}
                                    </div>
                                    <div class="p-2 bg-gray-50 rounded-xl text-gray-300 group-hover:text-[#008751] group-hover:bg-emerald-50 transition-all">
                                        <i data-lucide="clock" class="w-4 h-4"></i>
                                    </div>
                                </div>

                                <div class="space-y-4">
                                    <div class="flex items-center justify-between">
                                        <span class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Chi phí</span>
                                        <span class="text-sm font-black text-[#008751] tracking-tight">
                                            <fmt:formatNumber value="${s.price}" pattern="#,##0" /> đ
                                        </span>
                                    </div>
                                    <div class="flex items-center justify-between">
                                        <span class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Trạng thái</span>
                                        <div class={`px-3 py-1.5 rounded-full text-[8px] font-black tracking-widest uppercase 
                                            ${s.status == 'available' ? 'bg-emerald-50 text-[#008751]' : 
                                              s.status == 'booked' ? 'bg-blue-50 text-blue-600' : 'bg-amber-50 text-amber-500'}`}>
                                            <c:choose>
                                                <c:when test="${s.status == 'available'}">SẴN SÀNG</c:when>
                                                <c:when test="${s.status == 'booked'}">ĐÃ ĐẶT</c:when>
                                                <c:otherwise>BẢO TRÌ</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:forEach>

        </div>
    </div>

    <!-- LEGEND & DATA INFO -->
    <div class="flex flex-col md:flex-row items-center justify-between py-10 border-t border-gray-100 mt-8 gap-8">
        <div class="flex items-center gap-8">
            <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full bg-[#008751] shadow-lg shadow-[#008751]/30"></div>
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Sân trống</span>
            </div>
            <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full bg-blue-600 shadow-lg shadow-blue-600/30"></div>
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Đã được đặt</span>
            </div>
            <div class="flex items-center gap-3">
                <div class="w-3 h-3 rounded-full bg-amber-400 shadow-lg shadow-amber-400/30"></div>
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Bảo trì/Khóa</span>
            </div>
        </div>
        
        <div class="flex items-center gap-4 bg-emerald-50 px-8 py-4 rounded-3xl border border-emerald-100">
            <i data-lucide="shield-check" class="w-5 h-5 text-[#008751]"></i>
            <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">Dữ liệu đồng bộ tiêu chuẩn FIFA 2026</span>
        </div>
    </div>

</main>
 
<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    // Initialize Lucide Icons
    lucide.createIcons();

    const wrapper = document.getElementById("scrollWrapper");

    wrapper.addEventListener("scroll", () => {
        const rightEdge = wrapper.scrollLeft + wrapper.clientWidth;
        const max = wrapper.scrollWidth;

        if (rightEdge >= max - 2) {
            window.location.href = "?fieldId=${field.fieldId}&date=${nextWeek}&status=${param.status}";
        }

        if (wrapper.scrollLeft <= 2) {
            window.location.href = "?fieldId=${field.fieldId}&date=${prevWeek}&status=${param.status}";
        }
    });
</script>

</body>
</html>