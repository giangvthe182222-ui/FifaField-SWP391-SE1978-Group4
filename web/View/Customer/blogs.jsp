<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Blogs Khách Hàng | FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800;900&display=swap" rel="stylesheet">
    <style>body { font-family: 'Inter', sans-serif; }</style>
</head>
<body class="bg-slate-50 min-h-screen flex flex-col">
<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="flex-grow max-w-7xl mx-auto w-full px-6 py-10 space-y-8">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
            <p class="text-xs font-black uppercase tracking-[0.2em] text-slate-400">Customer Blog</p>
            <h1 class="text-4xl font-black text-slate-900 uppercase tracking-tight">Blog và mẹo bóng đá</h1>
            <p class="text-slate-500 font-semibold mt-2">Cập nhật tin tức, mẹo đặt sân và kinh nghiệm đá bóng mỗi tuần.</p>
        </div>
        <a href="${pageContext.request.contextPath}/customer/dashboard" class="px-5 py-3 rounded-2xl border border-slate-200 font-black uppercase text-xs tracking-wider text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">Về dashboard</a>
    </div>

    <jsp:include page="/View/Layout/CustomerQuickPanel.jsp"/>

    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        <c:forEach var="blog" items="${blogs}">
            <article class="bg-white rounded-3xl border border-slate-100 overflow-hidden shadow-sm">
                <img src="${blog.imageUrl}" alt="${blog.title}" class="w-full h-52 object-cover"/>
                <div class="p-6 space-y-3">
                    <p class="text-xs font-black uppercase tracking-widest text-[#008751]">${blog.category}</p>
                    <h2 class="text-xl font-black text-slate-900 tracking-tight">${blog.title}</h2>
                    <p class="text-slate-500 text-sm font-semibold">${blog.description}</p>
                </div>
            </article>
        </c:forEach>
    </div>

    <c:if test="${empty blogs}">
        <div class="bg-white rounded-3xl border-2 border-dashed border-slate-200 p-10 text-center space-y-3">
            <p class="text-slate-400 font-black uppercase tracking-widest text-xs">Hiện chưa có blog nào</p>
            <a href="${pageContext.request.contextPath}/customer/dashboard" class="inline-block px-5 py-3 rounded-2xl bg-[#008751] text-white font-black uppercase text-xs tracking-wider">Quay lại dashboard</a>
        </div>
    </c:if>

    <!-- Pagination -->
    <c:if test="${totalPages > 1}">
        <div class="flex items-center justify-center gap-2 py-6">
            <c:if test="${currentPage > 1}">
                <a href="${pageContext.request.contextPath}/customer/blogs?page=1" 
                   class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                    ⟨⟨ First
                </a>
                <a href="${pageContext.request.contextPath}/customer/blogs?page=${currentPage - 1}" 
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
                        <a href="${pageContext.request.contextPath}/customer/blogs?page=${i}" 
                           class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                            ${i}
                        </a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:if test="${currentPage < totalPages}">
                <a href="${pageContext.request.contextPath}/customer/blogs?page=${currentPage + 1}" 
                   class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                    Next ⟩
                </a>
                <a href="${pageContext.request.contextPath}/customer/blogs?page=${totalPages}" 
                   class="px-3 py-2 rounded-lg border border-slate-200 font-semibold text-sm text-slate-700 hover:border-[#008751] hover:text-[#008751] transition-colors">
                    Last ⟩⟩
                </a>
            </c:if>
        </div>
        <p class="text-center text-sm font-semibold text-slate-500">Trang <span class="text-[#008751] font-bold">${currentPage}</span> / ${totalPages} (Tổng: ${totalItems} blog)</p>
    </c:if>

    <div class="rounded-3xl bg-white border border-slate-100 p-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <p class="font-semibold text-slate-600">Muốn đặt sân ngay sau khi đọc blog? Bấm nút bên dưới.</p>
        <a href="${pageContext.request.contextPath}/booking" class="px-6 py-3 rounded-2xl bg-[#008751] text-white font-black uppercase text-xs tracking-wider hover:bg-[#006f43] transition-colors text-center">Đặt sân ngay</a>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
<script>
    lucide.createIcons();
</script>
</body>
</html>
