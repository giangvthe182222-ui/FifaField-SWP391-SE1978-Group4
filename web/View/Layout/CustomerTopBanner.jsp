<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<section class="bg-white elite-card shadow-xl shadow-gray-200/50 border border-gray-100 p-10 relative overflow-hidden group">
    <div class="absolute -top-10 -right-10 opacity-[0.03] group-hover:scale-110 transition-transform duration-700">
        <i data-lucide="shield" class="w-64 h-64"></i>
    </div>

    <div class="relative z-10 flex flex-col md:flex-row md:items-center md:justify-between gap-8">
        <div class="space-y-2">
            <p class="text-[10px] font-black tracking-[0.3em] text-gray-400 uppercase">Hệ thống FIFAFIELD 2026</p>
            <h2 class="text-4xl font-black text-gray-900 uppercase tracking-tight">
                Xin chào, <span class="text-[#008751]">${sessionScope.user.fullName}</span>
            </h2>
            <p class="text-gray-500 font-bold text-sm uppercase tracking-widest opacity-60">Sẵn sàng cho trận đấu tiếp theo của bạn?</p>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full md:w-auto">
            <a href="${pageContext.request.contextPath}/booking?bookingMode=normal" class="px-8 py-5 rounded-[1.8rem] bg-[#008751] text-white font-black uppercase text-xs tracking-[0.2em] hover:bg-emerald-400 transition-all hover:-translate-y-1 shadow-2xl shadow-[#008751]/20 flex items-center justify-center gap-3">
                <i data-lucide="zap" class="w-4 h-4"></i>
                Đặt sân thường
            </a>
            <a href="${pageContext.request.contextPath}/booking?bookingMode=weekly" class="px-8 py-5 rounded-[1.8rem] border-2 border-[#008751] text-[#008751] bg-white font-black uppercase text-xs tracking-[0.2em] hover:bg-emerald-50 transition-all hover:-translate-y-1 flex items-center justify-center gap-3">
                <i data-lucide="calendar-range" class="w-4 h-4"></i>
                Đặt sân theo tuần
            </a>
        </div>
    </div>

    <div class="mt-12">
        <jsp:include page="/View/Layout/CustomerQuickPanel.jsp"/>
    </div>
</section>