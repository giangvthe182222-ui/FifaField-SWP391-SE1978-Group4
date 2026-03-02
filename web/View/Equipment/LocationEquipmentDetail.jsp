<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết thiết bị theo cơ sở</title>

    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
</head>

<body class="bg-slate-50 min-h-screen font-[Inter]">

<jsp:include page="/View/Layout/HeaderAdmin.jsp"/>

<div class="max-w-5xl mx-auto px-6 pt-10">

    <!-- BREADCRUMB -->
    <div class="mb-8 flex items-center gap-3 text-sm font-bold text-gray-400">
        <a href="${pageContext.request.contextPath}/location-equipment-list?locationId=${locationId}"
           class="hover:text-[#008751] transition">
            Danh sách thiết bị
        </a>
        <span>/</span>
        <span class="text-gray-900">Chi tiết</span>
    </div>

    <!-- CARD -->
    <div class="bg-white rounded-[3rem] shadow-2xl border border-gray-100 overflow-hidden">

        <!-- IMAGE -->
        <div class="relative h-80 bg-gray-100">
            <img src="${locationEquipment.imageUrl}"
                 class="w-full h-full object-cover">
        </div>

        <!-- CONTENT -->
        <div class="p-12 space-y-10">

            <!-- HEADER -->
            <div class="flex justify-between items-start gap-6">
                <div>
                    <p class="text-[11px] font-black text-gray-400 uppercase tracking-[0.25em]">
                        ${locationEquipment.equipmentType}
                    </p>
                    <h1 class="text-3xl font-black text-gray-900 mt-2">
                        ${locationEquipment.name}
                    </h1>
                </div>

                <!-- STATUS -->
                <span class="px-5 py-2 rounded-full text-xs font-black uppercase tracking-widest
                    ${locationEquipment.status == 'available'
                      ? 'bg-emerald-100 text-emerald-700'
                      : 'bg-gray-200 text-gray-600'}">
                    ${locationEquipment.status}
                </span>
            </div>

            <!-- INFO GRID -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">

                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">
                        Số lượng tại cơ sở
                    </p>
                    <p class="text-2xl font-black text-gray-900">
                        ${locationEquipment.quantity}
                    </p>
                </div>

                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">
                        Giá thuê
                    </p>
                    <p class="text-2xl font-black text-[#008751]">
                        <fmt:formatNumber value="${locationEquipment.rentalPrice}" /> đ
                    </p>
                </div>

                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">
                        Phí đền bù
                    </p>
                    <p class="text-2xl font-black text-gray-900">
                        <fmt:formatNumber value="${locationEquipment.damageFee}" /> đ
                    </p>
                </div>
            </div>

            <!-- ACTIONS -->
            <div class="flex gap-6 pt-6">

                <!-- BACK -->
                <a href="${pageContext.request.contextPath}/location-equipment-list?locationId=${locationId}"
                   class="flex-1 text-center py-5 rounded-[1.5rem]
                          bg-gray-200 hover:bg-gray-300
                          font-black uppercase tracking-wide">
                    Quay lại
                </a>

                <!-- EDIT -->
                <a href="${pageContext.request.contextPath}/update-location-equipment?locationId=${locationId}&equipmentId=${equipmentId}"
                   class="flex-1 text-center py-5 rounded-[1.5rem]
                          bg-[#008751] hover:bg-emerald-700
                          text-white font-black uppercase tracking-wide
                          flex justify-center items-center gap-3">
                    <i data-lucide="edit-3"></i>
                    Chỉnh sửa
                </a>
            </div>

        </div>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>

</body>
</html>
