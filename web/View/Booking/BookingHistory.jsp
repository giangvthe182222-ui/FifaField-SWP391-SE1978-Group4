<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="flex-grow max-w-7xl mx-auto px-6 py-12 w-full space-y-12">
    
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div class="space-y-2">
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                LỊCH SỬ <span class="text-[#008751]">ĐẶT SÂN</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">Hành trình thi đấu của bạn tại hệ thống FIFAFIELD</p>
        </div>
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
                    <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-2">Hãy bắt đầu trận đấu đầu tiên của bạn ngay hôm nay!</p>
                    <a href="${pageContext.request.contextPath}/booking" class="inline-flex items-center gap-2 mt-8 px-8 py-4 bg-[#008751] text-white rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-emerald-400 transition-all hover:-translate-y-1">
                        ĐẶT SÂN NGAY
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <!-- The Vertical List of Horizontal Bars -->
                <div class="space-y-4 pb-8">
                    <c:forEach var="b" items="${bookings}">
                        <div class="bg-white rounded-[2rem] border border-gray-100 shadow-lg shadow-gray-200/30 hover:shadow-xl hover:shadow-[#008751]/10 transition-all group relative overflow-hidden p-6 md:p-8 flex flex-col md:flex-row items-center gap-6 md:gap-10">
                            
                            <!-- Left: Icon & Status -->
                            <div class="flex items-center gap-6 w-full md:w-auto">
                                <div class="w-16 h-16 bg-emerald-50 rounded-2xl flex items-center justify-center text-[#008751] group-hover:bg-[#008751] group-hover:text-white transition-colors duration-500 shrink-0">
                                </div>
                                <div class="md:hidden flex-grow">
                                    <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight">${b.fieldName}</h3>
                                    <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.status == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-emerald-50 text-[#008751] border border-emerald-100'}">
                                        ${b.status}
                                    </span>
                                </div>
                            </div>

                            <!-- Middle: Info -->
                                <div class="hidden md:block flex-grow space-y-2">
                                <div class="flex items-center gap-4">
                                    <h3 class="text-xl font-black text-gray-900 uppercase tracking-tight group-hover:text-[#008751] transition-colors">${b.fieldName}</h3>
                                    <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest ${b.status == 'cancelled' ? 'bg-rose-50 text-rose-600 border border-rose-100' : 'bg-emerald-50 text-[#008751] border border-emerald-100'}">
                                        ${b.status}
                                    </span>
                                </div>
                                <div class="flex items-center gap-6">
                                    <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">${b.bookingDate}</p>
                                    <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">${b.startTime} - ${b.endTime}</p>
                                    <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">ID: ${b.bookingId}</p>
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
                                    <p class="text-xs font-black text-[#008751]"><fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> đ</p>
                                </div>
                            </div>

                            <!-- Right: Price & Action -->
                            <div class="flex items-center justify-between md:justify-end gap-8 w-full md:w-auto shrink-0">
                                <div class="hidden md:block text-right">
                                    <p class="text-[8px] font-black text-gray-400 uppercase tracking-widest mb-1">Tổng cộng</p>
                                    <p class="text-2xl font-black text-[#008751] tracking-tighter">
                                        <fmt:formatNumber value="${b.totalPrice}" pattern="#,##0"/> đ
                                    </p>
                                </div>
                                <a href="${pageContext.request.contextPath}/customer/bookingDetail?id=${b.bookingId}" class="w-full md:w-auto bg-gray-900 text-white px-8 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest flex items-center justify-center gap-2 hover:bg-[#008751] transition-all hover:scale-[1.05] active:scale-95 shadow-lg shadow-gray-200">Chi tiết</a>
                            </div>

                            <!-- Background Decoration -->
                            
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Stats Section (Optional but adds to Elite feel) -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="bg-white elite-card p-8 border border-gray-100 shadow-sm flex items-center gap-6">
                <div class="w-14 h-14 bg-emerald-50 rounded-2xl flex items-center justify-center text-[#008751]">
                </div>
                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Trận đấu đã chơi</p>
                    <p class="text-3xl font-black text-gray-900 tracking-tighter">${bookings.size()}</p>
                </div>
            </div>
        <div class="bg-white elite-card p-8 border border-gray-100 shadow-sm flex items-center gap-6">
            <div class="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-600">
            </div>
            <div>
                <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Giờ thi đấu</p>
                <p class="text-3xl font-black text-gray-900 tracking-tighter">--</p>
            </div>
        </div>
        <div class="bg-white elite-card p-8 border border-gray-100 shadow-sm flex items-center gap-6">
            <div class="w-14 h-14 bg-amber-50 rounded-2xl flex items-center justify-center text-amber-600">
            </div>
            <div>
                <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Điểm uy tín</p>
                <p class="text-3xl font-black text-gray-900 tracking-tighter">100</p>
            </div>
        </div>
    </div>

</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

    
</body>
</html>
