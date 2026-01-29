<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Thiết Bị - FIFA FIELD</title>

    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>

    <style>
        body { font-family: 'Inter', sans-serif; }
        .table-row-hover:hover { background-color: #f8fafc; }
        .status-select {
            appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b' stroke-width='2'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' d='M19 9l-7 7-7-7'/%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 0.5rem center;
            background-size: 1rem;
        }
    </style>
</head>

<body class="bg-slate-50 min-h-screen pb-20">

<!-- HEADER -->
<header class="bg-white border-b border-slate-200 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <div class="bg-emerald-600 p-2 rounded-xl text-white shadow">
                <i data-lucide="trophy" class="w-6 h-6"></i>
            </div>
            <h1 class="text-2xl font-semibold text-slate-900">
                FIFA<span class="text-emerald-600">FIELD</span>
            </h1>
        </div>
    </div>
</header>

<main class="max-w-7xl mx-auto px-6 py-10">

    <!-- TITLE + ADD BUTTON -->
    <div class="mb-6 flex items-center justify-between">
        <div>
            <h2 class="text-3xl font-semibold text-slate-900">Kho thiết bị</h2>
            <p class="text-slate-500 text-sm mt-1">
                Quản lý dụng cụ và vật tư thi đấu
            </p>
        </div>

        <a href="add-equipment"
           class="bg-emerald-600 text-white px-6 py-3 rounded-xl text-sm font-semibold
                  hover:bg-emerald-700 transition flex items-center gap-2 shadow">
            <i data-lucide="plus" class="w-4 h-4"></i>
            Thêm dụng cụ
        </a>
    </div>

    <!-- FILTER -->
    <form method="get" action="equipment-list"
          class="mb-8 flex flex-wrap gap-4 bg-white p-6 rounded-2xl
                 border border-slate-200 shadow-sm">

        <div>
            <label class="block text-xs font-medium text-slate-500 mb-1">Tìm kiếm</label>
            <input type="text" name="keyword" value="${param.keyword}"
                   placeholder="Tên thiết bị..."
                   class="px-4 py-2 border border-slate-300 rounded-lg text-sm
                          focus:outline-none focus:border-emerald-500">
        </div>

        <div>
            <label class="block text-xs font-medium text-slate-500 mb-1">Trạng thái</label>
            <select name="status"
                    class="px-4 py-2 border border-slate-300 rounded-lg text-sm
                           focus:outline-none focus:border-emerald-500">
                <option value="">Tất cả</option>
                <option value="available" ${param.status == 'available' ? 'selected' : ''}>available</option>
                <option value="unavailable" ${param.status == 'unavailable' ? 'selected' : ''}>unavailable</option>
            </select>
        </div>

        <div>
            <label class="block text-xs font-medium text-slate-500 mb-1">Loại</label>
            <select name="type"
                    class="px-4 py-2 border border-slate-300 rounded-lg text-sm
                           focus:outline-none focus:border-emerald-500">
                <option value="">Tất cả</option>
                <c:forEach items="${typeList}" var="t">
                    <option value="${t}" ${param.type == t ? 'selected' : ''}>${t}</option>
                </c:forEach>
            </select>
        </div>

        <div class="flex items-end">
            <button type="submit"
                    class="bg-emerald-600 text-white px-6 py-2 rounded-lg
                           text-sm font-semibold hover:bg-emerald-700">
                Lọc
            </button>
        </div>
    </form>

    <!-- TABLE -->
    <div class="bg-white rounded-3xl shadow border border-slate-200 overflow-hidden">
        <table class="w-full">
            <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
                <th class="px-8 py-4 text-xs font-semibold text-slate-500">Thiết bị</th>
                <th class="px-6 py-4 text-xs font-semibold text-slate-500">Loại</th>
                <th class="px-6 py-4 text-xs font-semibold text-slate-500 text-right">Giá thuê</th>
                <th class="px-6 py-4 text-xs font-semibold text-slate-500 text-right">Phí hỏng</th>
                <th class="px-6 py-4 text-xs font-semibold text-slate-500 text-center">Trạng thái</th>
                <th class="px-6 py-4 text-xs font-semibold text-slate-500 text-center">Thao tác</th>
            </tr>
            </thead>

            <tbody class="divide-y">
            <c:forEach var="e" items="${equipmentList}">
                <tr class="table-row-hover">
                    <td class="px-8 py-4">
                        <div class="flex items-center gap-4">
                            <img src="${e.imageUrl}"
                                 class="w-16 h-12 object-cover rounded-lg border">
                            <div>
                                <p class="font-medium text-slate-900">${e.name}</p>
                                <p class="text-xs text-slate-500">ID: #${e.equipmentId}</p>
                            </div>
                        </div>
                    </td>

                    <td class="px-6 py-4 text-sm text-slate-700">${e.equipmentType}</td>

                    <td class="px-6 py-4 text-right text-sm font-medium">
                        ${e.rentalPrice}đ
                    </td>

                    <td class="px-6 py-4 text-right text-sm font-medium">
                        ${e.damageFee}đ
                    </td>

                    <!-- STATUS -->
                    <td class="px-6 py-4 text-center">
                        <form action="update-equipment-status" method="post">
                            <input type="hidden" name="id" value="${e.equipmentId}">
                            <select name="newStatus"
                                    onchange="if(confirm('Thay đổi trạng thái?')) this.form.submit(); else this.value='${e.status}';"
                                    class="status-select px-4 py-2 rounded-lg text-xs font-semibold border
                                    ${e.status == 'available'
                                        ? 'bg-emerald-100 text-emerald-700 border-emerald-300'
                                        : 'bg-slate-100 text-slate-600 border-slate-300'}">

                                <option value="available" ${e.status == 'available' ? 'selected' : ''}>
                                    available
                                </option>
                                <option value="unavailable" ${e.status == 'unavailable' ? 'selected' : ''}>
                                    unavailable
                                </option>
                            </select>
                        </form>
                    </td>

                    <!-- ACTION -->
                    <td class="px-6 py-4 text-center">
                        <div class="flex justify-center gap-2">
                            <a href="equipment-detail?id=${e.equipmentId}"
                               class="p-2 rounded-lg hover:bg-emerald-50 text-slate-500 hover:text-emerald-600">
                                <i data-lucide="eye" class="w-4 h-4"></i>
                            </a>
                            <a href="edit-equipment?id=${e.equipmentId}"
                               class="p-2 rounded-lg hover:bg-blue-50 text-slate-500 hover:text-blue-600">
                                <i data-lucide="edit-3" class="w-4 h-4"></i>
                            </a>
                        </div>
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${empty equipmentList}">
                <tr>
                    <td colspan="6" class="py-16 text-center text-slate-500">
                        Không có thiết bị nào
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</main>

<script>lucide.createIcons();</script>
</body>
</html>
