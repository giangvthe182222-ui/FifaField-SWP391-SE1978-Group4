<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
            <h1 class="text-3xl font-black tracking-tight uppercase">
                <c:choose>
                    <c:when test="${isEditMode}">Sửa <span class="text-[#008751]">Đánh Giá</span></c:when>
                    <c:otherwise>Gửi <span class="text-[#008751]">Đánh Giá</span></c:otherwise>
                </c:choose>
            </h1>
            <p class="text-sm font-semibold text-gray-500">Chỉ booking đã hoàn tất mới có thể được đánh giá. Bạn cũng có thể sửa hoặc xóa feedback đã gửi.</p>
        </div>

        <c:if test="${not empty booking}">
            <div class="rounded-3xl border border-gray-100 bg-gray-50 p-5 space-y-2">
                <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Thông tin đơn đặt</p>
                <p class="text-lg font-black text-gray-900 uppercase">${booking.fieldName}</p>
                <p class="text-sm font-semibold text-gray-600">${booking.bookingDate} | ${booking.startTime} - ${booking.endTime}</p>
                <p class="text-xs font-black uppercase tracking-widest text-emerald-700">Trạng thái: ${booking.status}</p>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/customer/feedback" class="space-y-6">
            <input type="hidden" name="bookingId" value="${bookingId}">

            <div class="space-y-2">
                <label class="text-xs font-black uppercase tracking-widest text-gray-500">Số sao đánh giá</label>
                <div id="rating-widget" class="rounded-2xl border border-gray-200 bg-white px-4 py-4">
                    <input type="hidden" id="ratingInput" name="rating" value="${feedback != null ? feedback.rating : ''}">
                    <div class="flex items-center gap-2 text-3xl leading-none select-none">
                        <button type="button" data-value="1" class="rating-star text-gray-200 hover:scale-110 transition" aria-label="1 sao">★</button>
                        <button type="button" data-value="2" class="rating-star text-gray-200 hover:scale-110 transition" aria-label="2 sao">★</button>
                        <button type="button" data-value="3" class="rating-star text-gray-200 hover:scale-110 transition" aria-label="3 sao">★</button>
                        <button type="button" data-value="4" class="rating-star text-gray-200 hover:scale-110 transition" aria-label="4 sao">★</button>
                        <button type="button" data-value="5" class="rating-star text-gray-200 hover:scale-110 transition" aria-label="5 sao">★</button>
                    </div>
                    <p id="rating-text" class="mt-3 text-xs font-black uppercase tracking-widest text-gray-500">Chọn số sao (1-5)</p>
                </div>
            </div>

            <div class="space-y-2">
                <label class="text-xs font-black uppercase tracking-widest text-gray-500">Nhận xét</label>
                <textarea name="comment" rows="5" maxlength="1000" placeholder="Chia sẻ trải nghiệm của bạn..." class="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white font-semibold resize-y"><c:out value="${feedback.comment}"/></textarea>
            </div>

            <div class="flex flex-col sm:flex-row gap-3 pt-2">
                <button type="submit" class="px-8 py-4 rounded-2xl bg-[#008751] text-white font-black uppercase text-xs tracking-widest hover:bg-emerald-500 transition-colors">
                    <c:choose>
                        <c:when test="${isEditMode}">Cập nhật đánh giá</c:when>
                        <c:otherwise>Gửi đánh giá</c:otherwise>
                    </c:choose>
                </button>
                <a href="${pageContext.request.contextPath}/customer/bookings" class="px-8 py-4 rounded-2xl border border-gray-200 text-gray-700 font-black uppercase text-xs tracking-widest text-center hover:border-[#008751] hover:text-[#008751] transition-colors">
                    Quay lại
                </a>
            </div>
        </form>

        <c:if test="${isEditMode}">
            <form method="post" action="${pageContext.request.contextPath}/customer/feedback" onsubmit="return confirm('Bạn có muốn xóa đánh giá này không?');" class="pt-2">
                <input type="hidden" name="bookingId" value="${bookingId}">
                <input type="hidden" name="action" value="delete">
                <button type="submit" class="px-8 py-4 rounded-2xl border border-rose-200 text-rose-600 font-black uppercase text-xs tracking-widest hover:bg-rose-50 transition-colors">
                    Xóa đánh giá
                </button>
            </form>
        </c:if>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    (function () {
        var ratingInput = document.getElementById('ratingInput');
        var ratingText = document.getElementById('rating-text');
        var stars = Array.prototype.slice.call(document.querySelectorAll('.rating-star'));
        var form = document.querySelector('form[action$="/customer/feedback"]');

        function labelForRating(value) {
            switch (value) {
                case 5: return '5 sao - Rất hài lòng';
                case 4: return '4 sao - Hài lòng';
                case 3: return '3 sao - Bình thường';
                case 2: return '2 sao - Chưa hài lòng';
                case 1: return '1 sao - Rất tệ';
                default: return 'Chọn số sao (1-5)';
            }
        }

        function paintStars(activeValue) {
            stars.forEach(function (star) {
                var value = parseInt(star.getAttribute('data-value'), 10);
                if (value <= activeValue) {
                    star.classList.remove('text-gray-200');
                    star.classList.add('text-amber-400');
                } else {
                    star.classList.remove('text-amber-400');
                    star.classList.add('text-gray-200');
                }
            });
            ratingText.textContent = labelForRating(activeValue);
        }

        var selected = parseInt(ratingInput.value || '0', 10);
        if (isNaN(selected)) {
            selected = 0;
        }
        paintStars(selected);

        stars.forEach(function (star) {
            var value = parseInt(star.getAttribute('data-value'), 10);

            star.addEventListener('mouseenter', function () {
                paintStars(value);
            });

            star.addEventListener('click', function () {
                selected = value;
                ratingInput.value = String(value);
                paintStars(selected);
            });
        });

        var widget = document.getElementById('rating-widget');
        widget.addEventListener('mouseleave', function () {
            paintStars(selected);
        });

        form.addEventListener('submit', function (event) {
            if (!ratingInput.value) {
                event.preventDefault();
                alert('Vui lòng chọn số sao đánh giá từ 1 đến 5.');
            }
        });
    })();

</script>
</body>
</html>
