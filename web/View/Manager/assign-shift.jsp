<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Phân ca - Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-gray-50">
<jsp:include page="/View/Layout/Header.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Phân ca cho nhân viên</h1>
    <c:if test="${not empty error}">
        <div class="text-red-600 mb-3">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manager/assign-shift" class="bg-white p-6 rounded shadow space-y-4">
        <div>
            <label class="block text-sm font-medium">Nhân viên</label>
            <select name="staffId" class="mt-1 block w-full border px-3 py-2 rounded">
                <c:forEach items="${staffList}" var="s">
                    <option value="${s.userId}">${s.fullName} — ${s.locationName}</option>
                </c:forEach>
            </select>
        </div>

        <div>
            <label class="block text-sm font-medium">Ca</label>
            <select name="shiftId" class="mt-1 block w-full border px-3 py-2 rounded">
                <c:forEach items="${shifts}" var="sh">
                    <option value="${sh.shiftId}">${sh.shiftName} (${sh.startTime} - ${sh.endTime})</option>
                </c:forEach>
            </select>
        </div>

        <div>
            <label class="block text-sm font-medium">Ngày làm việc</label>
            <input type="date" name="workingDate" required class="mt-1 block w-full border px-3 py-2 rounded" />
        </div>

        <div>
            <label class="block text-sm font-medium">Mã sân (fieldId)</label>
            <input name="fieldId" placeholder="field id" class="mt-1 block w-full border px-3 py-2 rounded" />
        </div>

        <div>
            <button type="submit" class="px-4 py-2 bg-[#008751] text-white rounded">Phân ca</button>
            <a href="${pageContext.request.contextPath}/manager/assign-shift" class="ml-3 text-gray-600">Làm lại</a>
        </div>
    </form>

</main>
<jsp:include page="/View/Layout/Footer.jsp" />
</body>
</html>
