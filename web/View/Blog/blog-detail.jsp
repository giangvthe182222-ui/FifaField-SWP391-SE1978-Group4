<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${blog.title} | Blog Detail</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 min-h-screen">

<c:choose>
    <c:when test="${roleName == 'manager'}"><jsp:include page="/View/Layout/HeaderManager.jsp"/></c:when>
    <c:when test="${roleName == 'staff'}"><jsp:include page="/View/Layout/HeaderStaff.jsp"/></c:when>
    <c:otherwise><jsp:include page="/View/Layout/HeaderCustomer.jsp"/></c:otherwise>
</c:choose>

<c:set var="backPath" value="${pageContext.request.contextPath}/blogs"/>
<c:if test="${roleName == 'staff'}">
    <c:set var="backPath" value="${pageContext.request.contextPath}/staff/blogs"/>
</c:if>
<c:if test="${roleName == 'manager'}">
    <c:set var="backPath" value="${pageContext.request.contextPath}/manager/blogs"/>
</c:if>

<main class="max-w-4xl mx-auto px-6 py-8 space-y-6">
    <a href="${backPath}" class="inline-flex items-center gap-2 text-sm font-bold text-slate-600 hover:text-[#008751]">← Quay lại danh sách</a>

    <article class="bg-white rounded-3xl border border-slate-200 overflow-hidden">
        <c:if test="${not empty blog.imageUrl}">
            <img src="${blog.imageUrl}" alt="${blog.title}" class="w-full h-80 object-cover">
        </c:if>
        <div class="p-7 space-y-4">
            <div class="flex items-center justify-between gap-3 flex-wrap">
                <span class="px-3 py-1 rounded-full text-[10px] uppercase tracking-widest font-black
                             ${blog.status == 'approved' ? 'bg-emerald-50 text-[#008751]' : blog.status == 'pending' ? 'bg-amber-50 text-amber-700' : blog.status == 'rejected' ? 'bg-red-50 text-red-600' : 'bg-slate-100 text-slate-500'}">${blog.status}</span>
                <span class="text-xs font-semibold text-slate-400">Ngày đăng: ${fn:substring(empty blog.publishedAt ? blog.createdAt : blog.publishedAt, 0, 10)}</span>
            </div>
            <h1 class="text-3xl font-black text-slate-900 leading-tight">${blog.title}</h1>
            <p class="text-base font-semibold text-slate-500">${blog.summary}</p>
            <p class="text-sm text-slate-400">Tác giả: <span class="font-bold">${blog.createdByName}</span></p>
            <div class="prose max-w-none text-slate-700 whitespace-pre-line">${blog.content}</div>

            <div class="pt-3 flex items-center gap-3">
                <form method="post" action="${pageContext.request.contextPath}/blog/like/toggle">
                    <input type="hidden" name="blogId" value="${blog.blogId}">
                    <button type="submit" class="px-4 py-2 rounded-xl ${blog.likedByCurrentUser ? 'bg-red-500 text-white' : 'bg-slate-100 text-slate-700'} text-sm font-black uppercase tracking-wider">${blog.likedByCurrentUser ? 'Đã tym' : 'Thả tym'} (${blog.likeCount})</button>
                </form>
                <span class="text-sm text-slate-500 font-semibold">${blog.commentCount} bình luận</span>
            </div>
        </div>
    </article>

    <section class="bg-white rounded-3xl border border-slate-200 p-6 space-y-4">
        <h2 class="text-xl font-black text-slate-900">Bình luận</h2>

        <form method="post" action="${pageContext.request.contextPath}/blog/comment/add" class="space-y-3">
            <input type="hidden" name="blogId" value="${blog.blogId}">
            <textarea name="content" rows="3" maxlength="1000" class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#008751]" placeholder="Nhập bình luận..."></textarea>
            <button type="submit" class="px-4 py-2 rounded-xl bg-slate-900 text-white text-xs font-black uppercase tracking-wider">Gửi bình luận</button>
        </form>

        <div class="space-y-3">
            <c:forEach var="comment" items="${comments}">
                <c:set var="indentClass" value="ml-0"/>
                <c:if test="${comment.depth == 1}"><c:set var="indentClass" value="ml-6"/></c:if>
                <c:if test="${comment.depth == 2}"><c:set var="indentClass" value="ml-12"/></c:if>
                <c:if test="${comment.depth == 3}"><c:set var="indentClass" value="ml-16"/></c:if>
                <c:if test="${comment.depth >= 4}"><c:set var="indentClass" value="ml-20"/></c:if>

                <c:set var="cardClass" value="border border-slate-100 rounded-2xl p-4 space-y-3"/>
                <c:if test="${comment.depth > 0}"><c:set var="cardClass" value="${cardClass} bg-slate-50"/></c:if>

                <div class="${cardClass} ${indentClass}">
                    <div class="flex items-center justify-between gap-2">
                        <p class="text-sm font-black text-slate-800">${comment.commenterName}</p>
                        <p class="text-xs font-semibold text-slate-400">${fn:substring(comment.createdAt, 0, 10)}</p>
                    </div>

                    <c:if test="${not empty comment.replyToName}">
                        <p class="text-xs text-slate-400">Trả lời <span class="font-bold">${comment.replyToName}</span></p>
                    </c:if>

                    <p class="mt-2 text-sm text-slate-600 whitespace-pre-line break-words">${comment.content}</p>

                    <details class="mt-2">
                        <summary class="cursor-pointer text-xs font-black uppercase tracking-wider text-[#008751]">Trả lời</summary>
                        <form method="post" action="${pageContext.request.contextPath}/blog/comment/add" class="mt-3 space-y-2">
                            <input type="hidden" name="blogId" value="${blog.blogId}">
                            <input type="hidden" name="parentCommentId" value="${comment.commentId}">
                            <textarea name="content" rows="2" maxlength="1000" class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#008751]" placeholder="Trả lời ${comment.commenterName}..."></textarea>
                            <button type="submit" class="px-3 py-2 rounded-xl bg-slate-900 text-white text-[10px] font-black uppercase tracking-wider">Gửi phản hồi</button>
                        </form>
                    </details>

                    <c:if test="${isManager || comment.ownedByCurrentUser}">
                        <form method="post" action="${pageContext.request.contextPath}/blog/comment/delete" class="mt-3" onsubmit="return confirm('Xóa bình luận này?');">
                            <input type="hidden" name="blogId" value="${blog.blogId}">
                            <input type="hidden" name="commentId" value="${comment.commentId}">
                            <button type="submit" class="text-xs font-black uppercase tracking-wider text-red-500 hover:text-red-700">Xóa bình luận</button>
                        </form>
                    </c:if>
                </div>
            </c:forEach>

            <c:if test="${empty comments}">
                <p class="text-sm font-semibold text-slate-400">Chưa có bình luận nào.</p>
            </c:if>
        </div>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
