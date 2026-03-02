<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa Voucher - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderAdmin.jsp"/>

<main class="max-w-4xl mx-auto px-6 py-12">
    <h1 class="text-2xl font-black mb-4">Chỉnh sửa Voucher</h1>

    <div class="bg-white rounded-xl shadow p-6">
        <c:if test="${not empty error}">
            <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded mb-4">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/voucher/edit">
            <input type="hidden" name="voucherId" value="${voucher.voucherId}" />
            <div class="grid grid-cols-1 gap-4">
                <div>
                    <label class="text-sm font-bold">Mã</label>
                    <input type="text" name="code" value="${voucher.code}" class="w-full p-2 border rounded" required />
                </div>
                <div>
                    <label class="text-sm font-bold">Giảm (số)</label>
                    <input type="number" step="0.01" name="discountValue" value="${voucher.discountValue}" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="text-sm font-bold">Mô tả</label>
                    <textarea name="description" class="w-full p-2 border rounded">${voucher.description}</textarea>
                </div>
                <div>
                    <label class="text-sm font-bold">Ngày bắt đầu</label>
                    <input type="date" name="startDate" value="${voucher.startDate}" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="text-sm font-bold">Ngày kết thúc</label>
                    <input type="date" name="endDate" value="${voucher.endDate}" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="text-sm font-bold">Trạng thái</label>
                    <select name="status" class="w-full p-2 border rounded">
                        <option value="active" <c:if test="${voucher.status == 'active'}">selected</c:if>>Active</option>
                        <option value="inactive" <c:if test="${voucher.status == 'inactive'}">selected</c:if>>Inactive</option>
                    </select>
                </div>
            </div>
            <div class="mt-4">
                <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded">Lưu</button>
                <a href="${pageContext.request.contextPath}/voucher/detail?id=${voucher.voucherId}" class="px-4 py-2 bg-gray-200 rounded">Hủy</a>
            </div>
        </form>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
