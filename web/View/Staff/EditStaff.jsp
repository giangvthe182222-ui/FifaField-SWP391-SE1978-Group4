<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/Header.jsp"/>

<main class="max-w-4xl mx-auto px-6 py-12">
    <div class="bg-white rounded-xl shadow p-8">
        <h2 class="text-2xl font-bold mb-4">Chỉnh sửa: ${staff.fullName}</h2>

        <c:if test="${not empty error}">
            <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded mb-4">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/staff/edit">
            <input type="hidden" name="userId" value="${staff.userId}" />
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <label class="text-sm font-bold">Họ tên</label>
                    <input type="text" name="fullName" value="${staff.fullName}" class="w-full p-2 border rounded" required />
                </div>
                <div>
                    <label class="text-sm font-bold">Số điện thoại</label>
                    <input type="text" name="phone" value="${staff.phone}" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="text-sm font-bold">Mã nhân viên</label>
                    <input type="text" name="employeeCode" value="${staff.employeeCode}" class="w-full p-2 border rounded" required />
                </div>
                <div>
                    <label class="text-sm font-bold">Ngày gia nhập</label>
                    <input type="date" name="hireDate" value="${staff.hireDate}" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="text-sm font-bold">Trạng thái</label>
                    <select name="status" class="w-full p-2 border rounded" required>
                        <option value="active" <c:if test="${staff.status == 'active'}">selected</c:if>>Active</option>
                        <option value="inactive" <c:if test="${staff.status == 'inactive'}">selected</c:if>>Inactive</option>
                    </select>
                </div>
                <div>
                    <label class="text-sm font-bold">Cơ sở</label>
                    <select name="locationId" class="w-full p-2 border rounded" required>
                        <option value="">-- Chọn cơ sở --</option>
                        <c:forEach items="${locations}" var="loc">
                            <option value="${loc.locationId}" <c:if test="${loc.locationId == staff.locationId}">selected</c:if>>${loc.locationName}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="md:col-span-2">
                    <label class="text-sm font-bold">Địa chỉ</label>
                    <input type="text" name="address" value="${staff.address}" class="w-full p-2 border rounded" />
                </div>
            </div>
            <div class="mt-4 flex gap-3">
                <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded">Lưu</button>
                <a href="${pageContext.request.contextPath}/staff/list" class="px-4 py-2 bg-gray-200 rounded">Hủy</a>
            </div>
        </form>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
