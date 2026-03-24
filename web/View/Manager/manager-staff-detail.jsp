<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết nhân viên - Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-gray-50 text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp"/>

<main class="max-w-5xl mx-auto px-6 py-10 w-full flex-grow space-y-8">
    <div class="flex items-center justify-between">
        <a href="${pageContext.request.contextPath}/manager/staff/list"
           class="inline-flex items-center gap-2 text-sm font-semibold text-gray-600 hover:text-[#008751]">
            <i data-lucide="arrow-left" class="w-4 h-4"></i> Quay lại danh sách nhân viên
        </a>
    </div>

    <div class="bg-white rounded-2xl shadow border border-gray-100 p-6 md:p-8 space-y-8">
        <div class="flex flex-col md:flex-row md:items-start gap-6">
            <div class="w-20 h-20 rounded-2xl bg-[#008751] text-white flex items-center justify-center text-3xl font-black">
                ${staff.fullName.charAt(0)}
            </div>
            <div class="flex-1 space-y-2">
                <h1 class="text-3xl font-black">${staff.fullName}</h1>
                <p class="text-sm text-gray-500">Mã nhân viên: <span class="font-bold text-gray-800">${staff.employeeCode}</span></p>
                <p class="text-sm text-gray-500">Cơ sở: <span class="font-bold text-gray-800">${staff.locationName}</span></p>
                <p class="text-sm text-gray-500">Trạng thái: <span class="font-bold text-gray-800">${staff.status}</span></p>
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="rounded-xl border border-gray-100 p-5 bg-gray-50">
                <p class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Thông tin nhân viên</p>
                <p class="text-sm"><span class="font-semibold">Email:</span> ${staff.email}</p>
                <p class="text-sm mt-2"><span class="font-semibold">SĐT:</span> ${staff.phone}</p>
                <p class="text-sm mt-2"><span class="font-semibold">Địa chỉ:</span> ${staff.address}</p>
                <p class="text-sm mt-2"><span class="font-semibold">Giới tính:</span> ${staff.gender}</p>
                <p class="text-sm mt-2"><span class="font-semibold">Ngày vào làm:</span> ${staff.hireDate}</p>
            </div>

            <div class="rounded-xl border border-emerald-200 p-5 bg-emerald-50">
                <p class="text-xs font-bold text-emerald-700 uppercase tracking-wider mb-2">Số ca đã làm</p>
                <p class="text-sm text-gray-700">Tổng số ca đã làm tới hiện tại:</p>
                <p class="text-4xl font-black text-[#008751] mt-2">${workedShiftCount}</p>
            </div>
        </div>
    </div>

    <!-- SHIFT HISTORY SECTION -->
    <div class="bg-white rounded-2xl shadow border border-gray-100 p-6 md:p-8 space-y-6">

        <!-- Section header -->
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-emerald-50 flex items-center justify-center">
                    <i data-lucide="calendar-days" class="w-5 h-5 text-[#008751]"></i>
                </div>
                <div>
                    <h2 class="text-lg font-black text-gray-900">Lịch sử ca làm việc</h2>
                    <p class="text-xs text-gray-400">Hiển thị theo từng tuần trong tháng</p>
                </div>
            </div>

            <!-- Month / Year filter -->
            <form method="get" action="${pageContext.request.contextPath}/manager/staff/detail"
                  class="flex flex-wrap items-center gap-2">
                <input type="hidden" name="id" value="${staff.userId}"/>

                <select name="month"
                        class="border border-gray-200 rounded-lg px-3 py-2 text-sm font-semibold text-gray-700 bg-white focus:ring-2 focus:ring-[#008751] focus:border-[#008751] outline-none">
                    <option value="1"  <c:if test="${filterMonth == 1}">selected</c:if>>Tháng 1</option>
                    <option value="2"  <c:if test="${filterMonth == 2}">selected</c:if>>Tháng 2</option>
                    <option value="3"  <c:if test="${filterMonth == 3}">selected</c:if>>Tháng 3</option>
                    <option value="4"  <c:if test="${filterMonth == 4}">selected</c:if>>Tháng 4</option>
                    <option value="5"  <c:if test="${filterMonth == 5}">selected</c:if>>Tháng 5</option>
                    <option value="6"  <c:if test="${filterMonth == 6}">selected</c:if>>Tháng 6</option>
                    <option value="7"  <c:if test="${filterMonth == 7}">selected</c:if>>Tháng 7</option>
                    <option value="8"  <c:if test="${filterMonth == 8}">selected</c:if>>Tháng 8</option>
                    <option value="9"  <c:if test="${filterMonth == 9}">selected</c:if>>Tháng 9</option>
                    <option value="10" <c:if test="${filterMonth == 10}">selected</c:if>>Tháng 10</option>
                    <option value="11" <c:if test="${filterMonth == 11}">selected</c:if>>Tháng 11</option>
                    <option value="12" <c:if test="${filterMonth == 12}">selected</c:if>>Tháng 12</option>
                </select>

                <input type="number" name="year" value="${filterYear}" min="2020" max="2099"
                       class="w-24 border border-gray-200 rounded-lg px-3 py-2 text-sm font-semibold text-gray-700 bg-white focus:ring-2 focus:ring-[#008751] focus:border-[#008751] outline-none"/>

                <button type="submit"
                        class="inline-flex items-center gap-2 bg-[#008751] hover:bg-[#006e41] text-white text-sm font-bold px-4 py-2 rounded-lg transition-colors">
                    <i data-lucide="search" class="w-4 h-4"></i> Lọc
                </button>
            </form>
        </div>

        <!-- Monthly summary badge -->
        <div class="flex items-center gap-3 px-4 py-3 bg-emerald-50 border border-emerald-100 rounded-xl">
            <i data-lucide="bar-chart-2" class="w-5 h-5 text-[#008751]"></i>
            <p class="text-sm text-gray-700">
                Tháng <span class="font-black text-[#008751]">${filterMonth}/${filterYear}</span>:
                nhân viên làm tổng cộng
                <span class="font-black text-[#008751] text-lg">${monthlyShiftCount}</span> ca
            </p>
        </div>

        <!-- Weekly groups -->
        <c:choose>
            <c:when test="${empty weekGroups}">
                <div class="flex flex-col items-center py-12 text-gray-400 gap-3">
                    <i data-lucide="calendar-x" class="w-12 h-12 opacity-30"></i>
                    <p class="text-sm font-semibold">Không có ca làm nào trong tháng ${filterMonth}/${filterYear}</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="space-y-5">
                    <c:forEach var="week" items="${weekGroups}">
                        <div class="rounded-xl border border-gray-100 overflow-hidden">
                            <!-- Week header -->
                            <div class="flex items-center justify-between px-5 py-3 bg-gray-50 border-b border-gray-100">
                                <div class="flex items-center gap-2">
                                    <i data-lucide="calendar-range" class="w-4 h-4 text-[#008751]"></i>
                                    <span class="text-sm font-black text-gray-700">${week.label}</span>
                                </div>
                                <span class="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-emerald-100 text-emerald-700 text-xs font-black">
                                    <i data-lucide="clock" class="w-3 h-3"></i>
                                    ${week.shiftCount} ca
                                </span>
                            </div>

                            <!-- Shift rows -->
                            <div class="divide-y divide-gray-50">
                                <c:forEach var="shift" items="${week.shifts}">
                                    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between px-5 py-3 hover:bg-gray-50/60 transition-colors gap-1">
                                        <div class="flex items-center gap-3">
                                            <div class="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center text-gray-500 shrink-0">
                                                <i data-lucide="calendar" class="w-4 h-4"></i>
                                            </div>
                                            <div>
                                                <p class="text-sm font-bold text-gray-800">${shift.shiftName}</p>
                                                <p class="text-xs text-gray-400">${shift.fieldName}</p>
                                            </div>
                                        </div>
                                        <div class="flex items-center gap-3 pl-11 sm:pl-0">
                                            <span class="text-xs font-semibold text-gray-500">${shift.workingDate}</span>
                                            <c:choose>
                                                <c:when test="${shift.status == 'assigned'}">
                                                    <span class="px-2 py-0.5 rounded-full text-xs font-bold bg-blue-50 text-blue-600 border border-blue-100">Đã phân công</span>
                                                </c:when>
                                                <c:when test="${shift.status == 'completed'}">
                                                    <span class="px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-600 border border-emerald-100">Hoàn thành</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="px-2 py-0.5 rounded-full text-xs font-bold bg-gray-100 text-gray-500 border border-gray-200">${shift.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="/View/Layout/FooterManager.jsp"/>
<script>lucide.createIcons();</script>
</body>
</html>

