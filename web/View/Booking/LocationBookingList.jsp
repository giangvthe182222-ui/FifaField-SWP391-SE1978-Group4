<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Đặt Sân Chi Nhánh - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2.5rem; }
        .custom-scrollbar::-webkit-scrollbar { height: 6px; width: 6px; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #008751; border-radius: 10px; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="flex-grow max-w-7xl mx-auto px-6 py-12 w-full space-y-12">
    
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div class="space-y-2">
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                ĐẶT SÂN <span class="text-[#008751]">${locationName}</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">Quản lý điều phối chi tiết tại chi nhánh</p>
        </div>
        
        <!-- Quick Filters -->
        <form method="get" action="${pageContext.request.contextPath}/staff/locationBookings" class="flex flex-wrap items-center gap-4">
            <div>
                <input type="date" name="date" value="${param.date}" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
            </div>
            <div>
                <input type="time" name="time" value="${param.time}" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
            </div>
            <div>
                <input type="text" name="customerName" placeholder="Tên khách hàng..." value="${param.customerName}" class="px-4 py-3 bg-white border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
            </div>
            <button type="submit" class="bg-[#008751] text-white px-6 py-3 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-emerald-400 transition-all">Lọc</button>
            <a href="${pageContext.request.contextPath}/staff/locationBookings" class="text-[10px] font-black text-gray-400 uppercase tracking-widest hover:text-[#008751]">Xóa</a>
        </form>
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

    <!-- Vertical List of Horizontal Bars -->
    <div class="space-y-6">
        <div class="flex items-center gap-4">
            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Danh sách đơn đặt tại chi nhánh</h2>
        </div>

        <c:choose>
            <c:when test="${empty bookings}">
                <div class="py-20 text-center bg-white elite-card border-2 border-dashed border-gray-100 shadow-sm">
                    <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Không có đơn đặt nào cho chi nhánh này</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="space-y-4 pb-8">
                    <c:forEach var="b" items="${bookings}">
                                <div class="bg-white rounded-[2rem] border border-gray-100 shadow-lg shadow-gray-200/30 hover:shadow-xl hover:shadow-[#008751]/10 transition-all p-6 md:p-8 flex flex-col lg:flex-row items-center gap-6 lg:gap-10">
                                    <div class="flex items-center gap-6 w-full lg:w-auto">
                                        <div class="flex-grow">
                                            <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight">${b.fieldName}</h3>
                                            <div class="flex items-center gap-2 mt-1">
                                                <span class="px-3 py-1 rounded-full text-[8px] font-black uppercase tracking-widest 
                                                    ${b.status == 'cancelled' ? 'bg-rose-50 text-rose-600' : 
                                                      b.status == 'completed' ? 'bg-emerald-50 text-[#008751]' : 
                                                      'bg-blue-50 text-blue-600'} border border-current opacity-80">
                                                    ${b.status}
                                                </span>
                                            </div>
                                            <div class="text-sm text-gray-600 mt-2">Customer: ${b.customerName}</div>
                                        </div>
                                    </div>

                            <!-- Middle: Info -->
                            <div class="hidden lg:block flex-grow space-y-2">
                                <div class="flex items-center gap-6">
                                    <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">${b.bookingDate}</p>
                                    <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">${b.startTime} - ${b.endTime}</p>
                                    <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest">ID: ${b.bookingId}</p>
                                </div>
                            </div>

                            <!-- Right: Status Update & Action -->
                            <div class="flex flex-col sm:flex-row items-center gap-4 w-full lg:w-auto shrink-0">
                                <div class="flex items-center gap-2 bg-gray-50 p-2 rounded-2xl border border-gray-100 w-full sm:w-auto">
                                    <select name="status_${b.bookingId}" class="bg-transparent border-none text-[10px] font-black uppercase tracking-widest text-gray-700 focus:ring-0 cursor-pointer px-3">
                                        <option value="pending" ${b.status == 'pending' ? 'selected' : ''}>pending</option>
                                        <option value="checked in" ${b.status == 'checked in' ? 'selected' : ''}>checked in</option>
                                        <option value="completed" ${b.status == 'completed' ? 'selected' : ''}>completed</option>
                                        <option value="cancelled" ${b.status == 'cancelled' ? 'selected' : ''}>cancelled</option>
                                        <option value="refunded" ${b.status == 'refunded' ? 'selected' : ''}>refunded</option>
                                    </select>
                                    <button type="button" onclick="updateStatus('${b.bookingId}')" class="bg-gray-900 text-white p-2 rounded-xl hover:bg-[#008751] transition-colors">Update</button>
                                </div>
                                <a href="${pageContext.request.contextPath}/staff/bookingDetail?id=${b.bookingId}" class="w-full sm:w-auto bg-white border-2 border-gray-100 text-gray-400 px-6 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all flex items-center justify-center gap-2">Chi tiết</a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    
    function updateStatus(bookingId) {
        var status = document.getElementsByName('status_' + bookingId)[0].value;
        var f = document.createElement('form');
        f.method = 'post';
        f.action = '${pageContext.request.contextPath}/staff/locationBookings';
        
        var i1 = document.createElement('input');
        i1.name = 'bookingId';
        i1.value = bookingId;
        f.appendChild(i1);
        
        var i2 = document.createElement('input');
        i2.name = 'status';
        i2.value = status;
        f.appendChild(i2);
        
        document.body.appendChild(f);
        f.submit();
    }
</script>
</body>
</html>
