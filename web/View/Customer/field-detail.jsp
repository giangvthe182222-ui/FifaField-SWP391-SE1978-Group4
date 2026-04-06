<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chi tiết sân - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="bg-slate-100 min-h-screen text-slate-900">
<jsp:include page="/View/Layout/HeaderCustomer.jsp" />

<main class="max-w-6xl mx-auto px-6 py-10 space-y-8">
    <jsp:include page="/View/Layout/CustomerTopBanner.jsp"/>

    <div class="bg-white rounded-[2rem] p-8 shadow-xl shadow-slate-200/60 border border-slate-100">
        <div class="flex flex-col lg:flex-row items-start gap-8">
            <div class="w-full lg:w-80 h-64 lg:h-80 bg-slate-100 rounded-[1.75rem] overflow-hidden shrink-0">
                <c:choose>
                    <c:when test="${not empty field.imageUrl}">
                        <img src="${pageContext.request.contextPath}/${field.imageUrl}" alt="Ảnh sân" class="w-full h-full object-cover"/>
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/assets/img/default_field.jpg" alt="Ảnh sân" class="w-full h-full object-cover"/>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="flex-1 space-y-5">
                <div class="space-y-2">
                    <h1 class="text-4xl font-black tracking-tight uppercase">${field.fieldName}</h1>
                    <p class="text-sm text-slate-500">Loại: <span class="font-bold">${field.fieldType == '7-a-side' ? 'Sân 7' : (field.fieldType == '11-a-side' ? 'Sân 11' : field.fieldType)}</span></p>
                    <p class="text-sm text-slate-500">Tình trạng: <span class="font-bold">${field.fieldCondition}</span></p>
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div class="px-4 py-3 bg-slate-50 rounded-2xl border border-slate-200">
                        <p class="text-[10px] font-black uppercase tracking-widest text-slate-400">Trạng thái</p>
                        <p class="font-bold mt-1">${field.status}</p>
                    </div>
                    <div class="px-4 py-3 bg-slate-50 rounded-2xl border border-slate-200">
                        <p class="text-[10px] font-black uppercase tracking-widest text-slate-400">Thuộc cụm</p>
                        <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${field.locationId}" class="font-bold mt-1 inline-block text-[#008751]">Xem chi tiết cụm</a>
                    </div>
                </div>

                <div class="flex flex-wrap gap-3">
                    <a href="${pageContext.request.contextPath}/customer/field-schedule?fieldId=${field.fieldId}"
                       class="px-6 py-3 bg-slate-50 rounded-xl border border-slate-200 font-black">Xem lịch sân</a>

                    <a href="${pageContext.request.contextPath}/booking?locationId=${field.locationId}&fieldId=${field.fieldId}"
                       class="px-6 py-3 bg-[#008751] text-white rounded-xl font-black">Đặt ngay</a>

                    <a href="${pageContext.request.contextPath}/customer/location-detail?locationId=${field.locationId}" onclick="if (window.history.length > 1) { window.history.back(); return false; }"
                       class="w-12 h-12 border rounded-xl text-gray-500 hover:text-[#008751] hover:border-[#008751] transition-all flex items-center justify-center" aria-label="Quay lại" title="Quay lại">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <section class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="bg-white rounded-[2rem] p-7 shadow-lg shadow-slate-200/50 border border-slate-100 lg:col-span-1">
            <p class="text-[10px] font-black uppercase tracking-[0.3em] text-slate-400">Đánh giá sân</p>
            <div class="mt-4 space-y-3">
                <div class="flex items-end gap-3">
                    <p class="text-5xl font-black tracking-tight text-[#008751]">
                        <c:choose>
                            <c:when test="${averageRating != null}"><fmt:formatNumber value="${averageRating}" pattern="0.0"/></c:when>
                            <c:otherwise>0.0</c:otherwise>
                        </c:choose>
                    </p>
                    <p class="pb-1 text-sm font-bold text-slate-500">/ 5 sao</p>
                </div>
                <div class="flex gap-1 text-2xl leading-none">
                    <c:forEach begin="1" end="5" var="star">
                        <span class="${averageRating != null && averageRating >= star ? 'text-amber-400' : 'text-slate-200'}">★</span>
                    </c:forEach>
                </div>
                <p class="text-sm font-semibold text-slate-500">
                    <c:choose>
                        <c:when test="${feedbackCount > 0}">${feedbackCount} lượt đánh giá đã được ghi nhận</c:when>
                        <c:otherwise>Chưa có đánh giá nào cho sân này</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>

        <div class="bg-white rounded-[2rem] p-7 shadow-lg shadow-slate-200/50 border border-slate-100 lg:col-span-2">
            <div>
                <p class="text-[10px] font-black uppercase tracking-[0.3em] text-slate-400">Chi tiết đánh giá</p>
                <h2 class="mt-1 text-2xl font-black uppercase">Khách hàng nói gì</h2>
            </div>

            <div class="mt-6 space-y-4">
                <c:choose>
                    <c:when test="${not empty feedbacks}">
                        <c:forEach var="fb" items="${feedbacks}">
                            <article class="rounded-3xl border border-slate-200 bg-slate-50 p-5 space-y-3">
                                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                                    <div>
                                        <p class="font-black text-slate-900">${fb.customerName}</p>
                                        <p class="text-xs font-bold uppercase tracking-widest text-slate-400">${fb.createdAtDisplay}</p>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <div class="flex gap-1 text-base leading-none">
                                            <c:forEach begin="1" end="5" var="star">
                                                <span class="${fb.rating >= star ? 'text-amber-400' : 'text-slate-200'}">★</span>
                                            </c:forEach>
                                        </div>
                                        <span class="px-3 py-1 rounded-full bg-white border border-slate-200 text-slate-700 text-[10px] font-black uppercase tracking-widest">${fb.rating}/5</span>
                                    </div>
                                </div>
                                <c:if test="${not empty fb.comment}">
                                    <p class="text-sm leading-7 text-slate-700 whitespace-pre-line">${fb.comment}</p>
                                </c:if>
                                <c:if test="${not empty currentUserId and not empty fb.customerId and fb.customerId.toString() eq currentUserId}">
                                    <div class="flex flex-wrap gap-2 pt-2">
                                        <a href="${pageContext.request.contextPath}/customer/feedback?bookingId=${fb.bookingId}" class="px-4 py-2 rounded-xl bg-emerald-600 text-white text-[10px] font-black uppercase tracking-widest hover:bg-emerald-500 transition-colors">
                                            Sửa feedback
                                        </a>
                                        <form method="post" action="${pageContext.request.contextPath}/customer/feedback" onsubmit="return confirm('Bạn có muốn xóa feedback này không?');">
                                            <input type="hidden" name="bookingId" value="${fb.bookingId}">
                                            <input type="hidden" name="action" value="delete">
                                            <button type="submit" class="px-4 py-2 rounded-xl border border-rose-200 text-rose-600 text-[10px] font-black uppercase tracking-widest hover:bg-rose-50 transition-colors">
                                                Xóa feedback
                                            </button>
                                        </form>
                                    </div>
                                </c:if>
                            </article>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="rounded-3xl border border-dashed border-slate-200 bg-slate-50 p-8 text-center">
                            <p class="font-bold text-slate-500">Sân này chưa có đánh giá để hiển thị.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>