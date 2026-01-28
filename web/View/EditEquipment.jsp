<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Chỉnh sửa thiết bị</title>

    <!-- Tailwind + Icons -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>

    <!-- Reuse AddEquipment CSS -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/AddEquipment.css">

    <script>
        function previewImage(input) {
            const preview = document.getElementById("preview");
            const previewContainer = document.getElementById("preview-container");
            const file = input.files[0];

            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    preview.src = e.target.result;
                    previewContainer.classList.remove("hidden");
                };
                reader.readAsDataURL(file);
            }
        }

        function validateForm() {
            const price = document.forms["editForm"]["rental_price"].value;
            const fee = document.forms["editForm"]["damage_fee"].value;

            if (parseFloat(price) <= 0 || parseFloat(fee) <= 0) {
                alert("Giá thuê và Phí hỏng hóc phải lớn hơn 0");
                return false;
            }
            return true;
        }
    </script>
</head>

<body class="bg-slate-50 min-h-screen pb-20 font-[Inter]">

<!-- HEADER -->
<header class="bg-white border-b border-slate-200 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <div class="bg-emerald-600 p-2 rounded-xl text-white shadow-lg">
                <i data-lucide="trophy" class="w-6 h-6"></i>
            </div>
            <h1 class="text-2xl font-black tracking-tighter text-slate-900">
                FIFA<span class="text-emerald-600">FIELD</span>
            </h1>
        </div>

        <a href="${pageContext.request.contextPath}/equipment-detail?id=${equipment.id}"
           class="flex items-center gap-2 px-5 py-2 rounded-full
                  hover:bg-emerald-50 text-slate-600
                  hover:text-emerald-600 font-black text-sm">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            quay lại
        </a>
    </div>
</header>

<!-- CONTENT -->
<div class="max-w-3xl mx-auto px-6 pt-12">

    <!-- ALERT -->
    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border-2 border-red-100
                    rounded-2xl text-red-600 font-bold flex gap-3">
            <i data-lucide="alert-circle"></i> ${error}
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="mb-6 p-4 bg-emerald-50 border-2 border-emerald-100
                    rounded-2xl text-emerald-600 font-bold flex gap-3">
            <i data-lucide="check-circle"></i> ${success}
        </div>
    </c:if>

    <!-- CARD -->
    <div class="bg-white rounded-[3rem] shadow-2xl border border-slate-200 overflow-hidden">
        <form name="editForm"
              action="${pageContext.request.contextPath}/edit-equipment"
              method="post"
              enctype="multipart/form-data"
              onsubmit="return validateForm()"
              class="p-10 md:p-14 space-y-8">

            <!-- ID hidden -->
            <input type="hidden" name="equipment_id" value="${equipment.id}">
            <input type="hidden" name="old_image" value="${equipment.imageUrl}">

            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

                <!-- NAME -->
                <div class="space-y-2">
                    <label class="label-fancy">Tên thiết bị</label>
                    <div class="relative">
                        <i data-lucide="package"
                           class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                        <input type="text" name="name" required
                               class="input-fancy"
                               value="${equipment.name}">
                    </div>
                </div>

                <!-- TYPE -->
                <div class="space-y-2">
                    <label class="label-fancy">Loại thiết bị</label>
                    <div class="relative">
                        <i data-lucide="tag"
                           class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                        <input type="text" name="equipment_type" required
                               class="input-fancy"
                               value="${equipment.equipmentType}">
                    </div>
                </div>

                <!-- PRICE -->
                <div class="space-y-2">
                    <label class="label-fancy">Giá thuê (VNĐ)</label>
                    <div class="relative">
                        <i data-lucide="dollar-sign"
                           class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                        <input type="number" step="0.01" name="rental_price"
                               class="input-fancy"
                               value="${equipment.rentalPrice}">
                    </div>
                </div>

                <!-- DAMAGE -->
                <div class="space-y-2">
                    <label class="label-fancy">Phí hỏng hóc</label>
                    <div class="relative">
                        <i data-lucide="shield-alert"
                           class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                        <input type="number" step="0.01" name="damage_fee"
                               class="input-fancy"
                               value="${equipment.damageFee}">
                    </div>
                </div>
            </div>

            <!-- IMAGE -->
            <div class="space-y-4">
                <label class="label-fancy">Hình ảnh thiết bị</label>

                <!-- OLD IMAGE -->
                <div class="p-4 bg-slate-50 rounded-2xl border text-center">
                    <p class="text-[10px] font-black uppercase text-slate-400 mb-2">
                        Ảnh hiện tại
                    </p>
                    <img src="${pageContext.request.contextPath}/${equipment.imageUrl}"
                         class="max-h-56 mx-auto rounded-2xl shadow border">
                </div>

                <!-- UPLOAD NEW -->
                <label class="flex flex-col items-center justify-center w-full h-40
                              border-2 border-dashed rounded-[2rem] cursor-pointer
                              bg-slate-50 hover:bg-slate-100">
                    <i data-lucide="upload-cloud"
                       class="w-10 h-10 text-slate-400 mb-2"></i>
                    <p class="text-sm text-slate-500 font-medium">
                        Chọn ảnh mới (nếu muốn)
                    </p>
                    <input type="file" name="image" class="hidden"
                           accept="image/*"
                           onchange="previewImage(this)">
                </label>

                <!-- PREVIEW -->
                <div id="preview-container" class="hidden p-4 bg-slate-50 rounded-2xl border">
                    <p class="text-[10px] font-black uppercase text-slate-400 mb-2 text-center">
                        Xem trước ảnh mới
                    </p>
                    <img id="preview" class="max-h-56 mx-auto rounded-2xl shadow">
                </div>
            </div>

            <!-- STATUS + DESC -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div>
                    <label class="label-fancy">Trạng thái</label>
                    <select name="status"
                            class="w-full px-5 py-4 bg-slate-50 border-2
                                   border-slate-100 rounded-2xl font-black text-sm">
                        <option value="available"
                            ${equipment.status == 'available' ? 'selected' : ''}>
                            Available
                        </option>
                        <option value="unavailable"
                            ${equipment.status == 'unavailable' ? 'selected' : ''}>
                            Unavailable
                        </option>
                    </select>
                </div>

                <div class="md:col-span-2">
                    <label class="label-fancy">Mô tả</label>
                    <textarea name="description"
                              class="w-full p-5 bg-slate-50 border-2
                                     border-slate-100 rounded-2xl h-[120px] resize-none">${equipment.description}</textarea>
                </div>
            </div>

            <!-- SUBMIT -->
            <button type="submit"
                    class="w-full py-6 bg-emerald-600 hover:bg-emerald-700
                           text-white font-black rounded-[1.5rem]
                           shadow-2xl uppercase tracking-[0.2em]
                           flex items-center justify-center gap-3">
                <i data-lucide="save" class="w-5 h-5"></i>
                CẬP NHẬT THIẾT BỊ
            </button>

        </form>
    </div>
</div>

<script>
    lucide.createIcons();
</script>
</body>
</html>
