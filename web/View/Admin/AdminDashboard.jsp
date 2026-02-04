<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển Admin - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .stat-card:hover .icon-bg { transform: scale(1.1) rotate(5deg); }
    </style>
</head>
<body class="antialiased text-gray-900">

    <jsp:include page="../Layout/Header.jsp" />

    <main class="max-w-7xl mx-auto px-6 py-12 space-y-10">
        
        <!-- GREETING SECTION -->
        <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div class="space-y-2">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 border border-emerald-100 rounded-full text-[10px] font-black text-[#008751] uppercase tracking-[0.2em]">
                    <span class="w-1.5 h-1.5 rounded-full bg-[#008751] animate-pulse"></span>
                    Hệ thống trực tuyến
                </div>
                <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                    XIN CHÀO, <span class="text-[#008751]">${adminName}</span>
                </h1>
                <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">Quản trị viên cấp cao • FIFAFIELD Dashboard</p>
            </div>
            
            <div class="bg-white p-2 rounded-2xl shadow-sm border border-gray-100 flex items-center gap-4">
                <div class="px-6 py-2">
                    <div class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1">Thời gian</div>
                    <div class="text-sm font-black text-gray-900" id="current-time">--:--:--</div>
                </div>
                <div class="w-12 h-12 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 border border-gray-100">
                    <i data-lucide="clock" class="w-5 h-5"></i>
                </div>
            </div>
        </div>

        <!-- STATS GRID -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            <!-- Total Locations -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-blue-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="map-pin" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center border border-blue-100">
                        <i data-lucide="map-pin" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tổng Cơ Sở</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${totalLocations}</p>
                    </div>
                </div>
            </div>

            <!-- Total Fields -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-purple-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="box" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-purple-50 text-purple-600 rounded-2xl flex items-center justify-center border border-purple-100">
                        <i data-lucide="box" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tổng Sân Bóng</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${totalFields}</p>
                    </div>
                </div>
            </div>

            <!-- Total Equipment -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-[#008751]/5 icon-bg transition-transform duration-500">
                    <i data-lucide="shopping-bag" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-emerald-50 text-[#008751] rounded-2xl flex items-center justify-center border border-emerald-100">
                        <i data-lucide="shopping-bag" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tổng Dụng Cụ</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${totalEquipment}</p>
                    </div>
                </div>
            </div>

            <!-- Total Staff -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-indigo-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="users" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center border border-indigo-100">
                        <i data-lucide="users" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tổng Nhân Viên</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${totalStaff}</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- RECENT ACTIVITIES & QUICK LINKS -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            <!-- ACTIVITIES LIST -->
            <div class="lg:col-span-2 bg-white rounded-[3.5rem] p-10 md:p-14 shadow-xl shadow-gray-200/50 border border-gray-100">
                <div class="flex items-center justify-between mb-10">
                    <div class="flex items-center gap-4">
                        <div class="w-2.5 h-8 bg-[#008751] rounded-full"></div>
                        <h2 class="text-2xl font-black text-gray-900 uppercase tracking-tight leading-none">Hoạt động gần đây</h2>
                    </div>
                    <button class="text-[10px] font-black text-gray-400 hover:text-[#008751] uppercase tracking-widest transition-colors flex items-center gap-2">
                        Xem tất cả <i data-lucide="chevron-right" class="w-3 h-3"></i>
                    </button>
                </div>

                <div class="space-y-6">
                    <!-- Activity Item -->
                    <div class="group relative bg-gray-50/50 border border-gray-100 rounded-[2rem] p-6 hover:bg-white hover:shadow-lg transition-all flex items-center gap-6">
                        <div class="absolute top-0 left-0 w-1.5 h-full bg-blue-500 rounded-l-[2rem]"></div>
                        <div class="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center text-blue-500">
                            <i data-lucide="plus-circle" class="w-5 h-5"></i>
                        </div>
                        <div class="flex-1">
                            <p class="text-sm font-black text-gray-900 group-hover:text-blue-600 transition-colors uppercase tracking-tight">Thêm dụng cụ mới: Bóng đá số 5</p>
                            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Hệ thống kho vận • 2 giờ trước</p>
                        </div>
                    </div>

                    <div class="group relative bg-gray-50/50 border border-gray-100 rounded-[2rem] p-6 hover:bg-white hover:shadow-lg transition-all flex items-center gap-6">
                        <div class="absolute top-0 left-0 w-1.5 h-full bg-emerald-500 rounded-l-[2rem]"></div>
                        <div class="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center text-emerald-500">
                            <i data-lucide="user-plus" class="w-5 h-5"></i>
                        </div>
                        <div class="flex-1">
                            <p class="text-sm font-black text-gray-900 group-hover:text-emerald-600 transition-colors uppercase tracking-tight">Thêm nhân viên mới: Nguyễn Văn A</p>
                            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Quản lý nhân sự • 4 giờ trước</p>
                        </div>
                    </div>

                    <div class="group relative bg-gray-50/50 border border-gray-100 rounded-[2rem] p-6 hover:bg-white hover:shadow-lg transition-all flex items-center gap-6">
                        <div class="absolute top-0 left-0 w-1.5 h-full bg-amber-500 rounded-l-[2rem]"></div>
                        <div class="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center text-amber-500">
                            <i data-lucide="calendar-check" class="w-5 h-5"></i>
                        </div>
                        <div class="flex-1">
                            <p class="text-sm font-black text-gray-900 group-hover:text-amber-600 transition-colors uppercase tracking-tight">Đặt sân thành công: Sân 1 - 18:00</p>
                            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Điều hành lịch trình • 6 giờ trước</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- QUICK ACTIONS -->
            <div class="space-y-8">
                <div class="bg-[#008751] rounded-[3rem] p-10 text-white shadow-2xl shadow-[#008751]/30 relative overflow-hidden group">
                    <div class="absolute top-0 right-0 p-10 opacity-10 group-hover:scale-110 transition-transform">
                        <i data-lucide="shield" class="w-32 h-32"></i>
                    </div>
                    <div class="relative z-10 space-y-6">
                        <h3 class="text-xl font-black uppercase tracking-widest leading-none">Phím tắt nhanh</h3>
                        <p class="text-emerald-100/70 text-xs font-medium leading-relaxed">Truy cập nhanh các tính năng quản trị quan trọng nhất của hệ thống.</p>
                        <div class="space-y-3">
                            <a href="${pageContext.request.contextPath}/staff/add" class="flex items-center justify-between p-4 bg-white/10 hover:bg-white/20 rounded-2xl transition-all group/btn">
                                <span class="text-xs font-black uppercase tracking-widest">Thêm nhân viên</span>
                                <i data-lucide="arrow-right" class="w-4 h-4 group-hover/btn:translate-x-1 transition-transform"></i>
                            </a>
                            <a href="${pageContext.request.contextPath}/manager/add" class="flex items-center justify-between p-4 bg-white/10 hover:bg-white/20 rounded-2xl transition-all group/btn">
                                <span class="text-xs font-black uppercase tracking-widest">Thêm quản lý</span>
                                <i data-lucide="arrow-right" class="w-4 h-4 group-hover/btn:translate-x-1 transition-transform"></i>
                            </a>
                            <a href="${pageContext.request.contextPath}/add-equipment" class="flex items-center justify-between p-4 bg-white/10 hover:bg-white/20 rounded-2xl transition-all group/btn">
                                <span class="text-xs font-black uppercase tracking-widest">Cấp dụng cụ</span>
                                <i data-lucide="arrow-right" class="w-4 h-4 group-hover/btn:translate-x-1 transition-transform"></i>
                            </a>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-[2.5rem] p-8 border border-gray-100 shadow-sm flex flex-col items-center text-center space-y-4">
                    <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 border border-gray-100">
                        <i data-lucide="help-circle" class="w-8 h-8"></i>
                    </div>
                    <div>
                        <h4 class="text-sm font-black text-gray-900 uppercase tracking-widest">Trung tâm hỗ trợ</h4>
                        <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Gặp sự cố vận hành?</p>
                    </div>
                    <button class="w-full py-4 bg-gray-900 text-white rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-lg shadow-gray-900/10">Liên hệ kỹ thuật</button>
                </div>
            </div>
        </div>
    </main>

    <jsp:include page="../Layout/Footer.jsp" />

    <script>
        // Initialize Lucide icons
        lucide.createIcons();

        // Real-time clock
        function updateClock() {
            const now = new Date();
            const timeStr = now.toLocaleTimeString('vi-VN', { 
                hour: '2-digit', 
                minute: '2-digit', 
                second: '2-digit' 
            });
            document.getElementById('current-time').textContent = timeStr;
        }
        setInterval(updateClock, 1000);
        updateClock();
    </script>
</body>
</html>