<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển - FIFAFIELD Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .card-hover { transition: transform 0.2s ease, box-shadow 0.2s ease; }
        .card-hover:hover { transform: translateY(-4px); box-shadow: 0 20px 40px rgba(0,0,0,0.08); }
        .icon-bg { transition: transform 0.3s ease; }
        .stat-card:hover .icon-bg { transform: scale(1.1) rotate(5deg); }
    </style>
</head>
<body class="antialiased flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-7xl mx-auto px-6 py-10 w-full flex-grow space-y-10">

    <!-- WELCOME BANNER -->
    <div class="bg-gradient-to-r from-[#008751] to-[#00b368] rounded-[2.5rem] p-8 md:p-12 text-white shadow-2xl shadow-[#008751]/25 relative overflow-hidden">
        <div class="absolute -right-10 -top-10 w-64 h-64 bg-white/5 rounded-full"></div>
        <div class="absolute -right-4 bottom-0 w-40 h-40 bg-white/5 rounded-full"></div>
        <div class="relative z-10">
            <p class="text-green-200 text-xs font-black uppercase tracking-[0.3em] mb-2">FIFAFIELD MANAGER</p>
            <h1 class="text-3xl md:text-4xl font-black mb-2">
                Xin chào, <span class="text-green-200">${sessionScope.user.fullName != null ? sessionScope.user.fullName : 'Quản lý'}</span>!
            </h1>
            <p class="text-green-100 text-sm font-medium opacity-80">
                <c:choose>
                    <c:when test="${not empty locationName}">Bạn đang quản lý cụm sân: <strong>${locationName}</strong></c:when>
                    <c:otherwise>Chưa được gán cụm sân — liên hệ Admin.</c:otherwise>
                </c:choose>
            </p>
            <div class="mt-4 flex items-center gap-2 text-green-200 text-xs font-semibold">
                <i data-lucide="clock" class="w-4 h-4"></i>
                <span id="current-time"></span>
            </div>
        </div>
    </div>

    <!-- STAT CARDS -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-5">

        <!-- Nhân viên -->
        <div class="stat-card bg-white rounded-[2rem] p-6 shadow border border-gray-100 relative overflow-hidden">
            <div class="absolute -top-4 -right-4 opacity-5 icon-bg">
                <i data-lucide="users" class="w-28 h-28 text-indigo-600"></i>
            </div>
            <div class="relative z-10">
                <div class="w-11 h-11 bg-indigo-50 rounded-xl flex items-center justify-center mb-4">
                    <i data-lucide="users" class="w-5 h-5 text-indigo-600"></i>
                </div>
                <p class="text-3xl font-black text-gray-900">${totalStaff != null ? totalStaff : '—'}</p>
                <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mt-1">Nhân viên tại cụm</p>
            </div>
        </div>

        <!-- Ca đã phân -->
        <div class="stat-card bg-white rounded-[2rem] p-6 shadow border border-gray-100 relative overflow-hidden">
            <div class="absolute -top-4 -right-4 opacity-5 icon-bg">
                <i data-lucide="calendar-check" class="w-28 h-28 text-blue-600"></i>
            </div>
            <div class="relative z-10">
                <div class="w-11 h-11 bg-blue-50 rounded-xl flex items-center justify-center mb-4">
                    <i data-lucide="calendar-check" class="w-5 h-5 text-blue-600"></i>
                </div>
                <p class="text-3xl font-black text-gray-900">${assignedCount != null ? assignedCount : '—'}</p>
                <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mt-1">Ca đã phân công</p>
            </div>
        </div>

        <!-- Ca sắp tới -->
        <div class="stat-card bg-white rounded-[2rem] p-6 shadow border border-gray-100 relative overflow-hidden">
            <div class="absolute -top-4 -right-4 opacity-5 icon-bg">
                <i data-lucide="calendar-range" class="w-28 h-28 text-amber-500"></i>
            </div>
            <div class="relative z-10">
                <div class="w-11 h-11 bg-amber-50 rounded-xl flex items-center justify-center mb-4">
                    <i data-lucide="calendar-range" class="w-5 h-5 text-amber-500"></i>
                </div>
                <p class="text-3xl font-black text-gray-900">${upcoming != null ? upcoming : '—'}</p>
                <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mt-1">Ca sắp tới (7 ngày)</p>
            </div>
        </div>

        <!-- Ca hôm nay -->
        <div class="stat-card bg-white rounded-[2rem] p-6 shadow border border-gray-100 relative overflow-hidden">
            <div class="absolute -top-4 -right-4 opacity-5 icon-bg">
                <i data-lucide="sun" class="w-28 h-28 text-[#008751]"></i>
            </div>
            <div class="relative z-10">
                <div class="w-11 h-11 bg-emerald-50 rounded-xl flex items-center justify-center mb-4">
                    <i data-lucide="sun" class="w-5 h-5 text-[#008751]"></i>
                </div>
                <p class="text-3xl font-black text-gray-900">${todayCount != null ? todayCount : '—'}</p>
                <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mt-1">Ca hôm nay</p>
            </div>
        </div>

    </div>

    <!-- QUICK ACTIONS + SIDEBAR -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

        <!-- QUICK ACTIONS -->
        <div class="lg:col-span-2 bg-white rounded-[2.5rem] p-8 shadow border border-gray-100">
            <h2 class="text-xs font-black text-gray-400 uppercase tracking-[0.25em] mb-6">Thao tác nhanh</h2>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">

                <!-- Nhân viên của tôi -->
                <a href="${pageContext.request.contextPath}/manager/staff/list"
                   class="card-hover flex items-center gap-4 p-5 rounded-2xl bg-gradient-to-br from-indigo-50 to-blue-50 border border-indigo-100 group">
                    <div class="w-12 h-12 bg-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-200 flex-shrink-0">
                        <i data-lucide="users" class="w-6 h-6 text-white"></i>
                    </div>
                    <div>
                        <p class="font-black text-gray-900 text-sm">Nhân viên</p>
                        <p class="text-xs text-gray-400 mt-0.5">Quản lý nhân viên tại cụm</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-indigo-600 transition-colors"></i>
                </a>

                <!-- Phân ca nhân viên -->
                <a href="${pageContext.request.contextPath}/manager/assign-shift"
                   class="card-hover flex items-center gap-4 p-5 rounded-2xl bg-gradient-to-br from-emerald-50 to-green-50 border border-emerald-100 group">
                    <div class="w-12 h-12 bg-[#008751] rounded-xl flex items-center justify-center shadow-lg shadow-emerald-200 flex-shrink-0">
                        <i data-lucide="calendar-plus" class="w-6 h-6 text-white"></i>
                    </div>
                    <div>
                        <p class="font-black text-gray-900 text-sm">Phân ca</p>
                        <p class="text-xs text-gray-400 mt-0.5">Giao ca làm việc cho nhân viên</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-[#008751] transition-colors"></i>
                </a>

                <!-- Ca đã phân công -->
                <a href="${pageContext.request.contextPath}/manager/staff-shifts"
                   class="card-hover flex items-center gap-4 p-5 rounded-2xl bg-gradient-to-br from-blue-50 to-cyan-50 border border-blue-100 group">
                    <div class="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-200 flex-shrink-0">
                        <i data-lucide="list-checks" class="w-6 h-6 text-white"></i>
                    </div>
                    <div>
                        <p class="font-black text-gray-900 text-sm">Ca đã phân</p>
                        <p class="text-xs text-gray-400 mt-0.5">Xem lịch phân công nhân viên</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-blue-600 transition-colors"></i>
                </a>

                <!-- Danh sách ca -->
                <a href="${pageContext.request.contextPath}/manager/shifts"
                   class="card-hover flex items-center gap-4 p-5 rounded-2xl bg-gradient-to-br from-amber-50 to-yellow-50 border border-amber-100 group">
                    <div class="w-12 h-12 bg-amber-500 rounded-xl flex items-center justify-center shadow-lg shadow-amber-200 flex-shrink-0">
                        <i data-lucide="clock" class="w-6 h-6 text-white"></i>
                    </div>
                    <div>
                        <p class="font-black text-gray-900 text-sm">Ca làm việc</p>
                        <p class="text-xs text-gray-400 mt-0.5">Xem tất cả ca hệ thống</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-amber-500 transition-colors"></i>
                </a>

                <!-- Cụm sân của tôi -->
                <a href="${pageContext.request.contextPath}/manager/location"
                   class="card-hover flex items-center gap-4 p-5 rounded-2xl bg-gradient-to-br from-rose-50 to-pink-50 border border-rose-100 group">
                    <div class="w-12 h-12 bg-rose-500 rounded-xl flex items-center justify-center shadow-lg shadow-rose-200 flex-shrink-0">
                        <i data-lucide="map-pin" class="w-6 h-6 text-white"></i>
                    </div>
                    <div>
                        <p class="font-black text-gray-900 text-sm">Cụm sân của tôi</p>
                        <p class="text-xs text-gray-400 mt-0.5">Thông tin cụm sân đang quản lý</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-rose-500 transition-colors"></i>
                </a>

                <!-- Sân bóng -->
                <a href="${pageContext.request.contextPath}/manager/location/fields"
                   class="card-hover flex items-center gap-4 p-5 rounded-2xl bg-gradient-to-br from-teal-50 to-cyan-50 border border-teal-100 group">
                    <div class="w-12 h-12 bg-teal-600 rounded-xl flex items-center justify-center shadow-lg shadow-teal-200 flex-shrink-0">
                        <i data-lucide="layout-grid" class="w-6 h-6 text-white"></i>
                    </div>
                    <div>
                        <p class="font-black text-gray-900 text-sm">Sân bóng</p>
                        <p class="text-xs text-gray-400 mt-0.5">Danh sách sân tại cụm</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-teal-600 transition-colors"></i>
                </a>

                <!-- Dụng cụ -->
                <a href="${pageContext.request.contextPath}/manager/location/equipment"
                   class="card-hover flex items-center gap-4 p-5 rounded-2xl bg-gradient-to-br from-purple-50 to-violet-50 border border-purple-100 group sm:col-span-2">
                    <div class="w-12 h-12 bg-purple-600 rounded-xl flex items-center justify-center shadow-lg shadow-purple-200 flex-shrink-0">
                        <i data-lucide="shield" class="w-6 h-6 text-white"></i>
                    </div>
                    <div>
                        <p class="font-black text-gray-900 text-sm">Dụng cụ tại cụm</p>
                        <p class="text-xs text-gray-400 mt-0.5">Xem danh sách và trạng thái dụng cụ</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-purple-600 transition-colors"></i>
                </a>

            </div>
        </div>

        <!-- SIDEBAR -->
        <div class="space-y-6">

            <!-- Location Info -->
            <div class="bg-white rounded-[2rem] p-6 shadow border border-gray-100">
                <h3 class="text-xs font-black text-gray-400 uppercase tracking-[0.25em] mb-4 flex items-center gap-2">
                    <i data-lucide="map-pin" class="w-3.5 h-3.5 text-[#008751]"></i>
                    Cụm sân của tôi
                </h3>
                <c:choose>
                    <c:when test="${not empty locationName}">
                        <p class="font-black text-gray-900 text-lg leading-tight mb-1">${locationName}</p>
                        <a href="${pageContext.request.contextPath}/manager/location"
                           class="text-xs text-[#008751] font-bold hover:underline flex items-center gap-1 mt-3">
                            <i data-lucide="external-link" class="w-3 h-3"></i>
                            Xem chi tiết cụm sân
                        </a>
                    </c:when>
                    <c:otherwise>
                        <p class="text-sm text-gray-400 font-medium">Chưa được gán cụm sân</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Summary -->
            <div class="bg-gradient-to-br from-[#008751] to-[#006d41] rounded-[2rem] p-6 shadow text-white">
                <h3 class="text-xs font-black text-green-200 uppercase tracking-[0.25em] mb-4">Tóm tắt hôm nay</h3>
                <div class="space-y-3">
                    <div class="flex justify-between items-center">
                        <span class="text-green-200 text-xs font-semibold">Ca hôm nay</span>
                        <span class="font-black text-xl">${todayCount != null ? todayCount : '—'}</span>
                    </div>
                    <div class="h-px bg-white/10"></div>
                    <div class="flex justify-between items-center">
                        <span class="text-green-200 text-xs font-semibold">Ca sắp tới (7 ngày)</span>
                        <span class="font-black text-xl">${upcoming != null ? upcoming : '—'}</span>
                    </div>
                    <div class="h-px bg-white/10"></div>
                    <div class="flex justify-between items-center">
                        <span class="text-green-200 text-xs font-semibold">Tổng đã phân</span>
                        <span class="font-black text-xl">${assignedCount != null ? assignedCount : '—'}</span>
                    </div>
                </div>
                <a href="${pageContext.request.contextPath}/manager/staff-shifts"
                   class="mt-5 w-full flex items-center justify-center gap-2 py-3 rounded-xl bg-white/10 hover:bg-white/20 text-xs font-black uppercase tracking-wider transition-all">
                    <i data-lucide="list-checks" class="w-3.5 h-3.5"></i>
                    Xem tất cả ca
                </a>
            </div>

        </div>
    </div>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />

<script>
    lucide.createIcons();
    function updateClock() {
        const now = new Date();
        document.getElementById('current-time').textContent =
            now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }) +
            ' — ' + now.toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' });
    }
    setInterval(updateClock, 1000);
    updateClock();
