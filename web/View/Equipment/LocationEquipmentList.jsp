<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Vật tư sân bóng - FIFAFIELD</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
        <style>
            body {
                font-family: 'Inter', sans-serif;
                background-color: #f8fafc;
            }
            .fifa-green {
                color: #008751;
            }
            .bg-fifa-green {
                background-color: #008751;
            }
            .custom-scrollbar::-webkit-scrollbar {
                width: 6px;
            }
            .custom-scrollbar::-webkit-scrollbar-thumb {
                background: #008751;
                border-radius: 10px;
            }
            .filter-select {
                background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
                background-position: right 1rem center;
                background-repeat: no-repeat;
                background-size: 1.5em 1.5em;
                padding-right: 2.5rem;
                -webkit-appearance: none;
                -moz-appearance: none;
                appearance: none;
            }
        </style>
        
    </head>
 

    <body class="antialiased text-gray-900">
<jsp:include page="/View/Layout/Header.jsp"/>

        <!-- GLOBAL HEADER (Mocked for consistency with dashboard) -->
        


        <div class="max-w-7xl mx-auto px-6 py-10 space-y-8">

            
            <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
                <div class="space-y-2">
                    <a href="javascript:history.back()" 
                       class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="m12 19-7-7 7-7"/><path d="M19 12H5"/></svg>
                        QUAY LẠI
                    </a>
                    <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                        VẬT TƯ <span class="text-[#008751]">TẠI SÂN</span>
                    </h1>
                    <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Hệ thống kiểm kê thiết bị thi đấu & hỗ trợ</p>
                </div>

                <div class="bg-white px-6 py-3 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-3">
                    <div class="w-2 h-2 rounded-full bg-[#008751] animate-pulse"></div>
                    <span class="text-[10px] font-black text-gray-500 uppercase tracking-widest">Đồng bộ kho thực tế</span>
                </div>
            </div>

            <!-- ADVANCED FILTER SECTION -->
            <div class="bg-white p-8 rounded-[2.5rem] shadow-sm border border-gray-100">
                <form action="" method="GET" class="space-y-6">
                    <div class="grid grid-cols-1 md:grid-cols-12 gap-5">
                        <!-- Search -->
                        <div class="md:col-span-4 relative group">
                            <svg xmlns="http://www.w3.org/2000/svg" class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-[#008751] transition-colors" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
                            <input type="text" name="search" value="${param.search}" 
                                   placeholder="Tìm tên dụng cụ..."
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] transition-all font-bold text-sm">
                        </div>

                        <!-- Type -->
                        <div class="md:col-span-2">
                            <select name="type" class="filter-select w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:border-[#008751] font-black text-[10px] uppercase tracking-widest text-gray-500 cursor-pointer">
                                <option value="">Tất cả loại</option>
                                <option value="ball" ${param.type == 'ball' ? 'selected' : ''}>Bóng</option>
                                <option value="vest" ${param.type == 'vest' ? 'selected' : ''}>Áo tập</option>
                                <option value="cone" ${param.type == 'cone' ? 'selected' : ''}>Cọc tập</option>
                            </select>
                        </div>

                        <!-- Status -->
                        <div class="md:col-span-3">
                            <select name="status" class="filter-select w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:border-[#008751] font-black text-[10px] uppercase tracking-widest text-gray-500 cursor-pointer">
                                <option value="">Tất cả trạng thái</option>
                                <option value="available" ${param.status == 'available' ? 'selected' : ''}>Sẵn sàng (Available)</option>
                                <option value="unavailable" ${param.status == 'unavailable' ? 'selected' : ''}>Không sẵn sàng</option>
                            </select>
                        </div>

                        <!-- Sort -->
                        <div class="md:col-span-2">
                            <select name="sort" class="filter-select w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:border-[#008751] font-black text-[10px] uppercase tracking-widest text-gray-500 cursor-pointer">
                                <option value="">Sắp xếp giá</option>
                                <option value="asc" ${param.sort == 'asc' ? 'selected' : ''}>Giá: Thấp -> Cao</option>
                                <option value="desc" ${param.sort == 'desc' ? 'selected' : ''}>Giá: Cao -> Thấp</option>
                            </select>
                        </div>

                        <!-- Submit Button -->
                        <div class="md:col-span-1">
                            <button type="submit" class="w-full h-full bg-[#008751] text-white rounded-2xl flex items-center justify-center hover:bg-[#007043] transition-all shadow-lg shadow-[#008751]/20 group">
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" class="group-hover:scale-110 transition-transform"><path d="m9 18 6-6-6-6"/></svg>
                            </button>
                        </div>
                    </div>

                    <c:if test="${not empty param.search or not empty param.type or not empty param.status}">
                        <div class="flex items-center gap-3 pt-2">
                            <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Đang lọc:</span>
                            <a href="location-equipment-list" class="text-[10px] font-black text-[#008751] hover:underline uppercase tracking-widest flex items-center gap-1">
                                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                                Xóa tất cả bộ lọc
                            </a>
                        </div>
                    </c:if>
                </form>
            </div>

            <!-- EQUIPMENT GRID -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
                <c:forEach items="${locationEquipmentList}" var="e">
                    <div class="group">
                        <div class="bg-white border-2 border-gray-50 rounded-[2.8rem] overflow-hidden h-full transition-all hover:shadow-2xl hover:shadow-[#008751]/5 hover:border-[#008751] flex flex-col relative">

                            <!-- Status Badge Floating -->
                            <div class="absolute top-4 right-4 z-10">
                                <span class="px-3 py-1.5 rounded-full text-[8px] font-black tracking-[0.15em] uppercase shadow-lg
                                      ${e.status == 'available' ? 'bg-emerald-500 text-white' : 'bg-gray-400 text-white'}">
                                    ${e.status == 'available' ? 'SẴN SÀNG' : 'KHÔNG TRỐNG'}
                                </span>
                            </div>

                            <!-- Image Section -->
                            <div class="relative h-56 bg-gray-100 overflow-hidden">
                                
                                <img src="${e.imageUrl}" alt="${e.name}" 
                                     class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700">
                                <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
                            </div>

                            <!-- Content Section -->
                            <div class="p-8 flex-1 flex flex-col justify-between space-y-6">
                                <div class="space-y-3">
                                    <div class="flex justify-between items-center">
                                        <span class="text-[9px] font-black text-gray-300 uppercase tracking-[0.2em]">TYPE: ${e.equipmentType}</span>
                                    </div>
                                    <h3 class="text-xl font-black text-gray-900 leading-tight group-hover:text-[#008751] transition-colors">
                                        ${e.name}
                                    </h3>
                                </div>

                                <!-- Info Grid -->
                                <div class="grid grid-cols-2 gap-4 py-5 border-y border-gray-50">
                                    <div>
                                        <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1.5">Trong kho</p>
                                        <p class="text-lg font-black text-gray-900 leading-none">${e.quantity} <span class="text-[10px] font-bold text-gray-400">đv</span></p>
                                    </div>
                                    <div>
                                        <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1.5">Giá thuê</p>
                                        <p class="text-lg font-black text-[#008751] leading-none">
                                            <fmt:formatNumber value="${e.rentalPrice}" /> <span class="text-[10px] font-bold opacity-60">đ</span>
                                        </p>
                                    </div>
                                </div>

                                <!-- Footer Detail -->
                                <div class="flex items-center justify-between text-[10px] font-bold text-gray-400">
                                    <span class="uppercase tracking-widest">Phí đền bù:</span>
                                    <span class="text-gray-900 font-black"><fmt:formatNumber value="${e.damageFee}" /> đ</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <!-- EMPTY STATE -->
                <c:if test="${empty locationEquipmentList}">
                    <div class="col-span-full flex flex-col items-center justify-center py-32 text-center space-y-6">
                        <div class="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center text-gray-200 border border-gray-100">
                            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                        </div>
                        <div>
                            <h3 class="text-gray-900 font-black uppercase tracking-tight text-xl">Không tìm thấy vật tư</h3>
                            <p class="text-gray-400 font-medium text-sm mt-1">Hệ thống không tìm thấy kết quả phù hợp với bộ lọc hiện tại.</p>
                        </div>
                        <a href="location-equipment-list" class="px-10 py-4 bg-[#008751] text-white rounded-2xl font-black text-[10px] uppercase tracking-widest shadow-xl shadow-[#008751]/20 hover:-translate-y-0.5 transition-all">
                            LÀM MỚI DANH SÁCH
                        </a>
                    </div>
                </c:if>
            </div>
            <!-- PAGINATION -->
            <c:if test="${totalPages > 1}">
                <div class="flex justify-center pt-14">
                    <div class="flex items-center gap-2 bg-white px-6 py-4 rounded-2xl shadow-sm border border-gray-100">

                        <!-- PREV -->
                        <c:if test="${currentPage > 1}">
                            <a href="?page=${currentPage - 1}&search=${param.search}&type=${param.type}&status=${param.status}&sort=${param.sort}"
                               class="px-4 py-2 rounded-xl text-sm font-black text-gray-500
                               hover:text-[#008751] hover:bg-gray-50 transition">
                                ←
                            </a>
                        </c:if>

                        <!-- PAGE NUMBERS -->
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <a href="?page=${p}&search=${param.search}&type=${param.type}&status=${param.status}&sort=${param.sort}"
                               class="px-4 py-2 rounded-xl text-sm font-black transition
                               ${p == currentPage
                                 ? 'bg-[#008751] text-white shadow-lg shadow-[#008751]/30'
                                 : 'text-gray-500 hover:text-[#008751] hover:bg-gray-50'}">
                                   ${p}
                               </a>
                            </c:forEach>

                            <!-- NEXT -->
                            <c:if test="${currentPage < totalPages}">
                                <a href="?page=${currentPage + 1}&search=${param.search}&type=${param.type}&status=${param.status}&sort=${param.sort}"
                                   class="px-4 py-2 rounded-xl text-sm font-black text-gray-500
                                   hover:text-[#008751] hover:bg-gray-50 transition">
                                    →
                                </a>
                            </c:if>

                        </div>
                    </div>
                </c:if>




            </div>

        </body>
        <jsp:include page="/View/Layout/Footer.jsp"/>
        

    </html>