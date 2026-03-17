<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết nhân viên - Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-gray-50 text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp"/>

<main class="max-w-5xl mx-auto px-6 py-10 w-full flex-grow space-y-8">
    <div class="flex items-center justify-between">
        <a href="${pageContext.request.contextPath}/manager/staff/list"
           class="inline-flex items-center gap-2 text-sm font-semibold text-gray-600 hover:text-[#008751]">
            <i data-lucide="arrow-left" class="w-4 h-4"></i> Quay lại danh sách nhân viên
        </a>
    </div>

    <div class="bg-white rounded-2xl shadow border border-gray-100 p-6 md:p-8 space-y-8">
        <div class="flex flex-col md:flex-row md:items-start gap-6">
            <div class="w-20 h-20 rounded-2xl bg-[#008751] text-white flex items-center justify-center text-3xl font-black">
                ${staff.fullName.charAt(0)}
            </div>
            <div class="flex-1 space-y-2">
                <h1 class="text-3xl font-black">${staff.fullName}</h1>
                <p class="text-sm text-gray-500">Mã nhân viên: <span class="font-bold text-gray-800">${staff.employeeCode}</span></p>
                <p class="text-sm text-gray-500">Cơ sở: <span class="font-bold text-gray-800">${staff.locationName}</span></p>
                <p class="text-sm text-gray-500">Trạng thái: <span class="font-bold text-gray-800">${staff.status}</span></p>
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="rounded-xl border border-gray-100 p-5 bg-gray-50">
                <p class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Thông tin nhân viên</p>
                <p class="text-sm"><span class="font-semibold">Email:</span> ${staff.email}</p>
                <p class="text-sm mt-2"><span class="font-semibold">SĐT:</span> ${staff.phone}</p>
                <p class="text-sm mt-2"><span class="font-semibold">Địa chỉ:</span> ${staff.address}</p>
                <p class="text-sm mt-2"><span class="font-semibold">Giới tính:</span> ${staff.gender}</p>
                <p class="text-sm mt-2"><span class="font-semibold">Ngày vào làm:</span> ${staff.hireDate}</p>
            </div>

            <div class="rounded-xl border border-emerald-200 p-5 bg-emerald-50">
                <p class="text-xs font-bold text-emerald-700 uppercase tracking-wider mb-2">Số ca đã làm</p>
                <p class="text-sm text-gray-700">Tổng số ca đã làm tới hiện tại:</p>
                <p class="text-4xl font-black text-[#008751] mt-2">${workedShiftCount}</p>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/FooterManager.jsp"/>
<script>lucide.createIcons();</script>
</body>
</html>
