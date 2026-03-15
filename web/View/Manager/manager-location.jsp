<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cụm sân của tôi - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }</style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-4xl mx-auto px-6 py-12 w-full flex-grow">

    <div class="mb-6">
        <button type="button" onclick="history.back()"
                class="inline-flex items-center gap-2 text-sm font-semibold text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-4">
            <i data-lucide="arrow-left" class="w-4 h-4"></i> Quay lại
        </button>
        <h1 class="text-3xl font-black text-gray-900 uppercase tracking-tight">
            CỤM SÂN <span class="text-[#008751]">CỦA TÔI</span>
        </h1>
    </div>

    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-2xl flex items-start gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-500 mt-0.5 flex-shrink-0"></i>
            <p class="text-red-700 text-sm font-semibold">${error}</p>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty location}">
            <div class="bg-white rounded-2xl p-12 text-center shadow border border-gray-100">
                <i data-lucide="map-pin-off" class="w-16 h-16 text-gray-300 mx-auto mb-4"></i>
                <p class="text-gray-500 font-semibold">Chưa được gán cụm sân</p>
            </div>
        </c:when>
        <c:otherwise>
            <!-- Location Card -->
            <div class="bg-white rounded-2xl shadow border border-gray-100 overflow-hidden mb-6">
                <c:if test="${not empty location.imageUrl}">
                    <img src="${pageContext.request.contextPath}/${location.imageUrl}"
                         alt="${location.locationName}"
                         class="w-full h-52 object-cover" />
                </c:if>
                <div class="p-8">
                    <div class="flex items-start justify-between mb-6">
                        <div>
                            <h2 class="text-2xl font-black text-gray-900">${location.locationName}</h2>
                            <p class="text-gray-500 mt-1 text-sm">${location.address}</p>
                        </div>
                        <c:set var="locStatusCss" value="bg-gray-100 text-gray-500"/>
                        <c:if test="${location.status eq 'ACTIVE'}"><c:set var="locStatusCss" value="bg-emerald-100 text-emerald-700"/></c:if>
                        <c:set var="locDotCss" value="bg-gray-400"/>
                        <c:if test="${location.status eq 'ACTIVE'}"><c:set var="locDotCss" value="bg-emerald-500"/></c:if>
                        <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold uppercase ${locStatusCss}">
                            <span class="w-2 h-2 rounded-full ${locDotCss}"></span>
                            ${location.status}
                        </span>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div class="space-y-4">
                            <div class="flex items-center gap-3">
                                <div class="w-9 h-9 bg-green-50 rounded-xl flex items-center justify-center">
                                    <i data-lucide="phone" class="w-4 h-4 text-[#008751]"></i>
                                </div>
                                <div>
                                    <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Số điện thoại</p>
                                    <p class="text-sm font-bold text-gray-800">${location.phoneNumber}</p>
                                </div>
                            </div>
                            <div class="flex items-center gap-3">
                                <div class="w-9 h-9 bg-green-50 rounded-xl flex items-center justify-center">
                                    <i data-lucide="map-pin" class="w-4 h-4 text-[#008751]"></i>
                                </div>
                                <div>
                                    <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Địa chỉ</p>
                                    <p class="text-sm font-bold text-gray-800">${location.address}</p>
                                </div>
                            </div>
                        </div>
                        <div class="space-y-4">
                            <div class="flex items-center gap-3">
                                <div class="w-9 h-9 bg-blue-50 rounded-xl flex items-center justify-center">
                                    <i data-lucide="user-check" class="w-4 h-4 text-blue-600"></i>
                                </div>
                                <div>
                                    <p class="text-xs text-gray-400 font-semibold uppercase tracking-wider">Quản lý</p>
                                    <p class="text-sm font-bold text-gray-800">${location.managerName}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Quick Links -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <a href="${pageContext.request.contextPath}/manager/location/fields"
                   class="flex items-center gap-4 bg-white p-6 rounded-2xl shadow border border-gray-100 hover:border-[#008751] hover:-translate-y-1 transition-all group">
                    <div class="w-12 h-12 bg-emerald-50 group-hover:bg-[#008751] rounded-xl flex items-center justify-center transition-colors">
                        <i data-lucide="layout-grid" class="w-6 h-6 text-[#008751] group-hover:text-white transition-colors"></i>
                    </div>
                    <div>
                        <p class="font-bold text-gray-900">Sân bóng</p>
                        <p class="text-xs text-gray-400">Xem & quản lý các sân tại cụm</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-[#008751] transition-colors"></i>
                </a>
                <a href="${pageContext.request.contextPath}/manager/location/equipment"
                   class="flex items-center gap-4 bg-white p-6 rounded-2xl shadow border border-gray-100 hover:border-[#008751] hover:-translate-y-1 transition-all group">
                    <div class="w-12 h-12 bg-blue-50 group-hover:bg-blue-600 rounded-xl flex items-center justify-center transition-colors">
                        <i data-lucide="shield" class="w-6 h-6 text-blue-600 group-hover:text-white transition-colors"></i>
                    </div>
                    <div>
                        <p class="font-bold text-gray-900">Dụng cụ</p>
                        <p class="text-xs text-gray-400">Xem dụng cụ tại cụm sân</p>
                    </div>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300 ml-auto group-hover:text-blue-600 transition-colors"></i>
                </a>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />
<script>lucide.createIcons();</script>
</body>
</html>
