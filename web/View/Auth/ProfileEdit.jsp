<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa hồ sơ - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="max-w-3xl mx-auto px-6 py-12">
    <h1 class="text-2xl font-black mb-4">Chỉnh sửa hồ sơ</h1>

    <div class="bg-white rounded-xl shadow p-6">
        <c:if test="${not empty error}">
            <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded mb-4">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/auth/profile/edit">
            <div class="grid grid-cols-1 gap-4">
                <div>
                    <label class="text-sm font-bold">Họ tên</label>
                    <input type="text" name="fullName" value="${sessionScope.user.fullName}" class="w-full p-2 border rounded" required />
                </div>
                <div>
                    <label class="text-sm font-bold">Số điện thoại</label>
                    <input type="text" name="phone" value="${sessionScope.user.phone}" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="text-sm font-bold">Địa chỉ</label>
                    <input type="text" name="address" value="${sessionScope.user.address}" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="text-sm font-bold">Giới tính</label>
                    <select name="gender" class="w-full p-2 border rounded">
                        <option value="">-- Chọn --</option>
                        <option value="male" <c:if test="${sessionScope.user.gender == 'male'}">selected</c:if>>Nam</option>
                        <option value="female" <c:if test="${sessionScope.user.gender == 'female'}">selected</c:if>>Nữ</option>
                    </select>
                </div>
            </div>
            <div class="mt-4">
                <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded">Lưu</button>
                <a href="${pageContext.request.contextPath}/auth/profile" class="px-4 py-2 bg-gray-200 rounded">Hủy</a>
            </div>
        </form>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
