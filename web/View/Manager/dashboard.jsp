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
    </style>
</head>
<body class="bg-gray-50">
<jsp:include page="/View/Layout/HeaderManager.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Bảng điều khiển - Manager</h1>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="bg-white p-4 rounded shadow">
            <h3 class="text-sm text-gray-500">Tổng số nhân viên</h3>
            <p class="text-2xl font-bold">${totalStaff != null ? totalStaff : '—'}</p>
        </div>
        <div class="bg-white p-4 rounded shadow">
            <h3 class="text-sm text-gray-500">Ca đã phân</h3>
            <p class="text-2xl font-bold">${assignedCount != null ? assignedCount : '—'}</p>
        </div>
        <div class="bg-white p-4 rounded shadow">
            <h3 class="text-sm text-gray-500">Ca sắp tới 7 ngày</h3>
            <p class="text-2xl font-bold">${upcoming != null ? upcoming : '—'}</p>
        </div>
        <div class="bg-white p-4 rounded shadow">
            <h3 class="text-sm text-gray-500">Ca hôm nay</h3>
            <p class="text-2xl font-bold">${todayCount != null ? todayCount : '—'}</p>
        </div>
    </div>

        <!-- STATS GRID -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            
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
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${totalStaff != null ? totalStaff : '—'}</p>
                    </div>
                </div>
            </div>

            <!-- Assigned Shifts -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-blue-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="calendar-check" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center border border-blue-100">
                        <i data-lucide="calendar-check" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Ca Đã Phân</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${assignedCount != null ? assignedCount : '—'}</p>
                    </div>
                </div>
            </div>

            <!-- Upcoming Shifts -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-amber-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="calendar" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center border border-amber-100">
                        <i data-lucide="calendar" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Ca Sắp Tới</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${upcoming != null ? upcoming : '—'}</p>
                    </div>
                </div>
            </div>

            <!-- Tasks -->
            <div class="stat-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-purple-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="checklist" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-purple-50 text-purple-600 rounded-2xl flex items-center justify-center border border-purple-100">
                        <i data-lucide="checklist" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Công Việc Hôm Nay</p>
                        <p class="text-4xl font-black text-gray-900 mt-1 leading-none tracking-tighter">${todayCount != null ? todayCount : '—'}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

        <!-- ACTIONS & ACTIVITIES -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            <!-- QUICK ACTIONS -->
            <div class="lg:col-span-2 bg-white rounded-[3.5rem] p-10 md:p-14 shadow-xl shadow-gray-200/50 border border-gray-100">
                <div class="flex items-center justify-between mb-10">
                    <div class="flex items-center gap-4">
                        <div class="w-2.5 h-8 bg-[#008751] rounded-full"></div>
                        <h2 class="text-2xl font-black text-gray-900 uppercase tracking-tight leading-none">Hành Động Nhanh</h2>
                    </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <!-- Phân ca -->
                    <a href="${pageContext.request.contextPath}/manager/assign-shift" class="group relative bg-gradient-to-br from-[#008751] to-[#006d41] rounded-[2.5rem] p-8 text-white shadow-xl shadow-[#008751]/30 overflow-hidden hover:shadow-2xl hover:shadow-[#008751]/40 transition-all duration-300">
                        <div class="absolute top-0 right-0 opacity-10 group-hover:scale-125 transition-transform duration-500">
                            <i data-lucide="clipboard-check" class="w-40 h-40"></i>
                        </div>
                        <div class="relative z-10">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4 group-hover:bg-white/30 transition-all">
                                <i data-lucide="clipboard-check" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-black uppercase tracking-tight leading-none mb-2">Phân Ca</h3>
                            <p class="text-emerald-100/80 text-sm font-medium">Phân công ca làm việc cho nhân viên</p>
                        </div>
                    </a>

                    <!-- Xem ca đã phân -->
                    <a href="${pageContext.request.contextPath}/manager/staff-shifts" class="group relative bg-gradient-to-br from-blue-500 to-blue-600 rounded-[2.5rem] p-8 text-white shadow-xl shadow-blue-500/30 overflow-hidden hover:shadow-2xl hover:shadow-blue-500/40 transition-all duration-300">
                        <div class="absolute top-0 right-0 opacity-10 group-hover:scale-125 transition-transform duration-500">
                            <i data-lucide="eye" class="w-40 h-40"></i>
                        </div>
                        <div class="relative z-10">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4 group-hover:bg-white/30 transition-all">
                                <i data-lucide="eye" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-black uppercase tracking-tight leading-none mb-2">Xem Ca Đã Phân</h3>
                            <p class="text-blue-100/80 text-sm font-medium">Danh sách ca làm việc được phân công</p>
                        </div>
                    </a>

                    <!-- Danh sách ca -->
                    <a href="${pageContext.request.contextPath}/shifts" class="group relative bg-gradient-to-br from-purple-500 to-purple-600 rounded-[2.5rem] p-8 text-white shadow-xl shadow-purple-500/30 overflow-hidden hover:shadow-2xl hover:shadow-purple-500/40 transition-all duration-300">
                        <div class="absolute top-0 right-0 opacity-10 group-hover:scale-125 transition-transform duration-500">
                            <i data-lucide="calendar" class="w-40 h-40"></i>
                        </div>
                        <div class="relative z-10">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4 group-hover:bg-white/30 transition-all">
                                <i data-lucide="calendar" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-black uppercase tracking-tight leading-none mb-2">Danh Sách Ca</h3>
                            <p class="text-purple-100/80 text-sm font-medium">Xem các ca làm hiện có</p>
                        </div>
                    </a>

                    <!-- Thêm ca -->
                    <a href="${pageContext.request.contextPath}/shifts/add" class="group relative bg-gradient-to-br from-orange-500 to-orange-600 rounded-[2.5rem] p-8 text-white shadow-xl shadow-orange-500/30 overflow-hidden hover:shadow-2xl hover:shadow-orange-500/40 transition-all duration-300">
                        <div class="absolute top-0 right-0 opacity-10 group-hover:scale-125 transition-transform duration-500">
                            <i data-lucide="plus-circle" class="w-40 h-40"></i>
                        </div>
                        <div class="relative z-10">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4 group-hover:bg-white/30 transition-all">
                                <i data-lucide="plus-circle" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-black uppercase tracking-tight leading-none mb-2">Thêm Ca</h3>
                            <p class="text-orange-100/80 text-sm font-medium">Tạo ca mới cho hệ thống</p>
                        </div>
                    </a>
                </div>
            </div>

            <!-- SIDEBAR -->
            <div class="space-y-8">
                <!-- INFO BOX -->
                <div class="bg-[#008751] rounded-[3rem] p-10 text-white shadow-2xl shadow-[#008751]/30 relative overflow-hidden group">
                    <div class="absolute top-0 right-0 p-10 opacity-10 group-hover:scale-110 transition-transform">
                        <i data-lucide="shield" class="w-32 h-32"></i>
                    </div>
                    <div class="relative z-10 space-y-6">
                        <h3 class="text-xl font-black uppercase tracking-widest leading-none">Thông Tin Quản Lý</h3>
                        <div class="space-y-3 text-sm font-medium text-emerald-100/80">
                            <div class="flex items-center justify-between">
                                <span>Số nhân viên trực tiếp:</span>
                                <span class="font-black text-white">${totalStaff != null ? totalStaff : '—'}</span>
                            </div>
                            <div class="flex items-center justify-between">
                                <span>Ca được phân công:</span>
                                <span class="font-black text-white">${assignedCount != null ? assignedCount : '—'}</span>
                            </div>
                            <div class="flex items-center justify-between">
                                <span>Ca sắp tới 7 ngày:</span>
                                <span class="font-black text-white">${upcoming != null ? upcoming : '—'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- HELP BOX -->
                <div class="bg-white rounded-[2.5rem] p-8 border border-gray-100 shadow-sm flex flex-col items-center text-center space-y-4">
                    <div class="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-400 border border-gray-100">
                        <i data-lucide="help-circle" class="w-8 h-8"></i>
                    </div>
                    <div>
                        <h4 class="text-sm font-black text-gray-900 uppercase tracking-widest">Trung Tâm Hỗ Trợ</h4>
                        <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Cần giúp đỡ?</p>
                    </div>
                    <button class="w-full py-4 bg-gray-900 text-white rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-lg shadow-gray-900/10 hover:bg-gray-800 transition-all">
                        Liên Hệ Hỗ Trợ
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
            document.getElementById('current-time').textContent = timeStr;
        }
        setInterval(updateClock, 1000);
        updateClock();
    </script>
</body>
</html>
