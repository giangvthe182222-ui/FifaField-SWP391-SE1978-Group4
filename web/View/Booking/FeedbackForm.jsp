<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gửi Đánh Giá - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800;900&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
    </style>
</head>
<body class="min-h-screen text-gray-900">
<jsp:include page="/View/Layout/HeaderCustomer.jsp"/>

<main class="max-w-3xl mx-auto px-6 py-12">
    <section class="bg-white rounded-[2rem] border border-gray-100 shadow-xl shadow-gray-200/40 p-8 md:p-10 space-y-8">
        <div class="space-y-2">
            <p class="text-[10px] font-black uppercase tracking-[0.25em] text-gray-400">FIFAFIELD FEEDBACK</p>
            <h1 class="text-3xl font-black tracking-tight uppercase">Gửi <span class="text-[#008751]">Đánh Giá</span></h1>
            <p class="text-sm font-semibold text-gray-500">Đơn đặt đã hoàn tất của bạn sẽ được ghi nhận ý kiến để nâng cao chất lượng dịch vụ.</p>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/customer/feedback" class="space-y-6">
            <input type="hidden" name="bookingId" value="${bookingId}">

            <div class="space-y-2">
                <label class="text-xs font-black uppercase tracking-widest text-gray-500">Số sao đánh giá</label>
                <select name="rating" required class="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white font-semibold">
                    <option value="">Chọn số sao</option>
                    <option value="5">5 sao - Rất hài lòng</option>
                    <option value="4">4 sao - Hài lòng</option>
                    <option value="3">3 sao - Bình thường</option>
                    <option value="2">2 sao - Chưa hài lòng</option>
                    <option value="1">1 sao - Rất tệ</option>
                </select>
            </div>

            <div class="space-y-2">
                <label class="text-xs font-black uppercase tracking-widest text-gray-500">Nhận xét</label>
                <textarea name="comment" rows="5" maxlength="1000" placeholder="Chia sẻ trải nghiệm của bạn..." class="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white font-semibold resize-y"></textarea>
            </div>

            <div class="flex flex-col sm:flex-row gap-3 pt-2">
                <button type="submit" class="px-8 py-4 rounded-2xl bg-[#008751] text-white font-black uppercase text-xs tracking-widest hover:bg-emerald-500 transition-colors">
                    Gửi đánh giá
                </button>
                <a href="${pageContext.request.contextPath}/customer/bookings" class="px-8 py-4 rounded-2xl border border-gray-200 text-gray-700 font-black uppercase text-xs tracking-widest text-center hover:border-[#008751] hover:text-[#008751] transition-colors">
                    Quay lại lịch sử
                </a>
            </div>
        </form>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
