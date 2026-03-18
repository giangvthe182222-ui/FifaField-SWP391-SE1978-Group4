<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi Tiết Cơ Sở - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
    </style>
</head>
<body class="min-h-screen text-slate-900">
<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="max-w-5xl mx-auto px-6 py-12 space-y-8">
    <jsp:include page="/View/Layout/CustomerTopBanner.jsp"/>

    <div class="flex items-center justify-between gap-4">
        <div>
            <p class="text-[10px] font-black uppercase tracking-[0.25em] text-slate-400">FIFAFIELD LOCATION</p>
            <h1 class="text-4xl font-black tracking-tight uppercase">Chi tiết <span class="text-[#008751]">Cơ sở</span></h1>
        </div>
        <a href="${pageContext.request.contextPath}/customer/dashboard#locations" class="w-10 h-10 rounded-xl border border-slate-200 text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors flex items-center justify-center" aria-label="Quay lại" title="Quay lại">
            <i data-lucide="arrow-left" class="w-5 h-5"></i>
        </a>
    </div>

    <section class="bg-white rounded-[2.5rem] border border-slate-100 shadow-xl shadow-slate-200/40 p-8 md:p-10">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div class="w-full h-72 rounded-[2rem] overflow-hidden bg-slate-100">
                <c:choose>
                    <c:when test="${not empty location.imageUrl}">
                        <img src="${pageContext.request.contextPath}/${location.imageUrl}" alt="${location.locationName}" class="w-full h-full object-cover">
                    </c:when>
                    <c:otherwise>
                        <div class="w-full h-full flex items-center justify-center text-slate-300">
                            <i data-lucide="image" class="w-14 h-14"></i>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="space-y-6">
                <div>
                    <h2 class="text-3xl font-black uppercase tracking-tight">${location.locationName}</h2>
                    <p class="mt-2 text-sm font-semibold text-slate-500">Thông tin chi tiết cơ sở cho khách hàng đặt sân.</p>
                </div>

                <div class="space-y-3">
                    <div class="flex items-start gap-3 bg-slate-50 border border-slate-100 rounded-2xl p-4">
                        <i data-lucide="map-pin" class="w-4 h-4 text-[#008751] mt-0.5"></i>
                        <p class="text-sm font-bold text-slate-700">${location.address}</p>
                    </div>
                    <div class="flex items-center gap-3 bg-slate-50 border border-slate-100 rounded-2xl p-4">
                        <i data-lucide="phone" class="w-4 h-4 text-[#008751]"></i>
                        <p class="text-sm font-bold text-slate-700">Hotline: ${location.phoneNumber}</p>
                    </div>
                    <div class="flex items-center gap-3 bg-slate-50 border border-slate-100 rounded-2xl p-4">
                        <i data-lucide="shield-check" class="w-4 h-4 text-[#008751]"></i>
                        <p class="text-sm font-bold text-slate-700">Trạng thái: ${location.status}</p>
                    </div>
                </div>

                <div class="pt-2 flex flex-col sm:flex-row gap-3">
                    <a href="${pageContext.request.contextPath}/booking?locationId=${location.locationId}" class="px-8 py-4 rounded-2xl bg-[#008751] text-white font-black text-xs uppercase tracking-widest text-center hover:bg-emerald-500 transition-colors">
                        Đặt sân tại cơ sở này
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/vouchers?locationId=${location.locationId}" class="px-8 py-4 rounded-2xl border border-slate-200 text-slate-700 font-black text-xs uppercase tracking-widest text-center hover:border-[#008751] hover:text-[#008751] transition-colors">
                        Xem voucher cơ sở
                    </a>
                </div>
            </div>
        </div>
    </section>

    <section class="bg-white rounded-[2.5rem] border border-slate-100 shadow-xl shadow-slate-200/40 p-8 md:p-10 space-y-6">
        <div class="flex items-center gap-3">
            <div class="w-8 h-1 bg-[#008751] rounded-full"></div>
            <h2 class="text-[11px] font-black uppercase tracking-[0.24em] text-slate-500">Danh sách sân tại cơ sở</h2>
        </div>

        <c:choose>
            <c:when test="${empty fields}">
                <div class="rounded-2xl border-2 border-dashed border-slate-200 bg-slate-50 p-8 text-center">
                    <p class="text-sm font-bold text-slate-500">Hiện chưa có sân khả dụng tại cơ sở này.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <c:forEach var="f" items="${fields}">
                        <article class="rounded-2xl border border-slate-200 bg-white p-5 flex items-center justify-between gap-4">
                            <div class="flex items-center gap-4 min-w-0">
                                <div class="w-20 h-20 rounded-xl overflow-hidden bg-slate-100 shrink-0">
                                    <c:choose>
                                        <c:when test="${not empty f.imageUrl}">
                                            <img src="${pageContext.request.contextPath}/${f.imageUrl}" alt="${f.fieldName}" class="w-full h-full object-cover">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="w-full h-full flex items-center justify-center text-slate-300">
                                                <i data-lucide="image" class="w-5 h-5"></i>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="min-w-0">
                                    <p class="text-lg font-black text-slate-900 uppercase tracking-tight truncate">${f.fieldName}</p>
                                    <p class="text-xs font-bold text-slate-500 uppercase tracking-wider mt-1">Loại sân: ${f.fieldType}</p>
                                    <p class="text-xs font-bold text-emerald-700 uppercase tracking-wider mt-1">${f.status}</p>
                                </div>
                            </div>
                            <div class="flex flex-col gap-2">
                                <a href="${pageContext.request.contextPath}/customer/field-schedule?fieldId=${f.fieldId}" class="px-4 py-3 rounded-xl border border-slate-200 text-slate-700 font-black text-[10px] uppercase tracking-widest hover:border-[#008751] hover:text-[#008751] transition-colors text-center">
                                    Xem lịch sân
                                </a>
                                <a href="${pageContext.request.contextPath}/booking?locationId=${location.locationId}&fieldId=${f.fieldId}" class="px-4 py-3 rounded-xl bg-slate-900 text-white font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-colors text-center">
                                    Đặt ngay
                                </a>
                            </div>
                        </article>
                    </c:forEach>
                </div>

                <c:if test="${totalPages > 1}">
                    <div class="flex items-center justify-center gap-2 py-6">
                        <c:if test="${currentPage > 1}">
                            <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${location.locationId}&page=1" 
                               class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                                ⟨⟨ First
                            </a>
                            <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${location.locationId}&page=${currentPage - 1}" 
                               class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                                ⟨ Prev
                            </a>
                        </c:if>

                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="px-3 py-2 rounded-lg bg-[#008751] text-white font-bold text-sm">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${location.locationId}&page=${i}" 
                                       class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                                        ${i}
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${location.locationId}&page=${currentPage + 1}" 
                               class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                                Next ⟩
                            </a>
                            <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${location.locationId}&page=${totalPages}" 
                               class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                                Last ⟩⟩
                            </a>
                        </c:if>
                    </div>
                    <p class="text-center text-sm font-semibold text-slate-500">Trang <span class="text-[#008751] font-bold">${currentPage}</span> / ${totalPages} (Tổng: ${totalFields} sân)</p>
                </c:if>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
<script>
    lucide.createIcons();
</script>
</body>
</html>
