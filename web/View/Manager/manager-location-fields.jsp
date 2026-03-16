<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sân bóng tại cụm - FIFAFIELD</title>
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
                SÂN BÓNG <span class="text-[#008751]">CỦA TÔI</span>
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

    <!-- FIELDS GRID -->
    <c:choose>
        <c:when test="${empty fields}">
            <div class="bg-white rounded-2xl p-16 text-center shadow border border-gray-100">
                <i data-lucide="layout-grid" class="w-16 h-16 text-gray-300 mx-auto mb-4"></i>
                <p class="text-gray-500 font-semibold text-lg">Chưa có sân nào trong cụm</p>
                <p class="text-gray-400 text-sm mt-1">Liên hệ Admin để thêm sân mới</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <c:forEach items="${fields}" var="field">
                    <div class="bg-white rounded-2xl shadow border border-gray-100 overflow-hidden hover:-translate-y-1 transition-all group">
                        <div class="relative h-44 bg-gray-100 overflow-hidden">
                            <c:choose>
                                <c:when test="${not empty field.imageUrl}">
                                    <img src="${pageContext.request.contextPath}/${field.imageUrl}"
                                         alt="${field.fieldName}"
                                         class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                                </c:when>
                                <c:otherwise>
                                    <div class="w-full h-full flex items-center justify-center">
                                        <i data-lucide="layout-grid" class="w-12 h-12 text-gray-300"></i>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                            <div class="absolute top-3 right-3">
                                <c:set var="fieldStatusCss" value="bg-gray-500 text-white"/>
                                <c:if test="${field.status eq 'ACTIVE'}"><c:set var="fieldStatusCss" value="bg-emerald-500 text-white"/></c:if>
                                <c:if test="${field.status eq 'MAINTENANCE'}"><c:set var="fieldStatusCss" value="bg-amber-500 text-white"/></c:if>
                                <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold ${fieldStatusCss}">
                                    ${field.status}
                                </span>
                            </div>
                        </div>
                        <div class="p-5">
                            <h3 class="font-black text-gray-900 text-lg mb-1">${field.fieldName}</h3>
                            <div class="flex items-center gap-4 text-xs text-gray-500 mb-4">
                                <span class="flex items-center gap-1">
                                    <i data-lucide="users" class="w-3.5 h-3.5"></i>
                                    ${field.fieldType}
                                </span>
                                <span class="flex items-center gap-1">
                                    <i data-lucide="shield-check" class="w-3.5 h-3.5"></i>
                                    ${field.fieldCondition}
                                </span>
                            </div>
                            <a href="${pageContext.request.contextPath}/manager/fields/detail?fieldId=${field.fieldId}"
                               class="w-full flex items-center justify-center gap-2 py-2 rounded-xl bg-emerald-50 text-[#008751] text-sm font-bold hover:bg-[#008751] hover:text-white transition-all">
                                <i data-lucide="eye" class="w-4 h-4"></i>
                                Xem chi tiết
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <div class="bg-emerald-50 px-8 py-4 rounded-3xl border border-emerald-100 self-end inline-block">
                <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">
                    Tổng số sân: <span class="text-xl ml-2">${fields.size()}</span> sân
                </span>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />
<script>lucide.createIcons();</script>
</body>
</html>
