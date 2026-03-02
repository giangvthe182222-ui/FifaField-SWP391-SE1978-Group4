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
<jsp:include page="/View/Layout/HeaderManager.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Phân ca cho nhân viên</h1>
    <c:if test="${not empty error}">
        <div class="text-red-600 mb-3">${error}</div>
    </c:if>

    <form id="assignShiftForm" method="post" action="${pageContext.request.contextPath}/manager/assign-shift" onsubmit="return validateAssignShiftForm()" class="bg-white p-6 rounded shadow space-y-4">
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
            <div id="formError" class="text-red-600 mb-2" style="display:none"></div>
            <button type="submit" class="px-4 py-2 bg-[#008751] text-white rounded">Phân ca</button>
            <a href="${pageContext.request.contextPath}/manager/assign-shift" class="ml-3 text-gray-600">Làm lại</a>
        </div>
    </form>

</main>
<jsp:include page="/View/Layout/Footer.jsp" />
</body>
<script>
    function validateAssignShiftForm() {
        var staffId = document.querySelector('[name="staffId"]').value;
        var shiftId = document.querySelector('[name="shiftId"]').value;
        var workingDateEl = document.querySelector('[name="workingDate"]');
        var workingDate = workingDateEl.value;
        var errorEl = document.getElementById('formError');
        errorEl.style.display = 'none';
        errorEl.textContent = '';

        if (!staffId) {
            errorEl.textContent = 'Vui lòng chọn nhân viên.';
            errorEl.style.display = 'block';
            return false;
        }
        if (!shiftId) {
            errorEl.textContent = 'Vui lòng chọn ca.';
            errorEl.style.display = 'block';
            return false;
        }
        if (!workingDate) {
            errorEl.textContent = 'Vui lòng chọn ngày làm việc.';
            errorEl.style.display = 'block';
            return false;
        }
        var selected = new Date(workingDate);
        var today = new Date();
        today.setHours(0,0,0,0);
        if (selected < today) {
            errorEl.textContent = 'Ngày làm việc không được ở quá khứ.';
            errorEl.style.display = 'block';
            workingDateEl.focus();
            return false;
        }
        return true;
    }
</script>
</html>
