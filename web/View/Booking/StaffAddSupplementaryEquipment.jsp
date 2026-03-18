<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Equipment Bổ Sung - FIFAFIELD</title>
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
                THÊM <span class="text-[#008751]">EQUIPMENT BỔ SUNG</span>
            </h1>
        </div>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em] ml-14">Tính tiền riêng, không giới hạn deadline</p>
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
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
            
            <!-- Main Info Section -->
            <div class="md:col-span-2 space-y-8">
                <!-- Original Booking Info -->
                <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                    <div class="flex items-center gap-4">
                        <div class="w-8 h-1 bg-gray-200 rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Thông tin booking gốc</h2>
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
                    </div>

                    <div class="space-y-1">
                        <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">Khách hàng</p>
                        <p class="font-bold text-gray-700">${booking.customerName}</p>
                    </div>
                </section>

                <!-- Equipment Form -->
                <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 space-y-8">
                    <div class="flex items-center gap-4">
                        <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Chọn vật tư bổ sung</h2>
                    </div>

                    <c:choose>
                        <c:when test="${not empty availableEquipments}">
                            <form method="post" action="${pageContext.request.contextPath}/staff/addSupplementaryEquipment" class="space-y-6">
                                <input type="hidden" name="bookingId" value="${booking.bookingId}" />

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

                                <button type="submit" class="w-full bg-gray-900 hover:bg-[#008751] text-white px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all hover:-translate-y-1 active:scale-95 inline-flex items-center justify-center gap-2 shadow-lg shadow-gray-200">
                                    <i data-lucide="package-plus" class="w-4 h-4"></i>
                                    THÊM EQUIPMENT BỔ SUNG
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="py-6 text-center bg-gray-50 rounded-3xl border-2 border-dashed border-gray-100">
                                <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Không còn equipment khả dụng tại location này</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>

            <!-- Info Card -->
            <div class="space-y-6">
                <div class="bg-blue-50 rounded-3xl border border-blue-100 p-8 space-y-4">
                    <div class="flex items-start gap-3">
                        <i data-lucide="info" class="w-5 h-5 text-blue-600 shrink-0 mt-1"></i>
                        <div>
                            <p class="text-sm font-black text-blue-900 uppercase tracking-tight mb-2">Lưu ý</p>
                            <ul class="text-xs font-semibold text-blue-800 space-y-2">
                                <li>• Tính tiền riêng, không cộng vào booking gốc</li>
                                <li>• Không có deadline thanh toán</li>
                                <li>• Trả tiền lúc nào cũng được</li>
                                <li>• Trừ kho trực tiếp khi thêm</li>
                            </ul>
                        </div>
                    </div>
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
</script>
</body>
</html>
