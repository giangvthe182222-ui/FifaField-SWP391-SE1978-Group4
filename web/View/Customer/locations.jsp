<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách cơ sở - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="flex-grow max-w-7xl mx-auto w-full px-6 py-12 space-y-8">
    <jsp:include page="/View/Layout/CustomerTopBanner.jsp"/>

    <!-- FILTER -->
    <section class="bg-white rounded-[2rem] border border-gray-100 p-6 shadow-sm">
        <form method="get" action="${pageContext.request.contextPath}/customer/locations"
              class="grid grid-cols-1 lg:grid-cols-4 gap-4 items-end">

            <!-- CƠ SỞ -->
            <div>
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Cơ sở</label>
                <div class="relative mt-2">
                    <i data-lucide="building-2" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <select name="locationName"
                            class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700">
                        <option value="">Tất cả cơ sở</option>
                        <c:forEach var="nameOpt" items="${locationNameOptions}">
                            <option value="${nameOpt}" ${locationName == nameOpt ? 'selected' : ''}>
                                ${nameOpt}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <!-- TÊN -->
            <div>
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Tìm tên</label>
                <div class="relative mt-2">
                    <i data-lucide="search" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <input type="text" name="searchName" placeholder="Nhập tên cơ sở..."
                           value="${searchName}"
                           class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700"/>
                </div>
            </div>

            <!-- ĐỊA CHỈ -->
            <div>
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Tìm địa chỉ</label>
                <div class="relative mt-2">
                    <i data-lucide="map-pin" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <input type="text" name="searchAddress" placeholder="Nhập địa chỉ..."
                           value="${searchAddress}"
                           class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700"/>
                </div>
            </div>

            <!-- BUTTON -->
            <div class="flex gap-3">
                <button type="submit"
                        class="flex-1 px-8 py-4 rounded-2xl bg-[#008751] text-white font-black uppercase text-[10px] tracking-widest">
                    Tìm kiếm
                </button>
                <a href="${pageContext.request.contextPath}/customer/locations"
                   class="flex-1 px-8 py-4 rounded-2xl border-2 border-gray-100 text-gray-400 font-black uppercase text-[10px] tracking-widest text-center">
                    Xóa lọc
                </a>
            </div>
        </form>
    </section>

    <!-- ERROR -->
    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <i data-lucide="alert-circle" class="w-5 h-5 text-rose-500"></i>
            <p class="text-sm font-bold text-rose-700 uppercase">${error}</p>
        </div>
    </c:if>

    <!-- LIST -->
    <section class="space-y-6">
        <div class="flex items-center justify-between">
            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">
                Tổng cộng ${totalItems} cơ sở
            </p>
        </div>

        <c:choose>
            <c:when test="${empty locations}">
                <div class="bg-white rounded-[2.5rem] border-2 border-dashed border-gray-100 p-20 text-center">
                    <i data-lucide="search-x" class="w-12 h-12 text-gray-200 mx-auto mb-4"></i>
                    <p class="text-gray-300 font-black uppercase tracking-widest text-[10px]">
                        Không tìm thấy cơ sở phù hợp
                    </p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <c:forEach var="loc" items="${locations}">
                        <article class="bg-white border border-gray-100 rounded-[2.5rem] p-8 shadow-sm flex flex-col gap-6">

                            <!-- IMAGE -->
                            <div class="w-full h-56 rounded-[2rem] overflow-hidden bg-gray-50">
                                <c:choose>
                                    <c:when test="${not empty loc.imageUrl}">
                                        <img src="${pageContext.request.contextPath}/${loc.imageUrl}"
                                             class="w-full h-full object-cover"/>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="w-full h-full flex items-center justify-center text-gray-200">
                                            <i data-lucide="image" class="w-12 h-12"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <!-- MAP -->
                            <c:if test="${not empty loc.address}">
                                <iframe src="https://www.google.com/maps?q=${loc.address}&output=embed"
                                        class="w-full h-44 rounded-2xl border"></iframe>
                            </c:if>

                            <!-- INFO -->
                            <div class="space-y-3">
                                <h3 class="text-2xl font-black uppercase">${loc.locationName}</h3>

                                <p>📍 ${loc.address}</p>
                                <p>📞 Hotline: ${loc.phoneNumber}</p>

                                <p>
                                    ⭐
                                    <c:choose>
                                        <c:when test="${locationAverageRatingMap[loc.locationId] != null}">
                                            <fmt:formatNumber value="${locationAverageRatingMap[loc.locationId]}" pattern="0.0"/>
                                            sao | ${locationFeedbackCountMap[loc.locationId]} đánh giá
                                        </c:when>
                                        <c:otherwise>
                                            Chưa có đánh giá
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>

                            <!-- ACTION -->
                            <div class="flex gap-2 mt-auto">
                                <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${loc.locationId}"
                                   class="px-4 py-2 border rounded-xl">
                                    Xem cơ sở
                                </a>
                                <a href="${pageContext.request.contextPath}/booking?locationId=${loc.locationId}"
                                   class="px-4 py-2 bg-black text-white rounded-xl">
                                    Đặt sân
                                </a>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- PAGINATION -->
    <c:if test="${totalPages > 1}">
        <section class="text-center">
            <p>Trang ${currentPage} / ${totalPages}</p>
        </section>
    </c:if>

</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>

</body>
</html>