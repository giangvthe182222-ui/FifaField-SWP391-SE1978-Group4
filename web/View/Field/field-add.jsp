<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm Sân | FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-100 min-h-screen">

<jsp:include page="/View/Layout/HeaderAdmin.jsp" />

<div class="flex items-center justify-center min-h-[calc(100vh-120px)]">
    <div class="bg-white w-full max-w-xl rounded-[2.5rem] p-10 shadow-2xl">
        <h1 class="text-3xl font-black text-slate-900 uppercase mb-2">Thêm Sân Mới</h1>

        <c:if test="${not empty error}">
            <div class="bg-red-100 text-red-700 p-4 rounded-xl mb-6 font-mono text-sm">
                <c:out value="${error}" escapeXml="false"/>
            </div>
        </c:if>

        <form method="post" enctype="multipart/form-data" class="space-y-6">
            <c:choose>
                <c:when test="${not empty locationId}">
                    <input type="hidden" name="locationId" value="${locationId}"/>
                    <input type="hidden" name="location_id" value="${location_id}"/>
                    <div class="text-sm text-slate-500 mb-2">Thêm vào cụm: <strong>${not empty locationId ? locationId : ''}</strong></div>
                </c:when>
                <c:otherwise>
                    <label class="text-xs font-black uppercase text-slate-400">Chọn cụm sân</label>
                    <select name="locationId" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold mb-4">
                        <c:forEach var="loc" items="${locationsList}">
                            <option value="${loc.locationId}">${loc.locationName} - ${loc.address}</option>
                        </c:forEach>
                    </select>
                </c:otherwise>
            </c:choose>

            <div>
                  <label class="text-xs font-black uppercase text-slate-400">Tên sân</label>
                  <input name="fieldName" required value="${not empty fieldName ? fieldName : param.fieldName}"
                       class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div>
                    <label class="text-xs font-black uppercase text-slate-400">Loại sân</label>
                    <select name="fieldType" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                        <option value="7-a-side" ${fieldType == '7-a-side' || param.fieldType == '7-a-side' ? 'selected' : ''}>7-a-side</option>
                        <option value="11-a-side" ${fieldType == '11-a-side' || param.fieldType == '11-a-side' ? 'selected' : ''}>11-a-side</option>
                    </select>
                </div>

                <div>
                    <label class="text-xs font-black uppercase text-slate-400">Ảnh sân</label>
                    <input id="imageInputFieldAdd" type="file" name="image" accept="image/*"
                           class="w-full px-5 py-3 mt-2 rounded-2xl bg-slate-50 border font-bold"/>
                    <div class="mt-4">
                        <img id="previewFieldAdd" src="" alt="Preview" class="w-full h-40 object-cover rounded-2xl hidden" />
                        <div id="previewFieldAddPlaceholder" class="w-full h-40 rounded-2xl bg-slate-50 border flex items-center justify-center text-slate-400 mt-2">Chưa chọn ảnh</div>
                    </div>
                </div>
            </div>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Trạng thái</label>
                <select name="status" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                    <option value="ACTIVE" ${status == 'ACTIVE' || param.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${status == 'INACTIVE' || param.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                </select>
            </div>

            <div>
                <label class="text-xs font-black uppercase text-slate-400">Tình trạng</label>
                <select name="condition" class="w-full px-5 py-4 mt-2 rounded-2xl bg-slate-50 border font-bold">
                    <option value="GOOD" ${condition == 'GOOD' || param.condition == 'GOOD' ? 'selected' : ''}>GOOD</option>
                    <option value="FAIR" ${condition == 'FAIR' || param.condition == 'FAIR' ? 'selected' : ''}>FAIR</option>
                    <option value="BAD" ${condition == 'BAD' || param.condition == 'BAD' ? 'selected' : ''}>BAD</option>
                </select>
            </div>

            <div class="flex gap-4 pt-4">
                <c:choose>
                    <c:when test="${not empty locationId}">
                        <a href="${pageContext.request.contextPath}/fields?location_id=${locationId}"
                           class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">Hủy</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/locations"
                           class="flex-1 text-center py-4 rounded-2xl border font-black uppercase">Hủy</a>
                    </c:otherwise>
                </c:choose>
                <button class="flex-1 bg-[#008751] text-white py-4 rounded-2xl font-black uppercase shadow-xl">Tạo sân</button>
            </div>
            
            <!-- Sample week pricing for schedule generation -->
            <div class="mt-6">
                <h2 class="text-sm font-black uppercase text-slate-400 mb-2">Mẫu giá theo tuần (6 khung/1 ngày — 1h30 mỗi khung)</h2>
                <input type="hidden" name="sampleWeek" id="sampleWeek" />
                <div class="overflow-x-auto">
                    <table class="w-full text-sm border-collapse">
                        <thead>
                            <tr class="text-left">
                                <th class="p-2">Ngày</th>
                                <th class="p-2">Khung 1</th>
                                <th class="p-2">Khung 2</th>
                                <th class="p-2">Khung 3</th>
                                <th class="p-2">Khung 4</th>
                                <th class="p-2">Khung 5</th>
                                <th class="p-2">Khung 6</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td class="p-2 font-bold">Mon</td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_0_0" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_0_1" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_0_2" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_0_3" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_0_4" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_0_5" /></td>
                            </tr>
                            <tr>
                                <td class="p-2 font-bold">Tue</td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_1_0" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_1_1" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_1_2" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_1_3" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_1_4" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_1_5" /></td>
                            </tr>
                            <tr>
                                <td class="p-2 font-bold">Wed</td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_2_0" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_2_1" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_2_2" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_2_3" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_2_4" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_2_5" /></td>
                            </tr>
                            <tr>
                                <td class="p-2 font-bold">Thu</td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_3_0" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_3_1" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_3_2" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_3_3" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_3_4" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_3_5" /></td>
                            </tr>
                            <tr>
                                <td class="p-2 font-bold">Fri</td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_4_0" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_4_1" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_4_2" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_4_3" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_4_4" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_4_5" /></td>
                            </tr>
                            <tr>
                                <td class="p-2 font-bold">Sat</td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_5_0" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_5_1" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_5_2" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_5_3" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_5_4" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_5_5" /></td>
                            </tr>
                            <tr>
                                <td class="p-2 font-bold">Sun</td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_6_0" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_6_1" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_6_2" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_6_3" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_6_4" /></td>
                                <td class="p-2"><input type="number" step="0.01" class="w-full px-3 py-2 rounded" id="p_6_5" /></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="text-xs text-slate-500 mt-2">Gợi ý theo loại sân sẽ được điền tự động. Bạn có thể thay đổi từng ô trước khi tạo sân.</div>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/View/Layout/Footer.jsp" />

<script>
    const inputFA = document.getElementById('imageInputFieldAdd');
    const previewFA = document.getElementById('previewFieldAdd');
    const phFA = document.getElementById('previewFieldAddPlaceholder');
    if (inputFA) {
        inputFA.addEventListener('change', function (e) {
            const file = e.target.files && e.target.files[0];
            if (file) {
                previewFA.src = URL.createObjectURL(file);
                previewFA.classList.remove('hidden');
                phFA.style.display = 'none';
            } else {
                previewFA.src = '';
                previewFA.classList.add('hidden');
                phFA.style.display = 'flex';
            }
        });
    }
</script>

<script>
    // Prefill sample week suggestions and serialize values before submit
    function getDefaultsForType(type) {
        // prices are in basic currency unit (e.g., VND)
        if (type === '11-a-side') {
            // higher base prices for 11-a-side
            return [
                [300000, 300000, 350000, 400000, 450000, 400000], // Mon
                [300000, 300000, 350000, 400000, 450000, 400000], // Tue
                [300000, 300000, 350000, 400000, 450000, 400000], // Wed
                [300000, 300000, 350000, 400000, 450000, 400000], // Thu
                [350000, 350000, 400000, 450000, 500000, 450000], // Fri
                [400000, 400000, 450000, 500000, 600000, 550000], // Sat
                [350000, 350000, 400000, 450000, 500000, 450000]  // Sun
            ];
        }
        // default 7-a-side
        return [
            [150000,150000,180000,200000,220000,200000],
            [150000,150000,180000,200000,220000,200000],
            [150000,150000,180000,200000,220000,200000],
            [150000,150000,180000,200000,220000,200000],
            [170000,170000,200000,230000,250000,230000],
            [200000,200000,240000,280000,320000,300000],
            [180000,180000,220000,250000,280000,260000]
        ];
    }

    function fillDefaults() {
        const typeSel = document.querySelector('select[name="fieldType"]');
        const type = typeSel ? typeSel.value : '7-a-side';
        const defaults = getDefaultsForType(type);
        for (let d=0; d<7; d++) {
            for (let s=0; s<6; s++) {
                const el = document.getElementById('p_' + d + '_' + s);
                if (el) el.value = defaults[d][s];
            }
        }
    }

    // serialize into comma-separated CSV (42 values)
    function serializeSampleWeek() {
        const vals = [];
        for (let d=0; d<7; d++) for (let s=0; s<6; s++) {
            const el = document.getElementById('p_' + d + '_' + s);
            vals.push(el && el.value ? el.value : '0');
        }
        document.getElementById('sampleWeek').value = vals.join(',');
    }

    // fill defaults on load
    fillDefaults();

    // refill defaults if field type changes
    const typeSel = document.querySelector('select[name="fieldType"]');
    if (typeSel) typeSel.addEventListener('change', fillDefaults);

    // before submit, set hidden sampleWeek
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e){
            serializeSampleWeek();
        });
    }
</script>

</body>
</html>
