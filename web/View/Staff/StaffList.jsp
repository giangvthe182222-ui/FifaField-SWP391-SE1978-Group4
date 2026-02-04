<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800,900&display=swap" rel="stylesheet">
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="max-w-6xl mx-auto px-6 py-12">
    <div class="flex items-center justify-between mb-6">
        <h1 class="text-3xl font-black uppercase">Danh sách nhân viên</h1>
        <a href="${pageContext.request.contextPath}/staff/add" class="bg-[#008751] text-white px-4 py-2 rounded-lg font-bold">Thêm nhân viên</a>
    </div>

    <div class="bg-white rounded-xl shadow p-6 overflow-x-auto">
        <table class="w-full text-sm">
            <thead class="text-left text-gray-500 uppercase text-xs">
                <tr>
                    <th class="py-3">Mã</th>
                    <th>Họ tên</th>
                    <th>SĐT</th>
                    <th>Cơ sở</th>
                    <th>Trạng thái</th>
                    <th class="text-right">Hành động</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${staffs}" var="s">
                    <tr class="border-t">
                        <td class="py-3 font-bold">${s.employeeCode}</td>
                        <td>${s.fullName}</td>
                        <td>${s.phone}</td>
                        <td>${s.locationName}</td>
                        <td class="uppercase font-black text-sm">${s.status}</td>
                        <td class="text-right">
                            <a href="${pageContext.request.contextPath}/staff/detail?id=${s.userId}" class="text-blue-600 mr-3">Xem</a>
                            <a href="${pageContext.request.contextPath}/staff/edit?id=${s.userId}" class="text-green-600">Chỉnh sửa</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
