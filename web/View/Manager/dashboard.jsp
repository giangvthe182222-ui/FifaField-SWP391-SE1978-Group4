<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Manager Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50">
<jsp:include page="/View/Layout/Header.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Bảng điều khiển - Manager</h1>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div class="bg-white p-4 rounded shadow">
            <h3 class="text-sm text-gray-500">Tổng số nhân viên</h3>
            <p class="text-2xl font-bold">${totalStaff != null ? totalStaff : '—'}</p>
        </div>
        <div class="bg-white p-4 rounded shadow">
            <h3 class="text-sm text-gray-500">Ca đã phân</h3>
            <p class="text-2xl font-bold">${assignedCount != null ? assignedCount : '—'}</p>
        </div>
        <div class="bg-white p-4 rounded shadow">
            <h3 class="text-sm text-gray-500">Ca sắp tới</h3>
            <p class="text-2xl font-bold">${upcoming != null ? upcoming : '—'}</p>
        </div>
    </div>

    <div class="bg-white p-6 rounded shadow">
        <h2 class="text-lg font-semibold mb-3">Hành động nhanh</h2>
        <div class="flex gap-3">
            <a href="${pageContext.request.contextPath}/manager/assign-shift" class="px-4 py-2 bg-[#008751] text-white rounded">Phân ca</a>
            <a href="${pageContext.request.contextPath}/manager/staff-shifts" class="px-4 py-2 bg-indigo-600 text-white rounded">Xem ca đã phân</a>
            <a href="${pageContext.request.contextPath}/shifts" class="px-4 py-2 bg-gray-200 text-gray-800 rounded">Quản lý ca</a>
        </div>
    </div>

</main>
<jsp:include page="/View/Layout/Footer.jsp" />
</body>
</html>
