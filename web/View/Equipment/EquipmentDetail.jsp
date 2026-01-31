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

<header class="bg-white border-b border-gray-100 py-4 px-6 sticky top-0 z-50 shadow-sm">
  <div class="max-w-7xl mx-auto flex items-center justify-between">

    <!-- LOGO -->
    <a href="/" class="flex items-center gap-2">
      <div class="bg-[#008751] p-1.5 rounded-lg">
        <!-- Trophy -->
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

    <!-- NAV -->
    <nav class="hidden lg:flex items-center gap-4">

      <!-- Nhân viên -->
      <a href="/staff"
         class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
                text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
        <!-- 2 users -->
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
      <a href="/managers"
         class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
                text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
        <!-- Shield check -->
        <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24">
          <path d="M12 2l8 4v6c0 5-3.5 9-8 10-4.5-1-8-5-8-10V6l8-4z"/>
          <path d="M9 12l2 2 4-4"/>
        </svg>
        Quản lý
      </a>

      <!-- Cụm sân -->
      <a href="/locations"
         class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
                text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
        <!-- Map pin -->
        <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24">
          <path d="M12 21s7-6.5 7-11a7 7 0 10-14 0c0 4.5 7 11 7 11z"/>
          <circle cx="12" cy="10" r="3"/>
        </svg>
        Cụm sân
      </a>

      <!-- Dụng cụ -->
      <a href="/equipment-list"
         class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold
                text-gray-500 hover:text-gray-900 hover:bg-gray-50 transition-all">
        <!-- Football -->
        <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="9"/>
          <path d="M12 7l3 2-1 3h-4l-1-3 3-2z"/>
        </svg>
        Dụng cụ
      </a>

    </nav>

    <!-- RIGHT -->
    <div class="flex items-center gap-4">
        <div class="w-9 h-9 rounded-full border-2 border-[#008751]
                  flex items-center justify-center text-[#008751] font-bold text-sm">
        A
      </div>
      <!-- Logout -->
      <a href="/logout"
         class="text-gray-400 hover:text-red-500 transition-colors p-2 rounded-xl hover:bg-red-50">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2"
             viewBox="0 0 24 24">
          <path d="M16 17l5-5-5-5"/>
          <path d="M21 12H9"/>
          <path d="M4 4h8v16H4z"/>
        </svg>
      </a>

      <!-- Avatar (outline, không thông tin user) -->
      
    </div>

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
                    <a href="${pageContext.request.contextPath}/edit-equipment?id=${equipment.equipmentId}"
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
