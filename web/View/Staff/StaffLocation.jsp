<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Location của Staff</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="max-w-6xl mx-auto px-6 py-10 space-y-8">
    <section class="bg-white border border-gray-100 rounded-3xl p-8 shadow-sm">
        <p class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Location Của Staff</p>
        <h1 class="text-3xl font-black uppercase tracking-tight mt-2">${location.locationName}</h1>
        <p class="text-sm font-semibold text-gray-500 mt-2">${location.address}</p>
        <p class="text-sm font-semibold text-gray-500">Số điện thoại: ${location.phoneNumber}</p>
        <p class="text-sm font-semibold text-gray-500">Trạng thái: ${location.status}</p>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <a href="${pageContext.request.contextPath}/staff/fields" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">Điều hướng 1</p>
            <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Các sân và lịch</h2>
            <p class="text-sm font-semibold text-gray-500 mt-3">Đi tới danh sách sân phụ trách và màn hình lịch sân theo tuần.</p>
        </a>

        <a href="${pageContext.request.contextPath}/staff/locationBookings" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">Điều hướng 2</p>
            <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Location bookings</h2>
            <p class="text-sm font-semibold text-gray-500 mt-3">Mở trang bookings theo location đã có sẵn trong hệ thống staff.</p>
        </a>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>
