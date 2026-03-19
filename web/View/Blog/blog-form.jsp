<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${mode == 'edit' ? 'Chỉnh sửa Blog' : 'Tạo Blog mới'} | FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50 min-h-screen">

<c:choose>
    <c:when test="${roleName == 'manager'}"><jsp:include page="/View/Layout/HeaderManager.jsp"/></c:when>
    <c:otherwise><jsp:include page="/View/Layout/HeaderStaff.jsp"/></c:otherwise>
</c:choose>

<c:set var="formAction" value="${pageContext.request.contextPath}/blog/create"/>
<c:set var="cancelPath" value="${pageContext.request.contextPath}/staff/blogs"/>
<c:if test="${mode == 'edit'}">
    <c:set var="formAction" value="${pageContext.request.contextPath}/blog/edit"/>
</c:if>
<c:if test="${roleName == 'manager'}">
    <c:set var="cancelPath" value="${pageContext.request.contextPath}/manager/blogs"/>
</c:if>

<main class="max-w-4xl mx-auto px-6 py-8">
    <section class="bg-white rounded-3xl border border-slate-200 p-7 space-y-5">
        <div>
            <p class="text-xs uppercase tracking-[0.2em] text-slate-400 font-black">Blog Editor</p>
            <h1 class="text-3xl font-black text-slate-900">${mode == 'edit' ? 'Chỉnh sửa bài viết' : 'Tạo bài viết mới'}</h1>
        </div>

        <c:if test="${not empty error}">
            <div class="rounded-xl bg-red-50 border border-red-200 px-4 py-3 text-sm font-bold text-red-600">${error}</div>
        </c:if>

        <form method="post" action="${formAction}" enctype="multipart/form-data" class="space-y-4">
            <c:if test="${mode == 'edit'}">
                <input type="hidden" name="blogId" value="${blog.blogId}">
            </c:if>
            <input type="hidden" name="existingImageUrl" value="${blog.imageUrl}">

            <div>
                <label class="block text-xs font-black uppercase tracking-widest text-slate-400 mb-2">Tiêu đề</label>
                <input type="text" name="title" value="${blog.title}" maxlength="255" required class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#008751]">
            </div>

            <div>
                <label class="block text-xs font-black uppercase tracking-widest text-slate-400 mb-2">Mô tả ngắn</label>
                <textarea name="summary" rows="2" maxlength="500" class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#008751]">${blog.summary}</textarea>
            </div>

            <div>
                <label class="block text-xs font-black uppercase tracking-widest text-slate-400 mb-2">Ảnh đại diện (Tải từ máy)</label>
                <input type="file" name="imageFile" accept="image/*" class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 file:mr-4 file:rounded-lg file:border-0 file:bg-slate-100 file:px-3 file:py-2 file:text-xs file:font-black file:uppercase file:tracking-wider">
                <p class="mt-2 text-xs text-slate-400 font-semibold">Định dạng hỗ trợ: jpg, jpeg, png, gif, webp. Dung lượng tối đa 5MB.</p>

                <c:if test="${not empty blog.imageUrl}">
                    <c:set var="isExternalImage" value="${fn:startsWith(blog.imageUrl, 'http://') or fn:startsWith(blog.imageUrl, 'https://') or fn:startsWith(blog.imageUrl, '//')}"/>
                    <c:set var="imageSrc" value="${isExternalImage ? blog.imageUrl : pageContext.request.contextPath.concat('/').concat(blog.imageUrl)}"/>
                    <div class="mt-3">
                        <p class="text-xs font-black uppercase tracking-widest text-slate-400 mb-2">Ảnh hiện tại</p>
                        <img src="${imageSrc}" alt="Ảnh blog hiện tại" class="w-full max-w-sm h-48 object-cover rounded-xl border border-slate-200">
                        <p class="mt-2 text-xs text-slate-400 font-semibold">Không chọn ảnh mới sẽ giữ ảnh hiện tại.</p>
                    </div>
                </c:if>
            </div>

            <div>
                <label class="block text-xs font-black uppercase tracking-widest text-slate-400 mb-2">Nội dung</label>
                <textarea name="content" rows="14" required class="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#008751]">${blog.content}</textarea>
            </div>

            <div class="flex flex-wrap gap-3 pt-2">
                <button type="submit" name="submitType" value="save" class="px-5 py-3 rounded-xl border border-slate-300 text-slate-700 text-xs font-black uppercase tracking-wider hover:border-slate-600">Lưu nháp</button>
                <c:choose>
                    <c:when test="${roleName == 'manager'}">
                        <button type="submit" name="submitType" value="publish" class="px-5 py-3 rounded-xl bg-[#008751] text-white text-xs font-black uppercase tracking-wider hover:bg-[#006f43]">Đăng bài</button>
                    </c:when>
                    <c:otherwise>
                        <button type="submit" name="submitType" value="submit" class="px-5 py-3 rounded-xl bg-[#008751] text-white text-xs font-black uppercase tracking-wider hover:bg-[#006f43]">Gửi manager duyệt</button>
                    </c:otherwise>
                </c:choose>
                <a href="${cancelPath}" class="px-5 py-3 rounded-xl border border-slate-200 text-slate-500 text-xs font-black uppercase tracking-wider">Hủy</a>
            </div>
        </form>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
