<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ca nhân viên được phân - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<<<<<<< Updated upstream
<body class="bg-gray-50">
<jsp:include page="/View/Layout/Header.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Ca nhân viên được phân</h1>
=======
<body class="bg-slate-50 min-h-screen" style="font-family: 'Inter', sans-serif;">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<!-- MAIN CONTENT -->
<main class="max-w-7xl mx-auto px-6 py-12">

    <!-- HEADER SECTION -->
    <div class="mb-8">
        <button type="button" onclick="history.back()" class="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-all mb-4">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            Quay lại
        </button>
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
                <h1 class="text-3xl font-bold text-slate-900">Ca nhân viên được phân công</h1>
                <p class="text-slate-500 mt-2">Danh sách các ca làm việc được phân cho nhân viên của bạn</p>
            </div>
            <a href="${pageContext.request.contextPath}/manager/assign-shift" class="inline-flex items-center gap-2 px-6 py-3 bg-[#008751] text-white rounded-lg font-semibold hover:bg-[#006d41] transition-all shadow-sm">
                <i data-lucide="plus" class="w-4 h-4"></i>
                Phân ca mới
            </a>
        </div>
    </div>

    <!-- ERROR MESSAGE -->
>>>>>>> Stashed changes
    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
            <div>
                <h3 class="font-semibold text-red-800">Lỗi</h3>
                <p class="text-red-700 text-sm">${error}</p>
            </div>
        </div>
    </c:if>

    <!-- TABLE SECTION -->
    <div class="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
        <c:choose>
            <c:when test="${empty staffShifts}">
                <!-- EMPTY STATE -->
                <div class="px-6 py-16 text-center">
                    <div class="flex justify-center mb-4">
                        <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center">
                            <i data-lucide="inbox" class="w-8 h-8 text-slate-400"></i>
                        </div>
                    </div>
                    <h3 class="text-lg font-semibold text-slate-900 mb-2">Không có ca nào được phân</h3>
                    <p class="text-slate-500 mb-6">Bạn chưa phân công ca nào cho nhân viên.</p>
                    <a href="${pageContext.request.contextPath}/manager/assign-shift" class="inline-flex items-center gap-2 px-6 py-3 bg-[#008751] text-white rounded-lg font-semibold hover:bg-[#006d41] transition-all">
                        <i data-lucide="plus" class="w-4 h-4"></i>
                        Phân ca đầu tiên
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <!-- TABLE -->
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-slate-200">
                        <thead class="bg-slate-50 border-b border-slate-200">
                            <tr>
                                <th class="px-6 py-4 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Nhân viên</th>
                                <th class="px-6 py-4 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Sân bóng</th>
                                <th class="px-6 py-4 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Ca làm việc</th>
                                <th class="px-6 py-4 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Ngày làm việc</th>
                                <th class="px-6 py-4 text-center text-xs font-semibold text-slate-700 uppercase tracking-wider">Trạng thái</th>
                                <th class="px-6 py-4 text-right text-xs font-semibold text-slate-700 uppercase tracking-wider">Hành động</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-200">
                            <c:forEach items="${staffShifts}" var="ss">
                                <tr class="hover:bg-slate-50 transition-colors">
                                    <td class="px-6 py-4">
                                        <div class="flex items-center gap-3">
                                            <div class="w-10 h-10 bg-gradient-to-br from-[#008751] to-[#006d41] rounded-full flex items-center justify-center text-white font-semibold text-sm">
                                                <i data-lucide="user" class="w-5 h-5"></i>
                                            </div>
                                            <div>
                                                <p class="text-sm font-semibold text-slate-900">${ss.staffName}</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="px-6 py-4">
                                        <div class="text-sm font-medium text-slate-900">${ss.fieldName}</div>
                                        <div class="text-xs text-slate-500">Sân bóng</div>
                                    </td>
                                    <td class="px-6 py-4">
                                        <div class="text-sm font-medium text-slate-900">${ss.shiftName}</div>
                                        <div class="text-xs text-slate-500">Ca làm việc</div>
                                    </td>
                                    <td class="px-6 py-4">
                                        <div class="text-sm font-semibold text-slate-900">${ss.workingDate}</div>
                                        <div class="text-xs text-slate-500">
                                            <i data-lucide="calendar" class="w-3 h-3 inline mr-1"></i>
                                        </div>
                                    </td>
                                    <td class="px-6 py-4 text-center">
                                        <c:choose>
                                            <c:when test="${ss.status eq 'assigned'}">
                                                <span class="inline-flex items-center gap-2 px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-xs font-semibold">
                                                    <span class="w-1.5 h-1.5 bg-blue-500 rounded-full"></span>
                                                    Đã phân
                                                </span>
                                            </c:when>
                                            <c:when test="${ss.status eq 'completed'}">
                                                <span class="inline-flex items-center gap-2 px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-xs font-semibold">
                                                    <span class="w-1.5 h-1.5 bg-emerald-500 rounded-full"></span>
                                                    Hoàn thành
                                                </span>
                                            </c:when>
                                            <c:when test="${ss.status eq 'cancelled'}">
                                                <span class="inline-flex items-center gap-2 px-3 py-1 bg-red-50 text-red-700 rounded-full text-xs font-semibold">
                                                    <span class="w-1.5 h-1.5 bg-red-500 rounded-full"></span>
                                                    Hủy
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="inline-flex items-center gap-2 px-3 py-1 bg-slate-50 text-slate-700 rounded-full text-xs font-semibold">
                                                    <span class="w-1.5 h-1.5 bg-slate-500 rounded-full"></span>
                                                    ${ss.status}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="px-6 py-4 text-right space-x-2">
                                                     <a href="${pageContext.request.contextPath}/manager/staff-shift/edit?staffId=${ss.staffId}&fieldId=${ss.fieldId}&shiftId=${ss.shiftId}&workingDate=${ss.workingDate}"
                                                         class="text-indigo-600 hover:text-indigo-900 text-sm font-semibold">Sửa</a>
                                        <form method="post" action="${pageContext.request.contextPath}/manager/staff-shift/delete" class="inline">
                                            <input type="hidden" name="staffId" value="${ss.staffId}" />
                                            <input type="hidden" name="fieldId" value="${ss.fieldId}" />
                                            <input type="hidden" name="shiftId" value="${ss.shiftId}" />
                                            <input type="hidden" name="workingDate" value="${ss.workingDate}" />
                                            <button type="submit" class="text-red-600 hover:text-red-900 text-sm font-semibold" onclick="return confirm('Xác nhận xóa ca?');">Xóa</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <!-- FOOTER INFO -->
                <div class="px-6 py-4 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-sm">
                    <p class="text-slate-600">
                        Tổng cộng: <span class="font-semibold text-slate-900">${fn:length(staffShifts)}</span> ca được phân
                    </p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />

<script>
    lucide.createIcons();
</script>

</body>
</html>
