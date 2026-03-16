<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ca làm việc của Staff</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen antialiased text-gray-900">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="max-w-7xl mx-auto px-6 py-10 space-y-8">
    <section class="bg-white border border-gray-100 rounded-3xl p-8 shadow-sm">
        <p class="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em]">Ca làm việc</p>
        <h1 class="text-3xl font-black uppercase tracking-tight mt-2">Lịch ca của tôi</h1>
    </section>

    <section class="bg-white border border-gray-100 rounded-3xl p-6 shadow-sm overflow-x-auto">
        <c:choose>
            <c:when test="${empty staffShifts}">
                <div class="py-12 text-center">
                    <p class="text-sm font-bold text-gray-400 uppercase tracking-widest">Chưa có ca làm việc nào được phân.</p>
                </div>
            </c:when>
            <c:otherwise>
                <table class="min-w-full text-sm">
                    <thead>
                        <tr class="border-b border-gray-100 text-left text-[10px] uppercase tracking-widest text-gray-400 font-black">
                            <th class="py-3 pr-3">Ngày làm</th>
                            <th class="py-3 pr-3">Ca</th>
                            <th class="py-3 pr-3">Sân</th>
                            <th class="py-3 pr-3">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="ss" items="${staffShifts}">
                            <tr class="border-b border-gray-50">
                                <td class="py-4 pr-3 font-bold text-gray-800">${ss.workingDate}</td>
                                <td class="py-4 pr-3 font-semibold text-gray-700">${ss.shiftName}</td>
                                <td class="py-4 pr-3 font-semibold text-gray-700">${ss.fieldName}</td>
                                <td class="py-4 pr-3">
                                    <span class="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest ${ss.status == 'assigned' ? 'bg-emerald-50 text-[#008751]' : 'bg-slate-100 text-slate-500'}">${ss.status}</span>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

</body>
</html>
