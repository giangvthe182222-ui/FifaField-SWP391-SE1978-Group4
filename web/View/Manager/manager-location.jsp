<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cơ sở & Sân quản lý - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .field-card:hover { transform: translateY(-8px); }
    </style>
</head>
<body class="antialiased text-gray-900">
<jsp:include page="/View/Layout/HeaderManager.jsp"/>

<main class="max-w-7xl mx-auto px-6 py-12 space-y-12">
    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div class="space-y-2">
            <h1 class="text-4xl font-black uppercase tracking-tight leading-none">Cơ sở & <span class="text-[#008751]">Sân của tôi</span></h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.25em]">Hệ thống điều hành cơ sở vật chất trung tâm</p>
        </div>
        
        <div class="flex gap-3">
            <c:if test="${not empty flash_success}">
                <div class="flex items-center gap-3 px-4 py-3 bg-emerald-50 border border-emerald-100 rounded-2xl text-emerald-700 text-xs font-black uppercase tracking-widest animate-bounce">
                    <i data-lucide="check-circle" class="w-4 h-4"></i> ${flash_success}
                </div>
            </c:if>
        </div>
    </div>

    <c:if test="${not empty location}">
        <!-- LOCATION CONFIGURATION -->
        <section class="bg-white border border-gray-100 rounded-[3.5rem] p-10 shadow-xl shadow-gray-200/40">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-12">
                <div class="space-y-8">
                    <div>
                        <p class="text-[10px] font-black text-emerald-600 uppercase tracking-[0.3em] mb-4">Thông tin vận hành</p>
                        <h2 class="text-3xl font-black uppercase tracking-tight">${location.locationName}</h2>
                        <div class="mt-6 flex flex-col gap-3">
                            <div class="flex items-center gap-3 text-gray-500">
                                <i data-lucide="map-pin" class="w-4 h-4"></i>
                                <span class="text-sm font-bold">${location.address}</span>
                            </div>
                            <div class="flex items-center gap-3 text-gray-500">
                                <i data-lucide="phone-call" class="w-4 h-4"></i>
                                <span class="text-sm font-bold">${location.phoneNumber}</span>
                            </div>
                        </div>
                    </div>

                    <!-- UPDATE STATUS FORM -->
                    <div class="p-8 bg-gray-50 rounded-[2.5rem] border border-gray-100">
                        <form method="post" action="${pageContext.request.contextPath}/manager/location/status" class="space-y-6">
                            <div>
                                <label class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-3">Cập nhật trạng thái cơ sở</label>
                                <div class="flex flex-wrap gap-3">
                                    <select name="status" class="flex-1 min-w-[200px] px-6 py-4 bg-white border border-gray-200 rounded-2xl text-sm font-black uppercase tracking-widest focus:ring-2 focus:ring-[#008751] outline-none appearance-none">
                                        <option value="ACTIVE" ${location.status == 'ACTIVE' ? 'selected' : ''}>● Hoạt động</option>
                                        <option value="INACTIVE" ${location.status == 'INACTIVE' ? 'selected' : ''}>○ Tạm đóng</option>
                                        <option value="MAINTENANCE" ${location.status == 'MAINTENANCE' ? 'selected' : ''}>⚠ Bảo trì</option>
                                    </select>
                                    <button type="submit" class="px-8 py-4 bg-[#008751] text-white rounded-2xl font-black text-[10px] uppercase tracking-widest hover:bg-black transition-all shadow-lg shadow-emerald-900/10">Lưu thay đổi</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="rounded-[2.5rem] overflow-hidden border border-gray-100 shadow-sm relative group">
                    <c:choose>
                        <c:when test="${not empty location.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${location.imageUrl}" class="w-full h-full object-cover min-h-[350px] group-hover:scale-105 transition-transform duration-700"/>
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/assets/img/default_cluster.jpg" class="w-full h-full object-cover min-h-[350px]"/>
                        </c:otherwise>
                    </c:choose>
                    <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
                    <div class="absolute bottom-8 left-8">
                        <span class="px-4 py-2 bg-white/20 backdrop-blur-md rounded-xl text-white text-[10px] font-black uppercase tracking-widest">Phối cảnh cơ sở</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- FIELD LIST -->
        <section class="space-y-8">
            <div class="flex items-end justify-between px-4">
                <div class="flex items-center gap-4">
                    <div class="w-2 h-10 bg-[#008751] rounded-full"></div>
                    <div>
                        <h2 class="text-3xl font-black uppercase tracking-tight">Danh sách sân bóng</h2>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tổng cộng ${fn:length(fields)} sân tại cơ sở này</p>
                    </div>
                </div>
            </div>

            <c:if test="${empty fields}">
                <div class="bg-white rounded-[3rem] border-2 border-dashed border-gray-200 p-20 text-center">
                    <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                        <i data-lucide="folder-open" class="w-8 h-8"></i>
                    </div>
                    <p class="text-gray-400 text-xs font-black uppercase tracking-widest">Chưa có sân nào được tạo</p>
                </div>
            </c:if>

            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                <c:forEach var="f" items="${fields}">
                    <article class="field-card group bg-white border border-gray-100 rounded-[2.5rem] shadow-xl shadow-gray-200/40 p-6 transition-all duration-500">
                        <div class="relative w-full h-56 rounded-[2rem] overflow-hidden bg-gray-100 mb-6">
                            <c:choose>
                                <c:when test="${not empty f.imageUrl}">
                                    <img src="${pageContext.request.contextPath}/${f.imageUrl}" class="w-full h-full object-cover"/>
                                </c:when>
                                <c:otherwise>
                                    <img src="${pageContext.request.contextPath}/assets/img/default_field.jpg" class="w-full h-full object-cover"/>
                                </c:otherwise>
                            </c:choose>
                            <div class="absolute top-4 right-4">
                                <span class="px-3 py-1 bg-white/90 backdrop-blur rounded-full text-[9px] font-black uppercase tracking-widest shadow-sm">
                                    ${f.fieldType}
                                </span>
                            </div>
                        </div>

                        <div class="space-y-5 px-2">
                            <div class="flex justify-between items-start">
                                <h3 class="text-xl font-black uppercase tracking-tight group-hover:text-[#008751] transition-colors">${f.fieldName}</h3>
                                <div class="flex items-center gap-1 text-[10px] font-bold text-gray-400 uppercase">
                                    <i data-lucide="shield-check" class="w-3 h-3 text-emerald-500"></i> ${f.fieldCondition}
                                </div>
                            </div>

                            <div class="pt-4 border-t border-gray-50">
                                <form method="post" action="${pageContext.request.contextPath}/manager/field/status" class="grid grid-cols-1 gap-3">
                                    <input type="hidden" name="fieldId" value="${f.fieldId}">
                                    <div class="flex items-center gap-2">
                                        <select name="status" class="flex-1 px-4 py-3 bg-gray-50 border border-gray-100 rounded-xl text-[10px] font-black uppercase tracking-widest outline-none">
                                            <option value="available" ${fn:toLowerCase(f.status) == 'available' ? 'selected' : ''}>Sẵn sàng</option>
                                            <option value="unavailable" ${fn:toLowerCase(f.status) == 'unavailable' ? 'selected' : ''}>Tạm khóa</option>
                                        </select>
                                        <button type="submit" class="px-4 py-3 bg-gray-900 text-white rounded-xl hover:bg-[#008751] transition-all">
                                            <i data-lucide="refresh-cw" class="w-4 h-4"></i>
                                        </button>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/manager/fields/detail?fieldId=${f.fieldId}" 
                                       class="w-full py-3 bg-emerald-50 text-[#008751] rounded-xl text-[10px] font-black uppercase tracking-[0.2em] text-center hover:bg-[#008751] hover:text-white transition-all">
                                        Xem lịch sân
                                    </a>
                                </form>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </section>

        <!-- FOOTER NAV -->
        <section class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <a href="${pageContext.request.contextPath}/manager/location-equipment" class="group relative overflow-hidden bg-white border border-gray-100 rounded-[2.5rem] p-10 shadow-xl shadow-gray-200/30 transition-all hover:-translate-y-1">
                <div class="absolute -right-6 -top-6 text-emerald-500/5 group-hover:scale-110 transition-transform duration-500">
                    <i data-lucide="package" class="w-40 h-40"></i>
                </div>
                <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Kho vận</p>
                <h2 class="text-3xl font-black uppercase tracking-tight mt-2 leading-none">Dụng cụ cơ sở</h2>
                <p class="text-xs font-bold text-gray-500 mt-4">Quản lý số lượng và tình trạng thiết bị thuê sân.</p>
            </a>
            
            <a href="${pageContext.request.contextPath}/manager/dashboard" class="group relative overflow-hidden bg-gray-900 rounded-[2.5rem] p-10 shadow-xl shadow-gray-900/20 transition-all hover:-translate-y-1">
                <div class="absolute -right-6 -top-6 text-white/5 group-hover:scale-110 transition-transform duration-500">
                    <i data-lucide="layout-dashboard" class="w-40 h-40"></i>
                </div>
                <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Hệ thống</p>
                <h2 class="text-3xl font-black uppercase tracking-tight mt-2 leading-none text-white">Quay lại Dashboard</h2>
                <div class="mt-6 inline-flex items-center gap-2 text-emerald-400 font-black text-[10px] uppercase tracking-widest">
                    Về trang chủ <i data-lucide="arrow-right" class="w-4 h-4"></i>
                </div>
            </a>
        </section>
    </c:if>
</main>

<jsp:include page="/View/Layout/FooterManager.jsp"/>
<script>lucide.createIcons();</script>
</body>
</html>