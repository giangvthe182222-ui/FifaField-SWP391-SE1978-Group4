<%-- 
    Document   : FooterManager
    Created on : Mar 3, 2026
    Author     : FifaField Team
    Description: Manager-specific footer
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>


<footer class="mt-24 bg-gradient-to-b from-white to-slate-50 border-t border-slate-200">
    <div class="max-w-7xl mx-auto px-6 py-16">

        <div class="grid grid-cols-1 md:grid-cols-4 gap-12">

            <!-- BRAND -->
            <div class="space-y-5">
                <div class="flex items-center gap-2">
                    <div class="bg-[#008751] p-2 rounded-xl">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" stroke-width="2"
                             viewBox="0 0 24 24">
                        <path d="M8 21h8M12 17v4"/>
                        <path d="M7 4h10v4a5 5 0 01-10 0V4z"/>
                        <path d="M4 4h3v2a4 4 0 01-3 4"/>
                        <path d="M20 4h-3v2a4 4 0 003 4"/>
                        </svg>
                    </div>
                    <span class="text-xl font-black tracking-tight">
                        FIFA<span class="text-[#008751]">FIELD</span>
                    </span>
                </div>

                <p class="text-sm text-slate-500 leading-relaxed font-medium">
                    Nền tảng quản lý hiệu quả cho quản lý sân bóng.
                    Quản lý nhân viên, ca làm việc và các vị trí của bạn một cách đơn giản.
                </p>
            </div>

            <!-- MANAGER FEATURES -->
            <div>
                <h4 class="text-[11px] font-black uppercase tracking-widest text-slate-600 mb-5">
                    Công cụ quản lý
                </h4>
                <ul class="space-y-3 text-sm font-semibold">
                    <li><a href="${pageContext.request.contextPath}/manager/dashboard" class="text-slate-500 hover:text-[#008751] transition">Bảng điều khiển</a></li>
                    <li><a href="${pageContext.request.contextPath}/staff-list" class="text-slate-500 hover:text-[#008751] transition">Quản lý nhân viên</a></li>
                    <li><a href="${pageContext.request.contextPath}/manager/shifts" class="text-slate-500 hover:text-[#008751] transition">Ca làm việc</a></li>
                    <li><a href="${pageContext.request.contextPath}/bookings" class="text-slate-500 hover:text-[#008751] transition">Đặt sân</a></li>
                </ul>
            </div>

            <!-- RESOURCES -->
            <div>
                <h4 class="text-[11px] font-black uppercase tracking-widest text-slate-600 mb-5">
                    Tài nguyên
                </h4>
                <ul class="space-y-3 text-sm font-semibold">
                    <li><a href="${pageContext.request.contextPath}/locations" class="text-slate-500 hover:text-[#008751] transition">Cụm sân</a></li>
                    <li><a href="${pageContext.request.contextPath}/equipment-list" class="text-slate-500 hover:text-[#008751] transition">Dụng cụ</a></li>
                    <li><a href="${pageContext.request.contextPath}/manager/schedule" class="text-slate-500 hover:text-[#008751] transition">Lịch của tôi</a></li>
                </ul>
            </div>

            <!-- SYSTEM STATUS -->
            <div>
                <h4 class="text-[11px] font-black uppercase tracking-widest text-slate-600 mb-5">
                    Trạng thái hệ thống
                </h4>
                <div class="flex items-center gap-3 bg-slate-100 px-4 py-3 rounded-2xl border border-slate-200">
                    <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span class="text-sm font-bold text-slate-600">
                        Hệ thống hoạt động ổn định
                    </span>
                </div>
            </div>

        </div>

        <!-- DIVIDER -->
        <div class="border-t border-slate-200 my-12"></div>

        <!-- BOTTOM -->
        <div class="flex flex-col md:flex-row items-center justify-between gap-4">
            <p class="text-xs font-bold text-slate-500 tracking-wide">
                © <%= java.time.Year.now()%> FIFAFIELD. All rights reserved.
            </p>

            <div class="flex items-center gap-6 text-xs font-bold text-slate-500 uppercase tracking-widest">
                <span>Version 1.0.0</span>
                <span>Build: Stable</span>
            </div>
        </div>

    </div>
</footer>
