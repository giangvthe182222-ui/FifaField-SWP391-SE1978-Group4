<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dụng cụ cơ sở - Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">
<jsp:include page="/View/Layout/HeaderManager.jsp"/>

<main class="max-w-6xl mx-auto px-6 py-10 space-y-8">
    <div class="space-y-2">
        <h1 class="text-4xl font-black uppercase tracking-tight">Dụng cụ tại <span class="text-[#008751]">cơ sở</span></h1>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.25em]">Xem và chỉnh sửa số lượng, trạng thái dụng cụ</p>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl text-emerald-700 font-bold">${flashSuccess}</div>
    </c:if>

    <c:if test="${not empty flashError}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl text-rose-700 font-bold">${flashError}</div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl text-rose-700 font-bold">${error}</div>
    </c:if>

    <section class="bg-white border border-gray-100 rounded-3xl p-8 shadow-sm space-y-5">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
            <div>
                <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Cơ sở phụ trách</p>
                <h2 class="text-2xl font-black uppercase tracking-tight mt-1">${locationName}</h2>
            </div>
            <a href="${pageContext.request.contextPath}/manager/location" class="px-5 py-3 rounded-2xl border border-gray-200 text-xs font-black uppercase tracking-widest text-gray-600 hover:border-[#008751] hover:text-[#008751] transition-all">Xem detail cơ sở</a>
        </div>

        <c:choose>
            <c:when test="${empty equipments}">
                <div class="bg-slate-50 border border-dashed border-slate-200 rounded-2xl p-8 text-center text-gray-400 font-black uppercase tracking-widest text-[10px]">Không có dụng cụ nào tại cơ sở này.</div>
            </c:when>
            <c:otherwise>
                <div class="space-y-4">
                    <c:forEach var="e" items="${equipments}">
                        <form method="post" action="${pageContext.request.contextPath}/manager/location-equipment/update" class="grid grid-cols-1 lg:grid-cols-6 gap-4 items-end bg-slate-50 border border-slate-100 rounded-2xl p-4">
                            <input type="hidden" name="equipmentId" value="${e.equipmentId}">

                            <div class="lg:col-span-2">
                                <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Dụng cụ</p>
                                <p class="text-base font-black text-gray-900 mt-1 uppercase">${e.name}</p>
                                <p class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mt-1">${e.equipmentType}</p>
                            </div>

                            <div>
                                <label class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Số lượng</label>
                                <input type="number" min="0" name="quantity" value="${e.quantity}" class="w-full px-4 py-3 rounded-xl border border-gray-200 text-sm font-bold text-gray-700 outline-none">
                            </div>

                            <div>
                                <label class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Trạng thái</label>
                                <select name="status" class="w-full px-4 py-3 rounded-xl border border-gray-200 text-sm font-bold text-gray-700 outline-none">
                                    <option value="available" <c:if test="${e.status == 'available'}">selected</c:if>>available</option>
                                    <option value="unavailable" <c:if test="${e.status == 'unavailable'}">selected</c:if>>unavailable</option>
                                </select>
                            </div>

                            <div>
                                <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Giá thuê</p>
                                <p class="text-sm font-black text-[#008751]"><fmt:formatNumber value="${e.rentalPrice}" pattern="#,##0"/> đ</p>
                            </div>

                            <div class="lg:col-span-1">
                                <button type="submit" class="w-full px-5 py-3 rounded-xl bg-[#008751] text-white text-[10px] font-black uppercase tracking-widest hover:bg-emerald-500 transition-all">Lưu</button>
                            </div>
                        </form>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<jsp:include page="/View/Layout/FooterManager.jsp"/>
</body>
</html>
