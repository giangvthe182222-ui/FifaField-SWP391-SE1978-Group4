<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dụng cụ tại cụm - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }</style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-7xl mx-auto px-6 py-12 w-full flex-grow space-y-8">

    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div class="space-y-2">
            <button type="button" onclick="history.back()"
                    class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest">
                <i data-lucide="arrow-left" class="w-3 h-3"></i> Quay lại
            </button>
            <h1 class="text-4xl font-black text-gray-900 uppercase tracking-tight leading-none">
                DỤNG CỤ <span class="text-[#008751]">TẠI CỤM</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">
                Cụm sân: ${locationName}
            </p>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-500 flex-shrink-0"></i>
            <p class="text-red-700 text-sm font-semibold">${error}</p>
        </div>
    </c:if>

    <!-- EQUIPMENT TABLE -->
    <c:choose>
        <c:when test="${empty equipments}">
            <div class="bg-white rounded-2xl p-16 text-center shadow border border-gray-100">
                <i data-lucide="package" class="w-16 h-16 text-gray-300 mx-auto mb-4"></i>
                <p class="text-gray-500 font-semibold text-lg">Chưa có dụng cụ nào tại cụm sân này</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="overflow-x-auto bg-white shadow rounded-2xl border border-gray-100">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gradient-to-r from-slate-50 to-green-50">
                        <tr>
                            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-wider">Dụng cụ</th>
                            <th class="px-6 py-4 text-left text-xs font-black text-gray-500 uppercase tracking-wider">Loại</th>
                            <th class="px-6 py-4 text-center text-xs font-black text-gray-500 uppercase tracking-wider">Số lượng</th>
                            <th class="px-6 py-4 text-right text-xs font-black text-gray-500 uppercase tracking-wider">Giá thuê</th>
                            <th class="px-6 py-4 text-right text-xs font-black text-gray-500 uppercase tracking-wider">Phí hỏng</th>
                            <th class="px-6 py-4 text-center text-xs font-black text-gray-500 uppercase tracking-wider">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100">
                        <c:forEach items="${equipments}" var="equip">
                            <tr class="hover:bg-gray-50 transition-colors">
                                <td class="px-6 py-4">
                                    <div class="flex items-center gap-3">
                                        <div class="w-10 h-10 rounded-xl bg-gray-100 overflow-hidden flex-shrink-0">
                                            <c:choose>
                                                <c:when test="${not empty equip.imageUrl}">
                                                    <img src="${pageContext.request.contextPath}/${equip.imageUrl}"
                                                         alt="${equip.name}" class="w-full h-full object-cover" />
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="w-full h-full flex items-center justify-center">
                                                        <i data-lucide="package" class="w-5 h-5 text-gray-300"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <span class="font-bold text-gray-900 text-sm">${equip.name}</span>
                                    </div>
                                </td>
                                <td class="px-6 py-4 text-sm text-gray-600 font-medium">${equip.equipmentType}</td>
                                <td class="px-6 py-4 text-center">
                                    <span class="inline-flex items-center justify-center w-8 h-8 rounded-full bg-blue-50 text-blue-700 font-black text-sm">${equip.quantity}</span>
                                </td>
                                <td class="px-6 py-4 text-right text-sm font-bold text-gray-800">
                                    <fmt:formatNumber value="${equip.rentalPrice}" type="number" groupingUsed="true" />đ
                                </td>
                                <td class="px-6 py-4 text-right text-sm font-bold text-red-600">
                                    <fmt:formatNumber value="${equip.damageFee}" type="number" groupingUsed="true" />đ
                                </td>
                                <td class="px-6 py-4 text-center">
                                    <c:set var="statusCss" value="bg-gray-100 text-gray-500"/>
                                    <c:if test="${equip.status == 'AVAILABLE'}"><c:set var="statusCss" value="bg-emerald-100 text-emerald-700"/></c:if>
                                    <c:if test="${equip.status == 'RENTED'}"><c:set var="statusCss" value="bg-blue-100 text-blue-700"/></c:if>
                                    <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold ${statusCss}">
                                        ${equip.status}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="flex justify-end">
                <div class="bg-emerald-50 px-8 py-4 rounded-3xl border border-emerald-100">
                    <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">
                        Tổng loại dụng cụ: <span class="text-xl ml-2">${equipments.size()}</span>
                    </span>
                </div>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />
<script>lucide.createIcons();</script>
</body>
</html>
