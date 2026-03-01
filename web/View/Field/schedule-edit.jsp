<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Chỉnh sửa khung giờ</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
    <style>body{font-family:Inter, sans-serif}</style>
</head>
<body class="bg-gray-100 min-h-screen">
<div class="max-w-xl mx-auto p-6">
    <div class="bg-white p-6 rounded-2xl shadow">
        <h1 class="text-xl font-black mb-4">Chỉnh sửa khung giờ</h1>
        <c:if test="${not empty error}">
            <div class="bg-red-100 text-red-700 p-3 rounded mb-3">${error}</div>
        </c:if>
        <form method="post">
            <input type="hidden" name="scheduleId" value="${schedule.scheduleId}" />
            <input type="hidden" name="fieldId" value="${schedule.fieldId}" />

            <div class="mb-3">
                <label class="block text-xs font-black text-gray-500 uppercase">Ngày</label>
                <div class="text-lg font-bold">${schedule.bookingDate}</div>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-3">
                <div>
                    <label class="block text-xs font-black text-gray-500 uppercase">Bắt đầu</label>
                    <div class="text-lg font-bold">${schedule.startTime}</div>
                </div>
                <div>
                    <label class="block text-xs font-black text-gray-500 uppercase">Kết thúc</label>
                    <div class="text-lg font-bold">${schedule.endTime}</div>
                </div>
            </div>

            <div class="mb-3">
                <label class="text-xs font-black uppercase text-gray-500">Giá (đ)</label>
                <input name="price" value="${schedule.price}" type="number" step="0.01" class="w-full px-4 py-3 rounded border" />
            </div>

            <div class="mb-3">
                <label class="text-xs font-black uppercase text-gray-500">Trạng thái</label>
                <select name="status" class="w-full px-4 py-3 rounded border font-black uppercase">
                    <option value="available" ${schedule.status == 'available' ? 'selected' : ''}>SẴN SÀNG</option>
                    <option value="unavailable" ${schedule.status == 'unavailable' ? 'selected' : ''}>KHÓA / UNAVAILABLE</option>
                </select>
            </div>

            <div class="flex gap-3 mt-4">
                <a href="${pageContext.request.contextPath}/field-schedule?fieldId=${schedule.fieldId}" class="flex-1 text-center py-3 rounded-2xl border font-black uppercase">Hủy</a>
                <button class="flex-1 bg-[#008751] text-white py-3 rounded-2xl font-black uppercase">Lưu</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
