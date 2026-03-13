<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách Blog | FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 min-h-screen">

<c:choose>
    <c:when test="${roleName == 'manager'}"><jsp:include page="/View/Layout/HeaderManager.jsp"/></c:when>
    <c:when test="${roleName == 'staff'}"><jsp:include page="/View/Layout/HeaderStaff.jsp"/></c:when>
    <c:otherwise><jsp:include page="/View/Layout/HeaderCustomer.jsp"/></c:otherwise>
</c:choose>

<main class="max-w-7xl mx-auto px-6 py-8 space-y-6">
    <section class="bg-white rounded-3xl border border-slate-200 p-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
                <p class="text-xs uppercase tracking-[0.2em] text-slate-400 font-black">Blog Center</p>
                <h1 class="text-3xl font-black text-slate-900">Danh sách bài viết</h1>
                <p class="text-sm text-slate-500 font-semibold mt-2">Customer xem bài đã duyệt. Staff tạo và gửi duyệt. Manager duyệt, chỉnh sửa và xóa.</p>
            </div>
            <c:if test="${canCreate}">
                <a href="${pageContext.request.contextPath}/blog/create" class="px-5 py-3 rounded-2xl bg-[#008751] text-white text-sm font-black uppercase tracking-wider hover:bg-[#006f43] transition-colors">Tạo bài viết</a>
            </c:if>
        </div>
    </section>

    <section class="bg-white rounded-3xl border border-slate-200 p-6">
        <form method="get" action="${listPath}" class="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
            <div class="md:col-span-7">
                <label class="block text-xs font-black uppercase tracking-widest text-slate-400 mb-2">Từ khóa</label>
                <input type="text" name="q" value="${keyword}" class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#008751]" placeholder="Tìm theo tiêu đề hoặc nội dung">
            </div>
            <c:if test="${canManage}">
                <div class="md:col-span-3">
                    <label class="block text-xs font-black uppercase tracking-widest text-slate-400 mb-2">Trạng thái</label>
                    <select name="status" class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#008751]">
                        <option value="all" ${statusFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                        <option value="draft" ${statusFilter == 'draft' ? 'selected' : ''}>Draft</option>
                        <option value="pending" ${statusFilter == 'pending' ? 'selected' : ''}>Pending</option>
                        <option value="approved" ${statusFilter == 'approved' ? 'selected' : ''}>Approved</option>
                        <option value="rejected" ${statusFilter == 'rejected' ? 'selected' : ''}>Rejected</option>
                    </select>
                </div>
            </c:if>
            <div class="md:col-span-2">
                <button type="submit" class="w-full rounded-xl bg-slate-900 text-white py-3 text-sm font-black uppercase tracking-wider hover:bg-slate-700 transition-colors">Lọc</button>
            </div>
        </form>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        <c:forEach var="blog" items="${blogs}">
            <article class="bg-white rounded-3xl border border-slate-100 overflow-hidden shadow-sm flex flex-col">
                <c:if test="${not empty blog.imageUrl}">
                    <img src="${blog.imageUrl}" alt="${blog.title}" class="w-full h-48 object-cover">
                </c:if>
                <div class="p-5 space-y-3 flex-grow flex flex-col">
                    <div class="flex items-center justify-between gap-2">
                        <span class="px-3 py-1 rounded-full text-[10px] uppercase tracking-widest font-black
                                     ${blog.status == 'approved' ? 'bg-emerald-50 text-[#008751]' : blog.status == 'pending' ? 'bg-amber-50 text-amber-700' : blog.status == 'rejected' ? 'bg-red-50 text-red-600' : 'bg-slate-100 text-slate-500'}">${blog.status}</span>
                        <span class="text-xs text-slate-400 font-semibold">${fn:substring(empty blog.publishedAt ? blog.createdAt : blog.publishedAt, 0, 10)}</span>
                    </div>
                    <h2 class="text-xl font-black text-slate-900 leading-tight">${blog.title}</h2>
                    <p class="text-sm text-slate-500 font-semibold line-clamp-3">${blog.summary}</p>
                    <p class="text-xs text-slate-400">Tác giả: <span class="font-bold">${blog.createdByName}</span></p>
                    <div class="flex items-center gap-4 text-xs text-slate-500 font-bold">
                        <span>❤ ${blog.likeCount}</span>
                        <span>💬 ${blog.commentCount}</span>
                    </div>

                    <div class="pt-2 flex flex-wrap gap-2">
                        <a href="${pageContext.request.contextPath}/blog/detail?id=${blog.blogId}" class="px-3 py-2 rounded-lg border border-slate-200 text-xs font-black uppercase tracking-wider text-slate-700 hover:border-[#008751] hover:text-[#008751]">Chi tiết</a>

                        <c:if test="${roleName == 'manager' || roleName == 'staff'}">
                            <a href="${pageContext.request.contextPath}/blog/edit?id=${blog.blogId}" class="px-3 py-2 rounded-lg border border-slate-200 text-xs font-black uppercase tracking-wider text-slate-700 hover:border-slate-900 hover:text-slate-900">Sửa</a>
                        </c:if>
                    </div>

                    <c:if test="${canManage}">
                        <div class="pt-2 flex flex-wrap gap-2">
                            <c:if test="${blog.status == 'pending' || blog.status == 'draft' || blog.status == 'rejected'}">
                                <form method="post" action="${pageContext.request.contextPath}/manager/blogs">
                                    <input type="hidden" name="blogId" value="${blog.blogId}">
                                    <input type="hidden" name="action" value="approve">
                                    <button type="submit" class="px-3 py-2 rounded-lg bg-[#008751] text-white text-xs font-black uppercase tracking-wider">Duyệt</button>
                                </form>
                            </c:if>
                            <c:if test="${blog.status == 'pending'}">
                                <form method="post" action="${pageContext.request.contextPath}/manager/blogs">
                                    <input type="hidden" name="blogId" value="${blog.blogId}">
                                    <input type="hidden" name="action" value="reject">
                                    <button type="submit" class="px-3 py-2 rounded-lg bg-amber-500 text-white text-xs font-black uppercase tracking-wider">Từ chối</button>
                                </form>
                            </c:if>
                            <form method="post" action="${pageContext.request.contextPath}/manager/blogs" onsubmit="return confirm('Xóa bài viết này?');">
                                <input type="hidden" name="blogId" value="${blog.blogId}">
                                <input type="hidden" name="action" value="delete">
                                <button type="submit" class="px-3 py-2 rounded-lg bg-red-500 text-white text-xs font-black uppercase tracking-wider">Xóa</button>
                            </form>
                        </div>
                    </c:if>
                </div>
            </article>
        </c:forEach>
    </section>

    <c:if test="${empty blogs}">
        <section class="bg-white rounded-3xl border-2 border-dashed border-slate-200 p-10 text-center">
            <p class="text-xs uppercase tracking-[0.2em] font-black text-slate-400">Không có bài viết phù hợp</p>
        </section>
    </c:if>

    <c:if test="${totalPages > 1}">
        <section class="flex items-center justify-center gap-2 py-3">
            <c:forEach begin="1" end="${totalPages}" var="i">
                <c:choose>
                    <c:when test="${i == currentPage}">
                        <span class="px-3 py-2 rounded-lg bg-[#008751] text-white text-sm font-bold">${i}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="${listPath}?page=${i}&status=${statusFilter}&q=${keyword}" class="px-3 py-2 rounded-lg border border-slate-200 text-sm font-semibold text-slate-700 hover:border-[#008751] hover:text-[#008751]">${i}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </section>
    </c:if>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
