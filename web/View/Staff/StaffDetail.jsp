<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .detail-card { border-radius: 3rem; }
        .data-item:hover .icon-wrapper { background-color: #008751; color: white; transform: scale(1.1); }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="flex-grow max-w-5xl mx-auto px-6 py-12 space-y-8 w-full">
    
    <!-- TOP NAVIGATION -->
    <div class="flex items-center justify-between">
        <a href="${pageContext.request.contextPath}/staff/list" 
           class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
            <i data-lucide="arrow-left" class="w-3 h-3"></i>
            QUAY LẠI DANH SÁCH
        </a>
        <div class="bg-white px-6 py-3 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-3">
            <i data-lucide="shield-check" class="w-4 h-4 text-[#008751]"></i>
            <span class="text-[10px] font-black text-gray-500 uppercase tracking-widest">Hồ sơ nhân sự xác thực</span>
        </div>
    </div>

    <!-- MAIN PROFILE CARD -->
    <div class="bg-white detail-card shadow-2xl shadow-gray-200/50 border border-gray-100 overflow-hidden relative">
        
        <!-- Ghost Decor -->
        <div class="absolute top-0 right-0 p-12 opacity-[0.03] pointer-events-none">
            <i data-lucide="user" class="w-64 h-64"></i>
        </div>

        <div class="p-10 md:p-16 space-y-12 relative z-10">
            
            <!-- Profile Header -->
            <div class="flex flex-col md:flex-row items-center md:items-start gap-8 border-b border-gray-50 pb-12">
                <div class="w-32 h-32 bg-gray-900 rounded-[2.5rem] flex items-center justify-center text-white font-black text-5xl shadow-2xl shadow-gray-900/20">
                    ${staff.fullName.charAt(0)}
                </div>
                <div class="text-center md:text-left space-y-4 flex-1">
                    <div class="space-y-1">
                        <div class="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 border border-emerald-100 rounded-full text-[9px] font-black text-[#008751] uppercase tracking-[0.2em] mb-2">
<span class="w-1.5 h-1.5 rounded-full bg-[#008751] ${staff.status == 'active' ? 'animate-pulse' : ''}"></span>
                            ${staff.status == 'active' ? 'ĐANG HOẠT ĐỘNG' : 'TẠM KHÓA'}
                        </div>
                        <h1 class="text-4xl font-black text-gray-900 tracking-tighter uppercase leading-none">${staff.fullName}</h1>
                        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">${staff.locationName} • CHUYÊN VIÊN VẬN HÀNH</p>
                    </div>
                </div>
            </div>

            <!-- Profile Content Grid -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-16 gap-y-10">
                
                <!-- Left Column: Primary Info -->
                <div class="space-y-8">
                    <div class="flex items-center gap-4">
                        <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                        <h3 class="text-[10px] font-black text-gray-300 uppercase tracking-[0.3em]">Thông tin cá nhân</h3>
                    </div>

                    <div class="space-y-6">
                        <div class="data-item flex items-center gap-5 group">
                            <div class="icon-wrapper w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 transition-all duration-300">
                                <i data-lucide="phone" class="w-5 h-5"></i>
                            </div>
                            <div>
                                <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5">Số điện thoại</p>
                                <p class="text-sm font-bold text-gray-700">${staff.phone}</p>
                            </div>
                        </div>

                        <div class="data-item flex items-center gap-5 group">
                            <div class="icon-wrapper w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 transition-all duration-300">
                                <i data-lucide="map-pin" class="w-5 h-5"></i>
                            </div>
                            <div>
                                <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5">Địa chỉ thường trú</p>
                                <p class="text-sm font-bold text-gray-700">${staff.address}</p>
                            </div>
                        </div>

                        <div class="data-item flex items-center gap-5 group">
                            <div class="icon-wrapper w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 transition-all duration-300">
                                <i data-lucide="venus-mars" class="w-5 h-5"></i>
                            </div>
                            <div>
<p class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5">Giới tính</p>
                                <p class="text-sm font-bold text-gray-700 uppercase tracking-widest">
                                    <c:choose>
                                        <c:when test="${staff.gender == 'male' || staff.gender == 'Nam'}">NAM</c:when>
                                        <c:when test="${staff.gender == 'female' || staff.gender == 'Nữ'}">NỮ</c:when>
                                        <c:otherwise>${staff.gender}</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right Column: System Info -->
                <div class="space-y-8">
                    <div class="flex items-center gap-4">
                        <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
                        <h3 class="text-[10px] font-black text-gray-300 uppercase tracking-[0.3em]">Dữ liệu nhân sự</h3>
                    </div>

                    <div class="space-y-6">
                        <div class="data-item flex items-center gap-5 group">
                            <div class="icon-wrapper w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 transition-all duration-300">
                                <i data-lucide="fingerprint" class="w-5 h-5"></i>
                            </div>
                            <div>
                                <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5">Mã nhân viên (ID)</p>
                                <p class="text-sm font-black text-[#008751] tracking-widest">${staff.employeeCode}</p>
                            </div>
                        </div>

                        <div class="data-item flex items-center gap-5 group">
                            <div class="icon-wrapper w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 transition-all duration-300">
                                <i data-lucide="building-2" class="w-5 h-5"></i>
                            </div>
                            <div>
                                <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5">Cơ sở công tác</p>
                                <p class="text-sm font-bold text-gray-700 uppercase tracking-tight">${staff.locationName}</p>
                            </div>
                        </div>

                        <div class="data-item flex items-center gap-5 group">
                            <div class="icon-wrapper w-12 h-12 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 transition-all duration-300">
                                <i data-lucide="calendar" class="w-5 h-5"></i>
</div>
                            <div>
                                <p class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5">Ngày gia nhập</p>
                                <p class="text-sm font-bold text-gray-700">${staff.hireDate}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Footer Actions -->
            <div class="pt-12 border-t border-gray-50 flex flex-col md:flex-row gap-4">
                <a href="${pageContext.request.contextPath}/staff/edit?id=${staff.userId}" 
                   class="flex-1 bg-[#008751] hover:bg-[#007043] text-white py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] shadow-2xl shadow-[#008751]/20 transition-all hover:-translate-y-1 flex items-center justify-center gap-3">
                    <i data-lucide="edit-3" class="w-4 h-4"></i>
                    CHỈNH SỬA HỒ SƠ
                </a>
                <form method="post" action="${pageContext.request.contextPath}/staff/delete" 
                      class="flex-1"
                      onsubmit="return confirm('Bạn có chắc muốn xóa nhân viên này?');">
                    <input type="hidden" name="id" value="${staff.userId}" />
                    <button type="submit" 
                            class="w-full bg-rose-50 hover:bg-rose-500 text-rose-500 hover:text-white py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] transition-all hover:-translate-y-1 flex items-center justify-center gap-3 border border-rose-100">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                        XÓA NHÂN SỰ
                    </button>
                </form>
                <a href="${pageContext.request.contextPath}/staff/list" 
                   class="flex-1 bg-gray-50 hover:bg-gray-100 text-gray-400 py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] transition-all hover:-translate-y-1 flex items-center justify-center gap-3">
                    <i data-lucide="list" class="w-4 h-4"></i>
                    QUAY VỀ DANH SÁCH
                </a>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>

</body>
</html>