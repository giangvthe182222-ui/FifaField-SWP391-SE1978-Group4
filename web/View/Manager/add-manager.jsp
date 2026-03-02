<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Quản Lý - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 min-h-screen" style="font-family: 'Inter', sans-serif;">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<!-- MAIN CONTENT -->
<main class="max-w-2xl mx-auto px-6 py-12">

    <div class="mb-8 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <button type="button" onclick="history.back()" class="px-3 py-2 rounded-lg border bg-white text-sm font-semibold hover:bg-slate-50">← Trở về</button>
        </div>
        <div>
            <h2 class="text-3xl font-bold text-slate-900">Thêm quản lý mới</h2>
            <p class="text-slate-500 mt-2">Tạo tài khoản quản lý cho vị trí quản lý</p>
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

    <!-- FORM -->
    <form method="POST" action="${pageContext.request.contextPath}/add-manager" class="bg-white rounded-lg shadow-sm border border-slate-200 p-8">

        <!-- FULL NAME -->
        <div class="mb-6">
            <label for="fullName" class="block text-sm font-semibold text-slate-700 mb-2">Họ và tên</label>
                 <input type="text" id="fullName" name="fullName" required
                     pattern="^[A-Za-zÀ-ỹĐđƠơƯưẠ-ỹ\s.'-]+$"
                     title="Chỉ nhập chữ cái và khoảng trắng"
                     class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                     placeholder="Nhập họ tên">
        </div>

        <!-- EMAIL -->
        <div class="mb-6">
            <label for="email" class="block text-sm font-semibold text-slate-700 mb-2">Email</label>
                 <input type="email" id="email" name="email" required
                     pattern="^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"
                     title="Định dạng email không hợp lệ"
                     class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                     placeholder="example@gmail.com">
        </div>

        <!-- PHONE -->
        <div class="mb-6">
            <label for="phone" class="block text-sm font-semibold text-slate-700 mb-2">Số điện thoại</label>
                 <input type="tel" id="phone" name="phone" required
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
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
                <option value="Khác">Khác</option>
            </select>
        </div>

        <!-- ADDRESS -->
        <div class="mb-6">
            <label for="address" class="block text-sm font-semibold text-slate-700 mb-2">Địa chỉ</label>
            <input type="text" id="address" name="address" required
                   class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                   placeholder="Nhập địa chỉ">
        </div>

        <!-- PASSWORD -->
        <div class="mb-6">
            <label for="password" class="block text-sm font-semibold text-slate-700 mb-2">Mật khẩu (tối đa 20 ký tự)</label>
            <input type="password" id="password" name="password" required maxlength="20"
                   class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                   placeholder="Nhập mật khẩu">
        </div>

        <!-- CONFIRM PASSWORD -->
        <div class="mb-6">
            <label for="confirmPassword" class="block text-sm font-semibold text-slate-700 mb-2">Xác nhận mật khẩu</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required maxlength="20"
                   class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                   placeholder="Nhập lại mật khẩu">
        </div>

        <!-- START DATE -->
        <div class="mb-6">
            <label for="startDate" class="block text-sm font-semibold text-slate-700 mb-2">Ngày bắt đầu</label>
            <input type="date" id="startDate" name="startDate" required
                   class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500">
        </div>

        <!-- LOCATION -->
        <div class="mb-8">
            <label for="locationId" class="block text-sm font-semibold text-slate-700 mb-2">Vị trí quản lý</label>
            <select id="locationId" name="locationId" required
                    class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500">
                <option value="">-- Chọn vị trí --</option>
                <c:forEach items="${locations}" var="loc">
                    <option value="${loc.locationId}">${loc.locationName} - ${loc.address}</option>
                </c:forEach>
            </select>
        </div>

        <!-- BUTTONS -->
        <div class="flex gap-4">
            <button type="submit" 
                    class="flex-1 bg-emerald-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-emerald-700 transition flex items-center justify-center gap-2">
                <i data-lucide="check" class="w-4 h-4"></i>
                Thêm quản lý
            </button>
            <a href="${pageContext.request.contextPath}/manager-list"
               class="flex-1 bg-slate-200 text-slate-700 px-6 py-3 rounded-lg font-semibold hover:bg-slate-300 transition flex items-center justify-center gap-2">
                <i data-lucide="x" class="w-4 h-4"></i>
                Hủy
            </a>
        </div>

    </form>

</main>

<script>
    lucide.createIcons();
</script>

<jsp:include page="/View/Layout/Footer.jsp" />

</body>
</html>
