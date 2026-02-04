<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Danh sách sân</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-100 min-h-screen">
<jsp:include page="/View/Layout/Header.jsp" />

<div class="max-w-4xl mx-auto p-8">
    <div class="flex items-center justify-between mb-6">
        <h1 class="text-3xl font-black">Danh sách sân</h1>
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/fields/add?location_id=${locationId}" class="px-5 py-3 bg-[#008751] text-white rounded-xl font-black">Thêm sân</a>
            <a href="${pageContext.request.contextPath}/locations/view?location_id=${locationId}" class="px-4 py-3 border rounded-xl font-black">Quay về cụm</a>
        </div>
    </div>

    <div class="space-y-6">
        <c:forEach var="f" items="${fields}">
            <div class="bg-white rounded-3xl p-6 shadow-xl">
                <div class="flex items-center gap-6">
                    <div class="w-48 h-48 bg-slate-100 rounded-2xl overflow-hidden">
                        <c:choose>
                            <c:when test="${not empty f.imageUrl}">
                                <img src="${pageContext.request.contextPath}/${f.imageUrl}" class="w-full h-full object-cover" />
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/assets/img/default_field.jpg" class="w-full h-full object-cover" />
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="flex-1">
                        <h2 class="text-2xl font-black">${f.fieldName}</h2>
                        <p class="text-sm text-slate-500 mt-1">Loại: <span class="font-bold">${f.fieldType == '7-a-side' ? 'Sân 7' : (f.fieldType == '11-a-side' ? 'Sân 11' : f.fieldType)}</span></p>
                        <p class="text-sm text-slate-500">Tình trạng: <span class="font-bold">${f.fieldCondition}</span></p>

                        <div class="mt-4 flex gap-3">
                            <a href="${pageContext.request.contextPath}/field-schedule?fieldId=${f.fieldId}" class="px-5 py-3 bg-slate-50 rounded-xl border font-black">Lịch</a>
                            <a href="${pageContext.request.contextPath}/fields/view?field_id=${f.fieldId}" class="px-5 py-3 bg-white border rounded-xl font-black">Chi tiết</a>
                            <a href="${pageContext.request.contextPath}/fields/edit?field_id=${f.fieldId}" class="px-5 py-3 bg-[#008751] text-white rounded-xl font-black">Sửa sân</a>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty fields}">
            <div class="mt-12 text-center text-slate-500">Chưa có sân trong cụm này.</div>
        </c:if>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />
</body>
</html>
