<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển Quản lý - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .stat-card:hover .icon-bg { transform: scale(1.1) rotate(5deg); }
        .action-card:hover { transform: translateY(-5px); }
    </style>
</head>
<body class="antialiased text-gray-900">

    <jsp:include page="/View/Layout/HeaderManager.jsp" />

    <main class="max-w-7xl mx-auto px-6 py-12 space-y-10">
        
        <!-- GREETING SECTION -->
        <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div class="space-y-2">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 border border-emerald-100 rounded-full text-[10px] font-black text-[#008751] uppercase tracking-[0.2em]">
                    <span class="w-1.5 h-1.5 rounded-full bg-[#008751] animate-pulse"></span>
                    Hệ thống quản lý cơ sở
                </div>
                <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                    XIN CHÀO, <span class="text-[#008751]">${managerName != null ? managerName : 'QUẢN LÝ'}</span>
                </h1>
                <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">Điều hành khu vực • FIFAFIELD Manager</p>
            </div>
            
            <div class="bg-white p-2 rounded-2xl shadow-sm border border-gray-100 flex items-center gap-4">
                <div class="px-6 py-2">
                    <div class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1">Thời gian hệ thống</div>
                    <div class="text-sm font-black text-gray-900" id="current-time">--:--:--</div>
                </div>
                <div class="w-12 h-12 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 border border-gray-100">
                    <i data-lucide="clock" class="w-5 h-5"></i>
                </div>
            </div>
        </div>

        <!-- STATS GRID -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            <!-- Total Staff -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all">
                <div class="absolute -top-4 -right-4 text-indigo-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="users" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center border border-indigo-100">
                        <i data-lucide="users" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Tổng Nhân Viên</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${totalStaff != null ? totalStaff : '0'}</p>
                    </div>
                </div>
            </div>

            <!-- Assigned Shifts -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all">
                <div class="absolute -top-4 -right-4 text-blue-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="calendar-check" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center border border-blue-100">
                        <i data-lucide="calendar-check" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Ca Đã Phân</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${assignedCount != null ? assignedCount : '0'}</p>
                    </div>
                </div>
            </div>

            <!-- Upcoming Shifts -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all">
                <div class="absolute -top-4 -right-4 text-amber-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="calendar-days" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center border border-amber-100">
                        <i data-lucide="calendar-days" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Ca Sắp Tới (7 ngày)</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${upcoming != null ? upcoming : '0'}</p>
                    </div>
                </div>
            </div>

            <!-- Today's Tasks -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all">
                <div class="absolute -top-4 -right-4 text-emerald-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="clipboard-list" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-emerald-50 text-[#008751] rounded-2xl flex items-center justify-center border border-emerald-100">
                        <i data-lucide="clipboard-list" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Công Việc Hôm Nay</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${todayCount != null ? todayCount : '0'}</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- MAIN CONTENT: ACTIONS & SIDEBAR -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            <!-- QUICK ACTIONS GRID -->
            <div class="lg:col-span-2 bg-white rounded-[3.5rem] p-10 md:p-14 shadow-xl shadow-gray-200/50 border border-gray-100">
                <div class="flex items-center gap-4 mb-10">
                    <div class="w-2.5 h-8 bg-[#008751] rounded-full"></div>
                    <h2 class="text-2xl font-black text-gray-900 uppercase tracking-tight leading-none">Hành động quản lý</h2>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <!-- Cơ Sở & Sân -->
                    <a href="${pageContext.request.contextPath}/manager/location" class="action-card group relative bg-slate-900 rounded-[2.5rem] p-8 text-white overflow-hidden transition-all duration-300">
                        <div class="absolute -right-4 -top-4 opacity-10 group-hover:scale-110 transition-transform duration-500">
                            <i data-lucide="map-pin" class="w-32 h-32"></i>
                        </div>
                        <div class="relative z-10 flex flex-col h-full justify-between min-h-[120px]">
                            <div class="w-12 h-12 bg-white/10 rounded-2xl flex items-center justify-center mb-4">
                                <i data-lucide="building-2" class="w-6 h-6 text-emerald-400"></i>
                            </div>
                            <div>
                                <h3 class="text-xl font-black uppercase tracking-tight mb-2">Cơ sở & Sân bóng</h3>
                                <p class="text-slate-400 text-[10px] font-bold uppercase tracking-widest">Quản lý trạng thái & Xem lịch sân</p>
                            </div>
                        </div>
                    </a>

                    <!-- Phân ca -->
                    <a href="${pageContext.request.contextPath}/manager/assign-shift" class="action-card group relative bg-emerald-600 rounded-[2.5rem] p-8 text-white overflow-hidden transition-all duration-300 shadow-lg shadow-emerald-200">
                        <div class="absolute -right-4 -top-4 opacity-10 group-hover:scale-110 transition-transform duration-500">
                            <i data-lucide="user-plus" class="w-32 h-32"></i>
                        </div>
                        <div class="relative z-10 flex flex-col h-full justify-between min-h-[120px]">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4">
                                <i data-lucide="calendar-plus" class="w-6 h-6"></i>
                            </div>
                            <div>
                                <h3 class="text-xl font-black uppercase tracking-tight mb-2">Phân Công Ca</h3>
                                <p class="text-emerald-100 text-[10px] font-bold uppercase tracking-widest">Sắp xếp nhân sự làm việc</p>
                            </div>
                        </div>
                    </a>

                    <!-- Dụng cụ -->
                    <a href="${pageContext.request.contextPath}/manager/location-equipment" class="action-card group relative bg-white border border-gray-100 rounded-[2.5rem] p-8 text-gray-900 overflow-hidden transition-all duration-300 shadow-sm hover:shadow-xl">
                        <div class="absolute -right-4 -top-4 text-emerald-500/5 group-hover:scale-110 transition-transform duration-500">
                            <i data-lucide="package" class="w-32 h-32"></i>
                        </div>
                        <div class="relative z-10 flex flex-col h-full justify-between min-h-[120px]">
                            <div class="w-12 h-12 bg-emerald-50 rounded-2xl flex items-center justify-center mb-4">
                                <i data-lucide="box" class="w-6 h-6 text-[#008751]"></i>
                            </div>
                            <div>
                                <h3 class="text-xl font-black uppercase tracking-tight mb-2">Kho Dụng Cụ</h3>
                                <p class="text-gray-400 text-[10px] font-bold uppercase tracking-widest">Cập nhật số lượng & Tình trạng</p>
                            </div>
                        </div>
                    </a>

                    <!-- Xem ca đã phân -->
                    <a href="${pageContext.request.contextPath}/manager/staff-shifts" class="action-card group relative bg-white border border-gray-100 rounded-[2.5rem] p-8 text-gray-900 overflow-hidden transition-all duration-300 shadow-sm hover:shadow-xl">
                        <div class="absolute -right-4 -top-4 text-blue-500/5 group-hover:scale-110 transition-transform duration-500">
                            <i data-lucide="history" class="w-32 h-32"></i>
                        </div>
                        <div class="relative z-10 flex flex-col h-full justify-between min-h-[120px]">
                            <div class="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center mb-4">
                                <i data-lucide="eye" class="w-6 h-6 text-blue-600"></i>
                            </div>
                            <div>
                                <h3 class="text-xl font-black uppercase tracking-tight mb-2">Lịch Sử Phân Ca</h3>
                                <p class="text-gray-400 text-[10px] font-bold uppercase tracking-widest">Kiểm soát tiến độ công việc</p>
                            </div>
                        </div>
                    </a>
                </div>
            </div>

            <!-- SIDEBAR INFO -->
            <div class="space-y-8">
                <!-- MANAGEMENT CARD -->
                <div class="bg-[#008751] rounded-[3rem] p-10 text-white shadow-2xl shadow-emerald-200 relative overflow-hidden group">
                    <div class="absolute top-0 right-0 p-10 opacity-10 group-hover:scale-110 transition-transform">
                        <i data-lucide="shield-check" class="w-32 h-32"></i>
                    </div>
                    <div class="relative z-10 space-y-6">
                        <h3 class="text-xl font-black uppercase tracking-widest leading-none">Thông số vận hành</h3>
                        <div class="space-y-4">
                            <div class="flex items-center justify-between py-3 border-b border-white/10">
                                <span class="text-xs font-bold uppercase text-emerald-200">Nhân sự trực tiếp</span>
                                <span class="text-lg font-black">${totalStaff}</span>
                            </div>
                            <div class="flex items-center justify-between py-3 border-b border-white/10">
                                <span class="text-xs font-bold uppercase text-emerald-200">Ca đang hoạt động</span>
                                <span class="text-lg font-black">${todayCount}</span>
                            </div>
                            <a href="${pageContext.request.contextPath}/manager/shifts" class="flex items-center justify-between p-4 bg-white/10 hover:bg-white/20 rounded-2xl transition-all group/btn mt-4">
                                <span class="text-[10px] font-black uppercase tracking-widest">Danh sách ca mẫu</span>
                                <i data-lucide="chevron-right" class="w-4 h-4 group-hover/btn:translate-x-1 transition-transform"></i>
                            </a>
                        </div>
                    </div>
                </div>

                <!-- SUPPORT BOX -->
                <div class="bg-white rounded-[2.5rem] p-8 border border-gray-100 shadow-sm flex flex-col items-center text-center space-y-4">
                    <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 border border-gray-100">
                        <i data-lucide="help-circle" class="w-8 h-8"></i>
                    </div>
                    <div>
                        <h4 class="text-sm font-black text-gray-900 uppercase tracking-widest">Trung tâm điều hành</h4>
                        <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Gặp vấn đề về phân ca?</p>
                    </div>
                    <button class="w-full py-4 bg-gray-900 text-white rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-lg shadow-gray-900/10 hover:bg-emerald-600 transition-colors">
                        Liên hệ kỹ thuật
                    </button>
                </div>
            </div>
        </div>
    </main>

    <jsp:include page="/View/Layout/FooterManager.jsp" />

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
            const clockElem = document.getElementById('current-time');
            if(clockElem) clockElem.textContent = timeStr;
        }
        setInterval(updateClock, 1000);
        updateClock();
    </script>
</body>
</html>