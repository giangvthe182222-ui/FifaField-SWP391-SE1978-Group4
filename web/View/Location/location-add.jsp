<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm Cụm Sân | FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>

<body class="bg-slate-100 min-h-screen">

    <!-- HEADER -->
    <jsp:include page="/View/Layout/Header.jsp"/>

    <!-- CONTENT -->
    <div class="flex items-center justify-center min-h-[calc(100vh-120px)]">

        <div class="bg-white w-full max-w-xl rounded-[2.5rem] p-10 shadow-2xl">
            <h1 class="text-3xl font-black text-slate-900 uppercase mb-2">
                Thêm Cụm Sân Mới
            </h1>
            <p class="text-slate-400 text-xs font-bold uppercase tracking-widest mb-8">
                Khởi tạo hạ tầng FIFA FIELD
            </p>

            <!-- HIỂN THỊ LỖI -->
            <c:if test="${not empty error}">
                <div class="bg-red-100 text-red-700 p-4 rounded-xl mb-6 font-mono text-sm">
                    <c:out value="${error}" escapeXml="false"/>
                </div>
            </c:if>

            <form method="post" class="space-y-6">

                <div>
                    <label class="text-xs font-black uppercase text-slate-400">
                        Tên cụm sân
                    </label>
                    <input name="locationName" required
                           value="${param.locationName}"
                           class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"
                           placeholder="VD: FIFA FIELD Thanh Xuân"/>
                </div>

                <div>
                    <label class="text-xs font-black uppercase text-slate-400">
                        Địa chỉ
                    </label>
                    <input name="address" required
                           value="${param.address}"
                           class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"
                           placeholder="Nhập địa chỉ cụ thể"/>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="text-xs font-black uppercase text-slate-400">
                            Số điện thoại
                        </label>
                        <input name="phoneNumber" required
                               value="${param.phoneNumber}"
                               class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"
                               placeholder="0123456789"/>
                    </div>

                    <div>
                        <label class="text-xs font-black uppercase text-slate-400">
                            Ảnh (URL)
                        </label>
                        <input name="imageUrl"
                               value="${param.imageUrl}"
                               class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"
                               placeholder="default_cluster.jpg"/>
                    </div>
                </div>

                <div>
                    <label class="text-xs font-black uppercase text-slate-400">
                        Trạng thái
                    </label>
                    <input name="status"
                           value="${param.status}"
                           class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"
                           placeholder="ACTIVE"/>
                </div>

                <div class="flex gap-4 pt-4">
                    <a href="${pageContext.request.contextPath}/locations"
                       class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">
                        Hủy
                    </a>

                    <button class="flex-1 bg-[#008751] text-white py-4 rounded-2xl font-black uppercase shadow-xl">
                        Tạo Cụm Sân
                    </button>
                </div>

            </form>
        </div>
    </div>

    <!-- FOOTER -->
    <jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>
