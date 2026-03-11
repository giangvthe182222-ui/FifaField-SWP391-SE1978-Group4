<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa ca phân công - FIFAFIELD</title>
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
<body class="bg-slate-50 min-h-screen" style="font-family: 'Inter', sans-serif;">

<jsp:include page="/View/Layout/HeaderManager.jsp" />

<main class="max-w-4xl mx-auto px-6 py-12">
    <!-- HEADER SECTION -->
    <div class="mb-8">
        <button type="button" onclick="history.back()" class="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-300 bg-white text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-all mb-4">
            <i data-lucide="arrow-left" class="w-4 h-4"></i>
            Quay lại
        </button>
        <h1 class="text-3xl font-bold text-slate-900">Chỉnh sửa ca phân công</h1>
        <p class="text-slate-500 mt-2">Sửa thông tin ca đã phân cho nhân viên</p>
    </div>

    <!-- ERROR/SUCCESS MESSAGES -->
    <c:if test="${not empty success}">
        <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-3">
            <i data-lucide="check-circle" class="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0"></i>
            <div>
                <h3 class="font-semibold text-green-800">Thành công</h3>
                <p class="text-green-700 text-sm">${success}</p>
            </div>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
            <div>
                <h3 class="font-semibold text-red-800">Lỗi</h3>
                <p class="text-red-700 text-sm">${error}</p>
            </div>
        </div>
    </c:if>

    <div class="bg-white rounded-lg shadow-lg border border-slate-200 p-8">
        <form method="post" action="${pageContext.request.contextPath}/manager/staff-shift/edit" class="space-y-8" id="editShiftForm" onsubmit="return validateEditShiftForm()">
            <input type="hidden" name="origStaffId" value="${origStaffId}" />
            <input type="hidden" name="origFieldId" value="${origFieldId}" />
            <input type="hidden" name="origShiftId" value="${origShiftId}" />
            <input type="hidden" name="origWorkingDate" value="${origWorkingDate}" />

            <!-- CURRENT FIELD INFO -->
            <div class="bg-gradient-to-br from-slate-50 to-blue-50 rounded-lg p-6 border border-slate-200">
                <h3 class="text-sm font-semibold text-slate-600 uppercase tracking-wide mb-3 flex items-center gap-2">
                    <i data-lucide="info" class="w-4 h-4"></i>
                    Thông tin hiện tại
                </h3>
                <div class="flex items-center gap-4">
                    <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg flex items-center justify-center text-white shadow-md">
                        <i data-lucide="map" class="w-6 h-6"></i>
                    </div>
                    <div>
                        <p class="text-xs text-slate-500">Sân đang làm</p>
                        <p class="text-lg font-bold text-slate-900">${currentFieldName}</p>
                    </div>
                </div>
            </div>

            <!-- NEW FIELD SELECTION -->
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">
                    <div class="flex items-center gap-2">
                        <i data-lucide="grid-3x3" class="w-4 h-4 text-slate-500"></i>
                        Chọn sân mới <span class="text-red-500">*</span>
                    </div>
                </label>
                <select name="fieldId" id="fieldSelect" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-[#008751] focus:border-transparent transition-all bg-white cursor-pointer shadow-sm hover:border-[#008751]">
                    <option value="">-- Chọn sân --</option>
                    <c:forEach items="${fields}" var="f">
                        <option value="${f.fieldId}" ${fieldId == f.fieldId.toString() ? 'selected' : ''}>${f.fieldName}</option>
                    </c:forEach>
                </select>
                <p class="text-xs text-slate-500 mt-2 flex items-center gap-1">
                    <i data-lucide="alert-circle" class="w-3 h-3"></i>
                    Chỉ hiển thị các sân trong cùng cụm với nhân viên
                </p>
            </div>

            <!-- SHIFT & DATE RANGE -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-2">
                        <div class="flex items-center gap-2">
                            <i data-lucide="clock" class="w-4 h-4 text-slate-500"></i>
                            Ca làm việc <span class="text-red-500">*</span>
                        </div>
                    </label>
                    <select name="shiftId" class="select-custom w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-[#008751] focus:border-transparent transition-all bg-white cursor-pointer shadow-sm hover:border-[#008751]">
                        <option value="">-- Chọn ca --</option>
                        <c:forEach items="${shifts}" var="sh">
                            <option value="${sh.shiftId}" ${shiftId == sh.shiftId.toString() ? 'selected' : ''}>${sh.shiftName} (${sh.startTime} - ${sh.endTime})</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <!-- DATE RANGE -->
            <div class="border-t pt-6">
                <h3 class="text-sm font-semibold text-slate-700 uppercase tracking-wide mb-4 flex items-center gap-2">
                    <i data-lucide="calendar-range" class="w-4 h-4 text-slate-500"></i>
                    Khoảng thời gian làm việc
                </h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">
                            <div class="flex items-center gap-2">
                                <i data-lucide="calendar" class="w-4 h-4 text-green-600"></i>
                                Ngày bắt đầu <span class="text-red-500">*</span>
                            </div>
                        </label>
                        <input type="date" name="startDate" id="startDate" value="${startDate}" required
                               class="w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all shadow-sm" />
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-slate-700 mb-2">
                            <div class="flex items-center gap-2">
                                <i data-lucide="calendar" class="w-4 h-4 text-red-600"></i>
                                Ngày kết thúc <span class="text-red-500">*</span>
                            </div>
                        </label>
                        <input type="date" name="endDate" id="endDate" value="${endDate}" required
                               class="w-full px-4 py-3 border border-slate-300 rounded-lg text-slate-900 font-medium focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all shadow-sm" />
                    </div>
                </div>
                <p class="text-xs text-slate-500 mt-3 flex items-center gap-1">
                    <i data-lucide="info" class="w-3 h-3"></i>
                    Ca làm việc sẽ được áp dụng cho tất cả các ngày trong khoảng thời gian này
                </p>
            </div>

            <div id="formError" class="hidden p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
                <div>
                    <p class="text-red-700 font-medium" id="errorMessage"></p>
                </div>
            </div>

            <div class="flex gap-3 pt-6 border-t border-slate-200">
                <button type="submit" class="px-6 py-3 bg-[#008751] text-white rounded-lg font-semibold hover:bg-[#006d41] transition-all flex items-center gap-2 shadow-sm">
                    <i data-lucide="check" class="w-4 h-4"></i>
                    Lưu thay đổi
                </button>
                <a href="${pageContext.request.contextPath}/manager/staff-shifts" class="px-6 py-3 border border-slate-300 text-slate-700 rounded-lg font-semibold hover:bg-slate-50 transition-all flex items-center gap-2">
                    <i data-lucide="x" class="w-4 h-4"></i>
                    Hủy
                </a>
            </div>
        </form>
    </div>

    <script>
        lucide.createIcons();
        
        function validateEditShiftForm(){
            const fld = document.querySelector('[name="fieldId"]').value;
            const sh = document.querySelector('[name="shiftId"]').value;
            const start = document.querySelector('[name="startDate"]').value;
            const end = document.querySelector('[name="endDate"]').value;
            const err=document.getElementById('formError');
            const msg=document.getElementById('errorMessage');
            err.classList.add('hidden');
            msg.textContent='';
            
            if(!fld){
                msg.textContent='Vui lòng chọn sân bóng.';
                err.classList.remove('hidden');
                return false;
            }
            if(!sh){
                msg.textContent='Vui lòng chọn ca làm việc.';
                err.classList.remove('hidden');
                return false;
            }
            if(!start || !end){
                msg.textContent='Vui lòng chọn khoảng thời gian.';
                err.classList.remove('hidden');
                return false;
            }
            if(new Date(start) > new Date(end)){
                msg.textContent='Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.';
                err.classList.remove('hidden');
                return false;
            }
            const today = new Date();
            today.setHours(0,0,0,0);
            if(new Date(end) < today){
                msg.textContent='Ngày kết thúc không được ở quá khứ.';
                err.classList.remove('hidden');
                return false;
            }
            return true;
        }
    </script>

</main>

<jsp:include page="/View/Layout/FooterManager.jsp" />

<script>lucide.createIcons();</script>
</body>
</html>