<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh Sach Co So - FIFAFIELD</title>
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

    <section class="bg-white rounded-[2rem] border border-gray-100 p-6 shadow-sm">
        <form method="get" action="${pageContext.request.contextPath}/customer/locations" class="grid grid-cols-1 lg:grid-cols-4 gap-4 items-end">
            <div>
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Co so</label>
                <div class="relative mt-2">
                    <i data-lucide="building-2" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <select name="locationName" class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all">
                        <option value="">Tat ca co so</option>
                        <c:forEach var="nameOpt" items="${locationNameOptions}">
                            <option value="${nameOpt}" ${locationName == nameOpt ? 'selected' : ''}>${nameOpt}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div>
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Tim ten</label>
                <div class="relative mt-2">
                    <i data-lucide="search" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <input type="text" name="searchName" placeholder="Nhap ten co so..." value="${searchName}"
                           class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all"/>
                </div>
            </div>
            <div>
                <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-2">Tim dia chi</label>
                <div class="relative mt-2">
                    <i data-lucide="map-pin" class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-300"></i>
                    <input type="text" name="searchAddress" placeholder="Nhap dia chi..." value="${searchAddress}"
                           class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all"/>
                </div>
            </div>
            <div class="flex gap-3">
                <button type="submit" class="flex-1 px-8 py-4 rounded-2xl bg-[#008751] text-white font-black uppercase text-[10px] tracking-widest hover:bg-emerald-400 transition-all shadow-lg shadow-[#008751]/20">
                    Tim kiem
                </button>
                <a href="${pageContext.request.contextPath}/customer/locations" class="flex-1 px-8 py-4 rounded-2xl border-2 border-gray-100 bg-white text-gray-400 font-black uppercase text-[10px] tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all text-center">
                    Xoa loc
                </a>
            </div>
        </form>
    </section>

    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <i data-lucide="alert-circle" class="w-5 h-5 text-rose-500"></i>
            <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${error}</p>
        </div>
    </c:if>

    <section class="space-y-6">
        <div class="flex items-center justify-between">
            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">Tong cong ${totalItems} co so</p>
            <a href="${pageContext.request.contextPath}/booking" class="px-5 py-3 rounded-2xl bg-gray-900 text-white font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-all">Dat san ngay</a>
        </div>

        <c:choose>
            <c:when test="${empty locations}">
                <div class="bg-white rounded-[2.5rem] border-2 border-dashed border-gray-100 p-20 text-center">
                    <i data-lucide="search-x" class="w-12 h-12 text-gray-200 mx-auto mb-4"></i>
                    <p class="text-gray-300 font-black uppercase tracking-widest text-[10px]">Khong tim thay co so phu hop</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <c:forEach var="loc" items="${locations}">
                        <article class="bg-white border border-gray-100 rounded-[2.5rem] p-8 shadow-sm flex flex-col gap-6 group hover:shadow-xl hover:shadow-[#008751]/10 transition-all">
                            <div class="w-full h-56 rounded-[2rem] overflow-hidden bg-gray-50 relative">
                                <c:choose>
                                    <c:when test="${not empty loc.imageUrl}">
                                        <img src="${pageContext.request.contextPath}/${loc.imageUrl}" alt="${loc.locationName}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"/>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="w-full h-full flex items-center justify-center text-gray-200">
                                            <i data-lucide="image" class="w-12 h-12"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="space-y-3">
                                <h3 class="text-2xl font-black text-gray-900 tracking-tight uppercase group-hover:text-[#008751] transition-colors">${loc.locationName}</h3>
                                <div class="flex items-start gap-2 text-gray-500">
                                    <i data-lucide="map-pin" class="w-4 h-4 mt-0.5 shrink-0"></i>
                                    <p class="font-bold text-sm">${loc.address}</p>
                                </div>
                                <div class="flex items-center gap-2 text-gray-400">
                                    <i data-lucide="phone" class="w-3 h-3"></i>
                                    <p class="text-[10px] font-black uppercase tracking-widest">Hotline: ${loc.phoneNumber}</p>
                                </div>
                            </div>

                            <div class="mt-auto pt-6 border-t border-gray-50 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
                                <a href="https://www.google.com/maps/search/?api=1&query=${loc.address}" target="_blank" rel="noopener noreferrer"
                                   class="px-6 py-4 rounded-2xl border border-blue-100 text-blue-700 font-black text-[10px] uppercase tracking-widest hover:border-blue-500 hover:text-blue-600 transition-all text-center bg-blue-50">
                                    Google Maps
                                </a>
                                <div class="flex items-center gap-2">
                                    <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${loc.locationId}"
                                       class="px-6 py-4 rounded-2xl border border-gray-200 text-gray-700 font-black text-[10px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all text-center">
                                        Xem co so
                                    </a>
                                    <a href="${pageContext.request.contextPath}/booking?locationId=${loc.locationId}"
                                       class="px-6 py-4 rounded-2xl bg-gray-900 text-white font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-all text-center">
                                        Dat san
                                    </a>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <c:if test="${totalPages > 1}">
        <section class="flex flex-col items-center gap-4 py-6">
            <div class="flex items-center gap-3">
                <c:if test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/customer/locations?page=1<c:if test='${not empty locationName}'>&locationName=${locationName}</c:if><c:if test='${not empty searchName}'>&searchName=${searchName}</c:if><c:if test='${not empty searchAddress}'>&searchAddress=${searchAddress}</c:if>"
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevrons-left" class="w-4 h-4"></i>
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/locations?page=${currentPage - 1}<c:if test='${not empty locationName}'>&locationName=${locationName}</c:if><c:if test='${not empty searchName}'>&searchName=${searchName}</c:if><c:if test='${not empty searchAddress}'>&searchAddress=${searchAddress}</c:if>"
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevron-left" class="w-4 h-4"></i>
                    </a>
                </c:if>

                <c:forEach begin="1" end="${totalPages}" var="i">
                    <c:choose>
                        <c:when test="${i == currentPage}">
                            <span class="w-10 h-10 rounded-xl bg-[#008751] text-white flex items-center justify-center font-black text-xs shadow-lg shadow-[#008751]/20">${i}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/customer/locations?page=${i}<c:if test='${not empty locationName}'>&locationName=${locationName}</c:if><c:if test='${not empty searchName}'>&searchName=${searchName}</c:if><c:if test='${not empty searchAddress}'>&searchAddress=${searchAddress}</c:if>"
                               class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 font-black text-xs hover:border-[#008751] hover:text-[#008751] transition-all">
                                ${i}
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:if test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/customer/locations?page=${currentPage + 1}<c:if test='${not empty locationName}'>&locationName=${locationName}</c:if><c:if test='${not empty searchName}'>&searchName=${searchName}</c:if><c:if test='${not empty searchAddress}'>&searchAddress=${searchAddress}</c:if>"
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevron-right" class="w-4 h-4"></i>
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/locations?page=${totalPages}<c:if test='${not empty locationName}'>&locationName=${locationName}</c:if><c:if test='${not empty searchName}'>&searchName=${searchName}</c:if><c:if test='${not empty searchAddress}'>&searchAddress=${searchAddress}</c:if>"
                       class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                        <i data-lucide="chevrons-right" class="w-4 h-4"></i>
                    </a>
                </c:if>
            </div>
            <p class="text-center text-[10px] font-black text-gray-400 uppercase tracking-widest">Trang <span class="text-[#008751]">${currentPage}</span> / ${totalPages}</p>
        </section>
    </c:if>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>
</body>
</html>
