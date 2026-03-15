<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết sân - FIFAFIELD Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }</style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-4xl mx-auto px-6 py-12 w-full flex-grow">

    <!-- BACK BUTTON -->
    <button type="button" onclick="history.back()"
            class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-6">
        <i data-lucide="arrow-left" class="w-3 h-3"></i> Quay lại danh sách sân
    </button>

    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-500 flex-shrink-0"></i>
            <p class="text-red-700 text-sm font-semibold">${error}</p>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty field}">
            <div class="bg-white rounded-2xl p-16 text-center shadow border border-gray-100">
                <i data-lucide="layout-grid" class="w-16 h-16 text-gray-300 mx-auto mb-4"></i>
                <p class="text-gray-500 font-semibold text-lg">Không tìm thấy sân</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="bg-white rounded-2xl shadow border border-gray-100 overflow-hidden">

                <!-- FIELD IMAGE -->
                <div class="w-full h-64 bg-gray-100 overflow-hidden">
                    <c:choose>
                        <c:when test="${not empty field.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${field.imageUrl}"
                                 alt="${field.fieldName}"
                                 class="w-full h-full object-cover"/>
                        </c:when>
                        <c:otherwise>
                            <div class="w-full h-full flex items-center justify-center">
                                <i data-lucide="layout-grid" class="w-16 h-16 text-gray-300"></i>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- FIELD INFO -->
                <div class="p-8">
                    <div class="flex items-start justify-between mb-6">
                        <div>
                            <h1 class="text-3xl font-black text-gray-900 uppercase">${field.fieldName}</h1>
                            <p class="text-gray-400 text-sm mt-1 font-medium">ID: ${field.fieldId}</p>
                        </div>
                        <c:set var="detailStatusCss" value="bg-gray-100 text-gray-600"/>
                        <c:if test="${field.status eq 'ACTIVE'}"><c:set var="detailStatusCss" value="bg-emerald-100 text-emerald-700"/></c:if>
                        <c:if test="${field.status eq 'MAINTENANCE'}"><c:set var="detailStatusCss" value="bg-amber-100 text-amber-700"/></c:if>
                        <span class="inline-flex items-center gap-1.5 px-4 py-2 rounded-full text-sm font-black ${detailStatusCss}">
                            <i data-lucide="circle" class="w-2.5 h-2.5 fill-current"></i>
                            ${field.status}
                        </span>
                    </div>

                    <!-- INFO GRID -->
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">

                        <div class="bg-gray-50 rounded-xl p-5 flex items-start gap-4">
                            <div class="w-10 h-10 rounded-xl bg-[#008751]/10 flex items-center justify-center flex-shrink-0">
                                <i data-lucide="users" class="w-5 h-5 text-[#008751]"></i>
                            </div>
                            <div>
                                <p class="text-xs font-black text-gray-400 uppercase tracking-widest mb-0.5">Loại sân</p>
                                <p class="font-black text-gray-900 text-lg">
                                    <c:choose>
                                        <c:when test="${field.fieldType eq '7-a-side'}">Sân 7 người</c:when>
                                        <c:when test="${field.fieldType eq '11-a-side'}">Sân 11 người</c:when>
                                        <c:otherwise>${field.fieldType}</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </div>

                        <div class="bg-gray-50 rounded-xl p-5 flex items-start gap-4">
                            <div class="w-10 h-10 rounded-xl bg-[#008751]/10 flex items-center justify-center flex-shrink-0">
                                <i data-lucide="shield-check" class="w-5 h-5 text-[#008751]"></i>
                            </div>
                            <div>
                                <p class="text-xs font-black text-gray-400 uppercase tracking-widest mb-0.5">Tình trạng</p>
                                <p class="font-black text-gray-900 text-lg">${field.fieldCondition}</p>
                            </div>
                        </div>

                        <div class="bg-gray-50 rounded-xl p-5 flex items-start gap-4 md:col-span-2">
                            <div class="w-10 h-10 rounded-xl bg-[#008751]/10 flex items-center justify-center flex-shrink-0">
                                <i data-lucide="map-pin" class="w-5 h-5 text-[#008751]"></i>
                            </div>
                            <div>
                                <p class="text-xs font-black text-gray-400 uppercase tracking-widest mb-0.5">Thuộc cụm sân</p>
                                <p class="font-black text-gray-900 text-lg">${locationName}</p>
                            </div>
                        </div>

                    </div>

                    <!-- ACTIONS -->
                    <div class="flex flex-wrap gap-3 pt-6 border-t border-gray-100">
                        <a href="${pageContext.request.contextPath}/manager/field-schedule?fieldId=${field.fieldId}"
                           class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gray-100 text-gray-700 text-sm font-black hover:bg-gray-200 transition-all">
                            <i data-lucide="calendar" class="w-4 h-4"></i>
                            Xem lịch sân
                        </a>
                        <a href="${pageContext.request.contextPath}/manager/location/fields"
                           class="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl border border-gray-200 text-gray-600 text-sm font-black hover:bg-gray-50 transition-all">
                            <i data-lucide="layout-grid" class="w-4 h-4"></i>
                            Quay về danh sách
                        </a>
                    </div>

                </div>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<script>lucide.createIcons();</script>
<jsp:include page="/View/Layout/FooterManager.jsp" />
</body>
</html>
