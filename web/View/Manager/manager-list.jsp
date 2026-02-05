<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách quản lý - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .manager-card:hover { transform: translateY(-5px); }
        .avatar-glow { box-shadow: 0 0 20px rgba(0, 135, 81, 0.15); }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen pb-10">

<jsp:include page="/View/Layout/Header.jsp" />

<!-- MAIN CONTENT -->
<main class="max-w-7xl mx-auto px-6 py-12 space-y-10 w-full flex-grow">

    <!-- TOP SECTION: HEADER & ACTIONS -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div class="space-y-2">
            <button type="button" onclick="history.back()" 
               class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
                <i data-lucide="arrow-left" class="w-3 h-3"></i>
                QUAY LẠI
            </button>
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                ĐỘI NGŨ <span class="text-[#008751]">QUẢN LÝ</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Danh sách nhân sự điều hành các cụm sân hệ thống</p>
        </div>

        <a href="${pageContext.request.contextPath}/add-manager"
           class="bg-[#008751] hover:bg-[#007043] text-white px-8 py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] shadow-2xl shadow-[#008751]/30 transition-all hover:-translate-y-1 flex items-center gap-3">
            <i data-lucide="plus" class="w-4 h-4"></i>
            THÊM QUẢN LÝ MỚI
        </a>
    </div>

    <!-- ERROR ALERT -->
    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <div>
                <p class="text-[10px] font-black text-rose-400 uppercase tracking-widest leading-none mb-1">Cảnh báo hệ thống</p>
                <p class="text-sm font-bold text-rose-700 tracking-tight">${error}</p>
            </div>
        </div>
    </c:if>

    <!-- MANAGER GRID -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <c:choose>
            <c:when test="${empty managers}">
                <div class="col-span-full py-32 bg-white rounded-[3rem] border border-gray-100 flex flex-col items-center justify-center text-center space-y-4">
                    <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center text-gray-200">
                        <i data-lucide="users" class="w-10 h-10"></i>
                    </div>
                    <div>
                        <h3 class="text-lg font-black text-gray-900 uppercase tracking-tight">Trống danh sách</h3>
                        <p class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-1">Chưa có tài khoản quản lý nào được khởi tạo</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${managers}" var="manager">
                    <div class="manager-card group bg-white rounded-[3rem] border border-gray-100 shadow-xl shadow-gray-200/50 transition-all flex flex-col relative overflow-hidden">
                        
                        <!-- Ghost Icon Decor -->
                        <div class="absolute top-0 right-0 p-8 opacity-[0.03] group-hover:opacity-10 transition-all pointer-events-none">
                            <i data-lucide="shield-check" class="w-32 h-32"></i>
                        </div>

                        <!-- Top Info -->
                        <div class="p-8 space-y-6 flex-1 relative z-10">
                            <div class="flex justify-between items-start">
                                <div class="w-16 h-16 bg-gray-900 rounded-2xl flex items-center justify-center text-white font-black text-2xl shadow-lg shadow-gray-900/20 group-hover:bg-[#008751] transition-all duration-500">
                                    ${manager.fullName.charAt(0)}
                                </div>
                                <div class={`px-3 py-1.5 rounded-full text-[8px] font-black tracking-widest uppercase flex items-center gap-2 
                                    ${manager.status == 'active' ? 'bg-emerald-50 text-[#008751]' : 'bg-gray-100 text-gray-400'}`}>
                                    <span class={`w-1.5 h-1.5 rounded-full ${manager.status == 'active' ? 'bg-[#008751] animate-pulse' : 'bg-gray-400'}`}></span>
                                    ${manager.status == 'active' ? 'ĐANG HOẠT ĐỘNG' : 'TẠM KHÓA'}
                                </div>
                            </div>

                            <div>
                                <h3 class="text-2xl font-black text-gray-900 tracking-tighter leading-none group-hover:text-[#008751] transition-colors">${manager.fullName}</h3>
                                <p class="text-[10px] font-black text-[#008751] uppercase tracking-[0.2em] mt-2 opacity-70">${manager.locationName}</p>
</div>

                            <!-- Detail List -->
                            <div class="space-y-4 pt-6 border-t border-gray-50">
                                <div class="flex items-center gap-3">
                                    <div class="w-8 h-8 rounded-xl bg-gray-50 flex items-center justify-center text-gray-400">
                                        <i data-lucide="mail" class="w-4 h-4"></i>
                                    </div>
                                    <span class="text-xs font-bold text-gray-600 truncate">${manager.email}</span>
                                </div>
                                <div class="flex items-center gap-3">
                                    <div class="w-8 h-8 rounded-xl bg-gray-50 flex items-center justify-center text-gray-400">
                                        <i data-lucide="phone" class="w-4 h-4"></i>
                                    </div>
                                    <span class="text-xs font-bold text-gray-600">${manager.phone}</span>
                                </div>
                                <div class="flex items-center gap-3">
                                    <div class="w-8 h-8 rounded-xl bg-gray-50 flex items-center justify-center text-gray-400">
                                        <i data-lucide="calendar" class="w-4 h-4"></i>
                                    </div>
                                    <div>
                                        <p class="text-[8px] font-black text-gray-300 uppercase tracking-widest leading-none mb-1">Ngày bắt đầu</p>
                                        <p class="text-[10px] font-black text-gray-600 uppercase tracking-tight">${manager.startDate}</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Actions Area -->
                        <div class="flex border-t border-gray-50 bg-gray-50/30">
                            <a href="${pageContext.request.contextPath}/manager-detail?manager_id=${manager.userId}" 
                               class="flex-1 py-5 flex items-center justify-center gap-2 text-[9px] font-black text-gray-400 hover:text-gray-900 hover:bg-white transition-all uppercase tracking-widest border-r border-gray-50">
                                <i data-lucide="eye" class="w-3.5 h-3.5"></i> CHI TIẾT
                            </a>
                            <a href="${pageContext.request.contextPath}/manager-edit?manager_id=${manager.userId}" 
                               class="flex-1 py-5 flex items-center justify-center gap-2 text-[9px] font-black text-blue-500 hover:bg-white transition-all uppercase tracking-widest border-r border-gray-50">
                                <i data-lucide="edit-3" class="w-3.5 h-3.5"></i> SỬA
                            </a>
                            <a href="#"
onclick="if(confirm('Bạn chắc chắn muốn xóa?')) { window.location='${pageContext.request.contextPath}/manager-delete?manager_id=${manager.userId}'; } return false;"
                               class="flex-1 py-5 flex items-center justify-center gap-2 text-[9px] font-black text-rose-500 hover:bg-white transition-all uppercase tracking-widest">
                                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i> XÓA
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- SUMMARY SECTION -->
    <div class="flex flex-col md:flex-row items-center justify-between py-10 border-t border-gray-100 mt-12 gap-8">
        <div class="flex items-center gap-8">
            <div class="flex items-center gap-3">
                <i data-lucide="shield-check" class="w-4 h-4 text-[#008751]"></i>
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Xác thực 2 lớp Admin</span>
            </div>
            <div class="flex items-center gap-3">
                <i data-lucide="database" class="w-4 h-4 text-[#008751]"></i>
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Dữ liệu mã hóa RSA</span>
            </div>
        </div>
        
        <div class="bg-emerald-50 px-8 py-4 rounded-3xl border border-emerald-100 shadow-sm">
            <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">
                Tổng số quản lý: <span class="text-xl leading-none ml-2 tracking-tighter">${managers.size()}</span> nhân sự
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
