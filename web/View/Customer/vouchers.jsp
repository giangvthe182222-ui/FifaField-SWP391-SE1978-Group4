<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ưu Đãi Của Bạn - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .elite-card { border-radius: 2.5rem; }
        .voucher-cut {
            mask-image: radial-gradient(circle at 0 50%, transparent 15px, black 16px), 
                        radial-gradient(circle at 100% 50%, transparent 15px, black 16px);
            mask-composite: intersect;
        }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">
<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="flex-grow max-w-7xl mx-auto w-full px-6 py-12 space-y-12">
    
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        <div class="space-y-2">
            <p class="text-[10px] font-black uppercase tracking-[0.3em] text-gray-400">FIFAFIELD REWARDS</p>
            <h1 class="text-4xl font-black text-gray-900 uppercase tracking-tight">DANH SÁCH <span class="text-[#008751]">VOUCHER</span></h1>
            <p class="text-gray-500 font-bold text-sm uppercase tracking-widest opacity-60">Ưu đãi độc quyền dành riêng cho bạn</p>
        </div>
        <a href="${pageContext.request.contextPath}/customer/dashboard" class="px-8 py-4 rounded-2xl border-2 border-gray-100 bg-white text-gray-400 font-black uppercase text-[10px] tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all flex items-center gap-2">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            Về Dashboard
        </a>
    </div>

    <jsp:include page="/View/Layout/CustomerQuickPanel.jsp"/>

    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <i data-lucide="alert-circle" class="w-5 h-5 text-rose-500"></i>
            <p class="text-sm font-bold text-rose-700 uppercase tracking-tight">${error}</p>
        </div>
    </c:if>

    <form method="get" action="${pageContext.request.contextPath}/customer/vouchers" class="bg-white rounded-[2rem] border border-gray-100 p-6 shadow-xl shadow-gray-200/30 flex flex-col md:flex-row gap-4 md:items-end">
        <div class="w-full md:flex-1">
            <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-1">Lọc theo cơ sở</label>
            <select name="locationId" class="mt-2 w-full px-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all">
                <option value="">Tất cả cơ sở</option>
                <c:forEach var="loc" items="${locations}">
                    <option value="${loc.locationId}" ${selectedLocationId == loc.locationId.toString() ? 'selected' : ''}>${loc.locationName}</option>
                </c:forEach>
            </select>
        </div>
        <div class="w-full md:flex-1">
            <label class="text-[9px] font-black uppercase text-gray-400 tracking-widest ml-1">Nhập tên cơ sở</label>
            <input type="text" name="locationName" value="${locationName}" placeholder="Ví dụ: Cơ sở Quận 7" class="mt-2 w-full px-4 py-4 bg-gray-50 border border-gray-50 rounded-2xl font-bold text-sm text-gray-700 outline-none focus:bg-white focus:ring-4 focus:ring-[#008751]/5 transition-all"/>
        </div>
        <div class="flex gap-3 w-full md:w-auto">
            <button type="submit" class="flex-1 md:flex-none px-8 py-4 rounded-2xl bg-[#008751] text-white font-black uppercase text-[10px] tracking-widest hover:bg-emerald-400 transition-all shadow-lg shadow-[#008751]/20">
                Lọc
            </button>
            <a href="${pageContext.request.contextPath}/customer/vouchers" class="flex-1 md:flex-none px-8 py-4 rounded-2xl border-2 border-gray-100 bg-white text-gray-400 font-black uppercase text-[10px] tracking-widest hover:border-[#008751] hover:text-[#008751] transition-all text-center">
                Xóa lọc
            </a>
        </div>
    </form>

    <!-- Vouchers Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <c:forEach var="v" items="${vouchers}">
            <article class="bg-white rounded-[2.5rem] border border-gray-100 shadow-xl shadow-gray-200/50 group hover:shadow-2xl hover:shadow-[#008751]/10 transition-all flex flex-col md:flex-row overflow-hidden">
                <!-- Left Side: Visual -->
                <div class="w-full md:w-48 bg-[#008751] p-8 flex flex-col items-center justify-center text-white relative overflow-hidden">
                    <div class="absolute inset-0 opacity-10">
                        <i data-lucide="ticket" class="w-32 h-32 -rotate-12 translate-x-10 translate-y-10"></i>
                    </div>
                    <p class="text-[10px] font-black uppercase tracking-widest opacity-60 mb-2">GIẢM GIÁ</p>
                    <p class="text-5xl font-black tracking-tighter leading-none"><fmt:formatNumber value="${v.discountValue}" pattern="#,##0.##"/>%</p>
                    <div class="mt-6 px-4 py-1.5 bg-white/20 backdrop-blur-md rounded-full text-[8px] font-black uppercase tracking-widest">
                        ${v.status}
                    </div>
                </div>

                <!-- Right Side: Content -->
                <div class="flex-grow p-8 space-y-6 relative">
                    <!-- Perforation Line -->
                    <div class="hidden md:block absolute left-0 top-8 bottom-8 border-l-2 border-dashed border-gray-100 -translate-x-[1px]"></div>

                    <div class="flex items-center justify-between">
                        <h2 class="text-2xl font-black text-gray-900 tracking-tight uppercase">${v.code}</h2>
                        <div class="flex items-center gap-2 text-gray-400">
                            <i data-lucide="users" class="w-3 h-3"></i>
                            <p class="text-[9px] font-black uppercase tracking-widest">Đã dùng: ${v.usedCount}</p>
                        </div>
                    </div>

                    <p class="text-gray-500 text-sm font-bold leading-relaxed">
                        ${empty v.description ? 'Sử dụng mã này để nhận ưu đãi giảm giá cho trận đấu tiếp theo của bạn.' : v.description}
                    </p>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div class="bg-gray-50 rounded-2xl p-4 border border-gray-50">
                            <p class="text-[8px] font-black uppercase text-gray-400 tracking-widest mb-1">Áp dụng tại</p>
                            <p class="text-xs font-black text-gray-900 uppercase tracking-tight truncate">${locationNames[v.locationId]} (Cơ sở)</p>
                        </div>
                        <div class="bg-gray-50 rounded-2xl p-4 border border-gray-50">
                            <p class="text-[8px] font-black uppercase text-gray-400 tracking-widest mb-1">Thời hạn</p>
                            <p class="text-xs font-black text-gray-900 uppercase tracking-tight">${v.endDate}</p>
                        </div>
                    </div>

                    <div class="pt-4 flex items-center justify-between gap-4">
                        <div class="flex items-center gap-2 text-gray-400">
                            <i data-lucide="calendar" class="w-3 h-3"></i>
                            <p class="text-[9px] font-black uppercase tracking-widest">Từ: ${v.startDate}</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/booking?locationId=${v.locationId}"
                           class="px-8 py-4 rounded-2xl bg-gray-900 text-white font-black text-[10px] uppercase tracking-widest hover:bg-[#008751] transition-all shadow-lg shadow-gray-200">
                            Sử dụng ngay
                        </a>
                    </div>
                </div>
            </article>
        </c:forEach>
    </div>

    <c:if test="${empty vouchers}">
        <div class="bg-white rounded-[2.5rem] border-2 border-dashed border-gray-100 p-20 text-center space-y-6">
            <i data-lucide="ticket" class="w-12 h-12 text-gray-200 mx-auto"></i>
            <p class="text-gray-300 font-black uppercase tracking-widest text-[10px]">Hiện chưa có voucher khả dụng</p>
            <a href="${pageContext.request.contextPath}/customer/dashboard" class="inline-flex px-8 py-4 rounded-2xl bg-[#008751] text-white font-black uppercase text-[10px] tracking-widest hover:bg-emerald-400 transition-all shadow-lg shadow-[#008751]/20">
                Quay lại Dashboard
            </a>
        </div>
    </c:if>

    <!-- Pagination -->
    <c:if test="${totalPages > 1}">
        <div class="flex items-center justify-center gap-3 py-10">
            <c:if test="${currentPage > 1}">
                <a href="${pageContext.request.contextPath}/customer/vouchers?page=1<c:if test='${not empty selectedLocationId}'>&locationId=${selectedLocationId}</c:if><c:if test='${not empty locationName}'>&locationName=${locationName}</c:if>" 
                   class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                    <i data-lucide="chevrons-left" class="w-4 h-4"></i>
                </a>
                <a href="${pageContext.request.contextPath}/customer/vouchers?page=${currentPage - 1}<c:if test='${not empty selectedLocationId}'>&locationId=${selectedLocationId}</c:if><c:if test='${not empty locationName}'>&locationName=${locationName}</c:if>" 
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
                        <a href="${pageContext.request.contextPath}/customer/vouchers?page=${i}<c:if test='${not empty selectedLocationId}'>&locationId=${selectedLocationId}</c:if><c:if test='${not empty locationName}'>&locationName=${locationName}</c:if>" 
                           class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 font-black text-xs hover:border-[#008751] hover:text-[#008751] transition-all">
                            ${i}
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:if test="${currentPage < totalPages}">
                <a href="${pageContext.request.contextPath}/customer/vouchers?page=${currentPage + 1}<c:if test='${not empty selectedLocationId}'>&locationId=${selectedLocationId}</c:if><c:if test='${not empty locationName}'>&locationName=${locationName}</c:if>" 
                   class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                    <i data-lucide="chevron-right" class="w-4 h-4"></i>
                </a>
                <a href="${pageContext.request.contextPath}/customer/vouchers?page=${totalPages}<c:if test='${not empty selectedLocationId}'>&locationId=${selectedLocationId}</c:if><c:if test='${not empty locationName}'>&locationName=${locationName}</c:if>" 
                   class="w-10 h-10 rounded-xl border-2 border-gray-50 bg-white flex items-center justify-center text-gray-400 hover:border-[#008751] hover:text-[#008751] transition-all">
                    <i data-lucide="chevrons-right" class="w-4 h-4"></i>
                </a>
            </c:if>
        </div>
        <p class="text-center text-[10px] font-black text-gray-400 uppercase tracking-widest">Trang <span class="text-[#008751]">${currentPage}</span> / ${totalPages} (Tổng: ${totalItems} voucher)</p>
    </c:if>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>
</body>
</html>
