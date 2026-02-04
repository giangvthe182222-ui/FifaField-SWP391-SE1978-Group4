<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Voucher của cụm sân</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen">

<jsp:include page="/View/Layout/Header.jsp" />

<div class="max-w-4xl mx-auto p-8">
    <div class="bg-white rounded-3xl p-8 shadow-xl">
        <h1 class="text-2xl font-black">Vouchers cho cụm</h1>
        <div class="mt-6">
            <table class="w-full border-collapse">
                <thead>
                    <tr class="text-left">
                        <th class="p-2">Mã</th>
                        <th class="p-2">Giảm</th>
                        <th class="p-2">Mô tả</th>
                        <th class="p-2">Hạn</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${vouchers}" var="v">
                        <tr>
                            <td class="p-2">${v.code}</td>
                            <td class="p-2">${v.discountValue}</td>
                            <td class="p-2">${v.description}</td>
                            <td class="p-2">${v.startDate} - ${v.endDate}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty vouchers}">
                        <tr><td colspan="4" class="p-2 text-center text-slate-500">Không có voucher</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <div class="mt-8">
            <h2 class="font-bold">Thêm voucher cho cụm</h2>
            <form method="post" action="${pageContext.request.contextPath}/locations/vouchers" class="mt-4 grid grid-cols-2 gap-4">
                <input type="hidden" name="location_id" value="${locationId}" />
                <input name="code" placeholder="Mã voucher" class="p-3 border rounded" required />
                <input name="discount" placeholder="Ví dụ: 10.00" class="p-3 border rounded" />
                <input type="date" name="start_date" class="p-3 border rounded" />
                <input type="date" name="end_date" class="p-3 border rounded" />
                <textarea name="description" placeholder="Mô tả" class="p-3 border rounded col-span-2"></textarea>
                <button class="col-span-2 px-6 py-3 bg-[#008751] text-white rounded-xl font-black">Thêm voucher</button>
            </form>
        </div>

        <div class="mt-6">
            <a href="${pageContext.request.contextPath}/locations/view?location_id=${locationId}" class="px-4 py-2 border rounded">Quay lại</a>
        </div>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />

</body>
</html>
