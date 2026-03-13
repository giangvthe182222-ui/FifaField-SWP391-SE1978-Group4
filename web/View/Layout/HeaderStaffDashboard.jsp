<%@page contentType="text/html" pageEncoding="UTF-8"%>

<header class="bg-white border-b border-gray-100 py-4 px-6 sticky top-0 z-50 shadow-sm">
    <div class="max-w-7xl mx-auto flex items-center justify-between">
        <a href="${pageContext.request.contextPath}/staff/dashboard" class="flex items-center gap-2">
            <div class="bg-[#008751] p-1.5 rounded-lg">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M8 21h8M12 17v4"/>
                    <path d="M7 4h10v4a5 5 0 01-10 0V4z"/>
                    <path d="M4 4h3v2a4 4 0 01-3 4"/>
                    <path d="M20 4h-3v2a4 4 0 003 4"/>
                </svg>
            </div>
            <span class="text-2xl font-bold tracking-tight">FIFA<span class="text-[#008751]">FIELD</span></span>
        </a>

        <nav class="hidden lg:flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/booking" class="px-4 py-2 rounded-xl text-sm font-semibold text-gray-600 hover:text-gray-900 hover:bg-gray-50 transition-all">Book sân</a>
            <a href="${pageContext.request.contextPath}/staff/my-shifts" class="px-4 py-2 rounded-xl text-sm font-semibold text-gray-600 hover:text-gray-900 hover:bg-gray-50 transition-all">Ca làm việc</a>
            <a href="${pageContext.request.contextPath}/staff/location" class="px-4 py-2 rounded-xl text-sm font-semibold text-gray-600 hover:text-gray-900 hover:bg-gray-50 transition-all">Location</a>
            <a href="${pageContext.request.contextPath}/staff/fields" class="px-4 py-2 rounded-xl text-sm font-semibold text-gray-600 hover:text-gray-900 hover:bg-gray-50 transition-all">Sân & lịch</a>
            <a href="${pageContext.request.contextPath}/staff/locationBookings" class="px-4 py-2 rounded-xl text-sm font-semibold text-gray-600 hover:text-gray-900 hover:bg-gray-50 transition-all">Location bookings</a>
        </nav>

        <div class="relative group">
            <button class="text-gray-400 hover:text-gray-700 transition-colors p-2 rounded-xl hover:bg-gray-50 focus:outline-none">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                </svg>
            </button>
            <div class="absolute right-0 top-full mt-2 w-48 bg-white border border-gray-200 rounded-xl shadow-lg overflow-hidden opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150 z-50">
                <a href="${pageContext.request.contextPath}/auth/profile" class="flex items-center gap-2 px-4 py-3 text-sm font-semibold text-gray-700 hover:bg-gray-50 hover:text-[#008751] transition-colors">Thông tin cá nhân</a>
                <a href="${pageContext.request.contextPath}/logout" class="flex items-center gap-2 px-4 py-3 text-sm font-semibold text-red-500 hover:bg-red-50 transition-colors border-t border-gray-100">Đăng xuất</a>
            </div>
        </div>
    </div>
</header>
