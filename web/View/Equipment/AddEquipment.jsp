
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <!-- Tailwind CSS & Google Fonts -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;900&display=swap" rel="stylesheet">
    <link rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/AddEquipment.css">
    <script src="https://unpkg.com/lucide@latest"></script>
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
            const price = document.forms["addForm"]["rental_price"].value;
            const fee = document.forms["addForm"]["damage_fee"].value;

            if (parseFloat(price) <= 0 || parseFloat(fee) <= 0) {
                alert("Giá thuê và Phí hỏng hóc phải lớn hơn 0");
                return false;
            }
            return true;
        }
    </script>
</head>
<body class="bg-slate-50 min-h-screen pb-20">

    <!-- Header tương tự các view khác -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="bg-emerald-600 p-2 rounded-xl text-white shadow-lg">
                    <i data-lucide="trophy" class="w-6 h-6"></i>
                </div>
                <h1 class="text-2xl font-black text-slate-900 tracking-tighter">FIFA<span class="text-emerald-600">FIELD</span></h1>
            </div>
            <div class="flex items-center gap-4">
    <a href="${pageContext.request.contextPath}/equipment-list"
       class="flex items-center gap-2 px-5 py-2 rounded-full hover:bg-emerald-50 
              text-slate-600 hover:text-emerald-600 font-black text-sm transition-all">
        <i data-lucide="arrow-left" class="w-4 h-4"></i>
        quay lại
    </a>
    </div>

        </div>
    </header>

    <div class="max-w-3xl mx-auto px-6 pt-12">
        <!-- Breadcrumb & Title -->
        

        <!-- Thông báo JSTL -->
        <c:if test="${not empty error}">
            <div class="mb-6 p-4 bg-red-50 border-2 border-red-100 rounded-2xl flex items-center gap-3 text-red-600 font-bold animate-pulse">
                <i data-lucide="alert-circle"></i> ${error}
            </div>
        </c:if>

        <c:if test="${not empty success}">
            <div class="mb-6 p-4 bg-emerald-50 border-2 border-emerald-100 rounded-2xl flex items-center gap-3 text-emerald-600 font-bold">
                <i data-lucide="check-circle"></i> ${success}
            </div>
        </c:if>

        <!-- Form Card -->
        <div class="bg-white rounded-[3rem] shadow-2xl border border-slate-200 overflow-hidden">
            <form name="addForm"
                  action="${pageContext.request.contextPath}/add-equipment"
                  method="post"
                  enctype="multipart/form-data"
                  onsubmit="return validateForm()"
                  class="p-10 md:p-14 space-y-8">

                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <!-- Tên thiết bị -->
                    <div class="space-y-2">
                        <label class="label-fancy">Tên thiết bị</label>
                        <div class="relative">
                            <i data-lucide="package" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                            <input type="text" name="name" required class="input-fancy" placeholder="Ví dụ: Bóng Pro 2024">
                        </div>
                    </div>

                    <!-- Loại thiết bị -->
                    <div class="space-y-2">
                        <label class="label-fancy">Loại thiết bị</label>
                        <div class="relative">
                            <i data-lucide="tag" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                            <input type="text" name="equipment_type" required class="input-fancy" placeholder="Ví dụ: Dụng cụ thi đấu">
                        </div>
                    </div>

                    <!-- Giá thuê -->
                    <div class="space-y-2">
                        <label class="label-fancy">Giá thuê (VNĐ)</label>
                        <div class="relative">
                            <i data-lucide="dollar-sign" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                            <input type="number" step="0.01" name="rental_price" required class="input-fancy" placeholder="0.00">
                        </div>
                    </div>

                    <!-- Phí hỏng hóc -->
                    <div class="space-y-2">
                        <label class="label-fancy">Phí đền bù hỏng hóc</label>
                        <div class="relative">
                            <i data-lucide="shield-alert" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5"></i>
                            <input type="number" step="0.01" name="damage_fee" required class="input-fancy" placeholder="0.00">
                        </div>
                    </div>
                </div>

                <!-- Tải ảnh -->
                <div class="space-y-4">
                    <label class="label-fancy">Hình ảnh thiết bị</label>
                    <div class="flex items-center justify-center w-full">
                        <label class="flex flex-col items-center justify-center w-full h-40 border-2 border-slate-100 border-dashed rounded-[2rem] cursor-pointer bg-slate-50 hover:bg-slate-100 transition-all">
                            <div class="flex flex-col items-center justify-center pt-5 pb-6">
                                <i data-lucide="upload-cloud" class="w-10 h-10 text-slate-400 mb-3"></i>
                                <p class="text-sm text-slate-500 font-medium">Nhấn để tải ảnh hoặc kéo thả</p>
                                <p class="text-[10px] text-slate-400 uppercase font-black">PNG, JPG (MAX. 800x400px)</p>
                            </div>
                            <input type="file" name="image" accept="image/*" class="hidden" onchange="previewImage(this)" required />
                        </label>
                    </div>
                    <!-- Preview Ảnh -->
                    <div id="preview-container" class="hidden mt-4 p-4 bg-slate-50 rounded-[2rem] border-2 border-slate-100">
                        <p class="text-[10px] font-black text-slate-400 uppercase mb-3 text-center">Xem trước ảnh</p>
                        <img id="preview" class="max-h-60 mx-auto rounded-2xl shadow-lg border-4 border-white" />
                    </div>
                </div>

                <!-- Trạng thái & Mô tả -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <div class="md:col-span-1 space-y-2">
                        <label class="label-fancy">Trạng thái</label>
                        <select name="status" class="w-full px-5 py-4 bg-slate-50 border-2 border-slate-100 rounded-2xl font-black text-sm outline-none focus:border-emerald-500 transition-all appearance-none cursor-pointer">
                            <option value="available">Available</option>
                            <option value="unavailable">Unavailable</option>
                        </select>
                    </div>
                    <div class="md:col-span-2 space-y-2">
                        <label class="label-fancy">Mô tả chi tiết</label>
                        <textarea name="description" class="w-full p-5 bg-slate-50 border-2 border-slate-100 rounded-2xl focus:border-emerald-500 outline-none transition-all font-medium h-[120px] resize-none" placeholder="Nhập ghi chú về thiết bị..."></textarea>
                    </div>
                </div>

                <!-- Nút Submit -->
                <button type="submit" class="w-full py-6 bg-emerald-600 hover:bg-emerald-700 text-white font-black rounded-[1.5rem] shadow-2xl shadow-emerald-100 transition-all active:scale-[0.98] uppercase tracking-[0.2em] text-sm flex items-center justify-center gap-3">
                    <i data-lucide="plus-circle" class="w-5 h-5"></i> XÁC NHẬN THÊM THIẾT BỊ
                </button>
            </form>
        </div>
    </div>

    <script>lucide.createIcons();</script>
</body>
</html>
