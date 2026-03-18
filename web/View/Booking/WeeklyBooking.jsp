<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"   %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"       prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"  %>
<%@ page import="java.time.LocalDate, java.time.format.DateTimeFormatter, java.util.List, java.util.Locale" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Sân Theo Tuần - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2.5rem; }
        .custom-scrollbar::-webkit-scrollbar { height: 4px; width: 4px; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #008751; border-radius: 10px; }
        .input-focus:focus { border-color: #008751; outline: none; box-shadow: 0 0 0 4px rgba(0,135,81,.05); }
        .field-type-btn.active { border-color: #008751 !important; background-color: #ecfdf5 !important; color: #008751 !important; }
        /* Slot cells */
        .slot-avail { cursor: pointer; transition: all .12s; }
        .slot-avail:hover  { background-color: #ecfdf5; border-color: #34d399 !important; }
        .slot-avail.picked { background-color: #bbf7d0; border-color: #047857 !important; box-shadow: 0 0 0 2px rgba(4,120,87,.22), 0 10px 20px rgba(4,120,87,.14); }
        .slot-avail.picked .slot-icon { color: #065f46; }
        .day-today-header { border-top: 3px solid #008751; }
        .day-column { min-width: 260px; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<%-- Role-aware header --%>
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

<main class="flex-grow max-w-7xl mx-auto px-6 py-12 space-y-10 w-full">

    <%-- ── Title & mode switch ───────────────────────────────────────────────── --%>
    <div class="flex flex-col space-y-8">
        <div class="space-y-2">
            <button type="button" onclick="history.back()"
                    class="w-10 h-10 bg-white rounded-xl border border-gray-100 text-gray-400 hover:text-[#008751] hover:border-[#008751] transition-all flex items-center justify-center">
                <i data-lucide="arrow-left" class="w-5 h-5"></i>
            </button>
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                ĐẶT SÂN <span class="text-[#008751]">THEO TUẦN</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">Chọn nhiều khung giờ trong tuần – tất cả được đặt trong một lần</p>
        </div>

        <%-- Mode card buttons --%>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <a href="${pageContext.request.contextPath}/booking?locationId=${selectedLocationId}&fieldId=${selectedFieldId}"
               class="mode-card elite-card p-6 border-2 border-gray-100 bg-white hover:border-[#008751]/30 transition-all no-underline flex">
                <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-[#008751] shadow-sm">
                        <i data-lucide="zap" class="w-6 h-6"></i>
                    </div>
                    <div class="text-left">
                        <h3 class="font-black text-gray-900 uppercase tracking-tighter">ĐẶT SÂN THƯỜNG</h3>
                        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Khách vãng lai linh hoạt</p>
                    </div>
                </div>
            </a>
            <div class="mode-card elite-card p-6 border-2 border-[#008751] bg-emerald-50">
                <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-[#008751] shadow-sm">
                        <i data-lucide="calendar-range" class="w-6 h-6"></i>
                    </div>
                    <div class="text-left">
                        <h3 class="font-black text-[#008751] uppercase tracking-tighter">ĐẶT SÂN THEO TUẦN</h3>
                        <p class="text-[10px] font-bold text-emerald-600 uppercase tracking-widest">Cố định theo chu kỳ hằng tuần</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- ── Flash messages ────────────────────────────────────────────────────── --%>
    <c:if test="${not empty flashSuccess}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-[#008751] text-white rounded-xl flex items-center justify-center shadow-lg shadow-[#008751]/20 flex-shrink-0">
                <i data-lucide="check" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-[#008751] uppercase tracking-tight">${flashSuccess}</p>
        </div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center flex-shrink-0">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${flashError}</p>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl">
            <p class="text-sm font-bold text-rose-700">${error}</p>
        </div>
    </c:if>

    <%-- ── Main form (POST to /booking/weekly-confirm) ───────────────────────── --%>
    <%-- Filter state (locationId, fieldType, fieldId, weekStart) is ALSO carried in
         this form; when user changes location/fieldType/field we do a GET redirect via JS. --%>
    <form method="post" action="${pageContext.request.contextPath}/booking/weekly-confirm"
          id="weeklyForm" class="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start">

        <%-- Hidden state carried through to POST --%>
        <input type="hidden" name="fieldId"    id="hFieldId"    value="${selectedFieldId}">
        <input type="hidden" name="locationId" id="hLocationId" value="${selectedLocationId}">
        <input type="hidden" name="weekStart"  id="hWeekStart"  value="${weekStart}">
        <input type="hidden" name="weekCount"  id="hWeekCount"  value="${selectedWeekCount}">
        <%-- fieldType is only for GET filter, not needed in POST --%>
        <div id="autoRecurringSelections" class="hidden"></div>
        <div id="persistedCarrySelections" class="hidden"></div>

        <%-- ═══════════════════════════════════════════════════════════
             LEFT PANEL
        ═══════════════════════════════════════════════════════════════ --%>
        <div class="lg:col-span-8 space-y-10">

            <%-- ── STEP 1: Cấu hình sân ──────────────────────────────── --%>
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">1. Cấu hình trận đấu</h2>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

                    <%-- Location select --%>
                    <div class="space-y-3">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Chọn chi nhánh <span class="text-rose-500">*</span></label>
                        <div class="relative">
                            <i data-lucide="map-pin" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300 pointer-events-none"></i>
                            <select id="locationSelect" onchange="filterSubmit()"
                                    class="w-full pl-12 pr-10 py-4 bg-gray-50 border border-gray-100 rounded-2xl appearance-none font-bold text-gray-700 input-focus cursor-pointer">
                                <option value="">-- Chọn địa điểm --</option>
                                <c:forEach var="l" items="${locations}">
                                    <option value="${l.locationId}"
                                        <c:if test="${l.locationId.toString() eq selectedLocationId}">selected</c:if>>
                                        ${not empty l.locationName ? l.locationName : l.address}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <%-- Field type buttons --%>
                    <div class="space-y-3">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Loại sân bóng <span class="text-rose-500">*</span></label>
                        <div class="flex gap-3">
                            <button type="button" data-type="7-a-side"
                                    class="field-type-btn flex-1 py-4 rounded-2xl border-2 font-black text-[10px] uppercase tracking-widest transition-all
                                    ${selectedFieldType eq '7-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                7-A-SIDE
                            </button>
                            <button type="button" data-type="11-a-side"
                                    class="field-type-btn flex-1 py-4 rounded-2xl border-2 font-black text-[10px] uppercase tracking-widest transition-all
                                    ${selectedFieldType eq '11-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                11-A-SIDE
                            </button>
                        </div>
                    </div>

                    <%-- Field specific select (only when location chosen) --%>
                    <c:if test="${not empty selectedLocationId}">
                    <div class="md:col-span-2 space-y-3">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Chọn sân <span class="text-rose-500">*</span></label>
                        <div class="relative">
                            <i data-lucide="layout-grid" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300 pointer-events-none"></i>
                            <select id="fieldSelect" onchange="filterSubmit()"
                                    class="w-full pl-12 pr-10 py-4 bg-gray-50 border border-gray-100 rounded-2xl appearance-none font-bold text-gray-700 input-focus cursor-pointer">
                                <option value="">-- Chọn sân cụ thể --</option>
                                <c:forEach var="f" items="${fields}">
                                    <option value="${f.fieldId}"
                                        <c:if test="${f.fieldId.toString() eq selectedFieldId}">selected</c:if>>
                                        ${f.fieldName} (${f.fieldType})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    </c:if>
                </div>
            </section>

            <%-- ── STEP 2: Chọn lịch theo tuần ──────────────────────────── --%>
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="bg-white px-6 py-5 rounded-[2rem] shadow-sm border border-gray-100 flex items-center justify-between flex-wrap gap-4">
                    <div class="flex items-center gap-4">
                        <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">2. Chọn lịch theo tuần</h2>
                    </div>
                    <c:if test="${not empty selectedFieldId}">
                    <%-- Week navigation --%>
                    <div class="flex items-center gap-3">
                        <a href="${pageContext.request.contextPath}/booking/weekly?locationId=${selectedLocationId}&fieldType=${selectedFieldType}&fieldId=${selectedFieldId}&weekStart=${prevWeekStart}&weekCount=${selectedWeekCount}"
                           data-week-start="${prevWeekStart}"
                                    class="week-nav-link w-9 h-9 flex items-center justify-center bg-gray-50 border border-gray-200 rounded-xl hover:border-[#008751] hover:text-[#008751] transition-all text-gray-500">
                            <i data-lucide="chevron-left" class="w-4 h-4"></i>
                        </a>
                        <%
                            java.time.LocalDate ws = java.time.LocalDate.parse((String) request.getAttribute("weekStart"));
                            java.time.LocalDate we = java.time.LocalDate.parse((String) request.getAttribute("weekEnd"));
                            java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
                        %>
                        <span class="text-sm font-black text-gray-700 whitespace-nowrap">
                            <%= ws.format(df) %> – <%= we.format(df) %>
                        </span>
                        <span class="px-2.5 py-1 rounded-full bg-emerald-50 text-[#008751] text-[10px] font-black uppercase tracking-widest">
                            ${selectedWeekCount} tuần
                        </span>
                                <a href="${pageContext.request.contextPath}/booking/weekly?locationId=${selectedLocationId}&fieldType=${selectedFieldType}&fieldId=${selectedFieldId}&weekStart=${nextWeekStart}&weekCount=${selectedWeekCount}"
                                    data-week-start="${nextWeekStart}"
                                    class="week-nav-link w-9 h-9 flex items-center justify-center bg-gray-50 border border-gray-200 rounded-xl hover:border-[#008751] hover:text-[#008751] transition-all text-gray-500">
                            <i data-lucide="chevron-right" class="w-4 h-4"></i>
                        </a>
                    </div>
                    </c:if>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Số tuần đặt lặp</label>
                        <select id="weekCountSelect" onchange="filterSubmit()"
                                class="w-full px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl font-black text-xs text-gray-700 input-focus cursor-pointer uppercase tracking-widest">
                            <c:forEach begin="4" end="12" var="wc">
                                <option value="${wc}" <c:if test="${wc == selectedWeekCount}">selected</c:if>>${wc} tuần</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Quy tắc auto chọn</label>
                        <div class="w-full px-4 py-3 bg-emerald-50 border border-emerald-100 rounded-2xl text-[11px] font-bold text-emerald-700 leading-relaxed">
                            Chọn 1 ca ở tuần đầu -> hệ thống tự chọn cùng thứ và cùng giờ cho các tuần tiếp theo nếu còn trống.
                        </div>
                    </div>
                </div>
                <div id="autoRecurringNotice" class="text-[10px] font-bold text-gray-500"></div>

                <c:choose>
                <c:when test="${empty selectedFieldId}">
                    <%-- Prompt --%>
                    <div class="py-12 text-center bg-gray-50 rounded-[2rem] border-2 border-dashed border-gray-100">
                        <i data-lucide="calendar-range" class="w-12 h-12 text-gray-200 mx-auto mb-4"></i>
                        <p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Vui lòng chọn đầy đủ chi nhánh &amp; sân để xem lịch</p>
                    </div>
                </c:when>
                <c:when test="${empty gridRows}">
                    <%-- No schedules this week --%>
                    <div class="py-10 text-center bg-gray-50 rounded-[2rem] border-2 border-dashed border-gray-100 space-y-4">
                        <i data-lucide="calendar-x" class="w-12 h-12 text-gray-200 mx-auto"></i>
                        <p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Không có lịch trong tuần này</p>
                        <div class="flex justify-center gap-3">
                                     <a href="${pageContext.request.contextPath}/booking/weekly?locationId=${selectedLocationId}&fieldType=${selectedFieldType}&fieldId=${selectedFieldId}&weekStart=${prevWeekStart}&weekCount=${selectedWeekCount}"
                                 data-week-start="${prevWeekStart}"
                                 class="week-nav-link px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-600 rounded-2xl text-xs font-bold transition-all">← Tuần trước</a>
                                     <a href="${pageContext.request.contextPath}/booking/weekly?locationId=${selectedLocationId}&fieldType=${selectedFieldType}&fieldId=${selectedFieldId}&weekStart=${nextWeekStart}&weekCount=${selectedWeekCount}"
                                 data-week-start="${nextWeekStart}"
                                 class="week-nav-link px-5 py-2.5 bg-[#008751] hover:bg-[#006d41] text-white rounded-2xl text-xs font-bold transition-all">Tuần sau →</a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <%-- Select-all / clear --%>
                    <div class="flex items-center gap-3 flex-wrap">
                        <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Chọn nhanh:</span>
                        <button type="button" onclick="selectAll()"
                                class="px-4 py-2 text-[10px] font-black uppercase tracking-widest border-2 border-[#008751] text-[#008751] rounded-xl hover:bg-emerald-50 transition-all">
                            Tất cả khả dụng
                        </button>
                        <button type="button" onclick="clearAll()"
                                class="px-4 py-2 text-[10px] font-black uppercase tracking-widest border-2 border-gray-300 text-gray-500 rounded-xl hover:bg-gray-50 transition-all">
                            Bỏ chọn
                        </button>

                        <%-- Legend --%>
                        <span class="ml-auto flex items-center gap-4 text-[10px] font-bold text-gray-500">
                            <span class="flex items-center gap-1.5"><span class="w-4 h-4 rounded bg-emerald-100 border-2 border-emerald-400 inline-block"></span>Có thể đặt</span>
                            <span class="flex items-center gap-1.5"><span class="w-4 h-4 rounded bg-rose-50 border-2 border-rose-300 inline-block"></span>Đã đặt</span>
                            <span class="flex items-center gap-1.5"><span class="w-4 h-4 rounded bg-gray-100 border-2 border-gray-200 inline-block"></span>Đã qua</span>
                        </span>
                    </div>

                    <%-- Weekly board (FieldSchedule-like day columns) --%>
                    <div class="overflow-x-auto custom-scrollbar pb-6" id="weeklyScroll">
                        <div class="flex gap-6 min-w-max px-2">
                            <c:forEach var="d" items="${weekDates}" varStatus="dSt">
                                <div class="day-column space-y-4">
                                    <div class="bg-white p-5 rounded-[1.8rem] border-2 border-gray-50 shadow-sm flex flex-col items-center text-center hover:border-[#008751] transition-all">
                                        <span class="text-[10px] font-black text-[#008751] uppercase tracking-[0.25em] opacity-70">
                                            <%= ((java.time.LocalDate) pageContext.findAttribute("d")).format(java.time.format.DateTimeFormatter.ofPattern("EEE", new Locale("vi","VN"))) %>
                                        </span>
                                        <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight mt-1">
                                            ${fn:substring(d,8,10)}/${fn:substring(d,5,7)}
                                        </h3>
                                    </div>

                                    <div class="space-y-3">
                                        <c:forEach var="row" items="${gridRows}">
                                            <c:set var="cell" value="${row.cells[dSt.index]}"/>
                                            <c:choose>
                                                <c:when test="${not cell.exists}">
                                                    <div class="bg-gray-50 border-2 border-dashed border-gray-100 rounded-[1.3rem] p-4 text-center opacity-70">
                                                        <span class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Không có lịch</span>
                                                    </div>
                                                </c:when>
                                                <c:when test="${cell.available}">
                                                    <label class="slot-avail block border-2 rounded-[1.4rem] p-4 bg-white shadow-sm ${cell.selected ? 'picked border-[#047857] bg-emerald-100/70' : 'border-gray-50 hover:border-[#008751] hover:shadow-xl'}"
                                                           for="sc_${cell.scheduleId}">
                                                        <input type="checkbox" id="sc_${cell.scheduleId}"
                                                               name="scheduleIds" value="${cell.scheduleId}"
                                                               data-date="${cell.bookingDate}"
                                                               data-start="${cell.startTime}"
                                                               class="slot-cb sr-only"
                                                               <c:if test="${cell.selected}">checked</c:if>
                                                               onchange="onCellChange(this)">
                                                        <div class="flex justify-between items-start mb-3">
                                                            <div class="text-sm font-black text-gray-900 tracking-tight">${row.startTime} - ${row.endTime}</div>
                                                            <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest bg-emerald-50 text-[#008751]">available</span>
                                                        </div>
                                                        <div class="flex items-center justify-between">
                                                            <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest"><fmt:formatNumber value="${cell.price}" pattern="#,##0"/>đ</span>
                                                            <i data-lucide="${cell.selected ? 'check-square' : 'clock'}" class="w-4 h-4 ${cell.selected ? 'text-[#065f46]' : 'text-gray-300'} slot-icon"></i>
                                                        </div>
                                                    </label>
                                                </c:when>
                                                <c:when test="${not cell.past}">
                                                    <div class="border-2 border-rose-200 bg-rose-50 rounded-[1.4rem] p-4 opacity-85">
                                                        <div class="flex justify-between items-start mb-3">
                                                            <div class="text-sm font-black text-rose-700 tracking-tight">${row.startTime} - ${row.endTime}</div>
                                                            <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest bg-rose-100 text-rose-600">booked</span>
                                                        </div>
                                                        <div class="flex items-center justify-between">
                                                            <span class="text-[10px] font-black text-rose-600 uppercase tracking-widest">Đã đặt</span>
                                                            <i data-lucide="x" class="w-4 h-4 text-rose-400"></i>
                                                        </div>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="border-2 border-gray-200 bg-gray-100 rounded-[1.4rem] p-4 opacity-80">
                                                        <div class="flex justify-between items-start mb-3">
                                                            <div class="text-sm font-black text-gray-500 tracking-tight">${row.startTime} - ${row.endTime}</div>
                                                            <span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest bg-gray-200 text-gray-500">past</span>
                                                        </div>
                                                        <div class="flex items-center justify-between">
                                                            <span class="text-[10px] font-black text-gray-500 uppercase tracking-widest">Đã qua</span>
                                                            <i data-lucide="clock" class="w-4 h-4 text-gray-400"></i>
                                                        </div>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
                </c:choose>
            </section>

            <%-- ── STEP 3: Equipment (optional) ──────────────────────────── --%>
            <c:if test="${not empty selectedLocationId}">
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">
                        3. Dịch vụ &amp; Vật tư <span class="text-gray-300 ml-2">(Tùy chọn)</span>
                    </h2>
                </div>

                <c:choose>
                <c:when test="${empty equipments}">
                    <div class="py-10 text-center bg-gray-50 rounded-[2rem] border-2 border-dashed border-gray-100">
                        <p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Chưa có vật tư tại chi nhánh này</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="overflow-hidden rounded-2xl border border-gray-100">
                        <table class="w-full text-left">
                            <thead class="bg-gray-50">
                                <tr>
                                    <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest">Tên vật tư</th>
                                    <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest text-center">Giá thuê / ca</th>
                                    <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest text-center">Tồn kho</th>
                                    <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest text-right">Số lượng / ca</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-gray-50">
                                <c:forEach var="e" items="${equipments}">
                                <tr class="hover:bg-gray-50/50 transition-colors">
                                    <td class="px-6 py-4 font-bold text-gray-700 text-sm">${e.name}</td>
                                    <td class="px-6 py-4 text-center font-black text-[#008751] text-xs">
                                        <fmt:formatNumber value="${e.rentalPrice}" pattern="#,##0"/> đ
                                    </td>
                                    <td class="px-6 py-4 text-center font-bold text-gray-400 text-xs">${e.quantity}</td>
                                    <td class="px-6 py-4 text-right">
                                        <input type="number" name="equipment_${e.equipmentId}"
                                               data-unit-price="${e.rentalPrice}"
                                               min="0" max="${e.quantity}" value="0"
                                               class="equipment-qty w-20 px-3 py-2 bg-gray-100 border-none rounded-xl text-center font-black text-gray-700 focus:bg-white focus:ring-2 focus:ring-[#008751]/20">
                                    </td>
                                </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <p class="text-[10px] text-amber-600 font-bold">
                        <i data-lucide="info" class="w-3 h-3 inline-block mr-1"></i>
                        Số lượng nhập là <strong>mỗi ca</strong>. Tổng tồn kho cần đủ cho tất cả ca được chọn.
                    </p>
                </c:otherwise>
                </c:choose>
            </section>
            </c:if>

            <c:if test="${sessionScope.user.role.roleName eq 'STAFF' or sessionScope.user.role.roleName eq 'staff'}">
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">4. Thông tin khách vãng lai</h2>
                </div>
                <div class="space-y-3">
                    <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Số điện thoại khách <span class="text-rose-500">*</span></label>
                    <div class="relative">
                        <i data-lucide="phone" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300 pointer-events-none"></i>
                        <input type="text" name="bookingPhone" maxlength="20" required
                               value="${param.bookingPhone}"
                               placeholder="Nhập số điện thoại khách"
                               class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl font-bold text-gray-700 input-focus" />
                    </div>
                </div>
            </section>
            </c:if>

            <%-- ── STEP 4: Voucher (optional) ─────────────────────────────── --%>
            <c:if test="${not empty vouchers}">
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">4. Ưu đãi Voucher</h2>
                </div>
                <div class="relative">
                    <i data-lucide="ticket" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#008751] pointer-events-none"></i>
                    <select name="voucherId" id="voucherId"
                            onchange="updateSummary()"
                            class="w-full pl-12 pr-10 py-4 bg-emerald-50/30 border border-emerald-100 rounded-2xl appearance-none font-black text-[#008751] text-xs uppercase tracking-widest input-focus cursor-pointer">
                        <option value="" data-discount="0">-- KHÔNG SỬ DỤNG VOUCHER --</option>
                        <c:forEach var="v" items="${vouchers}">
                            <option value="${v.voucherId}" data-discount="${v.discountValue}">${v.code} – GIẢM ${v.discountValue}%</option>
                        </c:forEach>
                    </select>
                </div>
            </section>
            </c:if>

        </div><%-- end left --%>

        <%-- ═══════════════════════════════════════════════════════════
             RIGHT PANEL — sticky payment summary
        ═══════════════════════════════════════════════════════════════ --%>
        <div class="lg:col-span-4 sticky top-28">
            <div class="bg-[#166534] text-white elite-card shadow-2xl shadow-[#166534]/20 p-10 space-y-10 relative overflow-hidden group">
                <div class="absolute -top-6 -right-6 opacity-10 group-hover:scale-110 transition-transform duration-700">
                    <i data-lucide="calendar-range" class="w-40 h-40"></i>
                </div>

                <div class="relative z-10 space-y-6">
                    <div class="flex items-center gap-4">
                        <div class="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center border border-white/20">
                            <i data-lucide="wallet" class="w-5 h-5 text-emerald-300"></i>
                        </div>
                        <h2 class="text-xl font-black uppercase tracking-widest">Tóm tắt</h2>
                    </div>

                    <div class="space-y-4 pt-6 border-t border-white/10">
                        <div class="flex justify-between text-[10px] font-black uppercase tracking-widest opacity-60">
                            <span>Chế độ đặt</span>
                            <span class="text-emerald-300">THEO TUẦN</span>
                        </div>
                        <div class="flex justify-between items-start gap-4">
                            <span class="text-[10px] font-black uppercase tracking-widest opacity-60">Sân bóng</span>
                            <span id="sumField" class="text-sm font-bold text-right">${not empty selectedField ? selectedField.fieldName : '—'}</span>
                        </div>
                        <div class="flex justify-between items-start gap-4">
                            <span class="text-[10px] font-black uppercase tracking-widest opacity-60">Tuần</span>
                            <span class="text-sm font-bold text-right">
                                <c:choose>
                                    <c:when test="${not empty weekStart and not empty weekEnd}">
                                        ${weekStart} – ${weekEnd}
                                    </c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="flex justify-between items-start gap-4">
                            <span class="text-[10px] font-black uppercase tracking-widest opacity-60">Số ca đã chọn</span>
                            <span id="sumCount" class="text-sm font-black text-emerald-300">0</span>
                        </div>
                    </div>

                    <div class="space-y-4 pt-6 border-t border-white/10">
                        <div class="flex justify-between text-sm">
                            <span class="font-medium opacity-80">Tiền thuê sân</span>
                            <span id="sumFieldPrice" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-sm">
                            <span class="font-medium opacity-80">Vật tư phụ trợ</span>
                            <span id="sumEquipPrice" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-sm">
                            <span class="font-medium opacity-80">Giảm giá voucher</span>
                            <span id="sumDiscount" class="font-black text-emerald-300">0%</span>
                        </div>
                    </div>

                    <div class="pt-8 mt-4 border-t-2 border-white/20">
                        <div class="flex flex-col gap-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.3em] text-emerald-400">TỔNG CỘNG (CÁC CA)</span>
                            <span id="sumTotal" class="text-4xl font-black tracking-tighter leading-none">0 đ</span>
                        </div>
                    </div>

                    <button type="submit" id="submitBtn"
                            disabled
                            onclick="return confirmSubmit()"
                            class="w-full bg-gray-500/50 text-white/50 py-6 rounded-[1.8rem] font-black text-xs uppercase tracking-[0.2em] transition-all flex items-center justify-center gap-3 mt-4 cursor-not-allowed">
                        <i data-lucide="shield-check" class="w-4 h-4"></i>
                        XÁC NHẬN ĐẶT SÂN TUẦN
                    </button>
                    <p id="noSelectMsg" class="text-[9px] text-center font-bold text-rose-300 uppercase tracking-widest hidden">Vui lòng chọn ít nhất 1 khung giờ</p>
                    <p class="text-[9px] text-center font-bold text-emerald-200/50 uppercase tracking-widest mt-2">2 giờ để thanh toán sau khi đặt</p>
                </div>
            </div>
        </div><%-- end right --%>

    </form><%-- end weeklyForm --%>

</main>

<%-- Embed slot prices as JSON for JS --%>
<script id="slotPricesData" type="application/json">
{
<c:forEach var="row" items="${gridRows}" varStatus="rSt">
    <c:forEach var="cell" items="${row.cells}" varStatus="cSt">
        <c:if test="${cell.available}">"${cell.scheduleId}":${cell.price}<c:if test="${not (rSt.last and cSt.last)}">,</c:if></c:if>
    </c:forEach>
</c:forEach>
}
</script>

<script id="allRangeSchedulesData" type="application/json">
[
<c:forEach var="s" items="${allRangeSchedules}" varStatus="st">
{"id":"${s.scheduleId}","date":"${s.bookingDate}","start":"${s.startTime}","status":"${s.status}","price":${s.price}}<c:if test="${not st.last}">,</c:if>
</c:forEach>
]
</script>

<script id="selectedScheduleIdsData" type="application/json">
[
<c:forEach var="sid" items="${selectedScheduleIds}" varStatus="st">"${sid}"<c:if test="${not st.last}">,</c:if></c:forEach>
]
</script>

<script id="selectedSchedulePricesData" type="application/json">
{
<c:forEach var="entry" items="${selectedSchedulePrices}" varStatus="st">"${entry.key}":${entry.value}<c:if test="${not st.last}">,</c:if></c:forEach>
}
</script>

<script id="anchorScheduleIdsData" type="application/json">
[
<c:forEach var="sid" items="${anchorScheduleIds}" varStatus="st">"${sid}"<c:if test="${not st.last}">,</c:if></c:forEach>
]
</script>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
(function () {
    lucide.createIcons();

    // ── slot price map ─────────────────────────────────────────────────────────
    var rawPricesJson = (document.getElementById('slotPricesData') || {}).textContent || '{}';
    var slotPrices = {};
    try { slotPrices = JSON.parse(rawPricesJson); } catch(e) {}

    var rawAllSchedulesJson = (document.getElementById('allRangeSchedulesData') || {}).textContent || '[]';
    var allRangeSchedules = [];
    try { allRangeSchedules = JSON.parse(rawAllSchedulesJson); } catch(e) {}

    var rawSelectedIdsJson = (document.getElementById('selectedScheduleIdsData') || {}).textContent || '[]';
    var persistedSelectedIds = {};
    try {
        JSON.parse(rawSelectedIdsJson).forEach(function(id) {
            if (id) persistedSelectedIds[String(id)] = true;
        });
    } catch (e) {}

    var rawSelectedPricesJson = (document.getElementById('selectedSchedulePricesData') || {}).textContent || '{}';
    try {
        var selectedPrices = JSON.parse(rawSelectedPricesJson);
        Object.keys(selectedPrices).forEach(function(id) {
            slotPrices[String(id)] = parseNum(selectedPrices[id]);
        });
    } catch (e) {}

    var rawAnchorIdsJson = (document.getElementById('anchorScheduleIdsData') || {}).textContent || '[]';
    var persistedAnchorIds = {};
    try {
        JSON.parse(rawAnchorIdsJson).forEach(function(id) {
            if (id) persistedAnchorIds[String(id)] = true;
        });
    } catch (e) {}

    function normalizeTime(raw) {
        if (!raw) return '';
        var t = String(raw);
        return t.length >= 5 ? t.substring(0, 5) : t;
    }

    var scheduleByDateStart = {};
    var scheduleById = {};
    allRangeSchedules.forEach(function(s) {
        var id = String(s.id || '');
        var date = String(s.date || '');
        var start = normalizeTime(s.start);
        if (id && !(id in slotPrices)) slotPrices[id] = parseNum(s.price);
        if (id) scheduleById[id] = s;
        if (date && start) scheduleByDateStart[date + '|' + start] = s;
    });

    // ── Helpers ───────────────────────────────────────────────────────────────
    function getChecked() { return Array.from(document.querySelectorAll('.slot-cb:checked')); }
    function getAutoSelectedIds() {
        return Array.from(document.querySelectorAll('#autoRecurringSelections input[name="scheduleIds"]')).map(function(inp){
            return inp.value;
        });
    }
    function getCarrySelectedIds() {
        return Array.from(document.querySelectorAll('#persistedCarrySelections input[name="scheduleIds"]')).map(function(inp){
            return inp.value;
        });
    }
    function unique(arr) {
        var out = [];
        var seen = {};
        arr.forEach(function(id) {
            var k = String(id);
            if (!seen[k]) { seen[k] = true; out.push(k); }
        });
        return out;
    }
    function getAllSubmittedScheduleIds() {
        return unique(
            getChecked().map(function(cb){ return cb.value; })
                .concat(getAutoSelectedIds())
                .concat(getCarrySelectedIds())
        );
    }
    function getVisibleSlotIds() {
        return Array.from(document.querySelectorAll('.slot-cb')).map(function(cb){ return cb.value; });
    }
    function fmt(v) { return Math.round(v).toLocaleString('vi-VN') + ' đ'; }
    function parseNum(v) { var n = parseFloat(String(v).replace(/[^0-9.-]/g,'')); return isNaN(n)?0:n; }
    function pad2(n) { return n < 10 ? '0' + n : String(n); }
    function addDays(dateStr, days) {
        var p = String(dateStr || '').split('-');
        if (p.length !== 3) return '';
        var d = new Date(parseInt(p[0], 10), parseInt(p[1], 10) - 1, parseInt(p[2], 10));
        if (isNaN(d.getTime())) return '';
        d.setDate(d.getDate() + days);
        return d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate());
    }

    function rebuildAutoRecurringSelections() {
        var container = document.getElementById('autoRecurringSelections');
        var notice = document.getElementById('autoRecurringNotice');
        if (!container) return;

        container.innerHTML = '';

        var weekCountSel = document.getElementById('weekCountSelect');
        var weekCount = parseInt(weekCountSel ? weekCountSel.value : '4', 10);
        if (isNaN(weekCount) || weekCount < 4) weekCount = 4;

        var hWeekCount = document.getElementById('hWeekCount');
        if (hWeekCount) hWeekCount.value = String(weekCount);

        var checked = getChecked();
        var baseIds = {};
        checked.forEach(function(cb) { baseIds[String(cb.value)] = true; });

        var autoIds = {};
        var skipped = 0;

        Object.keys(persistedAnchorIds).forEach(function(anchorId) {
            var anchor = scheduleById[String(anchorId)];
            if (!anchor) return;

            var baseDate = String(anchor.date || '');
            var baseStart = normalizeTime(anchor.start);
            if (!baseDate || !baseStart) return;

            for (var w = 1; w < weekCount; w++) {
                var targetDate = addDays(baseDate, w * 7);
                if (!targetDate) continue;

                var target = scheduleByDateStart[targetDate + '|' + baseStart];
                if (!target) { skipped++; continue; }

                var targetStatus = String(target.status || '').toLowerCase();
                var targetId = String(target.id || '');
                if (targetStatus !== 'available' || !targetId || baseIds[targetId]) {
                    skipped++;
                    continue;
                }
                autoIds[targetId] = true;
            }
        });

        Object.keys(autoIds).forEach(function(id) {
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'scheduleIds';
            input.value = id;
            container.appendChild(input);
        });

        if (notice) {
            var autoCount = Object.keys(autoIds).length;
            if (autoCount > 0 || skipped > 0) {
                notice.textContent = 'Tự động thêm ' + autoCount + ' ca cho các tuần tiếp theo' + (skipped > 0 ? ('; bỏ qua ' + skipped + ' ca không còn trống') : '') + '.';
                notice.className = 'text-[10px] font-bold text-emerald-700';
            } else {
                notice.textContent = 'Chọn ca ở tuần đầu để hệ thống tự thêm các tuần tiếp theo.';
                notice.className = 'text-[10px] font-bold text-gray-500';
            }
        }
    }

    function buildSelectedIdsForNavigation() {
        var merged = {};
        Object.keys(persistedSelectedIds).forEach(function(id) { merged[id] = true; });

        // Reconcile current visible week with user edits (allow unselect on this week)
        getVisibleSlotIds().forEach(function(id) { delete merged[id]; });
        getChecked().forEach(function(cb) { merged[String(cb.value)] = true; });
        getAutoSelectedIds().forEach(function(id) { merged[String(id)] = true; });

        return Object.keys(merged);
    }

    function buildAnchorIdsForNavigation() {
        return Object.keys(persistedAnchorIds);
    }

    function rebuildCarrySelections() {
        var container = document.getElementById('persistedCarrySelections');
        if (!container) return;
        container.innerHTML = '';

        var visibleCheckedMap = {};
        getChecked().forEach(function(cb) { visibleCheckedMap[String(cb.value)] = true; });

        var autoMap = {};
        getAutoSelectedIds().forEach(function(id) { autoMap[String(id)] = true; });

        buildSelectedIdsForNavigation().forEach(function(id) {
            var sid = String(id);
            if (visibleCheckedMap[sid] || autoMap[sid]) return;
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'scheduleIds';
            input.value = sid;
            container.appendChild(input);
        });
    }

    function syncPersistedSelectionSet() {
        var next = {};
        buildSelectedIdsForNavigation().forEach(function(id) { next[id] = true; });
        persistedSelectedIds = next;
    }

    function updateSummary() {
        var selectedIds = getAllSubmittedScheduleIds();
        var count       = selectedIds.length;
        var fieldSum    = selectedIds.reduce(function(s, id){ return s + (slotPrices[id] || 0); }, 0);
        var equipSum  = 0;
        document.querySelectorAll('.equipment-qty').forEach(function(inp){
            equipSum += (parseInt(inp.value,10)||0) * parseNum(inp.dataset.unitPrice);
        });
        // equipment cost x sessions
        var equipTotal = equipSum * count;

        var discount = 0;
        var vSel = document.getElementById('voucherId');
        if (vSel) discount = parseNum((vSel.options[vSel.selectedIndex]||{}).getAttribute ? vSel.options[vSel.selectedIndex].getAttribute('data-discount') : '0');

        var fieldAfterDiscount = fieldSum * (1 - discount/100);
        var grandTotal = fieldAfterDiscount + equipTotal;

        document.getElementById('sumCount').textContent     = count;
        document.getElementById('sumFieldPrice').textContent = fmt(fieldSum);
        document.getElementById('sumEquipPrice').textContent = fmt(equipTotal);
        document.getElementById('sumDiscount').textContent  = discount + '%';
        document.getElementById('sumTotal').textContent     = fmt(grandTotal);

        var btn = document.getElementById('submitBtn');
        var msg = document.getElementById('noSelectMsg');
        if (count > 0) {
            btn.disabled = false;
            btn.className = 'w-full bg-[#008751] hover:bg-emerald-400 text-white py-6 rounded-[1.8rem] font-black text-xs uppercase tracking-[0.2em] shadow-2xl transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-3 mt-4 cursor-pointer';
            if (msg) msg.classList.add('hidden');
        } else {
            btn.disabled = true;
            btn.className = 'w-full bg-gray-500/50 text-white/50 py-6 rounded-[1.8rem] font-black text-xs uppercase tracking-[0.2em] transition-all flex items-center justify-center gap-3 mt-4 cursor-not-allowed';
        }
    }

    window.updateSummary = updateSummary;

    function setCellVisual(cb, checked) {
        var label = cb.closest('label');
        if (!label) return;
        var icon  = label.querySelector('.slot-icon');
        if (checked) {
            label.classList.add('picked');
            if (icon) { icon.setAttribute('data-lucide','check-square'); }
        } else {
            label.classList.remove('picked');
            if (icon) { icon.setAttribute('data-lucide','check'); }
        }
        lucide.createIcons({ nodes: icon ? [icon] : [] });
    }

    window.onCellChange = function(cb) {
        var sid = String(cb.value);
        if (cb.checked) persistedAnchorIds[sid] = true;
        else delete persistedAnchorIds[sid];

        setCellVisual(cb, cb.checked);
        rebuildAutoRecurringSelections();
        syncPersistedSelectionSet();
        rebuildCarrySelections();
        updateSummary();
    };

    window.selectAll = function() {
        document.querySelectorAll('.slot-cb').forEach(function(cb){
            if (!cb.checked) { cb.checked = true; }
            persistedAnchorIds[String(cb.value)] = true;
            setCellVisual(cb, true);
        });
        rebuildAutoRecurringSelections();
        syncPersistedSelectionSet();
        rebuildCarrySelections();
        updateSummary();
    };

    window.clearAll = function() {
        document.querySelectorAll('.slot-cb').forEach(function(cb){
            cb.checked = false; setCellVisual(cb, false);
            delete persistedAnchorIds[String(cb.value)];
        });
        rebuildAutoRecurringSelections();
        syncPersistedSelectionSet();
        rebuildCarrySelections();
        updateSummary();
    };

    // ── Field type buttons ─────────────────────────────────────────────────────
    document.querySelectorAll('.field-type-btn').forEach(function(btn) {
        btn.addEventListener('click', function(){
            var type = this.dataset.type;
            var url = new URL(window.location.href);
            url.searchParams.set('fieldType', type);
            url.searchParams.delete('fieldId'); // reset field selection
            window.location.href = url.toString();
        });
    });

    // ── Location / Field dropdowns trigger GET reload ─────────────────────────
    window.filterSubmit = function() {
        var locationSel = document.getElementById('locationSelect');
        var fieldSel    = document.getElementById('fieldSelect');
        var baseUrl = '${pageContext.request.contextPath}/booking/weekly';
        var params  = new URLSearchParams();

        var locVal  = locationSel ? locationSel.value : '';
        var fieldVal = fieldSel  ? fieldSel.value  : '';
        var ft = '${selectedFieldType}';
        var ws = '${weekStart}';
        var wcSel = document.getElementById('weekCountSelect');
        var wc = wcSel ? wcSel.value : '${selectedWeekCount}';

        if (locVal)   params.set('locationId', locVal);
        if (ft)       params.set('fieldType',  ft);
        if (fieldVal) params.set('fieldId',    fieldVal);
        if (ws)       params.set('weekStart',  ws);
        if (wc)       params.set('weekCount',  wc);
        var selectedIds = buildSelectedIdsForNavigation();
        if (selectedIds.length > 0) params.set('selectedIds', selectedIds.join(','));
        var anchorIds = buildAnchorIdsForNavigation();
        if (anchorIds.length > 0) params.set('anchorIds', anchorIds.join(','));
        window.location.href = baseUrl + '?' + params.toString();
    };

    document.querySelectorAll('.week-nav-link').forEach(function(link) {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            var nextUrl = new URL(this.href, window.location.origin);
            var selectedIds = buildSelectedIdsForNavigation();
            if (selectedIds.length > 0) nextUrl.searchParams.set('selectedIds', selectedIds.join(','));
            else nextUrl.searchParams.delete('selectedIds');
            var anchorIds = buildAnchorIdsForNavigation();
            if (anchorIds.length > 0) nextUrl.searchParams.set('anchorIds', anchorIds.join(','));
            else nextUrl.searchParams.delete('anchorIds');
            window.location.href = nextUrl.toString();
        });
    });

    // ── Equipment qty changes re-calc summary ────────────────────────────────
    document.querySelectorAll('.equipment-qty').forEach(function(inp){
        inp.addEventListener('input', updateSummary);
    });

    window.confirmSubmit = function() {
        var count = getAllSubmittedScheduleIds().length;
        if (count === 0) {
            document.getElementById('noSelectMsg').classList.remove('hidden');
            return false;
        }
        var total = parseFloat(document.getElementById('sumTotal').textContent.replace(/[^0-9.-]/g,''))||0;
        return confirm(
            'Bạn đã chọn ' + count + ' khung giờ trong tuần.\n' +
            'Tổng tiền: ' + document.getElementById('sumTotal').textContent + '\n\n' +
            'Xác nhận đặt sân theo tuần?\nMỗi ca có 2 giờ để thanh toán sau khi đặt.'
        );
    };

    // Re-check slots persisted from previous week page and keep visual state in sync.
    document.querySelectorAll('.slot-cb').forEach(function(cb) {
        if (persistedSelectedIds[String(cb.value)]) {
            cb.checked = true;
            setCellVisual(cb, true);
        }
    });

    rebuildAutoRecurringSelections();
    syncPersistedSelectionSet();
    rebuildCarrySelections();
    updateSummary();
})();
</script>
</body>
</html>
