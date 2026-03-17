<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ca làm việc - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-5xl mx-auto px-6 py-10 w-full flex-grow space-y-8">
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div class="space-y-2">
            <button type="button" onclick="history.back()"
                    class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest">
                <i data-lucide="arrow-left" class="w-3 h-3"></i>
                Quay lại
            </button>
            <h1 class="text-4xl font-black text-gray-900 uppercase tracking-tight leading-none">
                DANH SÁCH <span class="text-[#008751]">CA</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Các ca làm việc đã được tạo trong hệ thống</p>
        </div>
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/shifts/add"
               class="bg-[#008751] hover:bg-[#007043] text-white px-5 py-3 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-lg shadow-[#008751]/20 transition-all flex items-center gap-2">
                <i data-lucide="plus" class="w-4 h-4"></i>
                Thêm ca
            </a>
            <a href="${pageContext.request.contextPath}/manager/assign-shift"
               class="bg-white border border-gray-200 text-gray-700 hover:border-[#008751] hover:text-[#008751] px-5 py-3 rounded-2xl font-black text-[10px] uppercase tracking-[0.2em] shadow-sm transition-all flex items-center gap-2">
                <i data-lucide="calendar-plus" class="w-4 h-4"></i>
                Phân ca nhân viên
            </a>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-500 flex-shrink-0"></i>
            <p class="text-red-700 text-sm font-semibold">${error}</p>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty shifts}">
            <div class="bg-white rounded-2xl p-16 text-center shadow border border-gray-100">
                <i data-lucide="clock" class="w-16 h-16 text-gray-300 mx-auto mb-4"></i>
                <p class="text-gray-500 font-semibold text-lg">Chưa có ca nào được tạo</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <c:forEach items="${shifts}" var="shift">
                    <div class="bg-white rounded-2xl p-6 shadow border border-gray-100 hover:border-[#008751] hover:-translate-y-1 transition-all group">
                        <div class="flex items-start justify-between">
                            <div class="w-12 h-12 bg-emerald-50 group-hover:bg-[#008751] rounded-xl flex items-center justify-center transition-colors flex-shrink-0">
                                <i data-lucide="clock-3" class="w-6 h-6 text-[#008751] group-hover:text-white transition-colors"></i>
                            </div>
                            <span class="text-xs font-black text-gray-400 uppercase tracking-wider bg-gray-50 px-3 py-1 rounded-full">
                                Ca #${shift.shiftId.toString().substring(0, 8)}
                            </span>
                        </div>
                        <div class="mt-4">
                            <h3 class="text-lg font-black text-gray-900">${shift.shiftName}</h3>
                            <div class="mt-3 flex items-center gap-6 text-sm">
                                <div class="flex items-center gap-2 text-gray-600">
                                    <i data-lucide="sun" class="w-4 h-4 text-amber-500"></i>
                                    <span class="font-semibold">${shift.startTime}</span>
                                </div>
                                <i data-lucide="arrow-right" class="w-4 h-4 text-gray-300"></i>
                                <div class="flex items-center gap-2 text-gray-600">
                                    <i data-lucide="moon" class="w-4 h-4 text-indigo-500"></i>
                                    <span class="font-semibold">${shift.endTime}</span>
                                </div>
                            </div>
                        </div>
                        <div class="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between gap-3">
                            <a href="${pageContext.request.contextPath}/shifts/edit?id=${shift.shiftId}"
                               class="text-xs font-black text-indigo-600 hover:underline uppercase tracking-wider flex items-center gap-1">
                                <i data-lucide="pencil" class="w-3.5 h-3.5"></i>
                                Sửa ca
                            </a>
                            <a href="${pageContext.request.contextPath}/manager/assign-shift?shiftId=${shift.shiftId}"
                               class="text-xs font-black text-[#008751] hover:underline uppercase tracking-wider flex items-center gap-1">
                                <i data-lucide="user-plus" class="w-3.5 h-3.5"></i>
                                Phân ca này
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <div class="flex justify-end">
                <div class="bg-emerald-50 px-8 py-4 rounded-3xl border border-emerald-100">
                    <span class="text-[10px] font-black text-[#008751] uppercase tracking-widest">
                        Tổng số ca: <span class="text-xl ml-2">${shifts.size()}</span> ca
                    </span>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />
<script>lucide.createIcons();</script>
</body>
</html>
