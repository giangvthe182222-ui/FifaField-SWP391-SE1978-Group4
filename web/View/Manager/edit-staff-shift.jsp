<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa ca phân công - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-4xl mx-auto px-6 py-12">
    <div class="mb-8">
        <button type="button" onclick="history.back()" class="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-all mb-4">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            Quay lại
        </button>
        <h1 class="text-3xl font-bold text-slate-900">Chỉnh sửa ca phân công</h1>
        <p class="text-slate-500 mt-2">Sửa thông tin ca đã phân cho nhân viên</p>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-slate-200 p-8">
        <form method="post" action="${pageContext.request.contextPath}/manager/staff-shift/edit" class="space-y-6">
            <input type="hidden" name="origStaffId" value="${origStaffId}" />
            <input type="hidden" name="origFieldId" value="${origFieldId}" />
            <input type="hidden" name="origShiftId" value="${origShiftId}" />
            <input type="hidden" name="origWorkingDate" value="${origWorkingDate}" />

            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">Nhân viên</label>
                <select name="staffId" class="w-full px-4 py-3 border rounded-lg">
                    <option value="">-- Chọn nhân viên --</option>
                    <c:forEach items="${staffList}" var="s">
                        <option value="${s.userId}" ${staffId == s.userId.toString() ? 'selected' : ''}>${s.fullName}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Sân bóng</label>
                    <select name="fieldId" class="w-full px-4 py-3 border rounded-lg">
                        <option value="">-- Chọn sân --</option>
                        <c:forEach items="${fields}" var="f">
                            <option value="${f.fieldId}" ${fieldId == f.fieldId.toString() ? 'selected' : ''}>${f.fieldName}</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Ca làm việc</label>
                    <select name="shiftId" class="w-full px-4 py-3 border rounded-lg">
                        <option value="">-- Chọn ca --</option>
                        <c:forEach items="${shifts}" var="sh">
                            <option value="${sh.shiftId}" ${shiftId == sh.shiftId.toString() ? 'selected' : ''}>${sh.shiftName} (${sh.startTime} - ${sh.endTime})</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày làm việc</label>
                <input type="date" name="workingDate" value="${workingDate}" class="w-full px-4 py-3 border rounded-lg" />
            </div>

            <div class="flex gap-3 pt-6 border-t border-slate-200">
                <button type="submit" class="px-6 py-3 bg-[#008751] text-white rounded-lg font-semibold">Lưu</button>
                <a href="${pageContext.request.contextPath}/manager/staff-shifts" class="px-6 py-3 border rounded-lg">Hủy</a>
            </div>
        </form>
    </div>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />

<script>lucide.createIcons();</script>
</body>
</html>