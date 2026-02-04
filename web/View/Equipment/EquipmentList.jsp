<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý thiết bị - FIFAFIELD</title>

    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>

    <style>
        body { font-family: 'Inter', sans-serif; background:#f8fafc }
        .filter-select {
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right 1rem center;
            background-size: 1.25rem;
            appearance: none;
            padding-right: 2.5rem;
        }
    </style>
</head>

<body class="text-gray-900">

<jsp:include page="/View/Layout/Header.jsp"/>

<div class="max-w-7xl mx-auto px-6 py-10 space-y-8">

    <!-- HEADER -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
            <h1 class="text-4xl font-black uppercase tracking-tight">
                QUẢN LÝ <span class="text-[#008751]">DỤNG CỤ</span>
            </h1>
            <p class="text-gray-400 text-[10px] font-bold uppercase tracking-[0.2em] mt-2">
                Kho vật tư & dụng cụ thi đấu
            </p>
        </div>

        <a href="add-equipment"
           class="bg-[#008751] text-white px-6 py-4 rounded-2xl
           text-[10px] font-black uppercase tracking-widest
           shadow-lg shadow-[#008751]/20 hover:bg-[#007043] transition
           flex items-center gap-2">
            <i data-lucide="plus" class="w-4 h-4"></i>
            Thêm dụng cụ
        </a>
    </div>

    
    <form method="get" action="equipment-list"
          class="bg-white p-8 rounded-[2.5rem] border border-gray-100 shadow-sm">

        <div class="grid grid-cols-1 md:grid-cols-12 gap-5">

            <!-- SEARCH -->
            <div class="md:col-span-4">
                <input type="text" name="search" value="${param.search}"
                       placeholder="Tìm tên thiết bị..."
                       class="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl
                       font-bold text-sm focus:ring-4 focus:ring-[#008751]/10 focus:border-[#008751]">
            </div>

            <!-- TYPE -->
            <div class="md:col-span-2">
                <select name="type"
                        class="filter-select w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl
                        font-black text-[10px] uppercase tracking-widest text-gray-500">
                    <option value="">Tất cả loại</option>
                    <c:forEach items="${typeList}" var="t">
                        <option value="${t}" ${param.type==t?'selected':''}>${t}</option>
                    </c:forEach>
                </select>
            </div>

            <!-- STATUS -->
            <div class="md:col-span-3">
                <select name="status"
                        class="filter-select w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl
                        font-black text-[10px] uppercase tracking-widest text-gray-500">
                    <option value="">Tất cả trạng thái</option>
                    <option value="available" ${param.status=='available'?'selected':''}>Available</option>
                    <option value="unavailable" ${param.status=='unavailable'?'selected':''}>Unavailable</option>
                </select>
            </div>

            <!-- SORT -->
            <div class="md:col-span-2">
                <select name="sort"
                        class="filter-select w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl
                        font-black text-[10px] uppercase tracking-widest text-gray-500">
                    <option value="">Sắp xếp giá</option>
                    <option value="asc" ${param.sort=='asc'?'selected':''}>Giá: Thấp -> Cao</option>
                    <option value="desc" ${param.sort=='desc'?'selected':''}>Giá: Cao -> Thấp</option>
                </select>
            </div>

            <!-- SUBMIT -->
            <div class="md:col-span-1">
                <button type="submit"
                        class="w-full h-full bg-[#008751] text-white rounded-2xl
                        font-black text-[10px] uppercase tracking-widest
                        hover:bg-[#007043] transition">
                    Lọc
                </button>
            </div>
        </div>

        <c:if test="${not empty param.search or not empty param.type or not empty param.status or not empty param.sort}">
            <div class="flex items-center gap-3 pt-2">
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Đang lọc:</span>
                <a href="${pageContext.request.contextPath}/equipment-list" class="text-[10px] font-black text-[#008751] hover:underline uppercase tracking-widest flex items-center gap-1">
                   <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
                    Xóa tất cả bộ lọc
                </a>
            </div>
        </c:if>
    </form>

    <!-- GRID -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">

        <c:forEach var="e" items="${equipmentList}">
            <div class="equipment-card bg-white border-2 border-gray-50 rounded-[2.8rem]
                        overflow-hidden transition-all hover:shadow-2xl
                        hover:shadow-[#008751]/5 hover:border-[#008751]
                        flex flex-col">

                <!-- IMAGE (hover zoom + overlay) -->
                <div class="relative h-52 bg-gray-100 overflow-hidden group">
                    <img src="${e.imageUrl}"
                         class="w-full h-full object-cover
                                group-hover:scale-110 transition-transform duration-700">
                    <div class="absolute inset-0 bg-gradient-to-t
                                from-black/30 to-transparent
                                opacity-0 group-hover:opacity-100 transition-opacity">
                    </div>
                </div>

                <!-- CONTENT -->
                <div class="p-6 flex flex-col gap-4 flex-1">

                    <h3 class="text-lg font-black leading-tight">
                        ${e.name}
                    </h3>

                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">
                        ${e.equipmentType}
                    </p>

                    <div class="grid grid-cols-2 gap-4 py-4 border-y border-gray-100">
                        <div>
                            <p class="text-[9px] font-black text-gray-400 uppercase">Giá thuê</p>
                            <p class="text-sm font-medium text-gray-700">
                                ${e.rentalPrice}đ
                            </p>
                        </div>
                        <div>
                            <p class="text-[9px] font-black text-gray-400 uppercase">Phí hỏng</p>
                            <p class="text-sm font-medium text-gray-700">
                                ${e.damageFee}đ
                            </p>
                        </div>
                    </div>

                    <!-- FOOTER -->
                    <div class="flex justify-between items-center mt-auto">

                        <!-- STATUS EDIT -->
                        <form action="update-equipment-status" method="post">
                            <input type="hidden" name="id" value="${e.equipmentId}">
                            <select name="newStatus"
                                    onchange="if(confirm('Thay đổi trạng thái?'))this.form.submit();else this.value='${e.status}'"
                                    class="px-4 py-2 rounded-xl text-[10px] font-black uppercase
                                    ${e.status=='available'
                                      ? 'bg-emerald-100 text-emerald-700'
                                      : 'bg-gray-100 text-gray-600'}">
                                <option value="available" ${e.status=='available'?'selected':''}>available</option>
                                <option value="unavailable" ${e.status=='unavailable'?'selected':''}>unavailable</option>
                            </select>
                        </form>

                        <!-- ACTION -->
                        <div class="flex gap-2">
<!--                            <a href="equipment-detail?id=${e.equipmentId}"
                               class="p-2 rounded-xl hover:bg-emerald-50 text-gray-500 hover:text-emerald-600">
                                <i data-lucide="eye" class="w-4 h-4"></i>
                            </a>-->
                            <a href="edit-equipment?id=${e.equipmentId}"
                               class="p-2 rounded-xl hover:bg-blue-50 text-gray-500 hover:text-blue-600">
                                <i data-lucide="edit-3" class="w-4 h-4"></i>
                            </a>
                        </div>
                    </div>

                </div>
            </div>
        </c:forEach>

        <c:if test="${empty equipmentList}">
            <div class="col-span-full py-32 text-center text-gray-400 font-bold">
                Không có thiết bị nào
            </div>
        </c:if>
    </div>

    <!-- PAGINATION -->
    <div class="flex justify-center pt-14 gap-2">
        <c:if test="${currentPage > 1}">
            <a href="equipment-list?page=${currentPage-1}&search=${search}&status=${status}&type=${type}&sort=${sort}"
               class="px-4 py-2 rounded-xl border font-black">←</a>
        </c:if>

        <c:forEach begin="1" end="${totalPages}" var="p">
            <a href="equipment-list?page=${p}&search=${search}&status=${status}&type=${type}&sort=${sort}"
               class="px-4 py-2 rounded-xl font-black
               ${p==currentPage?'bg-[#008751] text-white':'border'}">
                ${p}
            </a>
        </c:forEach>

        <c:if test="${currentPage < totalPages}">
            <a href="equipment-list?page=${currentPage+1}&search=${search}&status=${status}&type=${type}&sort=${sort}"
               class="px-4 py-2 rounded-xl border font-black">→</a>
        </c:if>
    </div>

</div>



<script>
    lucide.createIcons();
</script>
<jsp:include page="/View/Layout/Footer.jsp"/>
</body>
</html>
