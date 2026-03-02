<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Thêm Voucher - Cụm sân - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen">

<jsp:include page="/View/Layout/HeaderAdmin.jsp" />

<div class="max-w-4xl mx-auto p-8">
    <div class="bg-white rounded-3xl p-8 shadow-xl">
        <div class="mb-4">
            <button type="button" onclick="history.back()" class="px-3 py-2 rounded-lg border bg-white text-sm font-semibold hover:bg-slate-50">← Quay lại</button>
        </div>
        
        <h1 class="text-2xl font-black">Thêm Voucher cho Cụm sân</h1>

        <c:if test="${not empty error}">
            <div class="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">⚠️ ${error}</div>
        </c:if>

        <div class="mt-6">
            <form method="post" action="${pageContext.request.contextPath}/locations/vouchers/add" class="grid grid-cols-2 gap-4" onsubmit="return validateVoucher()">
                <input type="hidden" name="location_id" value="${param.location_id}" />
                
                <div class="col-span-2">
                    <label class="block text-sm font-semibold mb-1">Tên Voucher *</label>
                    <input type="text" name="name" placeholder="Ví dụ: Giảm Hè 2026" class="w-full p-3 border rounded" required />
                </div>

                <div>
                    <label class="block text-sm font-semibold mb-1">Mã Voucher *</label>
                    <input type="text" name="code" placeholder="Ví dụ: SUMMER2026" class="w-full p-3 border rounded" required />
                </div>

                <div>
                    <label class="block text-sm font-semibold mb-1">Phần Trăm Giảm Giá (%) *</label>
                    <input type="number" name="discount" placeholder="Ví dụ: 10" min="1" max="100" step="0.01" class="w-full p-3 border rounded" required />
                </div>

                <div>
                    <label class="block text-sm font-semibold mb-1">Ngày Bắt Đầu *</label>
                    <input type="date" name="start_date" class="w-full p-3 border rounded" required />
                </div>

                <div>
                    <label class="block text-sm font-semibold mb-1">Ngày Kết Thúc *</label>
                    <input type="date" name="end_date" class="w-full p-3 border rounded" required />
                </div>

                <button type="submit" class="col-span-2 px-6 py-3 bg-[#008751] text-white rounded-xl font-bold hover:bg-[#006a3f]">
                    ➕ Thêm Voucher
                </button>
            </form>
        </div>

    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />

<script>
function validateVoucher() {
    const startDate = new Date(document.querySelector('input[name="start_date"]').value);
    const endDate = new Date(document.querySelector('input[name="end_date"]').value);
    if (endDate <= startDate) { alert('❌ Ngày kết thúc phải sau ngày bắt đầu!'); return false; }
    return true;
}
</script>

</body>
</html>
