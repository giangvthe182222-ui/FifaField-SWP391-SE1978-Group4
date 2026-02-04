<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa quản lý - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 min-h-screen" style="font-family: 'Inter', sans-serif;">

<jsp:include page="/View/Layout/Header.jsp" />

<!-- MAIN CONTENT -->
<main class="max-w-2xl mx-auto px-6 py-12">

    <div class="mb-8 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <button type="button" onclick="history.back()" class="px-3 py-2 rounded-lg border bg-white text-sm font-semibold hover:bg-slate-50">← Trở về</button>
        </div>
        <div>
            <h2 class="text-3xl font-bold text-slate-900">Chỉnh sửa thông tin quản lý</h2>
            <p class="text-slate-500 mt-2">Cập nhật thông tin cá nhân của quản lý</p>
        </div>
    </div>

    <!-- ERROR MESSAGE -->
    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
            <div>
                <h3 class="font-semibold text-red-800">Lỗi</h3>
                <p class="text-red-700 text-sm">${error}</p>
            </div>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty manager}">
            <div class="text-center py-12">
                <i data-lucide="user-x" class="w-16 h-16 text-slate-300 mx-auto mb-4"></i>
                <p class="text-slate-600">Không tìm thấy quản lý</p>
                <a href="${pageContext.request.contextPath}/manager-list" class="mt-4 inline-block text-emerald-600 hover:text-emerald-700">
                    Quay lại danh sách
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <!-- FORM -->
            <form method="POST" action="${pageContext.request.contextPath}/manager-edit" class="bg-white rounded-lg shadow-sm border border-slate-200 p-8">

                <!-- Hidden ID -->
                <input type="hidden" name="manager_id" value="${manager.userId}">

                <!-- FULL NAME -->
                <div class="mb-6">
                    <label for="fullName" class="block text-sm font-semibold text-slate-700 mb-2">Họ và tên</label>
                          <input type="text" id="fullName" name="fullName" required
                              value="${manager.fullName}"
                              pattern="^[A-Za-zÀ-ỹĐđƠơƯưẠ-ỹ\s.'-]+$"
                              title="Chỉ nhập chữ cái và khoảng trắng"
                              class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                              placeholder="Nhập họ tên">
                </div>

                <!-- PHONE -->
                <div class="mb-6">
                    <label for="phone" class="block text-sm font-semibold text-slate-700 mb-2">Số điện thoại</label>
                          <input type="tel" id="phone" name="phone" required
                              value="${manager.phone}"
                              pattern="^\d{9,15}$"
                              title="Số điện thoại chỉ gồm 9-15 chữ số"
                              class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                              placeholder="0123456789">
                </div>

                <!-- GENDER -->
                <div class="mb-6">
                    <label for="gender" class="block text-sm font-semibold text-slate-700 mb-2">Giới tính</label>
                    <select id="gender" name="gender" required
                            class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500">
                        <option value="">-- Chọn giới tính --</option>
                        <option value="Nam" <c:if test="${manager.gender == 'Nam'}">selected</c:if>>Nam</option>
                        <option value="Nữ" <c:if test="${manager.gender == 'Nữ'}">selected</c:if>>Nữ</option>
                        <option value="Khác" <c:if test="${manager.gender == 'Khác'}">selected</c:if>>Khác</option>
                    </select>
                </div>

                <!-- ADDRESS -->
                <div class="mb-6">
                    <label for="address" class="block text-sm font-semibold text-slate-700 mb-2">Địa chỉ</label>
                    <input type="text" id="address" name="address" required
                           value="${manager.address}"
                           class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                           placeholder="Nhập địa chỉ">
                </div>

                <!-- START DATE -->
                <div class="mb-8">
                    <label for="startDate" class="block text-sm font-semibold text-slate-700 mb-2">Ngày bắt đầu</label>
                    <input type="date" id="startDate" name="startDate" required
                           value="${manager.startDate}"
                           class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500">
                </div>

                <!-- LOCATION SELECT + READ-ONLY EMAIL -->
                <div class="bg-slate-50 rounded-lg p-6 mb-8 border border-slate-200">
                    <h3 class="font-semibold text-slate-700 mb-4 flex items-center gap-2">
                        <i data-lucide="map-pin" class="w-4 h-4"></i>
                        Vị trí quản lý
                    </h3>
                    <div class="mb-4">
                        <label for="locationId" class="block text-sm font-semibold text-slate-700 mb-2">Chọn vị trí</label>
                        <select id="locationId" name="locationId" required
                                class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500">
                            <option value="">-- Chọn vị trí --</option>
                            <c:forEach items="${locations}" var="loc">
                                <option value="${loc.locationId}" <c:if test="${loc.locationId == manager.locationId}">selected</c:if>>${loc.locationName} - ${loc.address}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <h3 class="font-semibold text-slate-700 mb-3 mt-6">Thông tin không thể chỉnh sửa</h3>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold mb-1">Email</p>
                            <p class="text-slate-700 font-medium">${manager.email}</p>
                        </div>
                    </div>
                </div>

                <!-- BUTTONS -->
                <div class="flex gap-4">
                    <button type="submit" 
                            class="flex-1 bg-emerald-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-emerald-700 transition flex items-center justify-center gap-2">
                        <i data-lucide="check" class="w-4 h-4"></i>
                        Cập nhật
                    </button>
                    <a href="${pageContext.request.contextPath}/manager-list"
                       class="flex-1 bg-slate-200 text-slate-700 px-6 py-3 rounded-lg font-semibold hover:bg-slate-300 transition flex items-center justify-center gap-2">
                        <i data-lucide="x" class="w-4 h-4"></i>
                        Hủy
                    </a>
                </div>

            </form>
        </c:otherwise>
    </c:choose>

</main>

<script>
    lucide.createIcons();
</script>

<jsp:include page="/View/Layout/Footer.jsp" />

</body>
</html>
