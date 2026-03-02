<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Chi tiết sân</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen">
<jsp:include page="/View/Layout/HeaderAdmin.jsp" />

<div class="max-w-4xl mx-auto p-8">
    <div class="bg-white rounded-3xl p-8 shadow-xl">
        <div class="flex items-start gap-6">
            <div class="w-48 h-48 bg-slate-100 rounded-2xl overflow-hidden">
                <c:choose>
                    <c:when test="${not empty field.imageUrl}">
                        <img src="${pageContext.request.contextPath}/${field.imageUrl}" alt="Ảnh sân" class="w-full h-full object-cover"/>
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/assets/img/default_field.jpg" alt="Ảnh sân" class="w-full h-full object-cover"/>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="flex-1">
                <h1 class="text-3xl font-black">${field.fieldName}</h1>
                <p class="text-sm text-slate-500 mt-2">Loại: <span class="font-bold">${field.fieldType == '7-a-side' ? 'Sân 7' : (field.fieldType == '11-a-side' ? 'Sân 11' : field.fieldType)}</span></p>
                <p class="text-sm text-slate-500 mt-1">Tình trạng: <span class="font-bold">${field.fieldCondition}</span></p>
                <div class="mt-4 flex gap-4">
                    <div class="px-4 py-2 bg-slate-50 rounded-xl border">Trạng thái: ${field.status}</div>
                    <div class="px-4 py-2 bg-slate-50 rounded-xl border">Thuộc cụm: <a href="${pageContext.request.contextPath}/locations/view?location_id=${field.locationId}" class="font-black">Xem</a></div>
                </div>

                <div class="mt-6 flex gap-3">
                    <a href="${pageContext.request.contextPath}/field-schedule?fieldId=${field.fieldId}"
                       class="px-6 py-3 bg-slate-50 rounded-xl border font-black">Lịch</a>

                    <a href="${pageContext.request.contextPath}/fields/edit?field_id=${field.fieldId}"
                       class="px-6 py-3 bg-[#008751] text-white rounded-xl font-black">Sửa sân</a>

                    <a href="${pageContext.request.contextPath}/fields?location_id=${field.locationId}"
                       class="px-6 py-3 border rounded-xl font-black">Quay về danh sách</a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />
</body>
</html>
