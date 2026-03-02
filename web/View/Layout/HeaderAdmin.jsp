<%@page contentType="text/html" pageEncoding="UTF-8"%>

<header class="bg-white border-b border-gray-100 py-4 px-6 sticky top-0 z-50 shadow-sm">
    <div class="max-w-7xl mx-auto flex items-center justify-between">

        <!-- LOGO -->
        <a href="/" class="flex items-center gap-2">
            <div class="bg-[#008751] p-1.5 rounded-lg">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <path d="M8 21h8M12 17v4"/>
                    <path d="M7 4h10v4a5 5 0 01-10 0V4z"/>
                    <path d="M4 4h3v2a4 4 0 01-3 4"/>
                    <path d="M20 4h-3v2a4 4 0 003 4"/>
                </svg>
            </div>
            <span class="text-2xl font-bold tracking-tight">
                FIFA<span class="text-[#008751]">FIELD</span>
            </span>
        </a>

        <!-- NAV - ADMIN -->
        <nav class="hidden lg:flex items-center gap-4">

            <!-- Dashboard -->
            <a href="${pageContext.request.contextPath}/admin-dashboard"
               class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
               text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
                <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
                    <path d="M9 22V12h6v10"/>
                </svg>
                Bảng điều khiển
            </a>

            <!-- Nhân viên -->
            <a href="${pageContext.request.contextPath}/staff-list"
               class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
               text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
                <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <path d="M16 11c1.7 0 3-1.3 3-3s-1.3-3-3-3"/>
                    <path d="M8 11c1.7 0 3-1.3 3-3S9.7 5 8 5 5 6.3 5 8s1.3 3 3 3z"/>
                    <path d="M2 21c0-3 3-5 6-5"/>
                    <path d="M14 16c3 0 6 2 6 5"/>
                </svg>
                Nhân viên
            </a>

            <!-- Quản lý -->
            <a href="${pageContext.request.contextPath}/manager-list"
               class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
               text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
                <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <path d="M12 2l8 4v6c0 5-3.5 9-8 10-4.5-1-8-5-8-10V6l8-4z"/>
                    <path d="M9 12l2 2 4-4"/>
                </svg>
                Quản lý
            </a>

            <!-- Cụm sân -->
            <a href="${pageContext.request.contextPath}/locations"
               class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
               text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
                <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <path d="M12 21s7-6.5 7-11a7 7 0 10-14 0c0 4.5 7 11 7 11z"/>
                    <circle cx="12" cy="10" r="3"/>
                </svg>
                Cụm sân
            </a>

            <!-- Dụng cụ -->
            <a href="${pageContext.request.contextPath}/equipment-list"
               class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
               text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
                <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <path d="M12 6v12m6-6H6"/>
                </svg>
                Dụng cụ
            </a>

            <!-- Đặt sân -->
            <a href="${pageContext.request.contextPath}/bookings"
               class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
               text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
                <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <path d="M16 2v4M8 2v4M3 10h18"/>
                </svg>
                Đặt sân
            </a>

        </nav>

        <!-- RIGHT - PROFILE DROPDOWN -->
        <div class="relative group">
            <button class="text-gray-400 hover:text-gray-700 transition-colors p-2 rounded-xl hover:bg-gray-50 focus:outline-none">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24">
                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                </svg>
            </button>
            <div class="absolute right-0 top-full mt-2 w-48 bg-white border border-gray-200 rounded-xl shadow-lg overflow-hidden opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150 z-50">
                <a href="${pageContext.request.contextPath}/auth/profile"
                   class="flex items-center gap-2 px-4 py-3 text-sm font-semibold text-gray-700 hover:bg-gray-50 hover:text-[#008751] transition-colors">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                    </svg>
                    Thông tin cá nhân
                </a>
                <a href="${pageContext.request.contextPath}/logout"
                   class="flex items-center gap-2 px-4 py-3 text-sm font-semibold text-red-500 hover:bg-red-50 transition-colors border-t border-gray-100">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                    </svg>
                    Đăng xuất
                </a>
            </div>
        </div>

    </div>
</header>
