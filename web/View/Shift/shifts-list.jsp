<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Danh sách ca - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-gray-50">
<jsp:include page="/View/Layout/Header.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold">Quản lý ca</h1>
        <a href="${pageContext.request.contextPath}/shifts/add" class="px-4 py-2 bg-[#008751] text-white rounded">Thêm ca</a>
    </div>

    <div class="bg-white shadow rounded-lg overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tên ca</th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Bắt đầu</th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Kết thúc</th>
                    <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Hành động</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
                <c:forEach items="${shifts}" var="s">
                    <tr class="hover:bg-gray-50">
                        <td class="px-4 py-3 text-sm text-gray-900">${s.shiftName}</td>
                        <td class="px-4 py-3 text-sm text-gray-700">${s.startTime}</td>
                        <td class="px-4 py-3 text-sm text-gray-700">${s.endTime}</td>
                        <td class="px-4 py-3 text-sm text-right">
                            <a href="${pageContext.request.contextPath}/shifts/edit?id=${s.shiftId}" class="text-indigo-600 hover:text-indigo-900 mr-4">Edit</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</main>
<jsp:include page="/View/Layout/Footer.jsp" />
</body>
</html>
