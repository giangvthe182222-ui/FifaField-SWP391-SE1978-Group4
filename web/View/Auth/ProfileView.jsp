<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800,900&display=swap" rel="stylesheet">
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="max-w-3xl mx-auto px-6 py-12">
    <h1 class="text-2xl font-black mb-4">Hồ sơ cá nhân</h1>

    <div class="bg-white rounded-xl shadow p-6">
        <div class="grid grid-cols-1 gap-3 text-sm">
            <div><strong>Họ tên:</strong> ${sessionScope.user.fullName}</div>
            <div><strong>Số điện thoại:</strong> ${sessionScope.user.phone}</div>
            <div><strong>Email liên kết (gmail_id):</strong> ${sessionScope.user.gmailId}</div>
            <div><strong>Địa chỉ:</strong> ${sessionScope.user.address}</div>
            <div><strong>Giới tính:</strong> ${sessionScope.user.gender}</div>
        </div>

        <div class="mt-6">
            <a href="${pageContext.request.contextPath}/auth/profile/edit" class="px-4 py-2 bg-green-600 text-white rounded">Chỉnh sửa</a>
            <a href="${pageContext.request.contextPath}/" class="px-4 py-2 bg-gray-200 rounded">Quay về</a>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
