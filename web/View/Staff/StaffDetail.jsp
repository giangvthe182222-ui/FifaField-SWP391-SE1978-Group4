<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="max-w-4xl mx-auto px-6 py-12">
    <div class="mb-6">
        <a href="javascript:history.back()" class="text-gray-500">← Quay lại</a>
    </div>
    <div class="bg-white rounded-xl shadow p-8">
        <h2 class="text-2xl font-bold mb-4">${staff.fullName}</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div><strong>Mã nhân viên:</strong> ${staff.employeeCode}</div>
            <div><strong>Cơ sở:</strong> ${staff.locationName}</div>
            <div><strong>Số điện thoại:</strong> ${staff.phone}</div>
            <div><strong>Trạng thái:</strong> ${staff.status}</div>
            <div><strong>Ngày gia nhập:</strong> <c:out value="${staff.hireDate}"/></div>
        </div>

        <div class="mt-6 flex gap-3">
            <a href="${pageContext.request.contextPath}/staff/edit?id=${staff.userId}" class="px-4 py-2 bg-green-600 text-white rounded">Chỉnh sửa</a>
            <a href="${pageContext.request.contextPath}/staff/list" class="px-4 py-2 bg-gray-200 rounded">Quay về danh sách</a>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
