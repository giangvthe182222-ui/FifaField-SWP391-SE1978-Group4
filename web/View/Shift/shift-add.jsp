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
<c:choose>
    <c:when test="${sessionScope.user != null and sessionScope.user.role != null and sessionScope.user.role.roleName == 'MANAGER'}"><jsp:include page="/View/Layout/HeaderManager.jsp" /></c:when>
    <c:otherwise><jsp:include page="/View/Layout/HeaderAdmin.jsp" /></c:otherwise>
</c:choose>
<main class="max-w-3xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Thêm ca mới</h1>
    <c:if test="${not empty error}">
        <div class="text-red-600 mb-3">${error}</div>
    </c:if>
    <form method="post" action="${pageContext.request.contextPath}/shifts/add" class="space-y-4 bg-white p-6 rounded shadow">
        <div>
            <label class="block text-sm font-medium text-gray-700">Tên ca</label>
            <input name="shiftName" required class="mt-1 block w-full border px-3 py-2 rounded" />
        </div>
        <div class="grid grid-cols-2 gap-4">
            <div>
                <label class="block text-sm font-medium text-gray-700">Bắt đầu (HH:MM:SS)</label>
                <input name="startTime" placeholder="08:00:00" required class="mt-1 block w-full border px-3 py-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700">Kết thúc (HH:MM:SS)</label>
                <input name="endTime" placeholder="17:00:00" required class="mt-1 block w-full border px-3 py-2 rounded" />
            </div>
        </div>
        <div class="pt-4">
            <button type="submit" class="px-4 py-2 bg-[#008751] text-white rounded">Lưu</button>
            <a href="${pageContext.request.contextPath}/shifts" class="ml-3 text-gray-600">Hủy</a>
        </div>
    </form>
</main>
<c:choose>
    <c:when test="${sessionScope.user != null and sessionScope.user.role != null and sessionScope.user.role.roleName == 'MANAGER'}"><jsp:include page="/View/Layout/FooterManager.jsp" /></c:when>
    <c:otherwise><jsp:include page="/View/Layout/Footer.jsp" /></c:otherwise>
</c:choose>
</body>
</html>
