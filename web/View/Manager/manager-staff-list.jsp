<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý nhân viên - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .staff-row:hover { background-color: #f1f5f9; }
        .avatar-glow { box-shadow: 0 0 20px rgba(0, 135, 81, 0.15); }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen pb-10">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

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
    <c:if test="${param.success == 'true'}">
        <div class="bg-emerald-50 border border-emerald-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-emerald-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="check-circle" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-emerald-700">Cập nhật trạng thái nhân viên thành công!</p>
        </div>
    </c:if>

    <!-- ERROR ALERT -->
    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-rose-700">${error}</p>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <p class="text-sm font-bold text-rose-700">
                <c:choose>
                    <c:when test="${param.error == 'invalid'}">Dữ liệu không hợp lệ</c:when>
                    <c:when test="${param.error == 'no_location'}">Bạn chưa được gán cụm sân</c:when>
                    <c:when test="${param.error == 'unauthorized'}">Bạn không có quyền chỉnh sửa nhân viên này</c:when>
                    <c:when test="${param.error == 'invalid_id'}">ID nhân viên không hợp lệ</c:when>
                    <c:when test="${param.error == 'not_found'}">Không tìm thấy nhân viên</c:when>
                    <c:when test="${param.error == 'update_failed'}">Cập nhật không thành công</c:when>
                    <c:when test="${param.error == 'database'}">Lỗi kết nối cơ sở dữ liệu</c:when>
                    <c:otherwise>Lỗi không xác định</c:otherwise>
                </c:choose>
            </p>
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
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">SĐT</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Ngày vào làm</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Trạng thái</th>
                    <th class="px-6 py-4 text-left text-xs font-black text-slate-600 uppercase tracking-widest">Chi tiết</th>
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
                        <c:forEach items="${staffList}" var="sv">
                            <tr class="staff-row transition-colors duration-150">
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div class="w-12 h-12 bg-gradient-to-br from-[#008751] to-[#006d41] rounded-full flex items-center justify-center text-white font-black text-base shadow-lg avatar-glow">
                                        <c:choose>
                                            <c:when test="${not empty sv.fullName}">${fn:toUpperCase(fn:substring(sv.fullName, 0, 1))}</c:when>
                                            <c:otherwise>?</c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm font-bold text-slate-900 font-mono">${sv.employeeCode}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm font-bold text-slate-900">${sv.fullName}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm text-slate-600">${sv.phone}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm text-slate-600">${sv.hireDate}</span>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <c:set var="selectCss" value="bg-slate-100 text-slate-600"/>
                                    <c:if test="${sv.status == 'active'}">
                                        <c:set var="selectCss" value="bg-emerald-100 text-emerald-700"/>
                                    </c:if>
                                    <form action="${pageContext.request.contextPath}/manager/staff/update-status" method="post"
                                          onchange="this.submit()">
                                        <input type="hidden" name="staffId" value="${sv.userId}" />
                                        <select name="status"
                                                class="rounded-full px-3 py-1.5 text-xs font-black uppercase tracking-wider border-0 cursor-pointer focus:ring-2 focus:ring-offset-1 ${selectCss}">
                                            <option value="active" ${sv.status == 'active' ? 'selected' : ''}>Hoạt động</option>
                                            <option value="deactivated" ${sv.status == 'deactivated' ? 'selected' : ''}>Tạm khóa</option>
                                        </select>
                                    </form>
                                </td>
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <a href="${pageContext.request.contextPath}/manager/staff/detail?id=${sv.userId}"
                                       class="inline-flex items-center gap-1 text-xs font-black text-indigo-600 hover:text-indigo-800 uppercase tracking-wider">
                                        <i data-lucide="eye" class="w-3.5 h-3.5"></i>
                                        Xem
                                    </a>
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
                Tổng nhân viên: <span class="text-xl leading-none ml-2 tracking-tighter">
                    <c:choose>
                        <c:when test="${not empty staffList}">${staffList.size()}</c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                </span> người
            </span>
        </div>
    </div>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />
<script>lucide.createIcons();</script>
</body>
</html>

