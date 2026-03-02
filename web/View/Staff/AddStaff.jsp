<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .input-icon-wrapper svg { color: #cbd5e1; transition: all 0.3s; }
        .group:focus-within svg { color: #008751; }
    </style>
</head>
<body class="antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="max-w-4xl mx-auto px-6 py-12 space-y-8">
    
    <!-- BREADCRUMB & TITLE -->
    <div class="space-y-2">
        <a href="javascript:history.back()" 
           class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="m12 19-7-7 7-7"/><path d="M19 12H5"/></svg>
            QUAY LẠI DANH SÁCH
        </a>
        <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
            THÊM <span class="text-[#008751]">NHÂN VIÊN MỚI</span>
        </h1>
        <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Khởi tạo tài khoản vận hành hệ thống</p>
    </div>

    <!-- ERROR ALERT -->
    <c:if test="${not empty error}">
        <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-2xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="bg-red-500 text-white p-2 rounded-xl">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            </div>
            <p class="text-sm font-black text-red-700 uppercase tracking-tight">${error}</p>
        </div>
    </c:if>

    <!-- MAIN FORM CARD -->
    <div class="bg-white rounded-[2.5rem] shadow-xl shadow-gray-200/50 border border-gray-100 p-10 md:p-14 overflow-hidden relative">
        <!-- Decoration -->
        <div class="absolute top-0 right-0 p-10 opacity-[0.03] pointer-events-none">
<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" x2="19" y1="8" y2="14"/><line x1="22" x2="16" y1="11" y2="11"/></svg>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/staff/add" class="space-y-12">
            
            <!-- SECTION 1: ACCOUNT INFO -->
            <div class="space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-10 h-1 bg-[#008751] rounded-full"></div>
                    <h3 class="text-xs font-black text-gray-400 uppercase tracking-[0.3em]">Thông tin tài khoản</h3>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <!-- Họ tên -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Họ tên nhân viên</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                            </div>
                            <input type="text" name="fullName" required placeholder="VD: Nguyễn Văn An"
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] focus:bg-white transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Email -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Email liên kết</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><rect width="20" height="16" x="2" y="4" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
                            </div>
                            <input type="email" name="email" value="${email}" required placeholder="example@fifafield.com"
class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] focus:bg-white transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Mật khẩu -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Mật khẩu khởi tạo</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                            </div>
                            <input type="password" name="password" required placeholder="••••••••"
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] focus:bg-white transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Điện thoại -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Số điện thoại</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                            </div>
                            <input type="text" name="phone" placeholder="09xxxx..."
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] focus:bg-white transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Địa chỉ -->
                    <div class="space-y-2 group md:col-span-2">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Địa chỉ thường trú</label>
<div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>
                            </div>
                            <input type="text" name="address" placeholder="Nhập địa chỉ cụ thể..."
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] focus:bg-white transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Giới tính -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Giới tính</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2v20"/><path d="m17 17-5-5 5-5"/><path d="m7 7 5 5-5 5"/></svg>
                            </div>
                            <select name="gender" class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] appearance-none font-bold text-sm text-gray-700 cursor-pointer">
                                <option value="">-- Chọn giới tính --</option>
                                <option value="male">Nam (Male)</option>
                                <option value="female">Nữ (Female)</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>

            <!-- SECTION 2: STAFF INFO -->
            <div class="space-y-8">
                <div class="flex items-center gap-4">
                    <div class="w-10 h-1 bg-[#008751] rounded-full"></div>
                    <h3 class="text-xs font-black text-gray-400 uppercase tracking-[0.3em]">Thông tin nhân sự</h3>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <!-- Mã nhân viên -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Mã nhân viên (ID)</label>
                        <div class="relative input-icon-wrapper">
<div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/><rect width="20" height="14" x="2" y="6" rx="2"/></svg>
                            </div>
                            <input type="text" name="employeeCode" placeholder="VD: NV001"
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] focus:bg-white transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Ngày thuê -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Ngày gia nhập</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"/><line x1="16" x2="16" y1="2" y2="6"/><line x1="8" x2="8" y1="2" y2="6"/><line x1="3" x2="21" y1="10" y2="10"/></svg>
                            </div>
                            <input type="date" name="hireDate"
                                   class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] focus:bg-white transition-all text-sm font-bold text-gray-700">
                        </div>
                    </div>

                    <!-- Cụm sân -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Cơ sở làm việc (Location)</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                            </div>
<select name="locationId" required class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] appearance-none font-bold text-sm text-gray-700 cursor-pointer">
                                <option value="">-- Chọn cơ sở --</option>
                                <c:forEach items="${locations}" var="loc">
                                    <option value="${loc.locationId}">${loc.locationName}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Trạng thái -->
                    <div class="space-y-2 group">
                        <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Trạng thái hệ thống</label>
                        <div class="relative input-icon-wrapper">
                            <div class="absolute left-4 top-1/2 -translate-y-1/2">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/></svg>
                            </div>
                            <select name="status" required class="w-full pl-12 pr-4 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:outline-none focus:ring-4 focus:ring-[#008751]/5 focus:border-[#008751] appearance-none font-bold text-sm text-gray-700 cursor-pointer">
                                <option value="active" <c:if test="${status == 'active'}">selected</c:if>>Active</option>
                                <option value="inactive" <c:if test="${status == 'inactive'}">selected</c:if>>Inactive</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>

            <!-- SUBMIT BUTTON -->
            <div class="pt-8">
                <button type="submit" 
                        class="w-full bg-[#008751] hover:bg-[#007043] text-white py-5 rounded-3xl font-black flex items-center justify-center gap-3 shadow-2xl shadow-[#008751]/30 transition-all hover:-translate-y-1 active:scale-95 text-lg uppercase tracking-widest">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
                    XÁC NHẬN THÊM NHÂN VIÊN
                </button>
            </div>

        </form>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>