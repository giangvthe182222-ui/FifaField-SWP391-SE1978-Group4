<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách sân - Staff</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="max-w-6xl mx-auto px-6 py-10 space-y-8">
    <div class="space-y-2">
        <h1 class="text-4xl font-black uppercase tracking-tight leading-none">SÂN CỦA <span class="text-[#008751]">${locationName}</span></h1>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.25em]">Danh sách sân thuộc chi nhánh staff đang phụ trách</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <c:forEach var="f" items="${fields}">
            <article class="bg-white border border-gray-100 rounded-3xl shadow-sm p-6 flex gap-5">
                <div class="w-28 h-28 rounded-2xl overflow-hidden bg-slate-100 shrink-0">
                    <c:choose>
                        <c:when test="${not empty f.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${f.imageUrl}" alt="Ảnh sân" class="w-full h-full object-cover"/>
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/assets/img/default_field.jpg" alt="Ảnh sân" class="w-full h-full object-cover"/>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="flex-1 min-w-0 space-y-3">
                    <h2 class="text-xl font-black uppercase tracking-tight truncate">${f.fieldName}</h2>
                    <p class="text-xs font-bold text-gray-500 uppercase tracking-wider">Loại: ${f.fieldType}</p>
                    <p class="text-xs font-bold text-gray-500 uppercase tracking-wider">Tình trạng: ${f.fieldCondition}</p>
                    <div class="flex flex-wrap gap-2 pt-2">
                        <a href="${pageContext.request.contextPath}/staff/fields/detail?fieldId=${f.fieldId}" class="px-4 py-2 bg-[#008751] text-white rounded-xl text-[10px] font-black uppercase tracking-widest hover:bg-emerald-400 transition-all">Xem chi tiết lịch</a>
                    </div>
                </div>
            </article>
        </c:forEach>
    </div>

    <c:if test="${empty fields}">
        <div class="bg-white rounded-3xl border-2 border-dashed border-gray-100 p-16 text-center">
            <p class="text-gray-400 text-sm font-bold uppercase tracking-[0.2em]">Không có sân nào trong chi nhánh này.</p>
        </div>
    </c:if>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>
