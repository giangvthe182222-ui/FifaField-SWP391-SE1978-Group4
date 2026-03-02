<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân ca cho nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; }
        .select-custom {
            appearance: none;
            background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right 1rem center;
            background-size: 1.5em 1.5em;
            padding-right: 2.5rem;
        }
    </style>
</head>
<<<<<<< Updated upstream
<body class="bg-gray-50">
<jsp:include page="/View/Layout/Header.jsp" />
<main class="max-w-6xl mx-auto p-6">
    <h1 class="text-2xl font-bold mb-4">Phân ca cho nhân viên</h1>
    <c:if test="${not empty error}">
        <div class="text-red-600 mb-3">${error}</div>
    </c:if>
=======
<body class="bg-slate-50 min-h-screen" style="font-family: 'Inter', sans-serif;">

<jsp:include page="/View/Layout/HeaderManager.jsp" />
>>>>>>> Stashed changes

<!-- MAIN CONTENT -->
<main class="max-w-4xl mx-auto px-6 py-12">

    <!-- HEADER SECTION -->
    <div class="mb-8">
        <button type="button" onclick="history.back()" class="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-all mb-4">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            Quay lại
        </button>
        <h1 class="text-3xl font-bold text-slate-900">
            <c:choose>
                <c:when test="${editMode}">Chỉnh sửa ca làm việc</c:when>
                <c:otherwise>Phân ca cho nhân viên</c:otherwise>
            </c:choose>
        </h1>
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
            <div>
                <h3 class="font-semibold text-red-800">Lỗi</h3>
                <p class="text-red-700 text-sm">${error}</p>
            </div>
        </div>
 

    <!-- FORM SECTION -->
    <div class="bg-white rounded-lg shadow-sm border border-slate-200 p-8">
        <form id="assignShiftForm" method="post" action="${pageContext.request.contextPath}/manager/assign-shift" onsubmit="return validateAssignShiftForm()" class="space-y-6">
            <c:if test="${editMode}">
                <input type="hidden" name="origStaffId" value="${origStaffId}" />
                <input type="hidden" name="origFieldId" value="${origFieldId}" />
                <input type="hidden" name="origShiftId" value="${origShiftId}" />
                <input type="hidden" name="origWorkingDate" value="${origWorkingDate}" />
            </c:if>

            <!-- ROW 1: Nhân viên -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">
                    <div class="flex items-center gap-2">
                        <i data-lucide="user" class="w-4 h-4 text-slate-500"></i>
                        Chọn nhân viên <span class="text-red-500">*</span>
                    </div>
                </label>
                <select name="staffId" id="staffSelect" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-[#008751] focus:border-transparent transition-all bg-white cursor-pointer">
                    <option value="">-- Chọn nhân viên --</option>
                    <c:forEach items="${staffList}" var="s">
                        <option value="${s.userId}" ${staffId == s.userId.toString() ? 'selected' : ''}>${s.fullName} — ${s.locationName}</option>
                    </c:forEach>
                </select>
            </div>

            <!-- ROW 2: Cụm sân & Sân -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">
                        <div class="flex items-center gap-2">
                            <i data-lucide="map-pin" class="w-4 h-4 text-slate-500"></i>
                            Cụm sân <span class="text-red-500">*</span>
                        </div>
                    </label>
                    <select name="locationId" id="locationSelect" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-[#008751] focus:border-transparent transition-all bg-white cursor-pointer">
                        <option value="">-- Chọn cụm sân --</option>
                        <c:forEach items="${locations}" var="loc">
                            <option value="${loc.locationId}" ${locationId == loc.locationId.toString() ? 'selected' : ''}>${loc.locationName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">
                        <div class="flex items-center gap-2">
                            <i data-lucide="grid-3x3" class="w-4 h-4 text-slate-500"></i>
                            Sân bóng <span class="text-red-500">*</span>
                        </div>
                    </label>
                    <select name="fieldId" id="fieldSelect" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-[#008751] focus:border-transparent transition-all bg-white cursor-pointer">
                        <option value="">-- Chọn sân bóng --</option>
                        <c:forEach items="${fields}" var="f">
                            <option value="${f.fieldId}" data-location="${f.locationId}" ${fieldId == f.fieldId.toString() ? 'selected' : ''}>${f.fieldName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <!-- ROW 3: Ca làm việc & Ngày làm việc -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">
                        <div class="flex items-center gap-2">
                            <i data-lucide="clock" class="w-4 h-4 text-slate-500"></i>
                            Ca làm việc <span class="text-red-500">*</span>
                        </div>
                    </label>
                    <select name="shiftId" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-[#008751] focus:border-transparent transition-all bg-white cursor-pointer">
                        <option value="">-- Chọn ca làm việc --</option>
                        <c:forEach items="${shifts}" var="sh">
                            <option value="${sh.shiftId}" ${shiftId == sh.shiftId.toString() ? 'selected' : ''}>${sh.shiftName} (${sh.startTime} - ${sh.endTime})</option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">
                        <div class="flex items-center gap-2">
                            <i data-lucide="calendar" class="w-4 h-4 text-slate-500"></i>
                            Ngày làm việc <span class="text-red-500">*</span>
                        </div>
                    </label>
                    <input type="date" name="workingDate" id="workingDate" required value="${workingDate}" class="w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-[#008751] focus:border-transparent transition-all bg-white" />
                </div>
            </div>

            <!-- ERROR DISPLAY -->
            <div id="formError" class="hidden p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
                <div>
                    <p class="text-red-700 font-medium" id="errorMessage"></p>
                </div>
            </div>

            <!-- SUBMISSION BUTTONS -->
            <div class="flex gap-3 pt-6 border-t border-slate-200">
                <button type="submit" class="px-6 py-3 bg-[#008751] text-white rounded-lg font-semibold hover:bg-[#006d41] transition-all flex items-center gap-2 shadow-sm">
                    <i data-lucide="check" class="w-4 h-4"></i>
                    <c:choose>
                        <c:when test="${editMode}">Cập nhật</c:when>
                        <c:otherwise>Phân ca</c:otherwise>
                    </c:choose>
                </button>
                <button type="reset" class="px-6 py-3 border border-slate-300 text-slate-700 rounded-lg font-semibold hover:bg-slate-50 transition-all flex items-center gap-2">
                    <i data-lucide="rotate-ccw" class="w-4 h-4"></i>
                    Làm lại
                </button>
            </div>
        </form>
    </div>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />

<script>
    lucide.createIcons();

    // Filter fields by location
    const locationSelect = document.getElementById('locationSelect');
    const fieldSelect = document.getElementById('fieldSelect');
    const fieldOptions = Array.from(fieldSelect.options).slice(1); // Exclude default option

    locationSelect.addEventListener('change', function() {
        const selectedLocationId = this.value;
        
        // Reset field select
        fieldSelect.innerHTML = '<option value="">-- Chọn sân bóng --</option>';
        
        if (selectedLocationId) {
            // Filter and add matching fields
            const matchingFields = fieldOptions.filter(opt => opt.getAttribute('data-location') === selectedLocationId);
            matchingFields.forEach(opt => {
                const newOption = document.createElement('option');
                newOption.value = opt.value;
                newOption.textContent = opt.textContent;
                newOption.setAttribute('data-location', opt.getAttribute('data-location'));
                fieldSelect.appendChild(newOption);
            });
        }
    });

    function validateAssignShiftForm() {
        const staffId = document.querySelector('[name="staffId"]').value;
        const locationId = document.querySelector('[name="locationId"]').value;
        const fieldId = document.querySelector('[name="fieldId"]').value;
        const shiftId = document.querySelector('[name="shiftId"]').value;
        const workingDate = document.querySelector('[name="workingDate"]').value;
        const errorEl = document.getElementById('formError');
        const errorMsg = document.getElementById('errorMessage');

        errorEl.classList.add('hidden');
        errorMsg.textContent = '';

        if (!staffId) {
            errorMsg.textContent = 'Vui lòng chọn nhân viên.';
            errorEl.classList.remove('hidden');
            return false;
        }
        if (!locationId) {
            errorMsg.textContent = 'Vui lòng chọn cụm sân.';
            errorEl.classList.remove('hidden');
            return false;
        }
        if (!fieldId) {
            errorMsg.textContent = 'Vui lòng chọn sân bóng.';
            errorEl.classList.remove('hidden');
            return false;
        }
        if (!shiftId) {
            errorMsg.textContent = 'Vui lòng chọn ca làm việc.';
            errorEl.classList.remove('hidden');
            return false;
        }
        if (!workingDate) {
            errorMsg.textContent = 'Vui lòng chọn ngày làm việc.';
            errorEl.classList.remove('hidden');
            return false;
        }

        const selected = new Date(workingDate);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selected < today) {
            errorMsg.textContent = 'Ngày làm việc không được ở quá khứ.';
            errorEl.classList.remove('hidden');
            return false;
        }

        return true;
    }
</script>

</body>
</html>
