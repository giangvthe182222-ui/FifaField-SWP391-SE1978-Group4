<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm ca</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50">
<jsp:include page="/View/Layout/HeaderAdmin.jsp" />
<main class="max-w-3xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Thêm ca mới</h1>
    <c:if test="${not empty error}">
        <div class="text-red-600 mb-3">${error}</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/shifts/add" class="space-y-4 bg-white p-6 rounded shadow" onsubmit="return validateShiftTime()">
        <div>
            <label class="block text-sm font-medium text-gray-700">Tên ca</label>
            <input name="shiftName" value="${shiftName}" required class="mt-1 block w-full border px-3 py-2 rounded" />
        </div>
        <div class="grid grid-cols-2 gap-4">
            <div>
                <label class="block text-sm font-medium text-gray-700">Bắt đầu (HH:MM:SS)</label>
                <input id="startTime" type="time" step="1" name="startTime" value="${startTime}" placeholder="08:00:00" required class="mt-1 block w-full border px-3 py-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700">Kết thúc (HH:MM:SS)</label>
                <input id="endTime" type="time" step="1" name="endTime" value="${endTime}" placeholder="17:00:00" required class="mt-1 block w-full border px-3 py-2 rounded" />
            </div>
        </div>
        <div class="pt-4">
            <button type="submit" class="px-4 py-2 bg-[#008751] text-white rounded">Lưu</button>
            <a href="${pageContext.request.contextPath}/manager/shifts" class="ml-3 text-gray-600">Hủy</a>
        </div>
    </form>
</main>
<jsp:include page="/View/Layout/Footer.jsp" />
<script>
function validateShiftTime() {
    const startInput = document.getElementById('startTime');
    const endInput = document.getElementById('endTime');
    if (!startInput || !endInput) return true;
    if (!startInput.value || !endInput.value) return true;
    if (endInput.value <= startInput.value) {
        alert('Giờ kết thúc phải sau giờ bắt đầu.');
        endInput.focus();
        return false;
    }
    return true;
}
</script>
</body>
</html>
