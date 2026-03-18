<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cơ sở phụ trách - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .nav-card:hover .icon-bg { transform: scale(1.1) rotate(5deg); }
    </style>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="max-w-7xl mx-auto px-6 py-12 space-y-10">
    <!-- BREADCRUMB & HEADER -->
    <div class="space-y-2">
        <div class="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 border border-emerald-100 rounded-full text-[10px] font-black text-[#008751] uppercase tracking-[0.2em]">
            <i data-lucide="map-pin" class="w-3 h-3"></i> Thông tin cơ sở
        </div>
        <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
            CHI TIẾT <span class="text-[#008751]">LOCATION</span>
        </h1>
    </div>

    <!-- MAIN LOCATION DETAIL -->
    <section class="bg-white border border-gray-100 rounded-[3rem] p-8 md:p-12 shadow-xl shadow-gray-200/50">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-start">
            <div class="space-y-8">
                <div>
                    <h2 class="text-4xl font-black uppercase tracking-tighter text-gray-900 leading-tight">${location.locationName}</h2>
                    <div class="mt-6 space-y-4">
                        <div class="flex items-start gap-4">
                            <div class="w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 shrink-0">
                                <i data-lucide="navigation" class="w-5 h-5"></i>
                            </div>
                            <p class="text-lg font-medium text-gray-600">${location.address}</p>
                        </div>
                        <div class="flex items-center gap-4">
                            <div class="w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 shrink-0">
                                <i data-lucide="phone" class="w-5 h-5"></i>
                            </div>
                            <p class="text-lg font-bold text-gray-900">${location.phoneNumber}</p>
                        </div>
                        <div class="flex items-center gap-4">
                            <div class="w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 shrink-0">
                                <i data-lucide="activity" class="w-5 h-5"></i>
                            </div>
                            <span class="px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest 
                                ${location.status == 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}">
                                ${location.status}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- NAVIGATION BUTTONS -->
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-6">
                    <a href="${pageContext.request.contextPath}/staff/fields" 
                       class="nav-card relative overflow-hidden group bg-gray-900 p-8 rounded-[2rem] text-white transition-all hover:-translate-y-1">
                        <div class="absolute -right-4 -top-4 opacity-10 icon-bg transition-transform duration-500">
                            <i data-lucide="calendar" class="w-24 h-24"></i>
                        </div>
                        <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Điều hướng</p>
                        <h3 class="text-xl font-black uppercase mt-1">Sân & Lịch</h3>
                        <div class="mt-4 flex items-center gap-2 text-emerald-400 font-bold text-xs">
                            Chi tiết <i data-lucide="arrow-right" class="w-4 h-4"></i>
                        </div>
                    </a>

                    <a href="${pageContext.request.contextPath}/staff/locationBookings" 
                       class="nav-card relative overflow-hidden group bg-white border border-gray-100 p-8 rounded-[2rem] shadow-sm transition-all hover:shadow-lg hover:-translate-y-1">
                        <div class="absolute -right-4 -top-4 text-emerald-500/5 icon-bg transition-transform duration-500">
                            <i data-lucide="clipboard-check" class="w-24 h-24"></i>
                        </div>
                        <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Quản lý</p>
                        <h3 class="text-xl font-black uppercase mt-1">Bookings</h3>
                        <div class="mt-4 flex items-center gap-2 text-[#008751] font-bold text-xs">
                            Truy cập <i data-lucide="arrow-right" class="w-4 h-4"></i>
                        </div>
                    </a>
                </div>
            </div>

            <!-- MEDIA SECTION -->
            <div class="space-y-6">
                <div class="aspect-video rounded-[2.5rem] overflow-hidden bg-gray-100 shadow-inner border border-gray-100 group">
                    <c:choose>
                        <c:when test="${not empty location.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${location.imageUrl}" alt="Ảnh cơ sở" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"/>
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/assets/img/default_cluster.jpg" alt="Ảnh cơ sở" class="w-full h-full object-cover"/>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="rounded-[2.5rem] overflow-hidden border border-gray-100 shadow-sm h-64">
                    <iframe class="w-full h-full grayscale hover:grayscale-0 transition-all duration-500" 
                            loading="lazy" 
                            src="https://maps.google.com/maps?q=${fn:replace(location.address, ' ', '+')}&z=15&output=embed"></iframe>
                </div>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
<script>lucide.createIcons();</script>
</body>
</html>