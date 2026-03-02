<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>Sửa sân</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <style>
            /* small helper to keep image preview tidy */
            .img-preview {
                height: 220px;
                object-fit: cover;
                border-radius: 12px;
            }
        </style>
    </head>
    <body class="bg-slate-100 min-h-screen">

        <jsp:include page="/View/Layout/HeaderAdmin.jsp" />

        <div class="flex items-center justify-center min-h-[calc(100vh-120px)]">
            <div class="bg-white w-full max-w-xl rounded-[2.5rem] p-10 shadow-2xl">
                <h1 class="text-3xl font-black text-slate-900 uppercase mb-2">Sửa Thông Tin Sân</h1>

                <c:if test="${not empty error}">
                    <div class="bg-red-100 text-red-700 p-4 rounded-xl mb-6 font-mono text-sm">
                        <c:out value="${error}" escapeXml="false"/>
                    </div>
                </c:if>

                <form method="post" enctype="multipart/form-data" class="space-y-6">
                    <input type="hidden" name="field_id" value="${field.fieldId}"/>
                    <input type="hidden" name="old_image" value="${field.imageUrl}"/>

                    <div>
                        <label class="text-xs font-black uppercase text-slate-400">Tên sân</label>
                        <input name="fieldName" required value="${field.fieldName}"
                               class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <label class="text-xs font-black uppercase text-slate-400">Loại sân</label>
                            <select name="fieldType" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                                <option value="7-a-side" ${field.fieldType == '7-a-side' ? 'selected' : ''}>Sân 7</option>
                                <option value="11-a-side" ${field.fieldType == '11-a-side' ? 'selected' : ''}>Sân 11</option>
                            </select>
                        </div>

                        <div>
                            <label class="text-xs font-black uppercase text-slate-400">Tình trạng</label>
                            <select name="condition" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                                <option value="Good" ${field.fieldCondition == 'Good' ? 'selected' : ''}>Good</option>
                                <option value="Average" ${field.fieldCondition == 'Average' ? 'selected' : ''}>Average</option>
                                <option value="Bad" ${field.fieldCondition == 'Bad' ? 'selected' : ''}>Bad</option>
                            </select>
                        </div>
                    </div>

                    <div>
                        <label class="text-xs font-black uppercase text-slate-400">Trạng thái</label>
                        <select name="status" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                            <option value="ACTIVE" ${field.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                            <option value="INACTIVE" ${field.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                        </select>
                    </div>

                    <div>
                        <label class="text-xs font-black uppercase text-slate-400">Ảnh (Tải từ thiết bị)</label>
                        <input id="imageInputEditField" type="file" name="image" accept="image/*"
                               class="w-full px-5 py-3 mt-2 rounded-2xl bg-slate-50 border font-bold"/>

                        <div class="mt-4">
                            <c:choose>
                                <c:when test="${not empty field.imageUrl}">
                                    <a href="${pageContext.request.contextPath}/${field.imageUrl}" target="_blank">
                                        <img id="previewEditField" src="${pageContext.request.contextPath}/${field.imageUrl}" alt="Ảnh hiện tại" class="w-full img-preview" />
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <img id="previewEditField" src="${pageContext.request.contextPath}/assets/img/default_field.jpg" alt="Ảnh hiện tại" class="w-full img-preview" />
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="flex gap-4 pt-4">
                        <a href="${pageContext.request.contextPath}/fields?location_id=${field.locationId}" class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">Hủy</a>
                        <button class="flex-1 bg-[#008751] text-white py-4 rounded-2xl font-black uppercase shadow-xl">Lưu thay đổi</button>
                    </div>
                </form>
            </div>
        </div>

        <jsp:include page="/View/Layout/Footer.jsp" />

        <script>
            const inputEditField = document.getElementById('imageInputEditField');
            const previewEditField = document.getElementById('previewEditField');

            if (inputEditField) {
                inputEditField.addEventListener('change', function (e) {
                    const file = e.target.files && e.target.files[0];
                    if (file) {
                        const url = URL.createObjectURL(file);
                        previewEditField.src = url;
                    }
                });
            }
        </script>

    </body>
</html>
