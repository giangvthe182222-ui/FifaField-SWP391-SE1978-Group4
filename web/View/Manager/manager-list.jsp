<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách quản lý - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 min-h-screen pb-20" style="font-family: 'Inter', sans-serif;">

<jsp:include page="/View/Layout/Header.jsp" />

<!-- MAIN CONTENT -->
<main class="max-w-7xl mx-auto px-6 py-10">

    <!-- TITLE + ADD BUTTON -->
    <div class="mb-8 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <button type="button" onclick="history.back()" class="px-3 py-2 rounded-lg border bg-white text-sm font-semibold hover:bg-slate-50">
                ← Trở về
            </button>
        </div>
        <div>
            <h2 class="text-3xl font-bold text-slate-900">Danh sách quản lý</h2>
            <p class="text-slate-500 text-sm mt-1">Quản lý tài khoản quản lý vị trí</p>
        </div>
        <a href="${pageContext.request.contextPath}/add-manager"
           class="bg-emerald-600 text-white px-6 py-3 rounded-lg text-sm font-semibold hover:bg-emerald-700 transition flex items-center gap-2 shadow">
            <i data-lucide="plus" class="w-4 h-4"></i>
            Thêm quản lý
        </a>
    </div>

    <!-- ERROR MESSAGE -->
    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
            <div>
                <h3 class="font-semibold text-red-800">Lỗi</h3>
                <p class="text-red-700 text-sm">${error}</p>
            </div>
        </div>
    </c:if>

    <!-- TABLE -->
    <div class="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
        <div class="overflow-x-auto">
            <table class="w-full">
                <thead class="bg-slate-50 border-b border-slate-200">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Họ tên</th>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Email</th>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Điện thoại</th>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Vị trí</th>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Ngày bắt đầu</th>
                        <th class="px-6 py-3 text-left text-xs font-semibold text-slate-700 uppercase tracking-wider">Trạng thái</th>
                        <th class="px-6 py-3 text-right text-xs font-semibold text-slate-700 uppercase tracking-wider">Hành động</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-200">
                    <c:choose>
                        <c:when test="${empty managers}">
                            <tr>
                                <td colspan="7" class="px-6 py-8 text-center text-slate-500">
                                    <div class="flex flex-col items-center justify-center">
                                        <i data-lucide="users" class="w-12 h-12 text-slate-300 mb-3"></i>
                                        <p>Chưa có quản lý nào</p>
                                    </div>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${managers}" var="manager">
                                <tr class="hover:bg-slate-50 transition">
                                    <td class="px-6 py-4 text-sm font-semibold text-slate-900">${manager.fullName}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${manager.email}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${manager.phone}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${manager.locationName}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">
                                        <c:out value="${manager.startDate}" />
                                    </td>
                                    <td class="px-6 py-4 text-sm">
                                        <c:choose>
                                            <c:when test="${manager.status == 'active'}">
                                                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-800">
                                                    <span class="w-2 h-2 bg-green-600 rounded-full mr-1.5"></span>
                                                    Hoạt động
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-800">
                                                    <span class="w-2 h-2 bg-gray-600 rounded-full mr-1.5"></span>
                                                    Không hoạt động
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="px-6 py-4 text-right text-sm">
                                        <div class="flex justify-end gap-2">
                                            <a href="${pageContext.request.contextPath}/manager-detail?manager_id=${manager.userId}" title="Xem chi tiết"
                                               class="text-slate-600 hover:text-emerald-600 transition">
                                                <i data-lucide="eye" class="w-4 h-4"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/manager-edit?manager_id=${manager.userId}" title="Sửa"
                                               class="text-slate-600 hover:text-blue-600 transition">
                                                <i data-lucide="edit" class="w-4 h-4"></i>
                                            </a>
                                            <a href="#" title="Xóa"
                                               class="text-slate-600 hover:text-red-600 transition"
                                               onclick="if(confirm('Bạn chắc chắn muốn xóa?')) { window.location='${pageContext.request.contextPath}/manager-delete?manager_id=${manager.userId}'; } return false;">
                                                <i data-lucide="trash-2" class="w-4 h-4"></i>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <!-- SUMMARY -->
    <div class="mt-6 text-sm text-slate-600">
        Tổng cộng: <strong>${managers.size()}</strong> quản lý
    </div>

</main>

<script>
    lucide.createIcons();
</script>

<jsp:include page="/View/Layout/Footer.jsp" />

</body>
</html>

