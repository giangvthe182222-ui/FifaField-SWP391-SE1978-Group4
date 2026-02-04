<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cập nhật vật tư - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .input-focus-ring:focus {
            outline: none;
            ring: 4px;
            ring-color: rgba(0, 135, 81, 0.05);
            border-color: #008751;
            background-color: white;
        }
    </style>
    <script>
        function validateUpdateForm() {
            const status = document.forms["updateForm"]["status"].value;
            if (status !== "available" && status !== "unavailable") {
                alert("Trạng thái không hợp lệ (chỉ 'available' hoặc 'unavailable')");
                return false;
            }
            return true;
        }
    </script>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="max-w-4xl mx-auto px-6 py-12 space-y-8">
    
    <!-- BREADCRUMB & TITLE -->
    <div class="space-y-2">
        <a href="${pageContext.request.contextPath}/location-equipment-list?locationId=${locationId}" 
           class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="m12 19-7-7 7-7"/><path d="M19 12H5"/></svg>
            QUAY LẠI 
        </a>
        <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
            CẬP NHẬT THÔNG TIN<span class="text-[#008751]"> DỤNG CỤ TẠI CƠ SỞ</span>
        </h1>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Điều chỉnh số lượng & Trạng thái vận hành</p>
    </div>

    <!-- MAIN EDIT CARD -->
    <div class="bg-white rounded-[2.5rem] shadow-xl shadow-gray-200/50 border border-gray-100 overflow-hidden">
        <div class="md:flex">
            <!-- Left Side: Visual -->
            <div class="md:w-1/3 bg-gray-50 border-r border-gray-100 p-10 flex flex-col items-center justify-center space-y-6">
                <div class="relative group">
                    <div class="absolute -inset-4 bg-emerald-100 rounded-[2rem] opacity-30 group-hover:opacity-100 transition-opacity"></div>
                    <img src="${locationEquipment.imageUrl}" 
                         alt="${locationEquipment.name}"
                         class="relative w-48 h-48 object-cover rounded-[1.8rem] shadow-lg border-4 border-white">
                </div>
                <div class="text-center">
                    <span class="text-[9px] font-black text-emerald-600 bg-emerald-50 px-3 py-1.5 rounded-full uppercase tracking-widest border border-emerald-100">
                        ${locationEquipment.equipmentType}
                    </span>
                    <h2 class="text-xl font-black text-gray-900 mt-4 leading-tight">${locationEquipment.name}</h2>
                </div>
            </div>

            <!-- Right Side: Form -->
            <div class="md:w-2/3 p-10 md:p-14">
                <form method="post" 
                      action="${pageContext.request.contextPath}/update-location-equipment" 
                      class="space-y-8" 
                      name="updateForm" 
                      onsubmit="return validateUpdateForm()">
                    
                    <input type="hidden" name="locationId" value="${locationId}">
                    <input type="hidden" name="equipmentId" value="${equipmentId}">

                    <!-- Quantity Field -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Số lượng tồn kho (Đơn vị)</label>
                        <div class="relative">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m7.5 4.27 9 5.15"/><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/></svg>
                            </div>
                            <input type="number" name="quantity" min="0" required value="${locationEquipment.quantity}"
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl input-focus-ring transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Status Field -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Trạng thái sẵn dụng</label>
                        <div class="relative">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/></svg>
                            </div>
                            <select name="status" 
                                    class="w-full pl-12 pr-10 py-4 bg-gray-50 border border-gray-100 rounded-2xl input-focus-ring appearance-none font-bold text-sm text-gray-700 cursor-pointer transition-all">
                                <option value="available" ${locationEquipment.status == 'available' ? 'selected' : ''}>SẴN SÀNG (Available)</option>
                                <option value="unavailable" ${locationEquipment.status == 'unavailable' ? 'selected' : ''}>KHÔNG TRỐNG (Unavailable)</option>
                            </select>
                            <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-gray-300">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
                            </div>
                        </div>
                    </div>

                    <!-- Actions -->
                    <div class="flex items-center gap-4 pt-6">
                        <a href="${pageContext.request.contextPath}/location-equipment-list?locationId=${locationId}" 
                           class="flex-1 px-8 py-5 bg-gray-50 text-gray-400 rounded-3xl font-black text-[10px] uppercase tracking-widest text-center hover:bg-gray-100 transition-all border border-transparent">
                            HỦY THAY ĐỔI
                        </a>
                        <button type="submit" 
                                class="flex-[2] bg-[#008751] hover:bg-[#007043] text-white py-5 rounded-3xl font-black text-[10px] uppercase tracking-[0.2em] shadow-2xl shadow-[#008751]/30 transition-all hover:-translate-y-1 active:scale-95">
                            LƯU CẬP NHẬT KHO
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- TIPS SECTION -->
    <div class="bg-blue-50/50 border border-blue-100 rounded-3xl p-6 flex items-start gap-4">
        <div class="bg-blue-500 text-white p-2 rounded-xl">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        </div>
        <div>
            <h4 class="text-xs font-black text-blue-900 uppercase tracking-tight">Lưu ý kiểm kê:</h4>
            <p class="text-xs text-blue-700/70 font-medium mt-1">Số lượng cập nhật sẽ ảnh hưởng trực tiếp đến khả năng đặt vật tư của khách hàng trên ứng dụng người dùng.</p>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>