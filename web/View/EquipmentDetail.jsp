<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${equipment.name} - Equipment Detail</title>

    <!-- Tailwind -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/EquipmentDetail.css">
</head>

<body class="bg-slate-50 min-h-screen pb-20">

<!-- HEADER -->
<header class="bg-white border-b border-slate-200 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <div class="bg-emerald-600 p-2 rounded-lg text-white">
                <i data-lucide="trophy" class="w-5 h-5"></i>
            </div>
            <h1 class="text-xl font-bold text-slate-800">
                FIFA<span class="text-emerald-600">FIELD</span>
            </h1>
        </div>

        <a href="${pageContext.request.contextPath}/equipment-list"
           class="flex items-center gap-2 text-sm text-slate-500 hover:text-slate-700">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            Quay lại
        </a>
    </div>
</header>

<!-- CONTENT -->
<div class="max-w-6xl mx-auto px-6 pt-10">

    <!-- TITLE -->
    <div class="mb-8">
        <h2 class="text-2xl font-semibold text-slate-800">
            ${equipment.name}
        </h2>
        <p class="text-slate-400 text-sm">
            Chi tiết thiết bị
        </p>
    </div>

    <!-- CARD -->
    <div class="bg-white rounded-2xl shadow border border-slate-200">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-10 p-8">

            <!-- IMAGE -->
            <div>
                <div class="rounded-xl overflow-hidden border bg-slate-50">
                    <img src="${pageContext.request.contextPath}/${equipment.imageUrl}"
                         alt="${equipment.name}"
                         class="w-full h-[380px] object-cover">
                </div>
            </div>

            <!-- INFO -->
            <div class="space-y-6 text-slate-700">

                <div>
                    <p class="text-sm text-slate-400">Loại thiết bị</p>
                    <p class="text-lg font-medium">
                        ${equipment.equipmentType}
                    </p>
                </div>

                <div>
                    <p class="text-sm text-slate-400">Trạng thái</p>
                    <p class="text-base">
                        ${equipment.status}
                    </p>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div class="p-4 bg-slate-50 rounded-lg border">
                        <p class="text-sm text-slate-400">Giá thuê</p>
                        <p class="text-lg font-semibold text-slate-800">
                            ${equipment.rentalPrice} VNĐ
                        </p>
                    </div>

                    <div class="p-4 bg-slate-50 rounded-lg border">
                        <p class="text-sm text-slate-400">Phí hỏng hóc</p>
                        <p class="text-lg font-semibold text-slate-800">
                            ${equipment.damageFee} VNĐ
                        </p>
                    </div>
                </div>

                <div>
                    <p class="text-sm text-slate-400 mb-1">Mô tả</p>
                    <p class="text-sm leading-relaxed">
                        <c:choose>
                            <c:when test="${not empty equipment.description}">
                                ${equipment.description}
                            </c:when>
                            <c:otherwise>
                                <span class="italic text-slate-400">
                                    Không có mô tả
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>

                <!-- ACTIONS -->
                <div class="flex gap-3 pt-4 border-t">
                    <a href="${pageContext.request.contextPath}/edit-equipment?id=${equipment.id}"
                       class="flex-1 py-3 bg-emerald-600 hover:bg-emerald-700
                              text-white text-sm font-semibold rounded-lg
                              flex items-center justify-center gap-2">
                        <i data-lucide="edit-3" class="w-4 h-4"></i>
                        Chỉnh sửa
                    </a>

                    <a href="${pageContext.request.contextPath}/equipment-list"
                       class="flex-1 py-3 border border-slate-300
                              text-slate-600 text-sm font-semibold rounded-lg
                              flex items-center justify-center">
                        Danh sách
                    </a>
                </div>

            </div>
        </div>
    </div>
</div>

<script>
    lucide.createIcons();
</script>
</body>
</html>
