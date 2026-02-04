<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Sửa cụm sân</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen">

<jsp:include page="/View/Layout/Header.jsp" />

<div class="flex items-center justify-center min-h-[calc(100vh-120px)]">
    <div class="bg-white w-full max-w-xl rounded-[2.5rem] p-10 shadow-2xl">
        <h1 class="text-3xl font-black text-slate-900 uppercase mb-2">Sửa Thông Tin Cụm Sân</h1>

        <c:if test="${not empty error}">
            <div class="bg-red-100 text-red-700 p-4 rounded-xl mb-6 font-mono text-sm">
                <c:out value="${error}" escapeXml="false"/>
            </div>
        </c:if>

        <form method="post" enctype="multipart/form-data" class="space-y-6">
            <input type="hidden" name="locationId" value="${location.locationId}"/>
            <input type="hidden" name="old_image" value="${location.imageUrl}"/>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Tên cụm sân</label>
                <input name="locationName" required value="${location.locationName}"
                       class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
            </div>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Địa chỉ</label>
                <input name="address" required value="${location.address}"
                       class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div>
                    <label class="text-xs font-black uppercase text-slate-400">Số điện thoại</label>
                    <input name="phoneNumber" required value="${location.phoneNumber}"
                           class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
                </div>

                <div>
                    <label class="text-xs font-black uppercase text-slate-400">Ảnh (Tải từ thiết bị)</label>
                    <input id="imageInputEdit" type="file" name="image" accept="image/*"
                           class="w-full px-5 py-3 mt-2 rounded-2xl bg-slate-50 border font-bold"/>

                    <div class="mt-4">
                        <c:choose>
                            <c:when test="${not empty location.imageUrl}">
                                <img id="previewEdit" src="${pageContext.request.contextPath}/${location.imageUrl}" alt="Ảnh hiện tại" class="w-full h-48 object-cover rounded-2xl" />
                            </c:when>
                            <c:otherwise>
                                <img id="previewEdit" src="${pageContext.request.contextPath}/assets/img/default_cluster.jpg" alt="Ảnh hiện tại" class="w-full h-48 object-cover rounded-2xl" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Quản lý</label>
                <select name="managerId" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                    <option value="">-- Không chọn --</option>
                    <c:forEach var="m" items="${managers}">
                        <option value="${m.userId}" ${location.managerId != null && location.managerId.toString() == m.userId.toString() ? 'selected' : ''}>${m.fullName}</option>
                    </c:forEach>
                </select>
            </div>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Trạng thái</label>
                <select name="status" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                    <option value="ACTIVE" ${location.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${location.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </div>

            <div class="flex gap-4 pt-4">
                <button type="button" onclick="history.back()" class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">Quay về</button>
                <a href="${pageContext.request.contextPath}/locations" class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">Hủy</a>
                <button class="flex-1 bg-[#008751] text-white py-4 rounded-2xl font-black uppercase shadow-xl">Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />

<script>
    const inputEdit = document.getElementById('imageInputEdit');
    const previewEdit = document.getElementById('previewEdit');

    if (inputEdit) {
        inputEdit.addEventListener('change', function (e) {
            const file = e.target.files && e.target.files[0];
            if (file) {
                const url = URL.createObjectURL(file);
                previewEdit.src = url;
            }
        });
    }
</script>

</body>
</html>
