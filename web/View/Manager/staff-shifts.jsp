<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Ca nhân viên - Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50">
<jsp:include page="/View/Layout/HeaderManager.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Ca nhân viên được phân</h1>
    <c:if test="${not empty error}">
        <div class="text-red-600 mb-3">${error}</div>
    </c:if>

    <div class="bg-white shadow rounded-lg overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nhân viên</th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sân</th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ca</th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ngày</th>
                    <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
                <c:forEach items="${staffShifts}" var="ss">
                    <tr class="hover:bg-gray-50">
                        <td class="px-4 py-3 text-sm text-gray-900">${ss.staffId}</td>
                        <td class="px-4 py-3 text-sm text-gray-700">${ss.fieldId}</td>
                        <td class="px-4 py-3 text-sm text-gray-700">${ss.shiftId}</td>
                        <td class="px-4 py-3 text-sm text-gray-700">${ss.workingDate}</td>
                        <td class="px-4 py-3 text-sm text-gray-900"><span class="px-2 py-1 bg-blue-100 text-blue-800 rounded">${ss.status}</span></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty staffShifts}">
                    <tr>
                        <td colspan="5" class="px-4 py-3 text-center text-gray-500">Không có ca nào được phân</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <div class="mt-4">
        <a href="${pageContext.request.contextPath}/manager/assign-shift" class="text-indigo-600 hover:text-indigo-900">Phân ca mới</a>
    </div>
</main>
<jsp:include page="/View/Layout/Footer.jsp" />
</body>
</html>
