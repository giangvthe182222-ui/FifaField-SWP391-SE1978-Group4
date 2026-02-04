<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hệ thống Cụm sân | FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; }
        .fifa-gradient { background: linear-gradient(135deg, #008751 0%, #005c37 100%); }
    </style>
</head>

<jsp:include page="/View/Layout/Header.jsp"/>

<body class="bg-[#f8fafc] min-h-screen">

<div class="max-w-7xl mx-auto px-6 py-12">

    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
        <div class="space-y-2">
            <div class="flex items-center gap-3">
                <div class="w-12 h-2 bg-[#008751] rounded-full"></div>
                <span class="text-[10px] font-black text-[#008751] uppercase tracking-[0.4em]">
                    Infrastructure
                </span>
            </div>
            <h1 class="text-5xl font-black text-slate-900 tracking-tighter uppercase italic leading-none">
                FIFA FIELD SYSTEM
            </h1>
            <p class="text-slate-400 font-bold uppercase text-xs tracking-[0.2em]">
                Quản lý danh sách cụm sân toàn hệ thống
            </p>
        </div>

        <a href="${pageContext.request.contextPath}/locations/add"
           class="fifa-gradient text-white px-10 py-6 rounded-[2rem] font-black flex items-center gap-4 transition-all shadow-2xl shadow-[#008751]/40 hover:scale-105 active:scale-95 group">
            <i data-lucide="plus-circle"
               class="w-6 h-6 stroke-[3] group-hover:rotate-90 transition-transform"></i>
            <span class="tracking-tight uppercase">Thêm cụm sân mới</span>
        </a>
    </div>

    <!-- LOCATION GRID -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-10">
        <c:forEach var="loc" items="${locations}">
            <div class="bg-white rounded-[4rem] p-3 shadow-xl shadow-slate-200/50 border border-slate-100 hover:border-[#008751]/30 transition-all group overflow-hidden relative">
                <div class="bg-[#f1f5f9] rounded-[3.5rem] p-10 flex flex-col h-full relative overflow-hidden">

                    <div class="absolute -right-20 -top-20 w-64 h-64 bg-white/50 rounded-full blur-3xl group-hover:bg-[#008751]/10 transition-colors"></div>

                    <!-- TOP -->
                    <div class="flex justify-between items-start mb-10 relative z-10">
                        <div class="bg-white p-6 rounded-[2rem] shadow-lg text-[#008751] group-hover:scale-110 transition-transform">
                            <i data-lucide="map-pin" class="w-8 h-8 stroke-[2.5]"></i>
                        </div>
                        <div class="flex flex-col items-end gap-2">
                            <span class="bg-[#008751] text-white px-5 py-2 rounded-full text-[10px] font-black tracking-widest uppercase shadow-lg shadow-[#008751]/20">
                                ${loc.status}
                            </span>
                            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-tighter">
                                ID: ${loc.locationId.toString().substring(0,8)}
                            </span>
                        </div>
                    </div>

                    <!-- CONTENT -->
                    <div class="relative z-10 flex flex-col h-full">
                        <div class="mb-6 w-full h-40 rounded-2xl overflow-hidden">
                            <c:choose>
                                <c:when test="${not empty loc.imageUrl}">
                                    <img src="${pageContext.request.contextPath}/${loc.imageUrl}"
                                         alt="${loc.locationName}"
                                         class="w-full h-full object-cover" />
                                </c:when>
                                <c:otherwise>
                                    <img src="${pageContext.request.contextPath}/assets/img/default_cluster.jpg"
                                         alt="placeholder" class="w-full h-full object-cover" />
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <h3 class="text-3xl font-black text-slate-900 mb-3 tracking-tight group-hover:text-[#008751] transition-colors">
                            ${loc.locationName}
                        </h3>

                        <div class="flex items-start gap-2 text-slate-500 text-sm font-semibold mb-10 min-h-[40px]">
                            <i data-lucide="navigation" class="w-4 h-4 mt-0.5 text-[#008751]"></i>
                            ${loc.address}
                        </div>

                        <div class="grid grid-cols-2 gap-4 mb-10">
                            <div class="bg-white/80 backdrop-blur-sm p-6 rounded-[2rem] border border-white shadow-sm">
                                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">
                                    Khu vực
                                </p>
                                <p class="text-sm font-black text-slate-800 tracking-tight">
                                    HÀ NỘI
                                </p>
                            </div>
                            <div class="bg-white/80 backdrop-blur-sm p-6 rounded-[2rem] border border-white shadow-sm">
                                <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">
                                    Hotline
                                </p>
                                <p class="text-sm font-black text-slate-800 tracking-tight">
                                    0${loc.phoneNumber}
                                </p>
                            </div>
                        </div>

                        <!-- ACTION BUTTONS -->
                        <div class="mt-auto flex gap-4">
                            <a href="${pageContext.request.contextPath}/fields?location_id=${loc.locationId}"
                               class="flex-[2] bg-white border-2 border-slate-200 hover:border-[#008751] hover:text-[#008751]
                                      py-5 rounded-[1.8rem] font-black text-center text-xs uppercase tracking-[0.15em]
                                      transition-all shadow-sm">
                                DANH SÁCH SÂN
                            </a>

                            <a href="${pageContext.request.contextPath}/location-equipment-list?locationId=${loc.locationId}"
                               class="flex-[2] bg-white border-2 border-slate-200 hover:border-blue-600 hover:text-blue-600
                                      py-5 rounded-[1.8rem] font-black text-center text-xs uppercase tracking-[0.15em]
                                      transition-all shadow-sm flex items-center justify-center gap-2">
                                <i data-lucide="tool" class="w-4 h-4"></i>
                                THIẾT BỊ
                            </a>

                            <a href="${pageContext.request.contextPath}/locations/view?location_id=${loc.locationId}"
                               class="flex-1 bg-slate-900 text-white rounded-[1.8rem] hover:bg-black transition-all
                                      flex items-center justify-center shadow-lg">
                                <i data-lucide="settings-2" class="w-6 h-6"></i>
                            </a>
                        </div>
                    </div>

                </div>
            </div>
        </c:forEach>
    </div>

    <!-- EMPTY -->
    <c:if test="${empty locations}">
        <div class="text-center py-32 bg-white rounded-[5rem] border-4 border-dashed border-slate-100 mt-10">
            <div class="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-6">
                <i data-lucide="box" class="w-10 h-10 text-slate-200"></i>
            </div>
            <h2 class="text-2xl font-black text-slate-300 uppercase italic tracking-widest">
                Hệ thống chưa có dữ liệu
            </h2>
            <p class="text-slate-400 font-bold mt-2">
                Vui lòng khởi tạo cụm sân đầu tiên để bắt đầu
            </p>
        </div>
    </c:if>

</div>

<script>
    lucide.createIcons();
</script>

</body>

<jsp:include page="/View/Layout/Footer.jsp"/>

</html>
