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
    <title>Đặt Sân Theo Tuần - FIFAFIELD 2026</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .custom-scrollbar::-webkit-scrollbar { height: 4px; width: 4px; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #008751; border-radius: 10px; }
        .input-focus:focus { border-color: #008751; outline: none; box-shadow: 0 0 0 4px rgba(0,135,81,.05); }
        /* Slot cells */
        .slot-avail { cursor: pointer; transition: all .12s; }
        .slot-avail:hover  { background-color: #ecfdf5; border-color: #34d399 !important; }
        .slot-avail.picked { background-color: #bbf7d0; border-color: #047857 !important; box-shadow: 0 0 0 2px rgba(4,120,87,.22), 0 10px 20px rgba(4,120,87,.14); }
        .slot-avail.picked .slot-icon { color: #065f46; }
        .day-column { min-width: 200px; }
    </style>
</head>
<body class="antialiased text-gray-900 h-screen flex flex-col overflow-hidden">

    <c:set var="roleNameLower" value="${fn:toLowerCase(sessionScope.user.role.roleName)}" />

    <!-- HEADER BAR -->
    <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-end px-6 shrink-0 z-20">
        <div class="flex gap-2">
            <a href="${pageContext.request.contextPath}/booking?locationId=${selectedLocationId}&fieldId=${selectedFieldId}"
               class="px-4 py-2 rounded-xl border-2 flex items-center gap-2 transition-all border-gray-100 bg-white text-gray-400 no-underline hover:border-[#008751]/30">
                <i data-lucide="zap" class="w-4 h-4"></i>
                <span class="text-[10px] font-black uppercase tracking-widest">Đặt sân thường</span>
            </a>
            <button type="button" class="px-4 py-2 rounded-xl border-2 flex items-center gap-2 transition-all border-[#008751] bg-emerald-50 text-[#008751]">
                <i data-lucide="calendar-range" class="w-4 h-4"></i>
                <span class="text-[10px] font-black uppercase tracking-widest">Đặt theo tuần</span>
            </button>
        </div>
    </header>

    <!-- MAIN CONTENT AREA -->
    <form method="post" action="${pageContext.request.contextPath}/booking/weekly-confirm" id="weeklyForm" class="flex-1 flex overflow-hidden">
        
        <!-- HIDDEN STATE -->
        <input type="hidden" name="fieldId"    id="hFieldId"    value="${selectedFieldId}">
        <input type="hidden" name="locationId" id="hLocationId" value="${selectedLocationId}">
        <input type="hidden" name="weekStart"  id="hWeekStart"  value="${weekStart}">
        <input type="hidden" name="weekCount"  id="hWeekCount"  value="${selectedWeekCount}">
        <input type="hidden" name="action" value="confirm">
        <div id="autoRecurringSelections" class="hidden"></div>
        <div id="persistedCarrySelections" class="hidden"></div>

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
                            <select id="locationSelect" onchange="filterSubmit()"
                                    class="w-full pl-10 pr-8 py-3 bg-gray-50 border border-gray-100 rounded-xl appearance-none font-bold text-gray-700 text-xs input-focus cursor-pointer">
                                <option value="">-- Chọn địa điểm --</option>
                                <c:forEach var="l" items="${locations}">
                                    <option value="${l.locationId}" <c:if test="${l.locationId.toString() eq selectedLocationId}">selected</c:if>>
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
                                    class="field-type-btn flex-1 py-3 rounded-xl border-2 font-black text-[9px] uppercase tracking-widest transition-all ${selectedFieldType eq '7-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                7-A-SIDE
                            </button>
                            <button type="button" data-type="11-a-side"
                                    class="field-type-btn flex-1 py-3 rounded-xl border-2 font-black text-[9px] uppercase tracking-widest transition-all ${selectedFieldType eq '11-a-side' ? 'border-[#008751] bg-emerald-50 text-[#008751]' : 'border-gray-50 bg-gray-50 text-gray-400 hover:border-gray-200'}">
                                11-A-SIDE
                            </button>
                        </div>
                    </div>

                    <!-- Chọn sân cụ thể -->
                    <c:if test="${not empty selectedLocationId}">
                        <div class="space-y-2">
                            <label class="text-[9px] font-black text-gray-400 uppercase tracking-widest ml-1">Sân bóng</label>
                            <div class="relative">
                                <i data-lucide="layout-grid" class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-300"></i>
                                <select id="fieldSelect" onchange="filterSubmit()"
                                        class="w-full pl-10 pr-8 py-3 bg-gray-50 border border-gray-100 rounded-xl appearance-none font-bold text-gray-700 text-xs input-focus cursor-pointer">
                                    <option value="">-- Chọn sân cụ thể --</option>
                                    <c:forEach var="f" items="${fields}">
                                        <option value="${f.fieldId}" <c:if test="${f.fieldId.toString() eq selectedFieldId}">selected</c:if>>${f.fieldName}</option>
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

        <!-- CENTER AREA: WEEKLY GRID, EQUIPMENT, VOUCHER -->
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

            <!-- 2. Weekly Schedule Section -->
            <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-6 min-h-[500px]">
                <div class="flex items-center justify-between flex-wrap gap-4">
                    <div class="flex items-center gap-3">
                        <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">2. Chọn lịch theo tuần</h2>
                    </div>

                    <c:if test="${not empty selectedFieldId}">
                        <div class="flex items-center gap-3 bg-gray-50 p-1.5 rounded-2xl border border-gray-100">
                            <a href="${pageContext.request.contextPath}/booking/weekly?locationId=${selectedLocationId}&fieldType=${selectedFieldType}&fieldId=${selectedFieldId}&weekStart=${prevWeekStart}&weekCount=${selectedWeekCount}"
                               data-week-start="${prevWeekStart}" class="week-nav-link p-2 bg-white rounded-xl border border-gray-100 hover:border-[#008751] text-gray-400 hover:text-[#008751] transition-all">
                                <i data-lucide="chevron-left" class="w-4 h-4"></i>
                            </a>
                            <span class="text-[10px] font-black text-gray-700 uppercase tracking-widest px-2">
                                <fmt:parseDate value="${weekStart}" pattern="yyyy-MM-dd" var="wsParsed" />
                                <fmt:parseDate value="${weekEnd}" pattern="yyyy-MM-dd" var="weParsed" />
                                <fmt:formatDate value="${wsParsed}" pattern="dd/MM" /> – <fmt:formatDate value="${weParsed}" pattern="dd/MM" />
                            </span>
                            <a href="${pageContext.request.contextPath}/booking/weekly?locationId=${selectedLocationId}&fieldType=${selectedFieldType}&fieldId=${selectedFieldId}&weekStart=${nextWeekStart}&weekCount=${selectedWeekCount}"
                               data-week-start="${nextWeekStart}" class="week-nav-link p-2 bg-white rounded-xl border border-gray-100 hover:border-[#008751] text-gray-400 hover:text-[#008751] transition-all">
                                <i data-lucide="chevron-right" class="w-4 h-4"></i>
                            </a>
                        </div>
                    </c:if>

                    <div class="flex items-center gap-3">
                        <select id="weekCountSelect" onchange="filterSubmit()"
                                class="px-4 py-2 bg-gray-50 border border-gray-100 rounded-xl font-black text-[10px] text-gray-700 uppercase tracking-widest outline-none input-focus cursor-pointer">
                            <c:forEach begin="4" end="12" var="wc">
                                <option value="${wc}" <c:if test="${wc == selectedWeekCount}">selected</c:if>>${wc} tuần</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div id="autoRecurringNotice" class="text-[9px] font-bold text-emerald-600 bg-emerald-50 px-4 py-2 rounded-xl border border-emerald-100 inline-block"></div>

                <c:choose>
                    <c:when test="${empty selectedFieldId}">
                        <div class="h-full flex flex-col items-center justify-center py-20 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100">
                            <i data-lucide="calendar-range" class="w-10 h-10 text-gray-200 mb-4"></i>
                            <p class="text-[9px] font-black text-gray-300 uppercase tracking-widest">Vui lòng chọn đầy đủ chi nhánh & sân để xem lịch</p>
                        </div>
                    </c:when>
                    <c:when test="${empty gridRows}">
                        <div class="h-full flex flex-col items-center justify-center py-20 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100">
                            <i data-lucide="calendar-x" class="w-10 h-10 text-gray-200 mb-4"></i>
                            <p class="text-[9px] font-black text-gray-300 uppercase tracking-widest">Không có lịch trong tuần này</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="flex items-center justify-between mb-4">
                            <div class="flex gap-2">
                                <button type="button" onclick="selectAll()" class="px-3 py-1.5 text-[9px] font-black uppercase tracking-widest border-2 border-[#008751] text-[#008751] rounded-lg hover:bg-emerald-50 transition-all">Tất cả</button>
                                <button type="button" onclick="clearAll()" class="px-3 py-1.5 text-[9px] font-black uppercase tracking-widest border-2 border-gray-200 text-gray-400 rounded-lg hover:bg-gray-50 transition-all">Bỏ chọn</button>
                            </div>
                            <div class="flex gap-4 text-[8px] font-bold text-gray-400 uppercase tracking-widest">
                                <span class="flex items-center gap-1"><span class="w-2 h-2 rounded bg-emerald-100 border border-emerald-400"></span>Trống</span>
                                <span class="flex items-center gap-1"><span class="w-2 h-2 rounded bg-rose-50 border border-rose-300"></span>Hết</span>
                            </div>
                        </div>

                        <div class="overflow-x-auto custom-scrollbar pb-4" id="weeklyScroll">
                            <div class="flex gap-4 min-w-max px-1" id="weeklyBoard">
                                <c:forEach var="d" items="${weekDates}" varStatus="dSt">
                                    <div class="day-column space-y-3">
                                        <div class="bg-gray-50 p-3 rounded-2xl border border-gray-100 text-center">
                                            <span class="text-[8px] font-black text-[#008751] uppercase tracking-widest opacity-70">
                                                <fmt:parseDate value="${d}" pattern="yyyy-MM-dd" var="dParsed" />
                                                <fmt:formatDate value="${dParsed}" pattern="EEE" />
                                            </span>
                                            <div class="text-xs font-black text-gray-900 mt-0.5"><fmt:formatDate value="${dParsed}" pattern="dd/MM" /></div>
                                        </div>

                                        <div class="space-y-2">
                                            <c:forEach var="row" items="${gridRows}">
                                                <c:set var="cell" value="${row.cells[dSt.index]}"/>
                                                <c:choose>
                                                    <c:when test="${not cell.exists}">
                                                        <div class="bg-gray-50/50 border border-dashed border-gray-100 rounded-xl p-3 text-center opacity-40">
                                                            <span class="text-[8px] font-black text-gray-300 uppercase tracking-widest">N/A</span>
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${cell.available}">
                                                        <label class="slot-avail block border-2 rounded-xl p-3 bg-white shadow-sm ${cell.selected ? 'picked border-[#047857] bg-emerald-100/70' : 'border-gray-50'}" for="sc_${cell.scheduleId}">
                                                            <input type="checkbox" id="sc_${cell.scheduleId}" name="scheduleIds" value="${cell.scheduleId}" data-date="${cell.bookingDate}" data-start="${cell.startTime}" class="slot-cb sr-only" <c:if test="${cell.selected}">checked</c:if> onchange="onCellChange(this)">
                                                            <div class="flex justify-between items-start mb-2">
                                                                <div class="text-[10px] font-black text-gray-900 tracking-tight">${row.startTime} - ${row.endTime}</div>
                                                                <i data-lucide="${cell.selected ? 'check-square' : 'clock'}" class="w-3 h-3 ${cell.selected ? 'text-[#065f46]' : 'text-gray-300'} slot-icon"></i>
                                                            </div>
                                                            <div class="text-[9px] font-black text-[#008751] uppercase tracking-widest"><fmt:formatNumber value="${cell.price}" pattern="#,##0"/>đ</div>
                                                        </label>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="border border-gray-100 bg-gray-50 rounded-xl p-3 opacity-50 grayscale">
                                                            <div class="flex justify-between items-start mb-2">
                                                                <div class="text-[10px] font-black text-gray-400 tracking-tight">${row.startTime} - ${row.endTime}</div>
                                                                <i data-lucide="ban" class="w-3 h-3 text-gray-300"></i>
                                                            </div>
                                                            <div class="text-[8px] font-black text-gray-300 uppercase tracking-widest">Hết chỗ</div>
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

            <!-- 3. Equipment Section -->
            <c:if test="${not empty selectedLocationId}">
                <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-6">
                    <div class="flex items-center gap-3">
                        <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">3. Dịch vụ & Vật tư</h2>
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
                                            <input type="number" name="equipment_${e.equipmentId}" data-unit-price="${e.rentalPrice}" min="0" max="${e.quantity}" value="0"
                                                   class="equipment-qty w-16 px-2 py-1.5 bg-white border border-gray-100 rounded-lg text-center font-black text-gray-700 text-xs focus:ring-2 focus:ring-[#008751]/20 outline-none">
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:if>

            <!-- 4. Voucher Section -->
            <c:if test="${not empty vouchers}">
                <section class="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 space-y-4">
                    <div class="flex items-center gap-3">
                        <div class="w-6 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">4. Ưu đãi Voucher</h2>
                    </div>
                    <div class="relative max-w-md">
                        <i data-lucide="ticket" class="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-[#008751]"></i>
                        <select name="voucherId" id="voucherId" onchange="updateSummary()"
                                class="w-full pl-10 pr-8 py-3 bg-emerald-50/30 border border-emerald-100 rounded-xl appearance-none font-black text-[#008751] text-[10px] uppercase tracking-widest input-focus cursor-pointer">
                            <option value="" data-discount="0">-- CHỌN VOUCHER --</option>
                            <c:forEach var="v" items="${vouchers}">
                                <option value="${v.voucherId}" data-discount="${v.discountValue}">${v.code} - GIẢM ${v.discountValue}%</option>
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
                    <i data-lucide="calendar-range" class="w-32 h-32"></i>
                </div>

                <div class="relative z-10 space-y-6">
                    <div class="flex items-center gap-3">
                        <div class="w-8 h-8 bg-white/10 rounded-lg flex items-center justify-center border border-white/20">
                            <i data-lucide="wallet" class="w-4 h-4 text-emerald-300"></i>
                        </div>
                        <h2 class="text-sm font-black uppercase tracking-widest">Tóm tắt</h2>
                    </div>

                    <div class="space-y-3 pt-4 border-t border-white/10">
                        <div class="flex justify-between text-[9px] font-black uppercase tracking-widest opacity-60">
                            <span>Chế độ</span>
                            <span class="text-emerald-300">THEO TUẦN</span>
                        </div>
                        <div class="flex justify-between items-start gap-4">
                            <span class="text-[9px] font-black uppercase tracking-widest opacity-60">Sân bóng</span>
                            <span id="sumField" class="text-xs font-bold text-right">${not empty selectedField ? selectedField.fieldName : '—'}</span>
                        </div>
                        <div class="flex justify-between items-start gap-4">
                            <span class="text-[9px] font-black uppercase tracking-widest opacity-60">Số ca</span>
                            <span id="sumCount" class="text-xs font-black text-emerald-300">0</span>
                        </div>
                    </div>

                    <div class="space-y-3 pt-4 border-t border-white/10">
                        <div class="flex justify-between text-xs">
                            <span class="opacity-70">Tiền sân</span>
                            <span id="sumFieldPrice" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-xs">
                            <span class="opacity-70">Vật tư</span>
                            <span id="sumEquipPrice" class="font-black">0 đ</span>
                        </div>
                        <div class="flex justify-between text-xs">
                            <span class="opacity-70">Giảm giá</span>
                            <span id="sumDiscount" class="font-black text-emerald-300">0%</span>
                        </div>
                    </div>

                    <div class="pt-6 border-t-2 border-white/20">
                        <div class="space-y-2 mb-4">
                            <span class="text-[9px] font-black uppercase tracking-[0.3em] text-emerald-400">THANH TOÁN</span>
                            <label class="flex items-center justify-between gap-3 bg-white/5 border border-white/15 rounded-xl px-3 py-2 cursor-pointer">
                                <div>
                                    <div class="text-[10px] font-black uppercase tracking-widest">Trả hết</div>
                                    <div class="text-[8px] opacity-70">Thanh toán 100% ngay</div>
                                </div>
                                <input type="radio" name="bookingPaymentOption" value="full"
                                       <c:if test="${empty param.bookingPaymentOption or fn:toLowerCase(param.bookingPaymentOption) eq 'full'}">checked</c:if>
                                       onchange="updateSummary()"
                                       class="w-4 h-4 accent-emerald-400" />
                            </label>
                            <label class="flex items-center justify-between gap-3 bg-white/5 border border-white/15 rounded-xl px-3 py-2 cursor-pointer">
                                <div>
                                    <div class="text-[10px] font-black uppercase tracking-widest">Đặt cọc</div>
                                    <div class="text-[8px] opacity-70">Thanh toán trước 30%</div>
                                </div>
                                <input type="radio" name="bookingPaymentOption" value="deposit"
                                       <c:if test="${fn:toLowerCase(param.bookingPaymentOption) eq 'deposit'}">checked</c:if>
                                       onchange="updateSummary()"
                                       class="w-4 h-4 accent-emerald-400" />
                            </label>
                        </div>

                        <div class="flex flex-col gap-1">
                            <span class="text-[9px] font-black uppercase tracking-[0.3em] text-emerald-400">TỔNG TẠM TÍNH</span>
                            <span id="sumTotal" class="text-3xl font-black tracking-tighter leading-none">0 đ</span>
                        </div>
                        <div class="mt-3 pt-3 border-t border-white/10 flex items-center justify-between text-[10px] font-black uppercase tracking-widest">
                            <span class="opacity-70" id="paymentOptionLabel">Thanh toán hôm nay</span>
                            <span id="sumDueNow" class="text-emerald-300">0 đ</span>
                        </div>
                    </div>

                    <button type="submit" id="submitBtn" disabled onclick="return confirmSubmit()"
                            class="w-full bg-gray-500/50 text-white/50 py-4 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] transition-all flex items-center justify-center gap-2 mt-4 cursor-not-allowed">
                        <i data-lucide="shield-check" class="w-4 h-4"></i>
                        XÁC NHẬN LỊCH TUẦN
                    </button>
                    <p id="noSelectMsg" class="text-[8px] text-center font-bold text-rose-300 uppercase tracking-widest hidden">Vui lòng chọn ít nhất 1 khung giờ</p>
                </div>
            </div>
        </aside>
    </form>

    <!-- DATA FOR JS -->
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
    {"id":"${s.scheduleId}","date":"${s.bookingDate}","start":"${s.startTime}","status":"${fn:toLowerCase(s.status) eq 'available' ? 'available' : 'unavailable'}","price":${s.price}}<c:if test="${not st.last}">,</c:if>
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

    <script>
    (function () {
        lucide.createIcons();

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
        function parseIsoDate(dateStr) {
            var p = String(dateStr || '').split('-');
            if (p.length !== 3) return null;
            var d = new Date(parseInt(p[0], 10), parseInt(p[1], 10) - 1, parseInt(p[2], 10));
            return isNaN(d.getTime()) ? null : d;
        }
        function recurringKey(dateStr, startTimeStr) {
            var d = parseIsoDate(dateStr);
            var start = normalizeTime(startTimeStr);
            if (!d || !start) return '';
            var dow = d.getDay();
            return String(dow) + '|' + start;
        }
        function scheduleMeta(scheduleId, fallbackDate, fallbackStart) {
            var meta = scheduleById[String(scheduleId)] || {};
            var date = String(meta.date || fallbackDate || '');
            var start = normalizeTime(meta.start || fallbackStart || '');
            return {
                date: date,
                start: start,
                key: recurringKey(date, start)
            };
        }
        function clearSeriesByKey(seriesKey) {
            if (!seriesKey) return;
            Object.keys(persistedAnchorIds).forEach(function(anchorId) {
                var meta = scheduleMeta(anchorId);
                if (meta.key === seriesKey) delete persistedAnchorIds[String(anchorId)];
            });
            Object.keys(persistedSelectedIds).forEach(function(selectedId) {
                var meta = scheduleMeta(selectedId);
                if (meta.key === seriesKey) delete persistedSelectedIds[String(selectedId)];
            });
        }
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
                    if (targetStatus !== 'available' || !targetId || baseIds[targetId]) { skipped++; continue; }
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
                    notice.textContent = 'Tự động thêm ' + autoCount + ' ca lặp lại' + (skipped > 0 ? ('; bỏ qua ' + skipped + ' ca hết chỗ') : '') + '.';
                    notice.className = 'text-[9px] font-bold text-emerald-600 bg-emerald-50 px-4 py-2 rounded-xl border border-emerald-100 inline-block';
                } else {
                    notice.textContent = 'Chọn ca ở tuần đầu để tự động lặp lại.';
                    notice.className = 'text-[9px] font-bold text-gray-400 bg-gray-50 px-4 py-2 rounded-xl border border-gray-100 inline-block';
                }
            }
        }

        function buildSelectedIdsForNavigation() {
            var merged = {};
            Object.keys(persistedSelectedIds).forEach(function(id) { merged[id] = true; });
            getVisibleSlotIds().forEach(function(id) { delete merged[id]; });
            getChecked().forEach(function(cb) { merged[String(cb.value)] = true; });
            getAutoSelectedIds().forEach(function(id) { merged[String(id)] = true; });
            return Object.keys(merged);
        }

        function buildAnchorIdsForNavigation() { return Object.keys(persistedAnchorIds); }

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
            var equipTotal = equipSum * count;
            var discount = 0;
            var vSel = document.getElementById('voucherId');
            if (vSel) discount = parseNum((vSel.options[vSel.selectedIndex]||{}).getAttribute ? vSel.options[vSel.selectedIndex].getAttribute('data-discount') : '0');
            var total = (fieldSum * (1 - discount/100)) + equipTotal;
                var paymentOptionEl = document.querySelector('input[name="bookingPaymentOption"]:checked');
                var paymentOption = paymentOptionEl ? String(paymentOptionEl.value || '').toLowerCase() : 'full';
                var dueNow = paymentOption === 'deposit' ? Math.round(total * 0.3) : Math.round(total);

            document.getElementById('sumCount').textContent     = count;
            document.getElementById('sumFieldPrice').textContent = fmt(fieldSum);
            document.getElementById('sumEquipPrice').textContent = fmt(equipTotal);
            document.getElementById('sumDiscount').textContent  = discount + '%';
            document.getElementById('sumTotal').textContent     = fmt(total);
                document.getElementById('sumDueNow').textContent    = fmt(dueNow);
                document.getElementById('paymentOptionLabel').textContent = paymentOption === 'deposit'
                    ? 'Đặt cọc hôm nay (30%)'
                    : 'Thanh toán hôm nay';

            var btn = document.getElementById('submitBtn');
            var msg = document.getElementById('noSelectMsg');
            if (count > 0) {
                btn.disabled = false;
                btn.className = 'w-full bg-[#008751] hover:bg-emerald-400 text-white py-4 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-xl transition-all hover:-translate-y-1 active:scale-95 flex items-center justify-center gap-2 mt-4 cursor-pointer';
                if (msg) msg.classList.add('hidden');
            } else {
                btn.disabled = true;
                btn.className = 'w-full bg-gray-500/50 text-white/50 py-4 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] transition-all flex items-center justify-center gap-2 mt-4 cursor-not-allowed';
            }
        }

        window.updateSummary = updateSummary;

        function setCellVisual(cb, checked) {
            var label = cb.closest('label');
            if (!label) return;
            var icon  = label.querySelector('.slot-icon');
            if (checked) {
                label.classList.add('picked');
                if (icon) icon.setAttribute('data-lucide','check-square');
            } else {
                label.classList.remove('picked');
                if (icon) icon.setAttribute('data-lucide','clock');
            }
            lucide.createIcons({ nodes: icon ? [icon] : [] });
        }

        window.onCellChange = function(cb) {
            var sid = String(cb.value);
            var meta = scheduleMeta(sid, cb.getAttribute('data-date'), cb.getAttribute('data-start'));
            if (cb.checked) persistedAnchorIds[sid] = true;
            else {
                clearSeriesByKey(meta.key);
                delete persistedAnchorIds[sid];
                delete persistedSelectedIds[sid];
            }
            setCellVisual(cb, cb.checked);
            rebuildAutoRecurringSelections();
            syncPersistedSelectionSet();
            rebuildCarrySelections();
            updateSummary();
        };

        window.selectAll = function() {
            document.querySelectorAll('.slot-cb').forEach(function(cb){
                if (!cb.checked) cb.checked = true;
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
            });
            persistedAnchorIds = {};
            persistedSelectedIds = {};
            rebuildAutoRecurringSelections();
            syncPersistedSelectionSet();
            rebuildCarrySelections();
            updateSummary();
        };

        document.querySelectorAll('.field-type-btn').forEach(function(btn) {
            btn.addEventListener('click', function(){
                var type = this.dataset.type;
                var url = new URL(window.location.href);
                url.searchParams.set('fieldType', type);
                url.searchParams.delete('fieldId');
                window.location.href = url.toString();
            });
        });

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
            if (locVal) params.set('locationId', locVal);
            if (ft) params.set('fieldType', ft);
            if (fieldVal) params.set('fieldId', fieldVal);
            if (ws) params.set('weekStart', ws);
            if (wc) params.set('weekCount', wc);
            var paymentOptionEl = document.querySelector('input[name="bookingPaymentOption"]:checked');
            if (paymentOptionEl && paymentOptionEl.value) params.set('bookingPaymentOption', paymentOptionEl.value);
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
                var paymentOptionEl = document.querySelector('input[name="bookingPaymentOption"]:checked');
                if (paymentOptionEl && paymentOptionEl.value) {
                    nextUrl.searchParams.set('bookingPaymentOption', paymentOptionEl.value);
                }
                var selectedIds = buildSelectedIdsForNavigation();
                if (selectedIds.length > 0) nextUrl.searchParams.set('selectedIds', selectedIds.join(','));
                var anchorIds = buildAnchorIdsForNavigation();
                if (anchorIds.length > 0) nextUrl.searchParams.set('anchorIds', anchorIds.join(','));
                window.location.href = nextUrl.toString();
            });
        });

        document.querySelectorAll('.equipment-qty').forEach(function(inp){
            inp.addEventListener('input', updateSummary);
        });

        window.confirmSubmit = function() {
            var count = getAllSubmittedScheduleIds().length;
            if (count === 0) {
                document.getElementById('noSelectMsg').classList.remove('hidden');
                return false;
            }
            return confirm('Tạo lịch tuần cho ' + count + ' ca và chuyển đến trang xác nhận?\nTổng tiền: ' + document.getElementById('sumTotal').textContent + '\nThanh toán hôm nay: ' + document.getElementById('sumDueNow').textContent);
        };

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
