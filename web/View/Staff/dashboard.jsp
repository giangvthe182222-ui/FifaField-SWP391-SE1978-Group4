<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển Nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .action-card:hover .icon-bg { transform: scale(1.1) rotate(5deg); }
    </style>
</head>
<body class="antialiased text-gray-900">

    <jsp:include page="/View/Layout/HeaderStaff.jsp" />

    <main class="max-w-7xl mx-auto px-6 py-12 space-y-10">
        
        <!-- GREETING SECTION -->
        <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
            <div class="space-y-2">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 border border-emerald-100 rounded-full text-[10px] font-black text-[#008751] uppercase tracking-[0.2em]">
                    <span class="w-1.5 h-1.5 rounded-full bg-[#008751] animate-pulse"></span>
                    Nhân viên đang trực
                </div>
                <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                    XIN CHÀO, <span class="text-[#008751]">${staff.fullName}</span>
                </h1>
                <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.3em]">Cơ sở phụ trách: <span class="text-gray-600">${staff.locationName}</span></p>
            </div>
            
            <div class="bg-white p-2 rounded-2xl shadow-sm border border-gray-100 flex items-center gap-4">
                <div class="px-6 py-2">
                    <div class="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1">Ca làm việc</div>
                    <div class="text-sm font-black text-gray-900" id="current-time">--:--:--</div>
                </div>
                <div class="w-12 h-12 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 border border-gray-100">
                    <i data-lucide="clock" class="w-5 h-5"></i>
                </div>
            </div>
        </div>

        <!-- REFUND ALERTS -->
        <div class="bg-white rounded-[2.5rem] p-8 md:p-10 shadow-xl shadow-gray-200/50 border border-amber-100">
            <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
                <div class="flex items-center gap-3">
                    <div class="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 border border-amber-100 flex items-center justify-center">
                        <i data-lucide="bell-ring" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Refund queue</p>
                        <h2 class="text-2xl font-black text-gray-900 uppercase tracking-tight leading-none">Thông báo hoàn tiền</h2>
                    </div>
                </div>
                <div class="px-5 py-3 rounded-2xl bg-amber-50 border border-amber-100 text-amber-700 font-black text-[11px] uppercase tracking-widest">
                    Còn ${refundPendingCount} đơn cần refund
                </div>
            </div>

            <c:choose>
                <c:when test="${refundPendingCount > 0}">
                    <div class="space-y-3 max-h-72 overflow-y-auto pr-2">
                        <c:forEach var="refundBooking" items="${refundPendingBookings}">
                            <a href="${pageContext.request.contextPath}/staff/bookingDetail?id=${refundBooking.bookingId}" class="block p-4 rounded-2xl border border-amber-100 bg-amber-50/50 hover:bg-amber-50 transition-colors">
                                <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                                    <div>
                                        <p class="text-sm font-black text-gray-900 uppercase tracking-tight">${refundBooking.fieldName}</p>
                                        <p class="text-[10px] font-black text-gray-500 uppercase tracking-widest mt-1">
                                            ${refundBooking.bookingDate} | ${refundBooking.startTime} - ${refundBooking.endTime}
                                        </p>
                                        <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mt-1">
                                            KH: ${refundBooking.customerName}
                                        </p>
                                    </div>
                                    <span class="inline-flex items-center justify-center px-4 py-2 rounded-xl bg-white border border-amber-200 text-amber-700 text-[10px] font-black uppercase tracking-widest">
                                        Xem chi tiết
                                    </span>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="p-5 rounded-2xl border border-emerald-100 bg-emerald-50 text-emerald-700 text-sm font-bold">
                        Hiện chưa có đơn nào đang chờ refund tại cơ sở của bạn.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- MAIN ACTIONS GRID -->
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-8">
            <!-- Đặt sân thường -->
            <a href="${pageContext.request.contextPath}/booking?bookingMode=normal" 
               class="action-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-emerald-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="calendar-plus" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-emerald-50 text-[#008751] rounded-2xl flex items-center justify-center border border-emerald-100 group-hover:bg-[#008751] group-hover:text-white transition-colors">
                        <i data-lucide="calendar-plus" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Dịch vụ</p>
                        <p class="text-2xl font-black text-gray-900 mt-1 leading-none tracking-tight">ĐẶT SÂN THƯỜNG</p>
                    </div>
                </div>
            </a>

            <!-- Đặt sân tuần -->
                <a href="${pageContext.request.contextPath}/booking/weekly" 
               class="action-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-blue-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="layers" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center border border-blue-100 group-hover:bg-blue-600 group-hover:text-white transition-colors">
                        <i data-lucide="layers" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Cố định</p>
                        <p class="text-2xl font-black text-gray-900 mt-1 leading-none tracking-tight">ĐẶT THEO TUẦN</p>
                    </div>
                </div>
            </a>

            <!-- Ca làm việc -->
            <a href="${pageContext.request.contextPath}/staff/my-shifts" 
               class="action-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-indigo-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="user-check" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center border border-indigo-100 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                        <i data-lucide="user-check" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Cá nhân</p>
                        <p class="text-2xl font-black text-gray-900 mt-1 leading-none tracking-tight">CA LÀM VIỆC</p>
                    </div>
                </div>
            </a>

            <!-- Location -->
            <a href="${pageContext.request.contextPath}/staff/location" 
               class="action-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-amber-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="map-pin" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center border border-amber-100 group-hover:bg-amber-600 group-hover:text-white transition-colors">
                        <i data-lucide="map-pin" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Quản lý</p>
                        <p class="text-2xl font-black text-gray-900 mt-1 leading-none tracking-tight">CƠ SỞ TRỰC</p>
                    </div>
                </div>
            </a>

            <!-- Blog -->
            <a href="${pageContext.request.contextPath}/staff/blogs"
               class="action-card bg-white rounded-[2.5rem] p-8 shadow-xl shadow-gray-200/50 border border-gray-100 relative overflow-hidden group transition-all hover:-translate-y-1">
                <div class="absolute -top-4 -right-4 text-violet-500/5 icon-bg transition-transform duration-500">
                    <i data-lucide="book-open" class="w-32 h-32"></i>
                </div>
                <div class="relative z-10 space-y-4">
                    <div class="w-14 h-14 bg-violet-50 text-violet-600 rounded-2xl flex items-center justify-center border border-violet-100 group-hover:bg-violet-600 group-hover:text-white transition-colors">
                        <i data-lucide="book-open" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Noi dung</p>
                        <p class="text-2xl font-black text-gray-900 mt-1 leading-none tracking-tight">BLOG</p>
                    </div>
                </div>
            </a>
        </div>

        <!-- QUICK LINKS & INFO -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            <!-- QUICK LINKS BUTTONS -->
            <div class="lg:col-span-2 bg-white rounded-[3.5rem] p-10 md:p-14 shadow-xl shadow-gray-200/50 border border-gray-100">
                <div class="flex items-center gap-4 mb-10">
                    <div class="w-2.5 h-8 bg-[#008751] rounded-full"></div>
                    <h2 class="text-2xl font-black text-gray-900 uppercase tracking-tight leading-none">Liên kết nhanh</h2>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <a href="${pageContext.request.contextPath}/staff/fields" 
                       class="group flex items-center gap-6 p-6 bg-gray-50/50 border border-gray-100 rounded-[2rem] hover:bg-white hover:shadow-lg transition-all">
                        <div class="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center text-emerald-600 group-hover:bg-[#008751] group-hover:text-white transition-all">
                            <i data-lucide="layout-grid" class="w-5 h-5"></i>
                        </div>
                        <div>
                            <p class="text-sm font-black text-gray-900 uppercase tracking-tight">Sân và Lịch</p>
                            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Xem trạng thái sân thực tế</p>
                        </div>
                    </a>

                    <a href="${pageContext.request.contextPath}/staff/locationBookings" 
                       class="group flex items-center gap-6 p-6 bg-gray-50/50 border border-gray-100 rounded-[2rem] hover:bg-white hover:shadow-lg transition-all">
                        <div class="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-all">
                            <i data-lucide="clipboard-list" class="w-5 h-5"></i>
                        </div>
                        <div>
                            <p class="text-sm font-black text-gray-900 uppercase tracking-tight">Danh sách đặt chỗ</p>
                            <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Quản lý lịch đặt tại cơ sở</p>
                        </div>
                    </a>
                </div>
            </div>

            <!-- ANNOUNCEMENT / HELP -->
            <div class="space-y-8">
                <div class="bg-gray-900 rounded-[3rem] p-10 text-white shadow-2xl shadow-gray-900/20 relative overflow-hidden group">
                    <div class="absolute top-0 right-0 p-10 opacity-10 group-hover:scale-110 transition-transform">
                        <i data-lucide="shield-alert" class="w-32 h-32"></i>
                    </div>
                    <div class="relative z-10 space-y-6">
                        <h3 class="text-xl font-black uppercase tracking-widest leading-none">Ghi chú vận hành</h3>
                        <p class="text-gray-400 text-xs font-medium leading-relaxed italic">"Đảm bảo kiểm tra thẻ thành viên và tình trạng sân trước khi bàn giao cho khách hàng."</p>
                        <div class="pt-4 border-t border-white/10">
                            <div class="flex items-center gap-3 text-emerald-400 font-black text-[10px] uppercase tracking-widest">
                                <i data-lucide="check-circle-2" class="w-4 h-4"></i>
                                Tuân thủ quy trình FIFAFIELD
                            </div>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-[2.5rem] p-8 border border-gray-100 shadow-sm flex flex-col items-center text-center space-y-4">
                    <div class="w-16 h-16 bg-emerald-50 rounded-2xl flex items-center justify-center text-[#008751] border border-emerald-100">
                        <i data-lucide="headphones" class="w-8 h-8"></i>
                    </div>
                    <div>
                        <h4 class="text-sm font-black text-gray-900 uppercase tracking-widest">Hỗ trợ kỹ thuật</h4>
                        <p class="text-[10px] text-gray-400 font-bold uppercase tracking-widest mt-1">Cần hỗ trợ từ Quản lý?</p>
                    </div>
                    <button class="w-full py-4 bg-[#008751] text-white rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-lg shadow-emerald-900/10 hover:bg-[#007043] transition-colors">Gửi yêu cầu nhanh</button>
                </div>
            </div>
        </div>
    </main>

    <jsp:include page="/View/Layout/Footer.jsp" />

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
            const currentTimeElem = document.getElementById('current-time');
            if(currentTimeElem) currentTimeElem.textContent = timeStr;
        }
        setInterval(updateClock, 1000);
        updateClock();
    </script>
</body>
</html>