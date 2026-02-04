<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết Voucher - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="max-w-4xl mx-auto px-6 py-12">
    <h1 class="text-2xl font-black mb-4">Chi tiết Voucher</h1>

    <div class="bg-white rounded-xl shadow p-6">
        <div class="grid grid-cols-1 gap-3">
            <div><strong>Mã:</strong> ${voucher.code}</div>
            <div><strong>Giảm:</strong> ${voucher.discountValue}</div>
            <div><strong>Mô tả:</strong> ${voucher.description}</div>
            <div><strong>Ngày bắt đầu:</strong> ${voucher.startDate}</div>
            <div><strong>Ngày kết thúc:</strong> ${voucher.endDate}</div>
            <div><strong>Trạng thái:</strong> ${voucher.status}</div>
        </div>
        <div class="mt-6">
            <a href="${pageContext.request.contextPath}/voucher/edit?id=${voucher.voucherId}" class="px-4 py-2 bg-green-600 text-white rounded">Chỉnh sửa</a>
            <a href="${pageContext.request.contextPath}/voucher/list" class="px-4 py-2 bg-gray-200 rounded">Quay về</a>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
