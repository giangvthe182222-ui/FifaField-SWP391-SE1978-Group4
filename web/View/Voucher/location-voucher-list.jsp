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
        <div class="mb-4">
            <button type="button" onclick="history.back()" class="px-3 py-2 rounded-lg border bg-white text-sm font-semibold hover:bg-slate-50">← Quay lại</button>
        </div>
        
        <h1 class="text-2xl font-black">Danh sách Voucher</h1>

        <!-- ERROR MESSAGE -->
        <c:if test="${not empty error}">
            <div class="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
                ⚠️ ${error}
            </div>
        </c:if>

        <!-- SUCCESS MESSAGE -->
        <c:if test="${not empty success}">
            <div class="mt-4 p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">
                ✅ ${success}
            </div>
        </c:if>

        <div class="mt-6">
            <table class="w-full border-collapse">
                <thead>
                    <tr class="text-left bg-slate-50">
                        <th class="p-3 font-semibold">Tên Voucher</th>
                        <th class="p-3 font-semibold">Mã</th>
                        <th class="p-3 font-semibold">Giảm (%)</th>
                        <th class="p-3 font-semibold">Từ - Đến</th>
                        <th class="p-3 font-semibold">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty vouchers}">
                            <tr><td colspan="4" class="p-3 text-center text-slate-500">Chưa có voucher nào</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${vouchers}" var="v">
                                <tr class="border-t">
                                    <td class="p-3">${v.description}</td>
                                    <td class="p-3 font-mono">${v.code}</td>
                                    <td class="p-3">${v.discountValue}%</td>
                                    <td class="p-3 text-sm text-slate-600">${v.startDate} → ${v.endDate}</td>
                                    <td class="p-3">
                                        <a href="${pageContext.request.contextPath}/voucher/detail?id=${v.voucherId}" class="px-3 py-1 bg-blue-600 text-white rounded text-sm">Xem</a>
                                        <a href="${pageContext.request.contextPath}/voucher/edit?id=${v.voucherId}" class="px-3 py-1 bg-green-600 text-white rounded text-sm">Sửa</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <div class="mt-8">
            <a href="${pageContext.request.contextPath}/locations/vouchers/add?location_id=${locationId}" class="px-6 py-3 bg-[#008751] text-white rounded-xl font-bold hover:bg-[#006a3f]">
                ➕ Thêm Voucher
            </a>
        </div>

        <div class="mt-6">
            <a href="${pageContext.request.contextPath}/locations/view?location_id=${locationId}" class="px-4 py-2 border rounded inline-block hover:bg-slate-50">← Quay lại cụm sân</a>
        </div>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />

</body>
</html>
