<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết quản lý - FIFA FIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
</head>
<body class="bg-slate-50 min-h-screen" style="font-family: 'Inter', sans-serif;">

<!-- HEADER -->
<header class="bg-white border-b border-slate-200 sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
        <div class="flex items-center gap-3">
            <div class="bg-emerald-600 p-2 rounded-xl text-white shadow">
                <i data-lucide="trophy" class="w-6 h-6"></i>
            </div>
            <h1 class="text-2xl font-semibold text-slate-900">
                FIFA<span class="text-emerald-600">FIELD</span>
            </h1>
        </div>
        <nav class="flex items-center gap-6">
            <a href="${pageContext.request.contextPath}/manager-list" class="text-slate-600 hover:text-slate-900">Quay lại</a>
        </nav>
    </div>
</header>

<!-- MAIN CONTENT -->
<main class="max-w-3xl mx-auto px-6 py-12">

    <!-- ERROR MESSAGE -->
    <c:if test="${not empty error}">
        <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
            <i data-lucide="alert-circle" class="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0"></i>
            <div>
                <h3 class="font-semibold text-red-800">Lỗi</h3>
                <p class="text-red-700 text-sm">${error}</p>
            </div>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty manager}">
            <div class="text-center py-12">
                <i data-lucide="user-x" class="w-16 h-16 text-slate-300 mx-auto mb-4"></i>
                <p class="text-slate-600">Không tìm thấy quản lý</p>
            </div>
        </c:when>
        <c:otherwise>
            <!-- HEADER SECTION -->
            <div class="bg-white rounded-lg shadow-sm border border-slate-200 p-8 mb-6">
                <div class="flex items-start justify-between mb-6">
                    <div>
                        <h1 class="text-3xl font-bold text-slate-900">${manager.fullName}</h1>
                        <p class="text-slate-500 mt-1">Quản lý vị trí: <span class="font-semibold text-emerald-600">${manager.locationName}</span></p>
                    </div>
                    <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold 
                        <c:choose>
                            <c:when test="${manager.status == 'active'}">
                                bg-green-100 text-green-800
                            </c:when>
                            <c:otherwise>
                                bg-gray-100 text-gray-800
                            </c:otherwise>
                        </c:choose>">
                        <span class="w-2.5 h-2.5 rounded-full mr-2 
                            <c:choose>
                                <c:when test="${manager.status == 'active'}">
                                    bg-green-600
                                </c:when>
                                <c:otherwise>
                                    bg-gray-600
                                </c:otherwise>
                            </c:choose>"></span>
                        <c:choose>
                            <c:when test="${manager.status == 'active'}">
                                Hoạt động
                            </c:when>
                            <c:otherwise>
                                Không hoạt động
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <!-- ACTION BUTTONS -->
                <div class="flex gap-3">
                    <a href="${pageContext.request.contextPath}/manager-edit?manager_id=${manager.userId}"
                       class="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-blue-700 transition flex items-center gap-2">
                        <i data-lucide="edit" class="w-4 h-4"></i>
                        Chỉnh sửa
                    </a>
                    <a href="#" onclick="if(confirm('Bạn chắc chắn muốn xóa?')) { window.location='${pageContext.request.contextPath}/manager-delete?manager_id=${manager.userId}'; }"
                       class="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-red-700 transition flex items-center gap-2">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                        Xóa
                    </a>
                </div>
            </div>

            <!-- INFO GRID -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

                <!-- PERSONAL INFO -->
                <div class="bg-white rounded-lg shadow-sm border border-slate-200 p-6">
                    <h2 class="text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
                        <i data-lucide="user" class="w-5 h-5 text-emerald-600"></i>
                        Thông tin cá nhân
                    </h2>
                    <div class="space-y-4">
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">Email</p>
                            <p class="text-slate-900 font-medium">${manager.email}</p>
                        </div>
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">Điện thoại</p>
                            <p class="text-slate-900 font-medium">${manager.phone}</p>
                        </div>
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">Giới tính</p>
                            <p class="text-slate-900 font-medium">${manager.gender}</p>
                        </div>
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">Địa chỉ</p>
                            <p class="text-slate-900 font-medium">${manager.address}</p>
                        </div>
                    </div>
                </div>

                <!-- WORK INFO -->
                <div class="bg-white rounded-lg shadow-sm border border-slate-200 p-6">
                    <h2 class="text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
                        <i data-lucide="briefcase" class="w-5 h-5 text-emerald-600"></i>
                        Thông tin công việc
                    </h2>
                    <div class="space-y-4">
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">Vị trí quản lý</p>
                            <p class="text-slate-900 font-medium">${manager.locationName}</p>
                        </div>
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">Ngày bắt đầu</p>
                            <p class="text-slate-900 font-medium">${manager.startDate}</p>
                        </div>
                        <div>
                            <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">Trạng thái</p>
                            <div class="flex items-center gap-2 mt-1">
                                <c:choose>
                                    <c:when test="${manager.status == 'active'}">
                                        <span class="w-2.5 h-2.5 bg-green-600 rounded-full"></span>
                                        <span class="text-green-700 font-medium">Đang hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="w-2.5 h-2.5 bg-gray-600 rounded-full"></span>
                                        <span class="text-gray-700 font-medium">Không hoạt động</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

            <!-- FOOTER NAVIGATION -->
            <div class="mt-8 flex gap-3">
                <a href="${pageContext.request.contextPath}/manager-list"
                   class="flex-1 text-center bg-slate-200 text-slate-700 px-4 py-2 rounded-lg font-semibold hover:bg-slate-300 transition">
                    Quay lại danh sách
                </a>
            </div>

        </c:otherwise>
    </c:choose>

</main>

<script>
    lucide.createIcons();
</script>

</body>
</html>
