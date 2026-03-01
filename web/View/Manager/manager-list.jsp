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
    <script src="https://unpkg.com/alpinejs@3.12.0/dist/cdn.min.js" defer></script>
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

    <!-- MANAGER TABLE -->
    <div class="overflow-x-auto bg-white shadow rounded-lg">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
                <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avatar</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Họ tên</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cụm sân</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">SĐT</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ngày bắt đầu</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                    <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Hành động</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
                <c:choose>
                    <c:when test="${empty managers}">
                        <tr>
                            <td colspan="8" class="py-12 text-center text-gray-500">Trống danh sách - chưa có tài khoản quản lý nào được khởi tạo</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${managers}" var="manager">
                            <tr x-data="{ open:false, status:'${manager.status}' }" class="hover:bg-gray-50">
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div class="w-10 h-10 bg-gray-900 rounded-full flex items-center justify-center text-white font-black text-sm">
                                        ${manager.fullName.charAt(0)}
                                    </div>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${manager.fullName}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">${manager.locationName}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700 truncate">${manager.email}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">${manager.phone}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">${manager.startDate}</td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm">
                                    <div class="relative inline-block text-left">
                                        <button @click.prevent="open = !open" type="button"
                                            :class="status == 'active' ? 'inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-[#008751]' : 'inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-600'"
                                            class="focus:outline-none">
                                            <span :class="status == 'active' ? 'w-2 h-2 rounded-full mr-2 bg-[#008751] animate-pulse' : 'w-2 h-2 rounded-full mr-2 bg-gray-400'"></span>
                                            <span x-text="status == 'active' ? 'ĐANG HOẠT ĐỘNG' : 'TẠM KHÓA'"></span>
                                            <svg class="ml-2 h-3 w-3 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                                            </svg>
                                        </button>

                                        <div x-show="open" x-cloak @click.away="open=false"
                                            class="origin-top-left absolute left-0 mt-2 w-36 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 z-10">
                                            <div class="py-1">
                                                <a href="#" @click.prevent="status='active'; open=false"
                                                   class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Active</a>
                                                <a href="#" @click.prevent="status='deactivated'; open=false"
                                                   class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Deactivated</a>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm text-right">
                                    <a href="${pageContext.request.contextPath}/manager-detail?manager_id=${manager.userId}"
                                       class="text-indigo-600 hover:text-indigo-900 mr-4">View</a>
                                    <a href="${pageContext.request.contextPath}/manager-edit?manager_id=${manager.userId}"
                                       class="text-yellow-600 hover:text-yellow-800">Edit</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
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
