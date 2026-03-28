<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background:
                radial-gradient(circle at top right, rgba(16, 185, 129, 0.12), transparent 42%),
                radial-gradient(circle at 10% 80%, rgba(59, 130, 246, 0.08), transparent 36%),
                #f8fafc;
        }
    </style>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="max-w-5xl mx-auto px-4 sm:px-6 py-10 sm:py-14">
    <div class="bg-white/90 backdrop-blur rounded-[2rem] border border-white shadow-xl shadow-emerald-100/60 overflow-hidden">
        <div class="bg-gradient-to-r from-emerald-600 via-emerald-500 to-teal-500 px-6 sm:px-8 py-8 sm:py-10 text-white">
            <p class="text-[10px] sm:text-xs font-black tracking-[0.24em] uppercase text-white/80">FIFAFIELD Account</p>
            <h1 class="mt-2 text-2xl sm:text-3xl font-black tracking-tight">Hồ sơ cá nhân</h1>
            <p class="mt-2 text-sm text-white/90">Quản lý thông tin cá nhân để đặt sân nhanh hơn và nhận hỗ trợ chính xác hơn.</p>
        </div>

        <div class="p-6 sm:p-8 lg:p-10">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-5">
                <div class="rounded-2xl border border-gray-100 bg-gray-50/70 p-4 sm:p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-gray-400">Họ tên</p>
                    <p class="mt-2 text-lg font-black text-gray-900">${sessionScope.user.fullName}</p>
                </div>
                <div class="rounded-2xl border border-gray-100 bg-gray-50/70 p-4 sm:p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-gray-400">Số điện thoại</p>
                    <p class="mt-2 text-base font-bold text-gray-900">${empty sessionScope.user.phone ? 'Chưa cập nhật' : sessionScope.user.phone}</p>
                </div>
                <div class="rounded-2xl border border-gray-100 bg-gray-50/70 p-4 sm:p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-gray-400">Email</p>
                    <p class="mt-2 text-base font-bold text-gray-900 break-all">${sessionScope.user.email}</p>
                </div>
                <div class="rounded-2xl border border-gray-100 bg-gray-50/70 p-4 sm:p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-gray-400">Giới tính</p>
                    <p class="mt-2 text-base font-bold text-gray-900">
                        <c:choose>
                            <c:when test="${sessionScope.user.gender == 'male'}">Nam</c:when>
                            <c:when test="${sessionScope.user.gender == 'female'}">Nữ</c:when>
                            <c:otherwise>Chưa cập nhật</c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <div class="md:col-span-2 rounded-2xl border border-gray-100 bg-gray-50/70 p-4 sm:p-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-gray-400">Địa chỉ</p>
                    <p class="mt-2 text-base font-bold text-gray-900">${empty sessionScope.user.address ? 'Chưa cập nhật' : sessionScope.user.address}</p>
                </div>
            </div>

            <div class="mt-8 flex flex-col sm:flex-row gap-3">
                <a href="${pageContext.request.contextPath}/auth/profile/edit" class="inline-flex justify-center items-center px-6 py-3 rounded-2xl bg-emerald-600 text-white font-black text-sm uppercase tracking-wider hover:bg-emerald-700 transition-colors">
                    Chỉnh sửa hồ sơ
                </a>
                <a href="${dashboardPath}" class="inline-flex justify-center items-center px-6 py-3 rounded-2xl bg-gray-100 text-gray-700 font-black text-sm uppercase tracking-wider hover:bg-gray-200 transition-colors">
                    Quay về dashboard
                </a>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
