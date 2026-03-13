<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
        .elite-card { border-radius: 2.5rem; }
        .custom-scrollbar::-webkit-scrollbar { height: 4px; width: 4px; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #008751; border-radius: 10px; }
        .mode-card.selected { border-color: #008751; background-color: #ecfdf5; }
        .date-card.selected { background-color: #008751; color: white; border-color: #008751; transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(0, 135, 81, 0.2); }
        .slot-card.selected { border-color: #008751; background-color: #ecfdf5; }
        .input-focus:focus { border-color: #008751; outline: none; box-shadow: 0 0 0 4px rgba(0, 135, 81, 0.05); }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="flex-grow max-w-7xl mx-auto px-6 py-12 space-y-10 w-full">

    <!-- TOP TITLE & MODES -->
    <div class="flex flex-col space-y-8">
        <div class="space-y-2">
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                ĐẶT SÂN <span class="text-[#008751]">TRỰC TUYẾN</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">Hệ thống điều phối khung giờ thi đấu tiêu chuẩn FIFA</p>
        </div>

        <!-- Booking Modes Selection -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="mode-card selected elite-card p-6 border-2 border-gray-100 transition-all cursor-pointer group">
                <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-[#008751] shadow-sm">
                        <i data-lucide="zap" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <h3 class="font-black text-gray-900 uppercase tracking-tighter">ĐẶT LẺ</h3>
                        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Khách vãng lai linh hoạt</p>
                    </div>
                </div>
            </div>
            <div class="mode-card elite-card p-6 border-2 border-gray-100 bg-white hover:border-[#008751]/30 transition-all cursor-pointer opacity-60">
                <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400">
                        <i data-lucide="calendar-range" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <h3 class="font-black text-gray-900 uppercase tracking-tighter">ĐẶT TUẦN</h3>
                        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Cố định từ 4 tuần</p>
                    </div>
                </div>
            </div>
            <div class="mode-card elite-card p-6 border-2 border-gray-100 bg-white hover:border-[#008751]/30 transition-all cursor-pointer opacity-60">
                <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400">
                        <i data-lucide="trophy" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <h3 class="font-black text-gray-900 uppercase tracking-tighter">GIẢI ĐẤU</h3>
                        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Chuyên nghiệp toàn diện</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- FLASH MESSAGES -->
    <c:if test="${not empty flashSuccess}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="w-10 h-10 bg-[#008751] text-white rounded-xl flex items-center justify-center shadow-lg shadow-[#008751]/20">
                <i data-lucide="check" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-[#008751] uppercase tracking-tight">${flashSuccess}</p>
        </div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${flashError}</p>
        </div>
    </c:if>

    <form method="get" action="${pageContext.request.contextPath}/booking" id="bookingForm" class="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start">
        
        <!-- INPUTS HIDDEN FOR STATE -->
        <input type="hidden" name="scheduleId" id="scheduleId" value="${param.scheduleId}" />
        <input type="hidden" name="bookingDate" id="bookingDate" value="${param.bookingDate}" />

        <!-- LEFT PANEL: CONFIGURATION -->
        <div class="lg:col-span-8 space-y-10">
            
            <!-- 1. Match Config -->
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">1. Cấu hình trận đấu</h2>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <!-- Chi nhánh -->
                    <div class="space-y-3">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Chọn chi nhánh <span class="text-rose-500">*</span></label>
                        <div class="relative">
                            <i data-lucide="map-pin" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                            <select name="locationId" id="locationId" onchange="this.form.submit()" required
                                    class="w-full pl-12 pr-10 py-4 bg-gray-50 border border-gray-100 rounded-2xl appearance-none font-bold text-gray-700 input-focus cursor-pointer">
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
                    <div class="space-y-3">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Loại sân bóng <span class="text-rose-500">*</span></label>
                        <div class="flex gap-3">
                            <button type="button" data-type="7-a-side"
                                    class="field-type-btn flex-1 py-4 rounded-2xl border-2 font-black text-[10px] uppercase tracking-widest transition-all ${param.fieldType == '7-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                7-A-SIDE
                            </button>
                            <button type="button" data-type="11-a-side"
                                    class="field-type-btn flex-1 py-4 rounded-2xl border-2 font-black text-[10px] uppercase tracking-widest transition-all ${param.fieldType == '11-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                11-A-SIDE
                            </button>
                            <input type="hidden" name="fieldType" id="fieldType" value="${param.fieldType}" />
                        </div>
                    </div>

                    <!-- Chọn sân cụ thể -->
                    <c:if test="${not empty param.locationId}">
                        <div class="md:col-span-2 space-y-3">
                            <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Chọn sân <span class="text-rose-500">*</span></label>
                            <div class="relative">
                                <i data-lucide="box" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                                <select name="fieldId" id="fieldId" onchange="this.form.submit()" required
                                        class="w-full pl-12 pr-10 py-4 bg-gray-50 border border-gray-100 rounded-2xl appearance-none font-bold text-gray-700 input-focus cursor-pointer">
                                    <option value="">-- Chọn sân cụ thể --</option>
                                    <c:forEach var="f" items="${fields}">
                                        <option value="${f.fieldId}" ${param.fieldId == f.fieldId ? 'selected' : ''}>${f.fieldName} (${f.fieldType})</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </c:if>
                </div>
            </section>

            <!-- 2. Schedule Section -->
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">2. Chọn lịch thi đấu</h2>
                </div>

                <c:choose>
                    <c:when test="${empty schedules}">
                        <div class="py-12 text-center bg-gray-50 rounded-[2rem] border-2 border-dashed border-gray-100">
                            <i data-lucide="calendar-x" class="w-12 h-12 text-gray-200 mx-auto mb-4"></i>
                            <p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Vui lòng chọn đầy đủ thông tin chi nhánh & sân</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="space-y-4">
                            <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Lịch thi đấu theo ngày <span class="text-rose-500">*</span></label>
                            <div class="flex items-center gap-3">
                                <input type="date" id="scheduleDateFilter" value="${param.bookingDate}" class="px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none input-focus">
                                <button type="button" id="clearScheduleDateFilter" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-[10px] font-black text-gray-400 uppercase tracking-widest hover:text-[#008751] hover:border-[#008751] transition-all">
                                    Xóa lọc ngày
                                </button>
                            </div>
                            <div class="overflow-x-auto custom-scrollbar pb-4" id="scheduleScroll">
                                <div class="flex gap-4 min-w-max" id="scheduleBoard">
                                    <!-- JS Injected Schedule Board -->
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <!-- 3. Equipment Section -->
            <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                    <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">3. Dịch vụ & Vật tư <span class="text-gray-300 ml-2">(Tùy chọn)</span></h2>
                </div>

                <c:choose>
                    <c:when test="${empty equipments}">
                        <div class="py-12 text-center bg-gray-50 rounded-[2rem] border-2 border-dashed border-gray-100">
                            <p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Chưa có vật tư tại chi nhánh này</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="overflow-hidden rounded-2xl border border-gray-100">
                            <table class="w-full text-left">
                                <thead class="bg-gray-50">
                                    <tr>
                                        <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest">Tên vật tư</th>
                                        <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest text-center">Giá thuê</th>
                                        <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest text-center">Tồn kho</th>
                                        <th class="px-6 py-4 text-[9px] font-black text-gray-400 uppercase tracking-widest text-right">Số lượng</th>
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
                                                       data-unit-price="${e.rentalPrice}" min="0" max="${e.quantity}" value="0"
                                                       class="equipment-qty w-20 px-3 py-2 bg-gray-100 border-none rounded-xl text-center font-black text-gray-700 focus:bg-white focus:ring-2 focus:ring-[#008751]/20">
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <!-- 4. Voucher Section -->
            <c:if test="${not empty vouchers}">
                <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                    <div class="flex items-center gap-4">
                        <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">4. Ưu đãi Voucher</h2>
                    </div>
                    <div class="relative">
                        <i data-lucide="ticket" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#008751]"></i>
                        <select name="voucherId" id="voucherId"
                                class="w-full pl-12 pr-10 py-4 bg-emerald-50/30 border border-emerald-100 rounded-2xl appearance-none font-black text-[#008751] text-xs uppercase tracking-widest input-focus cursor-pointer">
                            <option value="" data-discount="0">-- KHÔNG SỬ DỤNG VOUCHER --</option>
                            <c:forEach var="v" items="${vouchers}">
                                <option value="${v.voucherId}" data-discount="${v.discountValue}">${v.code} - GIẢM ${v.discountValue}%</option>
                            </c:forEach>
                        </select>
                    </div>
                </section>
            </c:if>
        </div>

        <!-- RIGHT PANEL: PAYMENT SUMMARY (STICKY) -->
        <div class="lg:col-span-4 sticky top-28">
            <div class="bg-[#166534] text-white elite-card shadow-2xl shadow-[#166534]/20 p-10 space-y-10 relative overflow-hidden group">
                <!-- Ghost Icon -->
                <div class="absolute -top-6 -right-6 opacity-10 group-hover:scale-110 transition-transform duration-700">
                    <i data-lucide="wallet" class="w-40 h-40"></i>
                </div>

                <div class="relative z-10 space-y-6">
                    <div class="flex items-center gap-4">
                        <div class="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center border border-white/20">
                            <i data-lucide="credit-card" class="w-5 h-5 text-emerald-300"></i>
                        </div>
                        <h2 class="text-xl font-black uppercase tracking-widest">Thanh toán</h2>
                    </div>

                    <div class="space-y-4 pt-6 border-t border-white/10">
                        <div class="flex justify-between text-[10px] font-black uppercase tracking-widest opacity-60">
                            <span>Chế độ đặt</span>
                            <span class="text-emerald-300">ĐẶT LẺ</span>
                        </div>
                        <div class="flex justify-between items-start gap-4">
                            <span class="text-[10px] font-black uppercase tracking-widest opacity-60">Sân bóng</span>
                            <span id="payField" class="text-sm font-bold text-right">--</span>
                        </div>
                        <div class="flex justify-between items-start gap-4">
                            <span class="text-[10px] font-black uppercase tracking-widest opacity-60">Lịch thi đấu</span>
                            <span id="paySchedule" class="text-sm font-bold text-right italic">--</span>
                        </div>
                    </div>

                    <div class="space-y-4 pt-6 border-t border-white/10">
                        <div class="flex justify-between text-sm">
                            <span class="font-medium opacity-80">Tiền thuê sân</span>
                            <span id="schedulePriceText" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-sm">
                            <span class="font-medium opacity-80">Vật tư phụ trợ</span>
                            <span id="equipmentPriceText" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-sm">
                            <span class="font-medium opacity-80">Giảm giá voucher</span>
                            <span id="discountText" class="font-black text-emerald-300">0%</span>
                        </div>
                    </div>

                    <div class="pt-8 mt-4 border-t-2 border-white/20">
                        <div class="flex flex-col gap-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.3em] text-emerald-400">TỔNG CỘNG TẠM TÍNH</span>
                            <span id="totalText" class="text-4xl font-black tracking-tighter leading-none">0 đ</span>
                        </div>
                    </div>

                    <button type="submit" formaction="${pageContext.request.contextPath}/booking-confirm" formmethod="post" 
                            class="w-full bg-[#008751] hover:bg-emerald-400 text-white py-6 rounded-[1.8rem] font-black text-xs uppercase tracking-[0.2em] shadow-2xl transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-3 mt-4">
                        <i data-lucide="shield-check" class="w-4 h-4"></i>
                        XÁC NHẬN ĐẶT SÂN
                    </button>
                    <p class="text-[9px] text-center font-bold text-emerald-200/50 uppercase tracking-widest mt-6">Hệ thống bảo mật giao dịch FIFA 256-bit</p>
                </div>
            </div>
        </div>

    </form>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

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

    // Data from JSP to JS
    var schedules = JSON.parse((document.getElementById('schedulesData') || {}).textContent || '[]');
    var fields = JSON.parse((document.getElementById('fieldsData') || {}).textContent || '[]');
    var selectedFieldId = '${param.fieldId}';
    var selectedScheduleId = '${param.scheduleId}';
    var selectedDate = '${param.bookingDate}';
    var scheduleDateFilter = document.getElementById('scheduleDateFilter');

    function normalizeStatus(status) {
        var s = (status || '').toLowerCase().trim();
        if (!s) return 'available';
        return s;
    }

    function parseNum(v) { var n = parseFloat(String(v).replace(/[^0-9.-]/g, '')); return isNaN(n) ? 0 : n; }
    function fmt(v) { return Math.round(v).toLocaleString('vi-VN') + ' đ'; }

    function updatePrice() {
        var schedulePrice = 0;
        schedules.forEach(function(s) {
            if (s.id === selectedScheduleId) schedulePrice = parseNum(s.price);
        });
        var equipmentTotal = 0;
        document.querySelectorAll('.equipment-qty').forEach(function(inp) {
            equipmentTotal += (parseInt(inp.value, 10) || 0) * parseNum(inp.getAttribute('data-unit-price'));
        });
        var subtotal = schedulePrice;
        var discount = 0;
        var vSel = document.getElementById('voucherId');
        if (vSel) discount = parseNum(vSel.options[vSel.selectedIndex].getAttribute('data-discount'));
        var total = subtotal * (1 - discount / 100) + equipmentTotal;

        document.getElementById('schedulePriceText').textContent = fmt(schedulePrice);
        document.getElementById('equipmentPriceText').textContent = fmt(equipmentTotal);
        document.getElementById('discountText').textContent = discount + '%';
        document.getElementById('totalText').textContent = fmt(total);
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
        if (activeDateFilter) {
            selectedDate = activeDateFilter;
        }

        var orderedDates = Object.keys(days).sort();
        if (activeDateFilter) {
            orderedDates = orderedDates.filter(function(d) { return d === activeDateFilter; });
        }

        var selected = schedules.find(function(s) { return s.id === selectedScheduleId; });
        if (selected && (normalizeStatus(selected.status) !== 'available' || (activeDateFilter && selected.date !== activeDateFilter))) {
            selectedScheduleId = '';
        }

        var candidateSchedules = schedules;
        if (activeDateFilter) {
            candidateSchedules = schedules.filter(function(s) { return s.date === activeDateFilter; });
        }

        if (!selectedScheduleId) {
            var firstAvailable = candidateSchedules.find(function(s) {
                return normalizeStatus(s.status) === 'available';
            });
            if (firstAvailable) {
                selectedScheduleId = firstAvailable.id;
                selectedDate = firstAvailable.date;
            }
        }

        if (!orderedDates.length) {
            var board = document.getElementById('scheduleBoard');
            if (board) {
                board.innerHTML = '<div class="min-w-[320px] bg-gray-50 border-2 border-dashed border-gray-100 rounded-[2rem] p-10 text-center"><p class="text-[10px] font-black text-gray-300 uppercase tracking-widest">Không có lịch cho ngày đã chọn</p></div>';
            }
            selectedScheduleId = '';
            document.getElementById('scheduleId').value = '';
            document.getElementById('bookingDate').value = selectedDate || '';
            updatePrice();
            updateScheduleLabel();
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
                    ? 'slot-card cursor-pointer border-gray-50 hover:border-[#008751] hover:shadow-xl'
                    : 'opacity-70 border-gray-100 bg-gray-50/60';
                var statusClass = isAvailable
                    ? 'bg-emerald-50 text-[#008751]'
                    : 'bg-amber-50 text-amber-500';
                var statusText = isAvailable ? 'available' : 'unavailable';
                var selectedClass = isSelected ? ' border-[#008751] bg-emerald-50/40' : '';

                slotsHtml += '<div class="' + cardClass + selectedClass + ' group border-2 rounded-[1.5rem] p-4 transition-all" data-id="' + s.id + '" data-date="' + d + '" data-available="' + isAvailable + '">' +
                    '<div class="flex justify-between items-start mb-3">' +
                        '<div class="text-sm font-black text-gray-900 tracking-tight">' + s.start + ' - ' + s.end + '</div>' +
                        '<span class="px-2.5 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ' + statusClass + '">' + statusText + '</span>' +
                    '</div>' +
                    '<div class="flex items-center justify-between">' +
                        '<span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">' + fmt(parseNum(s.price)) + '</span>' +
                        (isAvailable ? '<i data-lucide="clock" class="w-4 h-4 text-gray-300 group-hover:text-[#008751] transition-colors"></i>' : '<i data-lucide="ban" class="w-4 h-4 text-amber-400"></i>') +
                    '</div>' +
                '</div>';
            });

            html += '<div class="min-w-[270px] space-y-3">' +
                '<div class="bg-white p-4 rounded-2xl border-2 border-gray-50 text-center">' +
                    '<div class="text-[10px] font-black text-[#008751] uppercase tracking-[0.2em] opacity-70">' + dayName + '</div>' +
                    '<div class="text-lg font-black text-gray-900 mt-1">' + dayNum + '/' + dayMonth + '</div>' +
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

        document.getElementById('scheduleId').value = selectedScheduleId || '';
        document.getElementById('bookingDate').value = selectedDate || '';
        updatePrice();
        updateScheduleLabel();
    }

    document.querySelectorAll('.field-type-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            var t = this.getAttribute('data-type');
            document.getElementById('fieldType').value = t;
            document.getElementById('bookingForm').submit();
        });
    });

    if (schedules.length) {
        buildScheduleBoard();
    }
    if (scheduleDateFilter) {
        scheduleDateFilter.addEventListener('change', function() {
            selectedDate = scheduleDateFilter.value || '';
            selectedScheduleId = '';
            buildScheduleBoard();
        });
    }
    var clearDateFilterBtn = document.getElementById('clearScheduleDateFilter');
    if (clearDateFilterBtn) {
        clearDateFilterBtn.addEventListener('click', function() {
            if (scheduleDateFilter) scheduleDateFilter.value = '';
            selectedDate = '';
            selectedScheduleId = '';
            buildScheduleBoard();
        });
    }
    updateFieldLabel();
    var vSel = document.getElementById('voucherId');
    if (vSel) vSel.addEventListener('change', updatePrice);
    document.getElementById('bookingForm').addEventListener('input', function(e) {
        if (e.target && e.target.classList.contains('equipment-qty')) updatePrice();
    });
})();
</script>
</body> 
</html>