<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý nhân viên - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <script src="https://unpkg.com/alpinejs@3.12.0/dist/cdn.min.js" defer></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .staff-row:hover { background-color: #f1f5f9; }
        .avatar-glow { box-shadow: 0 0 20px rgba(0, 135, 81, 0.15); }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen pb-10">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

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
                NHÂN VIÊN <span class="text-[#008751]">CỦA TÔI</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Quản lý nhân viên tại ${locationName}</p>
        </div>
    </div>

    <!-- SUCCESS MESSAGE -->
    <c:if test="${param.success eq 'true'}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="w-10 h-10 bg-emerald-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="check-circle" class="w-5 h-5"></i>
            </div>
            <div>
                <p class="text-[10px] font-black text-emerald-400 uppercase tracking-widest leading-none mb-1">Thành công</p>
                <p class="text-sm font-bold text-emerald-700 tracking-tight">Cập nhật trạng thái nhân viên thành công!</p>
            </div>
        </div>
    </c:if>

    <!-- ERROR ALERT -->
    <c:if test="${not empty error or not empty param.error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <div>
                <p class="text-[10px] font-black text-rose-400 uppercase tracking-widest leading-none mb-1">Lỗi hệ thống</p>
                <p class="text-sm font-bold text-rose-700 tracking-tight">
                    <c:choose>
                        <c:when test="${param.error eq 'invalid'}">Dữ liệu không hợp lệ</c:when>
                        <c:when test="${param.error eq 'no_location'}">Bạn chưa được gán cụm sân</c:when>
                        <c:when test="${param.error eq 'unauthorized'}">Bạn không có quyền chỉnh sửa nhân viên này</c:when>
                        <c:when test="${param.error eq 'update_failed'}">Cập nhật không thành công</c:when>
                        <c:when test="${param.error eq 'database'}">Lỗi kết nối cơ sở dữ liệu</c:when>
                        <c:when test="${param.error eq 'invalid_id'}">ID nhân viên không hợp lệ</c:when>
                        <c:otherwise>${error}</c:otherwise>
                    </c:choose>
                </p>
            </div>
        </div>
    </c:if>

    <!-- STAFF TABLE -->
    <div class="overflow-x-auto bg-white shadow-lg rounded-2xl">
        <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gradient-to-r from-slate-50 to-blue-50">
                <tr>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Avatar</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Mã NV</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Họ tên</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Email</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">SĐT</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Ngày vào làm</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Trạng thái</th>
                </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
                <c:choose>
                    <c:when test="${empty staffList}">
                        <tr>
                            <td colspan="7" class="py-16 text-center">
                                <div class="flex flex-col items-center gap-4">
                                    <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center">
                                        <i data-lucide="users" class="w-8 h-8 text-slate-300"></i>
                                    </div>
                                    <p class="text-slate-400 font-bold uppercase text-xs tracking-wider">Chưa có nhân viên nào tại cụm sân này</p>
                                </div>
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${staffList}" var="staff">
                            <tr class="staff-row transition-colors duration-150" x-data="{ 
                                open: false, 
                                status: '${staff.status}',
                                saving: false,
                                updateStatus(newStatus) {
                                    this.saving = true;
                                    fetch('${pageContext.request.contextPath}/manager/staff/update-status', {
                                        method: 'POST',
                                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                                        body: 'staffId=${staff.userId}&status=' + newStatus
                                    })
                                    .then(response => {
                                        if (response.redirected) {
                                            window.location.href = response.url;
                                        } else {
                                            this.status = newStatus;
                                            this.open = false;
                                        }
                                    })
                                    .catch(err => {
                                        console.error(err);
                                        alert('Lỗi cập nhật trạng thái');
                                    })
                                    .finally(() => this.saving = false);
                                }
                            }">
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div class="w-12 h-12 bg-gradient-to-br from-[#008751] to-[#006d41] rounded-full flex items-center justify-center text-white font-black text-base shadow-lg avatar-glow">
                                        ${staff.fullName.charAt(0)}
                                    </div>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm font-bold text-slate-900 font-mono">${staff.employeeCode}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm font-bold text-slate-900">${staff.fullName}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm text-slate-600">${staff.email}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm text-slate-600">${staff.phone}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm text-slate-600">${staff.hireDate}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap text-sm">
                                    <div class="relative inline-block text-left">
                                        <button @click.prevent="open = !open" type="button" :disabled="saving"
                                            :class="{
                                                'inline-flex items-center px-4 py-2 rounded-full text-xs font-black uppercase tracking-wider focus:outline-none focus:ring-2 focus:ring-offset-2 transition-all shadow-sm': true,
                                                'bg-emerald-100 text-emerald-700 hover:bg-emerald-200 focus:ring-emerald-500': status == 'active',
                                                'bg-slate-100 text-slate-600 hover:bg-slate-200 focus:ring-slate-400': status != 'active',
                                                'opacity-50 cursor-not-allowed': saving
                                            }">
                                            <span :class="status == 'active' ? 'w-2 h-2 rounded-full mr-2 bg-emerald-500 animate-pulse' : 'w-2 h-2 rounded-full mr-2 bg-slate-400'"></span>
                                            <span x-show="!saving" x-text="status == 'active' ? 'Hoạt động' : 'Tạm khóa'"></span>
                                            <span x-show="saving" class="flex items-center gap-2">
                                                <svg class="animate-spin h-3 w-3" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                                </svg>
                                                Đang lưu...
                                            </span>
                                            <svg class="ml-2 h-3 w-3" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                                            </svg>
                                        </button>

                                        <div x-show="open" x-cloak @click.away="open=false"
                                            class="origin-top-left absolute left-0 mt-2 w-48 rounded-xl shadow-2xl bg-white ring-1 ring-black ring-opacity-5 z-10 overflow-hidden">
                                            <div class="py-2">
                                                <button @click.prevent="updateStatus('active')"
                                                   :class="status == 'active' ? 'bg-emerald-50' : ''"
                                                   class="w-full text-left px-4 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50 transition-colors flex items-center gap-3">
                                                    <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
                                                    Hoạt động
                                                </button>
                                                <button @click.prevent="updateStatus('deactivated')"
                                                   :class="status == 'deactivated' ? 'bg-slate-50' : ''"
                                                   class="w-full text-left px-4 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50 transition-colors flex items-center gap-3">
                                                    <span class="w-2 h-2 rounded-full bg-slate-400"></span>
                                                    Tạm khóa
                                                </button>
                                            </div>
                                        </div>
                                    </div>
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
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Quản lý phân quyền</span>
            </div>
            <div class="flex items-center gap-3">
                <i data-lucide="map-pin" class="w-4 h-4 text-[#008751]"></i>
                <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Theo vị trí cụm sân</span>
            </div>
        </div>
        
        <div class="bg-emerald-50 px-8 py-4 rounded-3xl border border-emerald-100 shadow-sm">
            <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">
                Tổng nhân viên: <span class="text-xl leading-none ml-2 tracking-tighter">${staffList.size()}</span> người
            </span>
        </div>
    </div>

</main>

<jsp:include page="/View/Layout/Footer.jsp" />

<script>
    lucide.createIcons();
</script>

<style>
    [x-cloak] { display: none !important; }
</style>

</body>
</html>