</script>
</body>
</html>
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
                    <a href="${pageContext.request.contextPath}/manager/location" class="group relative bg-gradient-to-br from-slate-800 to-slate-700 rounded-[2.5rem] p-8 text-white shadow-xl shadow-slate-500/30 overflow-hidden hover:shadow-2xl hover:shadow-slate-500/40 transition-all duration-300">
                        <div class="absolute top-0 right-0 opacity-10 group-hover:scale-125 transition-transform duration-500">
                            <i data-lucide="map-pin" class="w-40 h-40"></i>
                        </div>
                        <div class="relative z-10">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4 group-hover:bg-white/30 transition-all">
                                <i data-lucide="map-pin" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-black uppercase tracking-tight leading-none mb-2">Cơ Sở Của Tôi</h3>
                            <p class="text-slate-100/80 text-sm font-medium">Xem detail cơ sở được gán và cập nhật trạng thái cơ sở</p>
                        </div>
                    </a>

                    <a href="${pageContext.request.contextPath}/manager/fields" class="group relative bg-gradient-to-br from-emerald-600 to-emerald-700 rounded-[2.5rem] p-8 text-white shadow-xl shadow-emerald-500/30 overflow-hidden hover:shadow-2xl hover:shadow-emerald-500/40 transition-all duration-300">
                        <div class="absolute top-0 right-0 opacity-10 group-hover:scale-125 transition-transform duration-500">
                            <i data-lucide="layout-grid" class="w-40 h-40"></i>
                        </div>
                        <div class="relative z-10">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4 group-hover:bg-white/30 transition-all">
                                <i data-lucide="layout-grid" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-black uppercase tracking-tight leading-none mb-2">Sân Của Tôi</h3>
                            <p class="text-emerald-100/80 text-sm font-medium">Xem các sân thuộc location được gán, sửa trạng thái và xem lịch sân</p>
                        </div>
                    </a>

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

                    <!-- Dụng cụ cơ sở -->
                    <a href="${pageContext.request.contextPath}/manager/location-equipment" class="group relative bg-gradient-to-br from-purple-500 to-purple-600 rounded-[2.5rem] p-8 text-white shadow-xl shadow-purple-500/30 overflow-hidden hover:shadow-2xl hover:shadow-purple-500/40 transition-all duration-300">
                        <div class="absolute top-0 right-0 opacity-10 group-hover:scale-125 transition-transform duration-500">
                            <i data-lucide="hammer" class="w-40 h-40"></i>
                        </div>
                        <div class="relative z-10">
                            <div class="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center mb-4 group-hover:bg-white/30 transition-all">
                                <i data-lucide="hammer" class="w-6 h-6"></i>
                            </div>
                            <h3 class="text-xl font-black uppercase tracking-tight leading-none mb-2">Dụng Cụ Cơ Sở</h3>
                            <p class="text-purple-100/80 text-sm font-medium">Xem và cập nhật số lượng, trạng thái dụng cụ tại cơ sở được gán</p>
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
