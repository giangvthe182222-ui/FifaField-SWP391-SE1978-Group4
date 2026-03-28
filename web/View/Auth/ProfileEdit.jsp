<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa hồ sơ - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background:
                radial-gradient(circle at top left, rgba(16, 185, 129, 0.1), transparent 38%),
                radial-gradient(circle at 85% 85%, rgba(59, 130, 246, 0.08), transparent 34%),
                #f8fafc;
        }
    </style>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="max-w-4xl mx-auto px-4 sm:px-6 py-10 sm:py-14">
    <div class="bg-white/90 backdrop-blur rounded-[2rem] border border-white shadow-xl shadow-emerald-100/60 overflow-hidden">
        <div class="bg-gradient-to-r from-sky-600 via-cyan-600 to-emerald-500 px-6 sm:px-8 py-8 sm:py-10 text-white">
            <p class="text-[10px] sm:text-xs font-black tracking-[0.24em] uppercase text-white/80">Update Account</p>
            <h1 class="mt-2 text-2xl sm:text-3xl font-black tracking-tight">Chỉnh sửa hồ sơ</h1>
            <p class="mt-2 text-sm text-white/90">Cập nhật thông tin để đội ngũ hỗ trợ và lịch đặt sân luôn chính xác.</p>
        </div>

        <div class="p-6 sm:p-8 lg:p-10">
        <c:if test="${not empty error}">
            <div class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700 mb-6">
                ${error}
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/auth/profile/edit">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                    <label class="block text-[11px] font-black uppercase tracking-[0.16em] text-gray-500 mb-2">Họ tên</label>
                    <input type="text" name="fullName" value="${sessionScope.user.fullName}" class="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 font-semibold focus:outline-none focus:ring-4 focus:ring-emerald-100 focus:border-emerald-500 transition-all" required />
                </div>
                <div>
                    <label class="block text-[11px] font-black uppercase tracking-[0.16em] text-gray-500 mb-2">Số điện thoại</label>
                    <input type="tel" name="phone" value="${sessionScope.user.phone}" class="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 font-semibold focus:outline-none focus:ring-4 focus:ring-emerald-100 focus:border-emerald-500 transition-all" inputmode="numeric" pattern="[0-9]*" maxlength="15" oninput="this.value = this.value.replace(/\D/g, '')" title="Chỉ nhập số" />
                </div>
                <div>
                    <label class="block text-[11px] font-black uppercase tracking-[0.16em] text-gray-500 mb-2">Giới tính</label>
                    <select name="gender" class="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 font-semibold focus:outline-none focus:ring-4 focus:ring-emerald-100 focus:border-emerald-500 transition-all">
                        <option value="">-- Chọn --</option>
                        <option value="male" <c:if test="${sessionScope.user.gender == 'male'}">selected</c:if>>Nam</option>
                        <option value="female" <c:if test="${sessionScope.user.gender == 'female'}">selected</c:if>>Nữ</option>
                    </select>
                </div>
                <div class="md:col-span-2">
                    <label class="block text-[11px] font-black uppercase tracking-[0.16em] text-gray-500 mb-2">Địa chỉ</label>
                    <input type="text" name="address" value="${sessionScope.user.address}" class="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 font-semibold focus:outline-none focus:ring-4 focus:ring-emerald-100 focus:border-emerald-500 transition-all" />
                </div>
            </div>

            <div class="mt-8 flex flex-col sm:flex-row gap-3">
                <button type="submit" class="inline-flex justify-center items-center px-6 py-3 rounded-2xl bg-emerald-600 text-white font-black text-sm uppercase tracking-wider hover:bg-emerald-700 transition-colors">
                    Lưu thay đổi
                </button>
                <a href="${dashboardPath}" class="inline-flex justify-center items-center px-6 py-3 rounded-2xl bg-gray-100 text-gray-700 font-black text-sm uppercase tracking-wider hover:bg-gray-200 transition-colors">
                    Hủy
                </a>
            </div>
        </form>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
