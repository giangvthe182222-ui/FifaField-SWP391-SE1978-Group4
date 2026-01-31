<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.time.LocalDate" %>

<%
    LocalDate today = LocalDate.now();

    int month = request.getParameter("month") != null
            ? Integer.parseInt(request.getParameter("month"))
            : today.getMonthValue();

    int day = request.getParameter("date") != null
            ? Integer.parseInt(request.getParameter("date"))
            : today.getDayOfMonth();

    int year = today.getYear();
    int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

    if (day > daysInMonth) day = daysInMonth;

    request.setAttribute("selMonth", month);
    request.setAttribute("selDay", day);
    request.setAttribute("year", year);
    request.setAttribute("daysInMonth", daysInMonth);
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sân</title>

    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: { fifa: '#008751' }
                }
            }
        }
    </script>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;900&display=swap" rel="stylesheet">
</head>

<body class="font-['Inter',sans-serif] bg-slate-50 text-gray-900">

<div class="max-w-7xl mx-auto px-6 py-10 space-y-10">

    <!-- HEADER -->
    <div class="space-y-3">
        <h1 class="text-4xl font-black uppercase">
            Lịch sân: <span class="text-fifa">${field.fieldName}</span>
        </h1>
        <div class="flex gap-3">
            <span class="bg-emerald-50 text-fifa px-3 py-1 rounded-lg text-[10px] font-black uppercase">
                ${field.fieldType}
            </span>
            
        </div>
    </div>

    <!-- DATE SELECTOR -->
    <div class="bg-white p-6 rounded-[2rem] shadow-sm border flex flex-col gap-6">

        <!-- MONTH -->
        <form method="get" class="flex items-center gap-4">
            <input type="hidden" name="fieldId" value="${field.fieldId}">
            <select name="month" onchange="this.form.submit()"
                    class="font-black text-sm uppercase">
                <c:forEach begin="1" end="12" var="m">
                    <option value="${m}" ${m == selMonth ? 'selected' : ''}>
                        Tháng ${m} / ${year}
                    </option>
                </c:forEach>
            </select>
        </form>

        <!-- DAYS -->
        <div class="flex gap-4 overflow-x-auto">
            <c:forEach begin="1" end="${daysInMonth}" var="d">
                <a href="?fieldId=${field.fieldId}&month=${selMonth}&date=${d}"
                   class="min-w-[120px] px-6 py-4 rounded-[1.8rem] text-center font-black transition
                   ${d == selDay ? 'bg-fifa text-white shadow-lg' :
                     'bg-gray-50 text-gray-400 hover:bg-emerald-50 hover:text-fifa'}">
                    ${d}/${selMonth}
                </a>
            </c:forEach>
        </div>
    </div>

    <!-- SCHEDULE GRID -->
    <div class="bg-white rounded-[3rem] p-10 shadow-sm border">

        <div class="flex items-center justify-between mb-10">
            <h2 class="text-2xl font-black uppercase">
                Khung giờ ngày ${selDay}/${selMonth}/${year}
            </h2>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-8">

            <c:if test="${empty schedules}">
                <div class="col-span-full text-center py-32 text-gray-400 font-bold">
                    Chưa có lịch cho ngày này
                </div>
            </c:if>

            <c:forEach items="${schedules}" var="s">
                <div class="group">
                    <div class="border-2 border-gray-50 rounded-[2.5rem] p-8 h-full
                                hover:border-fifa hover:shadow-xl transition">

                        <div class="text-xl font-black mb-4">
                            ${s.startTime} - ${s.endTime}
                        </div>

                        <div class="text-sm font-bold text-gray-400 mb-6">
                            Giá: <span class="text-fifa">${s.price} đ</span>
                        </div>

                        <div class="w-full py-3 rounded-xl text-[10px] font-black uppercase tracking-widest text-center
                            ${s.status == 'available' ? 'bg-fifa text-white' :
                              s.status == 'booked' ? 'bg-blue-600 text-white' :
                              'bg-amber-400 text-white'}">
                            <c:choose>
                                <c:when test="${s.status == 'available'}">Còn trống</c:when>
                                <c:when test="${s.status == 'booked'}">Đã đặt</c:when>
                                <c:otherwise>Bảo trì</c:otherwise>
                            </c:choose>
                        </div>

                    </div>
                </div>
            </c:forEach>

        </div>
    </div>

</div>

</body>
</html>
