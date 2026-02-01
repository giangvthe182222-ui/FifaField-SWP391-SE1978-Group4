<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Cập nhật vật tư</title>
        <script src="https://cdn.tailwindcss.com"></script>
    </head>

    <body class="bg-gray-50">
        <jsp:include page="/View/Layout/Header.jsp"/>

        <div class="max-w-3xl mx-auto py-12 px-6">

            <!-- BACK -->
            <a href="javascript:history.back()"
               class="text-sm font-bold text-gray-400 hover:text-[#008751] uppercase tracking-widest">
                ← Quay lại
            </a>

            <!-- CARD -->
            <div class="mt-6 bg-white rounded-3xl shadow-sm border border-gray-100 p-10">

                <h1 class="text-2xl font-black text-gray-900 mb-6">
                    Chỉnh sửa vật tư
                </h1>

                <!-- INFO -->
                <div class="flex gap-6 mb-8">
                    <img src="${locationEquipment.imageUrl}"
                         class="w-32 h-32 object-cover rounded-2xl border">

                    <div class="space-y-2">
                        <p class="text-xs font-black text-gray-400 uppercase tracking-widest">
                            ${locationEquipment.equipmentType}
                        </p>
                        <h2 class="text-xl font-black">
                            ${locationEquipment.name}
                        </h2>
                        <p class="text-sm text-gray-500">
                            Trong kho:
                            <input
                                type="number"
                                min="0"
                                name="quantity"
                                value="${locationEquipment.quantity}"
                                class="w-20 border rounded px-1 text-sm"
                                onchange="updateQuantity(this, '${locationEquipment.id}')"
                                />
                        </p>

                    </div>
                </div>

                <!-- FORM -->
                <form method="post" action="${pageContext.request.contextPath}/update-location-equipment" class="space-y-6">

                    <input type="hidden" name="locationId" value="${locationId}">
                    <input type="hidden" name="equipmentId" value="${equipmentId}">

                    <!-- PRICE -->
                    <div>
                        <label class="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">
                            Giá thuê (VNĐ)
                        </label>
                        <input type="number"
                               name="rentalPrice"
                               required
                               value="${locationEquipment.rentalPrice}"
                               class="w-full px-5 py-4 rounded-2xl bg-gray-50 border border-gray-200
                               focus:outline-none focus:ring-4 focus:ring-[#008751]/10">
                    </div>

                    <!-- STATUS -->
                    <div>
                        <label class="block text-xs font-black uppercase tracking-widest text-gray-400 mb-2">
                            Trạng thái
                        </label>
                        <select name="status"
                                class="w-full px-5 py-4 rounded-2xl bg-gray-50 border border-gray-200
                                font-bold focus:outline-none">
                            <option value="available"
                                    ${locationEquipment.status == 'available' ? 'selected' : ''}>
                                Sẵn sàng
                            </option>
                            <option value="unavailable"
                                    ${locationEquipment.status == 'unavailable' ? 'selected' : ''}>
                                Không sẵn sàng
                            </option>
                        </select>
                    </div>

                    <!-- ACTION -->
                    <div class="flex justify-end gap-4 pt-4">
                        <a href="javascript:history.back()"
                           class="px-6 py-3 rounded-2xl text-sm font-black text-gray-500 hover:bg-gray-100">
                            Hủy
                        </a>

                        <button type="submit"
                                class="px-8 py-3 rounded-2xl bg-[#008751] text-white
                                font-black uppercase tracking-widest shadow-lg
                                hover:bg-[#007043]">
                            Lưu thay đổi
                        </button>
                    </div>

                </form>
            </div>
        </div>

        <jsp:include page="/View/Layout/Footer.jsp"/>
    </body>
</html>
