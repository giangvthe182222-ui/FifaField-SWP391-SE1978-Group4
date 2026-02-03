<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm Sân | FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen">

<jsp:include page="/View/Layout/Header.jsp" />

<div class="flex items-center justify-center min-h-[calc(100vh-120px)]">
    <div class="bg-white w-full max-w-xl rounded-[2.5rem] p-10 shadow-2xl">
        <h1 class="text-3xl font-black text-slate-900 uppercase mb-2">Thêm Sân Mới</h1>

        <c:if test="${not empty error}">
            <div class="bg-red-100 text-red-700 p-4 rounded-xl mb-6 font-mono text-sm">
                <c:out value="${error}" escapeXml="false"/>
            </div>
        </c:if>

        <form method="post" enctype="multipart/form-data" class="space-y-6">
            <c:choose>
                <c:when test="${not empty locationId}">
                    <input type="hidden" name="locationId" value="${locationId}"/>
                    <input type="hidden" name="location_id" value="${location_id}"/>
                    <div class="text-sm text-slate-500 mb-2">Thêm vào cụm: <strong>${not empty locationId ? locationId : ''}</strong></div>
                </c:when>
                <c:otherwise>
                    <label class="text-xs font-black uppercase text-slate-400">Chọn cụm sân</label>
                    <select name="locationId" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold mb-4">
                        <c:forEach var="loc" items="${locationsList}">
                            <option value="${loc.locationId}">${loc.locationName} - ${loc.address}</option>
                        </c:forEach>
                    </select>
                </c:otherwise>
            </c:choose>

            <div>
                  <label class="text-xs font-black uppercase text-slate-400">Tên sân</label>
                  <input name="fieldName" required value="${not empty fieldName ? fieldName : param.fieldName}"
                       class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div>
                    <label class="text-xs font-black uppercase text-slate-400">Loại sân</label>
                    <select name="fieldType" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                        <option value="7-a-side" ${fieldType == '7-a-side' || param.fieldType == '7-a-side' ? 'selected' : ''}>7-a-side</option>
                        <option value="11-a-side" ${fieldType == '11-a-side' || param.fieldType == '11-a-side' ? 'selected' : ''}>11-a-side</option>
                    </select>
                </div>

                <div>
                    <label class="text-xs font-black uppercase text-slate-400">Ảnh sân</label>
                    <input id="imageInputFieldAdd" type="file" name="image" accept="image/*"
                           class="w-full px-5 py-3 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
                    <div class="mt-4">
                        <img id="previewFieldAdd" src="" alt="Preview" class="w-full h-40 object-cover rounded-2xl hidden" />
                        <div id="previewFieldAddPlaceholder" class="w-full h-40 rounded-2xl bg-slate-50 border flex items-center justify-center text-slate-400 mt-2">Chưa chọn ảnh</div>
                    </div>
                </div>
            </div>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Trạng thái</label>
                <select name="status" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                    <option value="ACTIVE" ${status == 'ACTIVE' || param.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${status == 'INACTIVE' || param.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </div>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Tình trạng</label>
                <select name="condition" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                    <option value="GOOD" ${condition == 'GOOD' || param.condition == 'GOOD' ? 'selected' : ''}>GOOD</option>
                    <option value="FAIR" ${condition == 'FAIR' || param.condition == 'FAIR' ? 'selected' : ''}>FAIR</option>
                    <option value="BAD" ${condition == 'BAD' || param.condition == 'BAD' ? 'selected' : ''}>BAD</option>
                </select>
            </div>

            <div class="flex gap-4 pt-4">
                <c:choose>
                    <c:when test="${not empty locationId}">
                        <a href="${pageContext.request.contextPath}/fields?location_id=${locationId}"
                           class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">Hủy</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/locations"
                           class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">Hủy</a>
                    </c:otherwise>
                </c:choose>
                <button class="flex-1 bg-[#008751] text-white py-4 rounded-2xl font-black uppercase shadow-xl">Tạo sân</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />

<script>
    const inputFA = document.getElementById('imageInputFieldAdd');
    const previewFA = document.getElementById('previewFieldAdd');
    const phFA = document.getElementById('previewFieldAddPlaceholder');
    if (inputFA) {
        inputFA.addEventListener('change', function (e) {
            const file = e.target.files && e.target.files[0];
            if (file) {
                previewFA.src = URL.createObjectURL(file);
                previewFA.classList.remove('hidden');
                phFA.style.display = 'none';
            } else {
                previewFA.src = '';
                previewFA.classList.add('hidden');
                phFA.style.display = 'flex';
            }
        });
    }
</script>

</body>
</html>
