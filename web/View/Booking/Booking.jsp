<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Sân - FIFAFIELD 2026</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .custom-scrollbar::-webkit-scrollbar { height: 4px; width: 4px; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #008751; border-radius: 10px; }
        .slot-card.selected { border-color: #008751; background-color: #ecfdf5; }
        .input-focus:focus { border-color: #008751; outline: none; box-shadow: 0 0 0 4px rgba(0, 135, 81, 0.05); }
    </style>
</head>
<body class="antialiased text-gray-900 h-screen flex flex-col overflow-hidden">

    <c:set var="dashboardPath" value="/customer/dashboard" />
    <c:set var="roleNameLower" value="${fn:toLowerCase(sessionScope.user.role.roleName)}" />
    <c:choose>
        <c:when test="${roleNameLower eq 'staff'}">
            <c:set var="dashboardPath" value="/staff/dashboard" />
        </c:when>
        <c:when test="${roleNameLower eq 'manager'}">
            <c:set var="dashboardPath" value="/manager/dashboard" />
        </c:when>
    </c:choose>

    <c:set var="bookingPaymentOption" value="${empty param.bookingPaymentOption ? 'full' : fn:toLowerCase(param.bookingPaymentOption)}" />

    <!-- HEADER BAR -->
    <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-end px-6 shrink-0 z-20">
        <div class="flex gap-2">
            <button type="button" class="booking-mode-btn px-4 py-2 rounded-xl border-2 flex items-center gap-2 transition-all ${empty param.bookingMode || param.bookingMode == 'normal' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-100 bg-white text-gray-400'}" data-mode="normal">
                <i data-lucide="zap" class="w-4 h-4"></i>
                <span class="text-[10px] font-black uppercase tracking-widest">Đặt sân thường</span>
            </button>
            <a href="${pageContext.request.contextPath}/booking/weekly<c:if test="${not empty param.locationId}">?locationId=${param.locationId}<c:if test="${not empty param.fieldId}">&amp;fieldId=${param.fieldId}</c:if></c:if>"
               class="px-4 py-2 rounded-xl border-2 flex items-center gap-2 transition-all border-gray-100 bg-white text-gray-400 no-underline hover:border-[#008751]/30">
                <i data-lucide="calendar-range" class="w-4 h-4"></i>
                <span class="text-[10px] font-black uppercase tracking-widest">Đặt theo tuần</span>
            </a>
        </div>
    </header>

    <!-- MAIN CONTENT AREA -->
    <form method="get" action="${pageContext.request.contextPath}/booking" id="bookingForm" class="flex-1 flex overflow-hidden">
        
        <!-- HIDDEN INPUTS -->
        <input type="hidden" name="scheduleId" id="scheduleId" value="${param.scheduleId}" />
        <input type="hidden" id="bookingDate" name="bookingDate" value="${param.bookingDate}" />
        <input type="hidden" id="weekStart" name="weekStart" value="${selectedWeekStart}" />
        <input type="hidden" name="bookingMode" id="bookingMode" value="${empty param.bookingMode ? 'normal' : param.bookingMode}" />
        <input type="hidden" name="fieldType" id="fieldType" value="${param.fieldType}" />

        <!-- LEFT SIDEBAR: CONFIG & CONTACT -->
        <aside class="w-72 bg-white border-r border-gray-200 overflow-y-auto p-6 space-y-8 shrink-0 custom-scrollbar">
            
            <!-- 1. Match Config -->
            <section class="space-y-6">
                <div class="flex items-center gap-3">
                    <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">1. Cấu hình</h2>
                </div>

                <div class="space-y-5">
                    <!-- Chi nhánh -->
                    <div class="space-y-2">
                        <label class="text-[9px] font-black text-gray-400 uppercase tracking-widest ml-1">Chi nhánh</label>
                        <div class="relative">
                            <i data-lucide="map-pin" class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-300"></i>
                            <select name="locationId" id="locationId" onchange="this.form.submit()" required
                                    class="w-full pl-10 pr-8 py-3 bg-gray-50 border border-gray-100 rounded-xl appearance-none font-bold text-gray-700 text-xs input-focus cursor-pointer">
                                <option value="">-- Chọn địa điểm --</option>
                                <c:forEach var="l" items="${locations}">
                                    <option value="${l.locationId}" ${param.locationId == l.locationId ? 'selected' : ''}>
                                        ${not empty l.locationName ? l.locationName : l.address}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Loại sân -->
                    <div class="space-y-2">
                        <label class="text-[9px] font-black text-gray-400 uppercase tracking-widest ml-1">Loại sân</label>
                        <div class="flex gap-2">
                            <button type="button" data-type="7-a-side"
                                    class="field-type-btn flex-1 py-3 rounded-xl border-2 font-black text-[9px] uppercase tracking-widest transition-all ${param.fieldType == '7-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                7-A-SIDE
                            </button>
                            <button type="button" data-type="11-a-side"
                                    class="field-type-btn flex-1 py-3 rounded-xl border-2 font-black text-[9px] uppercase tracking-widest transition-all ${param.fieldType == '11-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                11-A-SIDE
                            </button>
                        </div>
                    </div>

                    <!-- Chọn sân cụ thể -->
                    <c:if test="${not empty param.locationId}">
                        <div class="space-y-2">
                            <label class="text-[9px] font-black text-gray-400 uppercase tracking-widest ml-1">Sân bóng</label>
                            <div class="relative">
                                <i data-lucide="box" class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-300"></i>
                                <select name="fieldId" id="fieldId" onchange="this.form.submit()" required
                                        class="w-full pl-10 pr-8 py-3 bg-gray-50 border border-gray-100 rounded-xl appearance-none font-bold text-gray-700 text-xs input-focus cursor-pointer">
                                    <option value="">-- Chọn sân cụ thể --</option>
                                    <c:forEach var="f" items="${fields}">
                                        <option value="${f.fieldId}" ${param.fieldId == f.fieldId ? 'selected' : ''}>${f.fieldName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </c:if>
                </div>
            </section>

            <!-- Contact Info -->
            <section class="space-y-4">
                <div class="flex items-center gap-3">
                    <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Liên hệ</h2>
                </div>
                <div class="space-y-2">
                    <label class="text-[9px] font-black text-gray-400 uppercase tracking-widest ml-1">Số điện thoại <span class="text-rose-500">*</span></label>
                    <div class="relative">
                        <i data-lucide="phone" class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-300"></i>
                        <input type="text" name="bookingPhone" maxlength="20" required
                               placeholder="Số điện thoại"
                               value="${not empty param.bookingPhone ? param.bookingPhone : (roleNameLower eq 'staff' ? '' : sessionScope.user.phone)}"
                               class="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-100 rounded-xl font-bold text-gray-700 text-xs input-focus" />
                    </div>
                </div>
            </section>
        </aside>

        <!-- CENTER AREA: SCHEDULE, EQUIPMENT, VOUCHER -->
        <div class="flex-1 overflow-y-auto p-8 space-y-8 custom-scrollbar">
            
            <!-- FLASH MESSAGES -->
            <c:if test="${not empty flashSuccess}">
                <div class="bg-emerald-50 border border-emerald-100 p-4 rounded-2xl flex items-center gap-3">
                    <i data-lucide="check" class="w-4 h-4 text-[#008751]"></i>
                    <p class="text-xs font-bold text-[#008751] uppercase tracking-tight">${flashSuccess}</p>
                </div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="bg-rose-50 border border-rose-100 p-4 rounded-2xl flex items-center gap-3">
                    <i data-lucide="alert-circle" class="w-4 h-4 text-rose-500"></i>
                    <p class="text-xs font-bold text-rose-700 uppercase tracking-tight">${flashError}</p>
                </div>
            </c:if>

            <!-- 2. Schedule Section -->
            <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-6">
                <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">2. Chọn lịch thi đấu</h2>
                    </div>

                    <c:if test="${not empty param.fieldId}">
                        <div class="flex items-center gap-2 bg-gray-50 p-1.5 rounded-2xl border border-gray-100">
                            <button type="button" id="prevWeekBtn" data-week-start="${prevWeekStart}" ${!canGoPrevWeek ? 'disabled' : ''}
                                    class="p-2 bg-white rounded-xl border border-gray-100 ${!canGoPrevWeek ? 'opacity-50 cursor-not-allowed text-gray-300' : 'hover:border-[#008751] text-gray-400 hover:text-[#008751]'} transition-all">
                                <i data-lucide="chevron-left" class="w-4 h-4"></i>
                            </button>
                            <span class="text-[10px] font-black text-gray-700 uppercase tracking-widest px-2">
                                ${selectedWeekStart} - ${selectedWeekEnd}
                            </span>
                            <button type="button" id="nextWeekBtn" data-week-start="${nextWeekStart}"
                                    class="p-2 bg-white rounded-xl border border-gray-100 hover:border-[#008751] text-gray-400 hover:text-[#008751] transition-all">
                                <i data-lucide="chevron-right" class="w-4 h-4"></i>
                            </button>
                        </div>
                    </c:if>

                    <c:if test="${not empty schedules}">
                        <div class="flex items-center gap-2">
                            <input type="date" id="scheduleDateFilter" value="${param.bookingDate}" min="${minBookingDate}" class="px-3 py-2 bg-gray-50 border border-gray-100 rounded-xl text-[10px] font-bold text-gray-700 outline-none input-focus">
                            <button type="button" id="clearScheduleDateFilter" class="p-2 bg-white border border-gray-100 rounded-xl text-gray-400 hover:text-[#008751] hover:border-[#008751] transition-all">
                                <i data-lucide="rotate-ccw" class="w-4 h-4"></i>
                            </button>
                        </div>
                    </c:if>
                </div>

                <c:choose>
                    <c:when test="${empty schedules}">
                        <div class="py-20 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100">
                            <i data-lucide="calendar-x" class="w-10 h-10 text-gray-200 mx-auto mb-4"></i>
                            <p class="text-[9px] font-black text-gray-300 uppercase tracking-widest">Vui lòng chọn đầy đủ thông tin chi nhánh & sân</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="overflow-x-auto custom-scrollbar pb-4" id="scheduleScroll">
                            <div class="flex gap-4 min-w-max" id="scheduleBoard">
                                <!-- JS Injected Schedule Board -->
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <!-- 3. Equipment Section -->
            <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-6">
                <div class="flex items-center gap-3">
                    <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">3. Dịch vụ & Vật tư <span class="text-gray-300 ml-2">(Tùy chọn)</span></h2>
                </div>

                <c:choose>
                    <c:when test="${empty equipments}">
                        <p class="text-[9px] font-black text-gray-300 uppercase tracking-widest text-center py-4">Chưa có vật tư tại chi nhánh này</p>
                    </c:when>
                    <c:otherwise>
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <c:forEach var="e" items="${equipments}">
                                <div class="flex items-center justify-between p-4 bg-gray-50 rounded-2xl border border-gray-100 hover:border-[#008751]/20 transition-all">
                                    <div class="space-y-1">
                                        <p class="font-bold text-gray-700 text-xs">${e.name}</p>
                                        <p class="font-black text-[#008751] text-[10px] uppercase tracking-widest">
                                            <fmt:formatNumber value="${e.rentalPrice}" pattern="#,##0"/> đ
                                        </p>
                                    </div>
                                    <div class="flex items-center gap-3">
                                        <span class="text-[9px] font-bold text-gray-400 uppercase">Kho: ${e.quantity}</span>
                                        <input type="number" name="equipment_${e.equipmentId}" 
                                               data-unit-price="${e.rentalPrice}" min="0" max="${e.quantity}" value="0"
                                               class="equipment-qty w-16 px-2 py-1.5 bg-white border border-gray-100 rounded-lg text-center font-black text-gray-700 text-xs focus:ring-2 focus:ring-[#008751]/20 outline-none">
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <!-- 4. Voucher Section (Moved here as requested) -->
            <c:if test="${not empty vouchers}">
                <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-4">
                    <div class="flex items-center gap-3">
                        <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">4. Ưu đãi Voucher</h2>
                    </div>
                    <div class="relative max-w-md">
                        <i data-lucide="ticket" class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-[#008751]"></i>
                        <select name="voucherId" id="voucherId"
                                class="w-full pl-10 pr-8 py-3 bg-emerald-50/30 border border-emerald-100 rounded-xl appearance-none font-black text-[#008751] text-[10px] uppercase tracking-widest input-focus cursor-pointer">
                            <option value="" data-discount="0">-- CHỌN VOUCHER --</option>
                            <c:forEach var="v" items="${vouchers}">
                                <option value="${v.voucherId}" data-discount="${v.discountValue}" ${param.voucherId == v.voucherId.toString() ? 'selected' : ''}>${v.code} - GIẢM ${v.discountValue}%</option>
                            </c:forEach>
                        </select>
                    </div>
                </section>
            </c:if>
        </div>

        <!-- RIGHT SIDEBAR: PAYMENT SUMMARY -->
        <aside class="w-80 bg-white border-l border-gray-200 overflow-y-auto p-6 space-y-6 shrink-0 custom-scrollbar">
            <div class="bg-[#166534] text-white rounded-[2rem] shadow-xl shadow-[#166534]/20 p-8 space-y-8 relative overflow-hidden group">
                <!-- Ghost Icon -->
                <div class="absolute -top-4 -right-4 opacity-5 group-hover:scale-110 transition-transform duration-700">
                    <i data-lucide="wallet" class="w-32 h-32"></i>
                </div>

                <div class="relative z-10 space-y-6">
                    <div class="flex items-center gap-3">
                        <div class="w-8 h-8 bg-white/10 rounded-lg flex items-center justify-center border border-white/20">
                            <i data-lucide="credit-card" class="w-4 h-4 text-emerald-300"></i>
                        </div>
                        <h2 class="text-sm font-black uppercase tracking-widest">Thanh toán</h2>
                    </div>

                    <div class="space-y-3 pt-4 border-t border-white/10">
                        <div class="flex justify-between text-[9px] font-black uppercase tracking-widest opacity-60">
                            <span>Sân bóng</span>
                            <span id="payField" class="text-emerald-300 text-right">--</span>
                        </div>
                        <div class="flex justify-between text-[9px] font-black uppercase tracking-widest opacity-60">
                            <span>Lịch thi đấu</span>
                            <span id="paySchedule" class="text-emerald-300 text-right italic">--</span>
                        </div>
                    </div>

                    <div class="space-y-2 pt-4 border-t border-white/10">
                        <p class="text-[9px] font-black uppercase tracking-[0.2em] opacity-60">Hình thức thanh toán</p>
                        <div class="grid grid-cols-1 gap-2">
                            <label class="flex items-center justify-between gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2 cursor-pointer hover:bg-white/10 transition-colors">
                                <span class="text-[10px] font-black uppercase tracking-widest">Thanh toán 100%</span>
                                <input type="radio" name="bookingPaymentOption" value="full" class="h-3 w-3 accent-emerald-300" ${bookingPaymentOption == 'deposit' ? '' : 'checked'}>
                            </label>
                            <label class="flex items-center justify-between gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2 cursor-pointer hover:bg-white/10 transition-colors">
                                <span class="text-[10px] font-black uppercase tracking-widest">Đặt cọc 30%</span>
                                <input type="radio" name="bookingPaymentOption" value="deposit" class="h-3 w-3 accent-emerald-300" ${bookingPaymentOption == 'deposit' ? 'checked' : ''}>
                            </label>
                        </div>
                    </div>

                    <div class="space-y-3 pt-4 border-t border-white/10">
                        <div class="flex justify-between text-xs">
                            <span class="opacity-70">Tiền sân</span>
                            <span id="schedulePriceText" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-xs">
                            <span class="opacity-70">Vật tư</span>
                            <span id="equipmentPriceText" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-xs">
                            <span class="opacity-70">Giảm giá</span>
                            <span id="discountText" class="font-black text-emerald-300">0%</span>
                        </div>
                        <div class="flex justify-between text-xs pt-2 border-t border-white/5">
                            <span class="opacity-70">Thanh toán ngay</span>
                            <span id="dueNowText" class="font-black text-emerald-300">0 đ</span>
                        </div>
                    </div>

                    <div class="pt-6 border-t-2 border-white/20">
                        <div class="flex flex-col gap-1">
                            <span class="text-[9px] font-black uppercase tracking-[0.3em] text-emerald-400">TỔNG CỘNG</span>
                            <span id="totalText" class="text-3xl font-black tracking-tighter leading-none">0 đ</span>
                        </div>
                    </div>

                    <button type="submit" formaction="${pageContext.request.contextPath}/booking-confirm" formmethod="post" 
                            class="w-full bg-[#008751] hover:bg-emerald-400 text-white py-4 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-xl transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-2">
                        <i data-lucide="shield-check" class="w-4 h-4"></i>
                        XÁC NHẬN ĐẶT SÂN
                    </button>
                    <p class="text-[8px] text-center font-bold text-emerald-200/50 uppercase tracking-widest mt-4">Hệ thống bảo mật giao dịch FIFA</p>
                </div>
            </div>
        </aside>
    </form>

    <!-- DATA FOR JS -->
    <script id="schedulesData" type="application/json">
    [
    <c:forEach var="s" items="${schedules}" varStatus="st">
    {"id":"${s.scheduleId}","date":"${s.bookingDate}","start":"${s.startTime}","end":"${s.endTime}","status":"${s.status}","price":${s.price}}<c:if test="${not st.last}">,</c:if>
    </c:forEach>
    ]
    </script>

    <script id="fieldsData" type="application/json">
    [
    <c:forEach var="f" items="${fields}" varStatus="st">
    {"id":"${f.fieldId}","name":"${f.fieldName}"}<c:if test="${not st.last}">,</c:if>
    </c:forEach>
    ]
    </script>

    <script>
    (function() {
        lucide.createIcons();

        var schedules = JSON.parse((document.getElementById('schedulesData') || {}).textContent || '[]');
        var fields = JSON.parse((document.getElementById('fieldsData') || {}).textContent || '[]');
        var selectedFieldId = '${param.fieldId}';
        var selectedScheduleId = '${param.scheduleId}';
        var selectedDate = '${param.bookingDate}';
        var weekStartInput = document.getElementById('weekStart');
        var scheduleDateFilter = document.getElementById('scheduleDateFilter');
        var bookingPaymentOptionInput = document.querySelectorAll('input[name="bookingPaymentOption"]');

        function normalizeStatus(status) {
            var s = (status || '').toLowerCase().trim();
            return s || 'available';
        }

        function parseNum(v) { var n = parseFloat(String(v).replace(/[^0-9.-]/g, '')); return isNaN(n) ? 0 : n; }
        function fmt(v) { return Math.round(v).toLocaleString('vi-VN') + ' đ'; }

        function getSelectedPaymentOption() {
            var selected = document.querySelector('input[name="bookingPaymentOption"]:checked');
            return selected ? selected.value : 'full';
        }

        function updatePrice() {
            var schedulePrice = 0;
            schedules.forEach(function(s) {
                if (s.id === selectedScheduleId) schedulePrice = parseNum(s.price);
            });
            var equipmentTotal = 0;
            document.querySelectorAll('.equipment-qty').forEach(function(inp) {
                equipmentTotal += (parseInt(inp.value, 10) || 0) * parseNum(inp.getAttribute('data-unit-price'));
            });
            var subtotal = schedulePrice + equipmentTotal;
            var discount = 0;
            var vSel = document.getElementById('voucherId');
            if (vSel) discount = parseNum(vSel.options[vSel.selectedIndex].getAttribute('data-discount'));
            var total = subtotal * (1 - discount / 100);
            var paymentOption = getSelectedPaymentOption();
            var dueNow = paymentOption === 'deposit' ? Math.round(total * 0.3) : Math.round(total);

            document.getElementById('schedulePriceText').textContent = fmt(schedulePrice);
            document.getElementById('equipmentPriceText').textContent = fmt(equipmentTotal);
            document.getElementById('discountText').textContent = discount + '%';
            document.getElementById('totalText').textContent = fmt(total);
            document.getElementById('dueNowText').textContent = fmt(dueNow);
        }

        function updateFieldLabel() {
            var name = '--';
            fields.forEach(function(f) { if (f.id === selectedFieldId) name = f.name; });
            document.getElementById('payField').textContent = name;
        }

        function updateScheduleLabel() {
            var txt = '--';
            schedules.forEach(function(s) {
                if (s.id === selectedScheduleId) txt = s.date + ' | ' + s.start + ' - ' + s.end;
            });
            document.getElementById('paySchedule').textContent = txt;
        }

        function buildScheduleBoard() {
            var days = {};
            schedules.forEach(function(s) {
                if (!days[s.date]) days[s.date] = [];
                days[s.date].push(s);
            });

            var activeDateFilter = (scheduleDateFilter && scheduleDateFilter.value) ? scheduleDateFilter.value : '';
            var orderedDates = Object.keys(days).sort();
            if (activeDateFilter) {
                orderedDates = orderedDates.filter(function(d) { return d === activeDateFilter; });
            }

            if (!orderedDates.length) {
                var board = document.getElementById('scheduleBoard');
                if (board) board.innerHTML = '<div class="py-10 text-center w-full"><p class="text-[9px] font-black text-gray-300 uppercase tracking-widest">Không có lịch cho ngày đã chọn</p></div>';
                return;
            }

            var html = '';
            orderedDates.forEach(function(d) {
                var dObj = new Date(d);
                var dayNum = dObj.getDate();
                var dayMonth = dObj.getMonth() + 1;
                var dayNames = ['CN','T2','T3','T4','T5','T6','T7'];
                var dayName = dayNames[dObj.getDay()];
                var list = days[d].slice().sort(function(a, b) { return a.start.localeCompare(b.start); });

                var slotsHtml = '';
                list.forEach(function(s) {
                    var status = normalizeStatus(s.status);
                    var isAvailable = status === 'available';
                    var isSelected = s.id === selectedScheduleId;
                    var cardClass = isAvailable
                        ? 'slot-card cursor-pointer border-gray-50 hover:border-[#008751] hover:shadow-lg'
                        : 'opacity-50 border-gray-100 bg-gray-50 cursor-not-allowed';
                    var statusClass = isAvailable ? 'bg-emerald-50 text-[#008751]' : 'bg-gray-200 text-gray-500';
                    var selectedClass = isSelected ? ' selected border-[#008751] bg-emerald-50 ring-2 ring-[#008751]/10' : '';

                    slotsHtml += '<div class="' + cardClass + selectedClass + ' border-2 rounded-2xl p-4 transition-all" data-id="' + s.id + '" data-date="' + d + '" data-available="' + isAvailable + '">' +
                        '<div class="flex justify-between items-start mb-2">' +
                            '<div class="text-[10px] font-black text-gray-900">' + s.start + ' - ' + s.end + '</div>' +
                            '<span class="px-2 py-0.5 rounded-full text-[7px] font-black uppercase tracking-widest ' + statusClass + '">' + status + '</span>' +
                        '</div>' +
                        '<div class="flex items-center justify-between">' +
                            '<span class="text-[9px] font-black text-[#008751]">' + fmt(parseNum(s.price)) + '</span>' +
                            (isAvailable ? '<i data-lucide="clock" class="w-3.5 h-3.5 text-gray-300"></i>' : '<i data-lucide="ban" class="w-3.5 h-3.5 text-gray-400"></i>') +
                        '</div>' +
                    '</div>';
                });

                html += '<div class="min-w-[220px] space-y-3">' +
                    '<div class="bg-white p-3 rounded-xl border border-gray-100 text-center">' +
                        '<div class="text-[8px] font-black text-[#008751] uppercase tracking-[0.2em] opacity-70">' + dayName + '</div>' +
                        '<div class="text-sm font-black text-gray-900">' + dayNum + '/' + dayMonth + '</div>' +
                    '</div>' +
                    '<div class="space-y-3">' + slotsHtml + '</div>' +
                '</div>';
            });

            var board = document.getElementById('scheduleBoard');
            if (board) {
                board.innerHTML = html;
                lucide.createIcons();
                board.querySelectorAll('.slot-card').forEach(function(card) {
                    card.addEventListener('click', function() {
                        if (card.getAttribute('data-available') !== 'true') return;
                        selectedScheduleId = card.getAttribute('data-id');
                        selectedDate = card.getAttribute('data-date');
                        document.getElementById('scheduleId').value = selectedScheduleId;
                        document.getElementById('bookingDate').value = selectedDate;
                        buildScheduleBoard();
                        updatePrice();
                        updateScheduleLabel();
                    });
                });
            }
        }

        document.querySelectorAll('.field-type-btn').forEach(function(btn) {
            btn.addEventListener('click', function() {
                document.getElementById('fieldType').value = this.getAttribute('data-type');
                document.getElementById('bookingForm').submit();
            });
        });

        document.querySelectorAll('.booking-mode-btn').forEach(function(btn) {
            btn.addEventListener('click', function() {
                document.getElementById('bookingMode').value = this.getAttribute('data-mode');
                document.getElementById('bookingForm').submit();
            });
        });

        var prevWeekBtn = document.getElementById('prevWeekBtn');
        if (prevWeekBtn && !prevWeekBtn.disabled) {
            prevWeekBtn.addEventListener('click', function() {
                if (weekStartInput) {
                    weekStartInput.value = prevWeekBtn.getAttribute('data-week-start');
                }
                document.getElementById('scheduleId').value = '';
                document.getElementById('bookingDate').value = '';
                document.getElementById('bookingForm').submit();
            });
        }

        var nextWeekBtn = document.getElementById('nextWeekBtn');
        if (nextWeekBtn) {
            nextWeekBtn.addEventListener('click', function() {
                if (weekStartInput) {
                    weekStartInput.value = nextWeekBtn.getAttribute('data-week-start');
                }
                document.getElementById('scheduleId').value = '';
                document.getElementById('bookingDate').value = '';
                document.getElementById('bookingForm').submit();
            });
        }

        if (scheduleDateFilter) {
            scheduleDateFilter.addEventListener('change', function() {
                buildScheduleBoard();
            });
        }
        var clearDateFilterBtn = document.getElementById('clearScheduleDateFilter');
        if (clearDateFilterBtn) {
            clearDateFilterBtn.addEventListener('click', function() {
                if (scheduleDateFilter) scheduleDateFilter.value = '';
                buildScheduleBoard();
            });
        }

        bookingPaymentOptionInput.forEach(function(radio) {
            radio.addEventListener('change', updatePrice);
        });
        var vSel = document.getElementById('voucherId');
        if (vSel) vSel.addEventListener('change', updatePrice);
        document.getElementById('bookingForm').addEventListener('input', function(e) {
            if (e.target && e.target.classList.contains('equipment-qty')) updatePrice();
        });

        buildScheduleBoard();
        updateFieldLabel();
        updateScheduleLabel();
        updatePrice();
    })();
    </script>
</body>
</html>
