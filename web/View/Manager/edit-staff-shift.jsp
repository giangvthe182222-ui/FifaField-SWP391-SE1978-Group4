<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa ca phân công - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 min-h-screen">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-4xl mx-auto px-6 py-12">
    <div class="mb-8">
        <button type="button" onclick="history.back()" class="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-all mb-4">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            Quay lại
        </button>
        <h1 class="text-3xl font-bold text-slate-900">Chỉnh sửa ca phân công</h1>
        <p class="text-slate-500 mt-2">Sửa thông tin ca đã phân cho nhân viên</p>
    </div>

    <div class="bg-white rounded-lg shadow-sm border border-slate-200 p-8">
        <form method="post" action="${pageContext.request.contextPath}/manager/staff-shift/edit" class="space-y-6" id="editShiftForm" onsubmit="return validateEditShiftForm()">
            <input type="hidden" name="origStaffId" value="${origStaffId}" />
            <input type="hidden" name="origFieldId" value="${origFieldId}" />
            <input type="hidden" name="origShiftId" value="${origShiftId}" />
            <input type="hidden" name="origWorkingDate" value="${origWorkingDate}" />

            <!-- nhân viên (không cho thay đổi) -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">Nhân viên</label>
                <div class="px-4 py-3 border border-slate-200 rounded-lg bg-slate-50 text-slate-900">
                    <c:forEach items="${staffList}" var="s">
                        <c:if test="${s.userId eq staffId}">
                            <span class="font-semibold">${s.fullName}</span>
                        </c:if>
                    </c:forEach>
                </div>
            </div>

            <!-- location & field -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Cụm sân</label>
                    <select name="locationId" id="locationSelect" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium">
                        <option value="">-- Chọn cụm sân --</option>
                        <c:forEach items="${locations}" var="loc">
                            <option value="${loc.locationId}" ${locationId == loc.locationId.toString() ? 'selected' : ''}>${loc.locationName}</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Sân bóng</label>
                    <select name="fieldId" id="fieldSelect" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium">
                        <option value="">-- Chọn sân --</option>
                        <c:forEach items="${fields}" var="f">
                            <option value="${f.fieldId}" data-location="${f.locationId}" ${fieldId == f.fieldId.toString() ? 'selected' : ''}>${f.fieldName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <!-- shift & date -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Ca làm việc</label>
                    <select name="shiftId" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium">
                        <option value="">-- Chọn ca --</option>
                        <c:forEach items="${shifts}" var="sh">
                            <option value="${sh.shiftId}" ${shiftId == sh.shiftId.toString() ? 'selected' : ''}>${sh.shiftName} (${sh.startTime} - ${sh.endTime})</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">Ngày làm việc</label>
                    <input type="date" value="${workingDate}" disabled class="w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium bg-slate-100" />
                </div>
            </div>

            <div id="formError" class="hidden p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
                <div>
                    <p class="text-red-700 font-medium" id="errorMessage"></p>
                </div>
            </div>

            <div class="flex gap-3 pt-6 border-t border-slate-200">
                <button type="submit" class="px-6 py-3 bg-[#008751] text-white rounded-lg font-semibold">Lưu</button>
                <a href="${pageContext.request.contextPath}/manager/staff-shifts" class="px-6 py-3 border rounded-lg">Hủy</a>
            </div>
        </form>
    </div>

    <script>
        lucide.createIcons();
        const locationSelect = document.getElementById('locationSelect');
        const fieldSelect = document.getElementById('fieldSelect');
        const fieldOptions = Array.from(fieldSelect.options).slice(1);
        locationSelect.addEventListener('change', function() {
            const sel = this.value;
            fieldSelect.innerHTML = '<option value="">-- Chọn sân --</option>';
            if (sel) {
                const matching = fieldOptions.filter(o=>o.getAttribute('data-location')===sel);
                matching.forEach(o=>{
                    const n = document.createElement('option'); n.value=o.value; n.textContent=o.textContent; n.setAttribute('data-location',o.getAttribute('data-location')); fieldSelect.appendChild(n);
                });
            }
        });
        function validateEditShiftForm(){
            const loc = document.querySelector('[name="locationId"]').value;
            const fld = document.querySelector('[name="fieldId"]').value;
            const sh = document.querySelector('[name="shiftId"]').value;
            const err=document.getElementById('formError'); const msg=document.getElementById('errorMessage'); err.classList.add('hidden'); msg.textContent='';
            if(!loc){msg.textContent='Chọn cụm sân.';err.classList.remove('hidden');return false;}
            if(!fld){msg.textContent='Chọn sân bóng.';err.classList.remove('hidden');return false;}
            if(!sh){msg.textContent='Chọn ca.';err.classList.remove('hidden');return false;}
            return true;
        }
    </script>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />

<script>lucide.createIcons();</script>
</body>
</html>