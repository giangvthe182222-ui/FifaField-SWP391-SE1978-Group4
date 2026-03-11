<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
    <a href="${pageContext.request.contextPath}/customer/vouchers" class="group rounded-3xl border-2 border-gray-50 bg-gray-50/50 p-6 hover:border-[#008751] hover:bg-white transition-all">
        <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 group-hover:text-[#008751] shadow-sm mb-4 transition-colors">
            <i data-lucide="ticket" class="w-5 h-5"></i>
        </div>
        <p class="text-[9px] font-black uppercase text-gray-400 tracking-widest">Voucher</p>
        <p class="text-lg font-black text-gray-900 mt-1 uppercase tracking-tighter">Ưu đãi</p>
    </a>

    <a href="${pageContext.request.contextPath}/customer/blogs" class="group rounded-3xl border-2 border-gray-50 bg-gray-50/50 p-6 hover:border-[#008751] hover:bg-white transition-all">
        <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 group-hover:text-[#008751] shadow-sm mb-4 transition-colors">
            <i data-lucide="newspaper" class="w-5 h-5"></i>
        </div>
        <p class="text-[9px] font-black uppercase text-gray-400 tracking-widest">Blogs</p>
        <p class="text-lg font-black text-gray-900 mt-1 uppercase tracking-tighter">Tin tức</p>
    </a>

    <a href="${pageContext.request.contextPath}/customer/profile" class="group rounded-3xl border-2 border-gray-50 bg-gray-50/50 p-6 hover:border-[#008751] hover:bg-white transition-all">
        <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 group-hover:text-[#008751] shadow-sm mb-4 transition-colors">
            <i data-lucide="user" class="w-5 h-5"></i>
        </div>
        <p class="text-[9px] font-black uppercase text-gray-400 tracking-widest">Cá nhân</p>
        <p class="text-lg font-black text-gray-900 mt-1 uppercase tracking-tighter">Hồ sơ </p>
    </a>

    <a href="${pageContext.request.contextPath}/customer/bookings" class="group rounded-3xl border-2 border-gray-50 bg-gray-50/50 p-6 hover:border-[#008751] hover:bg-white transition-all">
        <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 group-hover:text-[#008751] shadow-sm mb-4 transition-colors">
            <i data-lucide="history" class="w-5 h-5"></i>
        </div>
        <p class="text-[9px] font-black uppercase text-gray-400 tracking-widest">Lịch sử</p>
        <p class="text-lg font-black text-gray-900 mt-1 uppercase tracking-tighter">Đơn đặt sân</p>
    </a>

    <a href="${pageContext.request.contextPath}/customer/dashboard#locations" class="group rounded-3xl border-2 border-gray-50 bg-gray-50/50 p-6 hover:border-[#008751] hover:bg-white transition-all">
        <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 group-hover:text-[#008751] shadow-sm mb-4 transition-colors">
            <i data-lucide="building-2" class="w-5 h-5"></i>
        </div>
        <p class="text-[9px] font-black uppercase text-gray-400 tracking-widest">Địa điểm</p>
        <p class="text-lg font-black text-gray-900 mt-1 uppercase tracking-tighter">Các cơ sở</p>
    </a>
</div>
