<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cơ sở của tôi - Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">
<jsp:include page="/View/Layout/HeaderManager.jsp"/>

<main class="max-w-6xl mx-auto px-6 py-10 space-y-8">
    <div class="space-y-2">
        <h1 class="text-4xl font-black uppercase tracking-tight">Cơ sở <span class="text-[#008751]">của tôi</span></h1>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.25em]">Xem chi tiết cơ sở và quản lý trạng thái</p>
    </div>

    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl text-rose-700 font-bold">${error}</div>
    </c:if>

    <c:if test="${not empty flashSuccess}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl text-emerald-700 font-bold">${flashSuccess}</div>
    </c:if>

    <c:if test="${not empty location}">
        <section class="bg-white border border-gray-100 rounded-3xl p-8 shadow-sm space-y-6">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
                <div>
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Detail cơ sở được gán</p>
                    <h2 class="text-3xl font-black uppercase tracking-tight mt-2">${location.locationName}</h2>
                    <p class="text-sm font-semibold text-gray-500 mt-2">${location.address}</p>
                    <p class="text-sm font-semibold text-gray-500">Số điện thoại: ${location.phoneNumber}</p>
                </div>
                <div class="space-y-4">
                    <div class="w-full h-72 rounded-2xl overflow-hidden bg-slate-100 border border-slate-100">
                        <c:choose>
                            <c:when test="${not empty location.imageUrl}">
                                <img src="${pageContext.request.contextPath}/${location.imageUrl}" alt="Ảnh cơ sở" class="w-full h-full object-cover"/>
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/assets/img/default_cluster.jpg" alt="Ảnh cơ sở" class="w-full h-full object-cover"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <iframe class="w-full h-72 rounded-2xl border border-slate-100" loading="lazy" referrerpolicy="no-referrer-when-downgrade" src="https://maps.google.com/maps?q=${fn:replace(location.address, ' ', '+')}&z=15&output=embed"></iframe>
                </div>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/manager/location/status" class="flex flex-col md:flex-row md:items-end gap-4">
                <div>
                    <label class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Trạng thái cơ sở</label>
                    <select name="status" class="px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-xs font-bold text-gray-700 outline-none">
                        <option value="ACTIVE" ${location.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                        <option value="INACTIVE" ${location.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                        <option value="MAINTENANCE" ${location.status == 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                    </select>
                </div>
                <button type="submit" class="px-6 py-3 rounded-2xl bg-[#008751] text-white font-black text-[10px] uppercase tracking-widest hover:bg-emerald-400 transition-all">Cập nhật trạng thái</button>
            </form>
        </section>

        <section class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <a href="${pageContext.request.contextPath}/manager/fields" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
                <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">Điều hướng 1</p>
                <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Sân trong cơ sở</h2>
                <p class="text-sm font-semibold text-gray-500 mt-3">Xem danh sách sân thuộc location của bạn và cập nhật trạng thái sân.</p>
            </a>
            <a href="${pageContext.request.contextPath}/manager/location-equipment" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
                <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">Điều hướng 2</p>
                <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Dụng cụ của cơ sở</h2>
                <p class="text-sm font-semibold text-gray-500 mt-3">Xem dụng cụ của cơ sở và chỉnh sửa số lượng, trạng thái dụng cụ.</p>
            </a>
            <a href="${pageContext.request.contextPath}/manager/dashboard" class="group bg-white border-2 border-gray-50 rounded-3xl p-7 shadow-sm hover:border-[#008751] hover:shadow-lg transition-all">
                <p class="text-[10px] font-black uppercase tracking-[0.2em] text-gray-400">Điều hướng 3</p>
                <h2 class="text-2xl font-black mt-2 uppercase tracking-tight text-gray-900 group-hover:text-[#008751]">Quay lại dashboard</h2>
                <p class="text-sm font-semibold text-gray-500 mt-3">Trở lại bảng điều khiển quản lý để thao tác các chức năng khác.</p>
            </a>
        </section>
    </c:if>
</main>

<jsp:include page="/View/Layout/FooterManager.jsp"/>
</body>
</html>
