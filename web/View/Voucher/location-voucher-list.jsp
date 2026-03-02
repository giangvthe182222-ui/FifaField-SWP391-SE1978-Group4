<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Voucher - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .voucher-card:hover { transform: translateY(-5px); }
        .ticket-cut-left, .ticket-cut-right {
            position: absolute;
            width: 20px;
            height: 20px;
            background: #f8fafc;
            border-radius: 50%;
            top: 50%;
            transform: translateY(-50%);
        }
        .ticket-cut-left { left: -10px; border-right: 1px solid #f1f5f9; }
        .ticket-cut-right { right: -10px; border-left: 1px solid #f1f5f9; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderAdmin.jsp" />

<main class="flex-grow max-w-7xl mx-auto px-6 py-12 space-y-10 w-full">

    <!-- TOP SECTION: HEADER & BREADCRUMB -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div class="space-y-2">
            <a href="${pageContext.request.contextPath}/locations/view?location_id=${locationId}" 
               class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
                <i data-lucide="arrow-left" class="w-3 h-3"></i>
                QUAY LẠI CỤM SÂN
            </a>
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                CHIẾN DỊCH <span class="text-[#008751]">VOUCHER</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Danh sách mã giảm giá đang áp dụng tại cơ sở</p>
        </div>

        <a href="${pageContext.request.contextPath}/locations/vouchers/add?location_id=${locationId}" 
           class="bg-[#008751] hover:bg-[#007043] text-white px-8 py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] shadow-2xl shadow-[#008751]/30 transition-all hover:-translate-y-1 flex items-center gap-3">
            <i data-lucide="plus" class="w-4 h-4"></i>
            TẠO VOUCHER MỚI
        </a>
    </div>

    <!-- NOTIFICATIONS -->
    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
<div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <p class="text-xs font-black text-rose-700 uppercase tracking-tight">${error}</p>
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="w-10 h-10 bg-emerald-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="check-circle-2" class="w-5 h-5"></i>
            </div>
            <p class="text-xs font-black text-emerald-700 uppercase tracking-tight">${success}</p>
        </div>
    </c:if>

    <!-- VOUCHER GRID -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <c:choose>
            <c:when test="${empty vouchers}">
                <div class="col-span-full py-32 bg-white rounded-[3rem] border border-gray-100 flex flex-col items-center justify-center text-center space-y-4">
                    <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center text-gray-200">
                        <i data-lucide="ticket-x" class="w-10 h-10"></i>
                    </div>
                    <div>
                        <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight">Trống danh sách</h3>
                        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-1">Cơ sở này hiện chưa thiết lập mã giảm giá nào</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${vouchers}" var="v">
                    <div class="voucher-card group bg-white rounded-[2.5rem] border border-gray-100 shadow-xl shadow-gray-200/50 transition-all flex flex-col relative overflow-hidden">
                        <!-- Top Accent -->
                        <div class="h-2 bg-[#008751] w-full"></div>
                        
                        <!-- Content -->
                        <div class="p-8 space-y-6 flex-1 relative">
                            <div class="flex justify-between items-start">
                                <div class="w-12 h-12 bg-emerald-50 text-[#008751] rounded-2xl flex items-center justify-center">
                                    <i data-lucide="ticket" class="w-6 h-6"></i>
                                </div>
                                <div class="px-3 py-1 bg-emerald-50 text-[#008751] rounded-full text-[9px] font-black uppercase tracking-widest border border-emerald-100">
                                    ACTIVE
                                </div>
                            </div>

                            <div>
<h3 class="text-xs font-black text-gray-400 uppercase tracking-widest mb-1">${v.description}</h3>
                                <div class="flex items-baseline gap-2">
                                    <span class="text-4xl font-black text-gray-900 tracking-tighter">-${v.discountValue}%</span>
                                    <span class="text-[10px] font-black text-[#008751] uppercase tracking-[0.2em]">GIẢM GIÁ</span>
                                </div>
                            </div>

                            <div class="bg-gray-50 border border-gray-100 rounded-2xl p-4 font-mono text-center relative">
                                <span class="text-sm font-black text-gray-900 tracking-[0.2em]">${v.code}</span>
                                <div class="ticket-cut-left"></div>
                                <div class="ticket-cut-right"></div>
                            </div>

                            <div class="pt-4 border-t border-dashed border-gray-100">
                                <div class="flex items-center gap-3 text-[10px] font-bold text-gray-400 uppercase tracking-widest">
                                    <i data-lucide="calendar" class="w-3 h-3"></i>
                                    <span>${v.startDate} <span class="mx-1 text-gray-200">→</span> ${v.endDate}</span>
                                </div>
                            </div>
                        </div>

                        <!-- Actions -->
                        <div class="flex border-t border-gray-50 bg-gray-50/30">
                            <a href="${pageContext.request.contextPath}/voucher/detail?id=${v.voucherId}" 
                               class="flex-1 py-5 flex items-center justify-center gap-2 text-[10px] font-black text-gray-400 hover:text-gray-900 hover:bg-white transition-all uppercase tracking-widest border-r border-gray-50">
                                <i data-lucide="eye" class="w-3 h-3"></i> XEM
                            </a>
                            <a href="${pageContext.request.contextPath}/voucher/edit?id=${v.voucherId}" 
                               class="flex-1 py-5 flex items-center justify-center gap-2 text-[10px] font-black text-[#008751] hover:bg-white transition-all uppercase tracking-widest">
                                <i data-lucide="edit-3" class="w-3 h-3"></i> CHỈNH SỬA
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- SUMMARY INFO -->
    <div class="flex flex-col md:flex-row items-center justify-between py-10 border-t border-gray-100 mt-8 gap-8">
        <div class="flex items-center gap-8">
            <div class="flex items-center gap-3">
                <i data-lucide="shield-check" class="w-4 h-4 text-[#008751]"></i>
<span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Mã bảo mật 2 lớp</span>
            </div>
            <div class="flex items-center gap-3">
                <i data-lucide="zap" class="w-4 h-4 text-[#008751]"></i>
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Áp dụng tức thì</span>
            </div>
        </div>
        
        <div class="bg-emerald-50 px-8 py-4 rounded-3xl border border-emerald-100">
            <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">
                Tổng cộng: <span class="text-lg leading-none ml-2">${vouchers.size()}</span> Voucher
            </span>
        </div>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp" />

<script>
    lucide.createIcons();
</script>

</body>
</html>