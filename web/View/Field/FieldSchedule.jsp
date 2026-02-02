<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sân</title>

    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: { extend: { colors: { fifa: '#008751' } } }
        }
    </script>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;900&display=swap" rel="stylesheet">
</head>

<body class="font-['Inter',sans-serif] bg-slate-50 text-gray-900">

<div class="max-w-full px-6 py-8 space-y-6">
<jsp:include page="/View/Layout/Header.jsp"/>
    <!-- HEADER -->
    <div>
        <h1 class="text-3xl font-black">
            Lịch sân <span class="text-fifa">${field.fieldName}</span>
        </h1>
    </div>

    <!-- WEEK INFO -->
    <div class="flex justify-between items-center bg-white p-4 rounded-xl shadow">
        <a href="?fieldId=${field.fieldId}&date=${prevWeek}&direction=prev"
           class="font-black text-fifa">← Tuần trước</a>

        <div class="font-black">
            ${weekStart} → ${weekEnd}
        </div>

        <a href="?fieldId=${field.fieldId}&date=${nextWeek}&direction=next"
           class="font-black text-fifa">Tuần sau →</a>
    </div>

    <!-- SCROLL CONTAINER -->
    <div class="overflow-x-auto" id="scrollWrapper">
        <div class="grid grid-cols-7 gap-6 min-w-[1400px] pb-10">

            <c:forEach items="${schedulesByDate}" var="entry">

                <!-- DAY COLUMN -->
                <div class="bg-white rounded-3xl p-4 shadow border space-y-4">

                    <div class="text-center font-black uppercase text-fifa">
                        ${displayDateMap[entry.key]}
                    </div>

                    <div class="space-y-3">

                        <c:if test="${empty entry.value}">
                            <div class="text-xs text-gray-400 font-bold text-center">
                                Không có lịch
                            </div>
                        </c:if>

                        <c:forEach items="${entry.value}" var="s">
                            <div class="border rounded-xl p-3 text-sm
                                ${s.status == 'available' ? 'border-fifa' :
                                  s.status == 'booked' ? 'border-blue-500' :
                                  'border-amber-400'}">

                                <div class="font-black">
                                    ${s.startTime} - ${s.endTime}
                                </div>

                                <div class="mt-2 text-[11px] font-black uppercase
                                    ${s.status == 'available' ? 'text-fifa' :
                                      s.status == 'booked' ? 'text-blue-600' :
                                      'text-amber-500'}">
                                    ${s.status}
                                </div>
                            </div>
                        </c:forEach>

                    </div>
                </div>

            </c:forEach>

        </div>
    </div>
</div>

<!-- AUTO SCROLL WEEK -->
<script>
const wrapper = document.getElementById("scrollWrapper");

wrapper.addEventListener("scroll", () => {
    const rightEdge = wrapper.scrollLeft + wrapper.clientWidth;
    const max = wrapper.scrollWidth;

    if (rightEdge >= max - 5) {
        window.location.href =
            "?fieldId=${field.fieldId}&date=${nextWeek}&direction=next";
    }

    if (wrapper.scrollLeft <= 5) {
        window.location.href =
            "?fieldId=${field.fieldId}&date=${prevWeek}&direction=prev";
    }
});
</script>

</body>
<jsp:include page="/View/Layout/Footer.jsp"/>
</html>
