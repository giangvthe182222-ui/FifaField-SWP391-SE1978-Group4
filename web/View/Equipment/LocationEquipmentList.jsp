<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thiết bị tại sân</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>

<body class="bg-slate-50 p-8">

<h1 class="text-2xl font-semibold mb-6">
    Danh sách thiết bị tại sân
</h1>

<div class="bg-white rounded-xl shadow border overflow-hidden">
    <table class="w-full">
        <thead class="bg-slate-100">
        <tr>
            <th class="px-6 py-3 text-left">Thiết bị</th>
            <th class="px-4 py-3">Loại</th>
            <th class="px-4 py-3 text-center">Số lượng</th>
            <th class="px-4 py-3 text-center">Trạng thái</th>
        </tr>
        </thead>

        <tbody class="divide-y">
            
        <c:forEach items="${locationEquipmentList}" var="e">
            <tr>
                <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                        <img src="${e.imageUrl}"
                             class="w-14 h-10 rounded border object-cover">
                        <div>
                            <p class="font-medium">${e.name}</p>
                            <p class="text-xs text-slate-500">
                                ${e.equipmentId}
                            </p>
                        </div>
                    </div>
                </td>

                <td class="px-4 py-4">${e.equipmentType}</td>

                <td class="px-4 py-4 text-center font-semibold">
                    ${e.quantity}
                </td>

                <td class="px-4 py-4 text-center">
                    <span class="px-3 py-1 rounded-full text-xs font-semibold
                        ${e.status == 'available'
                            ? 'bg-emerald-100 text-emerald-700'
                            : 'bg-slate-200 text-slate-600'}">
                        ${e.status}
                    </span>
                </td>
            </tr>
        </c:forEach>

        <c:if test="${empty locationEquipmentList}">
            <tr>
                <td colspan="4" class="py-10 text-center text-slate-500">
                    Không có thiết bị
                </td>
            </tr>
        </c:if>
        </tbody>
    </table>
    ${locationEquipmentList.size()}
</div>

</body>
</html>
