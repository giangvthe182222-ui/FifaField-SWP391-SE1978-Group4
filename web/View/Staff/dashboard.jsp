<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderStaffDashboard.jsp"/>

<main class="max-w-7xl mx-auto px-6 py-10 space-y-8">
    <section class="bg-white border border-gray-100 rounded-3xl p-8 shadow-sm">
        <p class="text-[10px] font-black text-gray-400 uppercase tracking-[0.25em]">Staff Center</p>
        <h1 class="text-4xl font-black uppercase tracking-tight mt-2">Xin chào, <span class="text-[#008751]">${staff.fullName}</span></h1>
        <p class="text-sm font-semibold text-gray-500 mt-2">Location phụ trách: ${staff.locationName}</p>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <a href="${pageContext.request.contextPath}/booking" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">01</p>
            <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Book sân</h2>
            <p class="text-sm font-semibold text-gray-500 mt-3">Đi tới trang chọn sân, chọn lịch và tạo booking.</p>
        </a>

        <a href="${pageContext.request.contextPath}/staff/my-shifts" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">02</p>
            <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Ca làm việc</h2>
            <p class="text-sm font-semibold text-gray-500 mt-3">Xem toàn bộ ca làm việc được phân cho tài khoản staff hiện tại.</p>
        </a>

        <a href="${pageContext.request.contextPath}/staff/location" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">03</p>
            <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Location</h2>
            <p class="text-sm font-semibold text-gray-500 mt-3">Vào location phụ trách và điều hướng sang sân/lịch hoặc location bookings.</p>
        </a>
    </section>

    <section class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm">
        <h3 class="text-xl font-black uppercase tracking-tight">Quick Links</h3>
        <div class="mt-4 flex flex-wrap gap-3">
            <a href="${pageContext.request.contextPath}/staff/fields" class="px-4 py-2 rounded-xl border border-gray-200 text-[11px] font-black uppercase tracking-widest text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Sân và lịch</a>
            <a href="${pageContext.request.contextPath}/staff/locationBookings" class="px-4 py-2 rounded-xl border border-gray-200 text-[11px] font-black uppercase tracking-widest text-gray-500 hover:border-[#008751] hover:text-[#008751] transition-all">Location bookings</a>
        </div>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>
