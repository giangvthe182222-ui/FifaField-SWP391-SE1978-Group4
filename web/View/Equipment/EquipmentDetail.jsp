<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Chi tiết thiết bị</title>

    <!-- Tailwind + Icons -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/AddEquipment.css">
</head>

<body class="bg-slate-50 min-h-screen pb-20 font-[Inter]">

<jsp:include page="/View/Layout/HeaderAdmin.jsp"/>

<!-- CONTENT -->
<div class="max-w-3xl mx-auto px-6 pt-12">

    <!-- BACK -->
    <div class="mb-6">
        <a href="${pageContext.request.contextPath}/equipment-list"
           class="inline-flex items-center gap-2
                  text-gray-400 hover:text-[#008751]
                  transition group">
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="26" height="26"
                 viewBox="0 0 24 24"
                 fill="none"
                 stroke="currentColor"
                 stroke-width="2.5"
                 stroke-linecap="round"
                 stroke-linejoin="round"
                 class="group-hover:-translate-x-1 transition-transform">
                <path d="M22 12H4"/>
                <path d="M11 19l-7-7 7-7"/>
            </svg>
            
        </a>
    </div>

    <!-- CARD -->
    <div class="bg-white rounded-[3rem] shadow-2xl
                border border-slate-200 overflow-hidden
                p-10 md:p-14 space-y-10">

        <!-- IMAGE -->
        <div class="text-center space-y-3">
            <p class="text-xs font-black uppercase text-slate-400">
                Hình ảnh 
            </p>
            <img src="${pageContext.request.contextPath}/${equipment.imageUrl}"
                 class="max-h-72 mx-auto rounded-2xl shadow border">
        </div>

        <!-- INFO GRID -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

            <!-- NAME -->
            <div class="space-y-1">
                <label class="label-fancy">Tên Dụng cụ</label>
                <p class="text-lg font-black text-slate-800">
                    ${equipment.name}
                </p>
            </div>

            <!-- TYPE -->
            <div class="space-y-1">
                <label class="label-fancy">Loại dụng cụ</label>
                <p class="text-slate-700 font-semibold">
                    ${equipment.equipmentType}
                </p>
            </div>

            <!-- PRICE -->
            <div class="space-y-1">
                <label class="label-fancy">Giá thuê</label>
                <p class="text-slate-700 font-semibold">
                    ${equipment.rentalPrice} VNĐ
                </p>
            </div>

            <!-- DAMAGE FEE -->
            <div class="space-y-1">
                <label class="label-fancy">Phí hỏng hóc</label>
                <p class="text-slate-700 font-semibold">
                    ${equipment.damageFee} VNĐ
                </p>
            </div>

            <!-- STATUS -->
            <div class="space-y-1">
                <label class="label-fancy">Trạng thái</label>

                <c:choose>
                    <c:when test="${equipment.status == 'available'}">
                        <span class="inline-block px-4 py-2
                                     rounded-full bg-emerald-100
                                     text-emerald-700 font-black text-sm">
                            Available
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="inline-block px-4 py-2
                                     rounded-full bg-red-100
                                     text-red-700 font-black text-sm">
                            Unavailable
                        </span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- DESCRIPTION -->
        <div class="space-y-2">
            <label class="label-fancy">Mô tả</label>
            <p class="text-slate-600 leading-relaxed">
                ${equipment.description}
            </p>
        </div>

        <!-- ACTIONS -->
        <div class="mt-10 flex gap-6">

            <!-- BACK -->
            <a href="${pageContext.request.contextPath}/equipment-list"
               class="flex-1 text-center py-5
                      bg-slate-200 hover:bg-slate-300
                      rounded-[1.5rem]
                      font-black uppercase tracking-wide">
                Quay lại
            </a>

            <!-- EDIT -->
            <a href="${pageContext.request.contextPath}/edit-equipment?id=${equipment.equipmentId}"
               class="flex-1 text-center py-5
                      bg-emerald-600 hover:bg-emerald-700
                      text-white rounded-[1.5rem]
                      font-black uppercase tracking-wide
                      flex justify-center gap-3 items-center">
                <i data-lucide="edit-3"></i>
                Chỉnh sửa
            </a>
        </div>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>

</body>
</html>
