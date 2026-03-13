<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng Điều Khiển - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2.5rem; }
        .fifa-green { color: #008751; }
        .bg-fifa-green { background-color: #008751; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="flex-grow max-w-7xl mx-auto w-full px-6 py-12 space-y-12">
    
    <!-- Welcome Section -->
    <section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 relative overflow-hidden group">
        <!-- Decoration -->
        <div class="absolute -top-10 -right-10 opacity-[0.03] group-hover:scale-110 transition-transform duration-700">
            <i data-lucide="shield" class="w-64 h-64"></i>
        </div>

        <div class="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-8">
            <div class="space-y-2">
                <p class="text-[10px] font-black tracking-[0.3em] text-gray-400 uppercase">Hệ thống FIFAFIELD 2026</p>
                <h1 class="text-4xl font-black text-gray-900 uppercase tracking-tight">
                    Xin chào, <span class="text-[#008751]"><%= currentUser.getFullName() %></span>
                </h1>
                <p class="text-gray-500 font-bold text-sm uppercase tracking-widest opacity-60">Sẵn sàng cho trận đấu tiếp theo của bạn?</p>
            </div>
            <a href="${pageContext.request.contextPath}/booking" class="px-10 py-5 rounded-[1.8rem] bg-[#008751] text-white font-black uppercase text-xs tracking-[0.2em] hover:bg-emerald-400 transition-all hover:-translate-y-1 shadow-2xl shadow-[#008751]/20 flex items-center justify-center gap-3">
                <i data-lucide="zap" class="w-4 h-4"></i>
                Đặt sân ngay
            </a>
        </div>

        <div class="mt-12">
            <jsp:include page="/View/Layout/CustomerQuickPanel.jsp"/>
        </div>
    </section>

    <!-- Locations Section -->
    <section id="locations" class="space-y-8">
        <div class="flex items-center gap-4">
            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
            <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">Danh sách cơ sở thi đấu</h2>
        </div>

        <!-- Filter Form -->
        <form method="get" action="${pageContext.request.contextPath}/customer/dashboard" class="bg-white rounded-[2rem] border border-gray-100 p-6 shadow-xl shadow-gray-200/30 flex flex-col lg:flex-row gap-4 items-end">
            <div class="flex-1 w-full">
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Cơ sở</label>
                <div class="relative mt-2">
                    <i data-lucide="building-2" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <select name="locationName" class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all">
                        <option value="">Tất cả cơ sở</option>
                        <c:forEach var="nameOpt" items="${locationNameOptions}">
                            <option value="${nameOpt}" ${locationName == nameOpt ? 'selected' : ''}>${nameOpt}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="flex-1 w-full">
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Tìm kiếm tên cơ sở</label>
                <div class="relative mt-2">
                    <i data-lucide="search" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <input type="text" name="searchName" placeholder="Nhập tên cơ sở..." value="${searchName}" 
                           class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all"/>
                </div>
            </div>
            <div class="flex-1 w-full">
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Tìm kiếm địa chỉ</label>
                <div class="relative mt-2">
                    <i data-lucide="map-pin" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <input type="text" name="searchAddress" placeholder="Nhập địa chỉ..." value="${searchAddress}"
                           class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all"/>
                </div>
            </div>
            <div class="flex gap-3 w-full lg:w-auto">
                <button type="submit" class="flex-1 lg:flex-none px-8 py-4 rounded-2xl bg-[#008751] text-white font-black uppercase text-[10px] tracking-widest hover:bg-emerald-400 transition-all shadow-lg shadow-[#008751]/20">
                    Tìm kiếm
                </button>
                <c:if test="${searchName != null || searchAddress != null || locationName != null}">
                    <a href="${pageContext.request.contextPath}/customer/dashboard" class="flex-1 lg:flex-none px-8 py-4 rounded-2xl border-2 border-gray-100 bg-white text-gray-400 font-black uppercase text-[10px] tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all text-center">
                        Xóa lọc
                    </a>
                </c:if>
            </div>
        </form>

        <c:if test="${not empty error}">
            <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
                <i data-lucide="alert-circle" class="w-5 h-5 text-rose-500"></i>
                <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${error}</p>
            </div>
        </c:if>
<div class="bg-white rounded-[2rem] border border-gray-100 p-6 shadow-xl shadow-gray-200/30 mb-8">
    
    <div class="flex items-center gap-4 mb-6">
        <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
        <h2 class="text-[10px] font-black text-gray-400 uppercase tracking-[0.3em]">
            Bản đồ cơ sở
        </h2>
    </div>

    <div id="map" class="w-full h-[500px] rounded-2xl"></div>

</div>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <c:forEach var="loc" items="${locations}">
                <article class="bg-white border border-gray-100 rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 flex flex-col gap-6 group hover:shadow-2xl hover:shadow-[#008751]/10 transition-all">
                    <div class="w-full h-56 rounded-[2rem] overflow-hidden bg-gray-50 relative">
                        <c:choose>
                            <c:when test="${not empty loc.imageUrl}">
                                <img src="${pageContext.request.contextPath}/${loc.imageUrl}" alt="${loc.locationName}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"/>
                            </c:when>
                            <c:otherwise>
                                <div class="w-full h-full flex items-center justify-center text-gray-200">
                                    <i data-lucide="image" class="w-12 h-12"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div class="absolute top-4 right-4">
                            <span class="px-4 py-2 bg-white/90 backdrop-blur-md rounded-full text-[9px] font-black uppercase tracking-widest text-[#008751] shadow-sm">
                                Đang hoạt động
                            </span>
                        </div>
                    </div>
                    <div class="space-y-3">
                        <h3 class="text-2xl font-black text-gray-900 tracking-tight uppercase group-hover:text-[#008751] transition-colors">${loc.locationName}</h3>
                        <div class="flex items-start gap-2 text-gray-500">
                            <i data-lucide="map-pin" class="w-4 h-4 mt-0.5 shrink-0"></i>
                            <p class="font-bold text-sm">${loc.address}</p>
                        </div>
                        <div class="flex items-center gap-2 text-gray-400">
                            <i data-lucide="phone" class="w-3 h-3"></i>
                            <p class="text-[10px] font-black uppercase tracking-widest">Hotline: ${loc.phoneNumber}</p>
                        </div>
                    </div>
                    <div class="rounded-[1.8rem] overflow-hidden border border-gray-100 bg-slate-50">
                        <iframe
                            title="Bản đồ ${loc.locationName}"
                            src="https://maps.google.com/maps?hl=vi&q=${loc.address}&t=&z=15&ie=UTF8&iwloc=B&output=embed"
                            class="w-full h-56 border-0"
                            loading="lazy"
                            referrerpolicy="no-referrer-when-downgrade"></iframe>
                    </div>
                    <div class="mt-auto pt-6 border-t border-gray-50 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
                        <div class="flex -space-x-2">
                            <div class="w-8 h-8 rounded-full border-2 border-white bg-gray-100 flex items-center justify-center text-[8px] font-black">7v7</div>
                            <div class="w-8 h-8 rounded-full border-2 border-white bg-gray-100 flex items-center justify-center text-[8px] font-black">11v11</div>
                        </div>
                        <div class="flex items-center gap-2 w-full sm:w-auto">
                            <a href="https://www.google.com/maps/search/?api=1&query=${loc.address}"
                               target="_blank"
                               rel="noopener noreferrer"
                               class="px-6 py-4 rounded-2xl border border-blue-100 text-blue-700 font-black text-[10px] uppercase tracking-widest hover:border-blue-500 hover:text-blue-600 transition-all text-center bg-blue-50">
                                Google Maps
                            </a>
                            <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${loc.locationId}"
                               class="px-6 py-4 rounded-2xl border border-gray-200 text-gray-700 font-black text-[10px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all text-center">
                                Xem cơ sở
                            </a>
                            <a href="${pageContext.request.contextPath}/booking?locationId=${loc.locationId}"
                               class="px-6 py-4 rounded-2xl bg-gray-900 text-white font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-all hover:scale-[1.03] active:scale-95 shadow-lg shadow-gray-200 text-center">
                                Đặt sân
                            </a>
                        </div>
                    </div>
                </article>
            </c:forEach>
        </div>

        <c:if test="${empty locations}">
            <div class="bg-white rounded-[2.5rem] border-2 border-dashed border-gray-100 p-20 text-center">
                <i data-lucide="search-x" class="w-12 h-12 text-gray-200 mx-auto mb-4"></i>
                <p class="text-gray-300 font-black uppercase tracking-widest text-[10px]">Chưa có dữ liệu cơ sở thi đấu</p>
            </div>
        </c:if>

        <!-- Pagination -->
        <c:if test="${totalPages > 1}">
            <div class="flex items-center justify-center gap-3 py-10">
                <c:if test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/customer/dashboard?page=1<c:if test="${not empty locationName}">&locationName=${locationName}</c:if><c:if test="${not empty searchName}">&searchName=${searchName}</c:if><c:if test="${not empty searchAddress}">&searchAddress=${searchAddress}</c:if>"
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevrons-left" class="w-4 h-4"></i>
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/dashboard?page=${currentPage - 1}<c:if test="${not empty locationName}">&locationName=${locationName}</c:if><c:if test="${not empty searchName}">&searchName=${searchName}</c:if><c:if test="${not empty searchAddress}">&searchAddress=${searchAddress}</c:if>" 
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevron-left" class="w-4 h-4"></i>
                    </a>
                </c:if>
                
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <c:choose>
                        <c:when test="${i == currentPage}">
                            <span class="w-10 h-10 rounded-xl bg-[#008751] text-white flex items-center justify-center font-black text-xs shadow-lg shadow-[#008751]/20">${i}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/customer/dashboard?page=${i}<c:if test="${not empty locationName}">&locationName=${locationName}</c:if><c:if test="${not empty searchName}">&searchName=${searchName}</c:if><c:if test="${not empty searchAddress}">&searchAddress=${searchAddress}</c:if>" 
                               class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 font-black text-xs hover:border-[#008751] hover:text-[#008751] transition-all">
                                ${i}
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:if test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/customer/dashboard?page=${currentPage + 1}<c:if test="${not empty locationName}">&locationName=${locationName}</c:if><c:if test="${not empty searchName}">&searchName=${searchName}</c:if><c:if test="${not empty searchAddress}">&searchAddress=${searchAddress}</c:if>" 
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevron-right" class="w-4 h-4"></i>
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/dashboard?page=${totalPages}<c:if test="${not empty locationName}">&locationName=${locationName}</c:if><c:if test="${not empty searchName}">&searchName=${searchName}</c:if><c:if test="${not empty searchAddress}">&searchAddress=${searchAddress}</c:if>" 
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevrons-right" class="w-4 h-4"></i>
                    </a>
                </c:if>
            </div>
            <p class="text-center text-[10px] font-black text-gray-400 uppercase tracking-widest">Trang <span class="text-[#008751]">${currentPage}</span> / ${totalPages} (Tổng: ${totalItems} cơ sở)</p>
        </c:if>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>
<script>

const locations = [
<c:forEach var="loc" items="${locations}" varStatus="loop">
{
    name: "${loc.locationName}",
    address: "${loc.address}",
    bookingUrl: "${pageContext.request.contextPath}/booking?locationId=${loc.locationId}"
}<c:if test="${!loop.last}">,</c:if>
</c:forEach>
];

</script>
<script>

function initMap() {

    const map = new google.maps.Map(document.getElementById("map"), {
        zoom: 12,
        center: { lat: 10.8231, lng: 106.6297 } // trung tâm HCM
    });

    const geocoder = new google.maps.Geocoder();

    locations.forEach((loc, index) => {

    setTimeout(() => {

        geocoder.geocode({ address: loc.address }, function(results, status) {

            if (status === "OK") {

                const marker = new google.maps.Marker({
                    map: map,
                    position: results[0].geometry.location,
                    title: loc.name
                });

            }

        });

    }, index * 200);

});

    });

}

</script>
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyXXXXXXX&callback=initMap" async defer></script></body>
</html>
