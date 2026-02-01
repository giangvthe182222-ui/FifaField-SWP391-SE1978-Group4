<%-- 
    Document   : Footer
    Created on : Feb 1, 2026, 11:05:42 AM
    Author     : admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>


<footer class="mt-24 bg-white border-t border-gray-100">
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

                        <p class="text-sm text-gray-400 leading-relaxed font-medium">
                            Hệ thống quản lý sân bóng, vật tư & lịch thi đấu hiện đại.
                            Tối ưu vận hành – minh bạch – chính xác theo thời gian thực.
                        </p>
                    </div>

                    <!-- QUICK LINKS -->
                    <div>
                        <h4 class="text-[11px] font-black uppercase tracking-widest text-gray-500 mb-5">
                            Điều hướng
                        </h4>
                        <ul class="space-y-3 text-sm font-semibold">
                            <li><a href="/locations" class="text-gray-400 hover:text-[#008751] transition">Cụm sân</a></li>
                            <li><a href="/equipment-list" class="text-gray-400 hover:text-[#008751] transition">Dụng cụ</a></li>
                            <li><a href="/staff" class="text-gray-400 hover:text-[#008751] transition">Nhân viên</a></li>
                            <li><a href="/managers" class="text-gray-400 hover:text-[#008751] transition">Quản lý</a></li>
                        </ul>
                    </div>

                    <!-- SUPPORT -->
                    <div>
                        <h4 class="text-[11px] font-black uppercase tracking-widest text-gray-500 mb-5">
                            Hỗ trợ
                        </h4>
                        <ul class="space-y-3 text-sm font-semibold">
                            <li class="text-gray-400">Hotline: 1900 9999</li>
                            <li class="text-gray-400">Email: support@fifafield.vn</li>
                            <li class="text-gray-400">Giờ làm việc: 08:00 – 22:00</li>
                        </ul>
                    </div>

                    <!-- SYSTEM STATUS -->
                    <div>
                        <h4 class="text-[11px] font-black uppercase tracking-widest text-gray-500 mb-5">
                            Trạng thái hệ thống
                        </h4>
                        <div class="flex items-center gap-3 bg-gray-50 px-4 py-3 rounded-2xl border border-gray-100">
                            <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
                            <span class="text-sm font-bold text-gray-500">
                                Hệ thống hoạt động ổn định
                            </span>
                        </div>
                    </div>

                </div>

                <!-- DIVIDER -->
                <div class="border-t border-gray-100 my-12"></div>

                <!-- BOTTOM -->
                <div class="flex flex-col md:flex-row items-center justify-between gap-4">
                    <p class="text-xs font-bold text-gray-400 tracking-wide">
                        © <%= java.time.Year.now()%> FIFAFIELD. All rights reserved.
                    </p>

                    <div class="flex items-center gap-6 text-xs font-bold text-gray-400 uppercase tracking-widest">
                        <span>Version 1.0.0</span>
                        <span>Build: Stable</span>
                    </div>
                </div>

            </div>
        </footer>