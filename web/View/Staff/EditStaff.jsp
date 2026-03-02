<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉnh sửa nhân viên - FIFAFIELD</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; }
        .form-card { border-radius: 3rem; }
        .input-group:focus-within .icon-box { background-color: #008751; color: white; }
        .input-group:focus-within input, .input-group:focus-within select { border-color: #008751; background-color: white; }
    </style>
</head>
<body class="antialiased text-gray-900 flex flex-col min-h-screen">

<jsp:include page="/View/Layout/HeaderStaff.jsp"/>

<main class="flex-grow max-w-5xl mx-auto px-6 py-12 space-y-8 w-full">
    
    <!-- TOP NAVIGATION & TITLE -->
    <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div class="space-y-2">
            <button type="button" onclick="history.back()" 
               class="inline-flex items-center gap-2 text-[10px] font-black text-gray-400 hover:text-[#008751] transition-all uppercase tracking-widest mb-1">
                <i data-lucide="arrow-left" class="w-3 h-3"></i>
                HỦY VÀ QUAY LẠI
            </button>
            <h1 class="text-4xl font-black text-gray-900 tracking-tight uppercase leading-none">
                CẬP NHẬT <span class="text-[#008751]">HỒ SƠ</span>
            </h1>
            <p class="text-gray-400 font-bold uppercase text-[10px] tracking-[0.2em]">Thay đổi thông tin nhân sự trong hệ thống vận hành</p>
        </div>

        <div class="bg-white px-6 py-3 rounded-2xl border border-gray-100 shadow-sm flex items-center gap-3">
            <i data-lucide="user-cog" class="w-4 h-4 text-[#008751]"></i>
            <span class="text-[10px] font-black text-gray-500 uppercase tracking-widest">Chế độ chỉnh sửa Admin</span>
        </div>
    </div>

    <!-- ERROR ALERT -->
    <c:if test="${not empty error}">
        <div class="bg-rose-50 border border-rose-100 p-5 rounded-3xl flex items-center gap-4 animate-in fade-in slide-in-from-top-4">
            <div class="w-10 h-10 bg-rose-500 text-white rounded-xl flex items-center justify-center">
                <i data-lucide="alert-circle" class="w-5 h-5"></i>
            </div>
            <div>
                <p class="text-[10px] font-black text-rose-400 uppercase tracking-widest leading-none mb-1">Lỗi dữ liệu</p>
                <p class="text-sm font-bold text-rose-700 tracking-tight">${error}</p>
            </div>
        </div>
    </c:if>
<!-- MAIN FORM CARD -->
    <div class="bg-white form-card shadow-2xl shadow-gray-200/50 border border-gray-100 overflow-hidden relative">
        
        <!-- Ghost Decor -->
        <div class="absolute top-0 right-0 p-12 opacity-[0.02] pointer-events-none">
            <i data-lucide="edit" class="w-64 h-64"></i>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/staff/edit" class="p-10 md:p-16 space-y-12 relative z-10">
            <input type="hidden" name="userId" value="${staff.userId}" />
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-10">
                
                <!-- Section 1: Personal Info -->
                <div class="md:col-span-2 flex items-center gap-4">
                    <div class="w-10 h-1 bg-[#008751] rounded-full"></div>
                    <h3 class="text-[10px] font-black text-gray-300 uppercase tracking-[0.3em]">Thông tin cá nhân</h3>
                </div>

                <!-- Họ tên -->
                <div class="space-y-3 input-group">
                    <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Họ tên đầy đủ</label>
                    <div class="relative">
                        <div class="icon-box absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 transition-all duration-300">
                            <i data-lucide="user" class="w-4 h-4"></i>
                        </div>
                        <input type="text" name="fullName" value="${staff.fullName}" 
                               class="w-full pl-16 pr-6 py-4 bg-gray-50 border border-gray-100 rounded-[1.5rem] focus:outline-none transition-all font-bold text-gray-700" 
                               required />
                    </div>
                </div>

                <!-- Số điện thoại -->
                <div class="space-y-3 input-group">
                    <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Số điện thoại</label>
                    <div class="relative">
                        <div class="icon-box absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 transition-all duration-300">
                            <i data-lucide="phone" class="w-4 h-4"></i>
                        </div>
                        <input type="text" name="phone" value="${staff.phone}" 
                               class="w-full pl-16 pr-6 py-4 bg-gray-50 border border-gray-100 rounded-[1.5rem] focus:outline-none transition-all font-bold text-gray-700" />
                    </div>
                </div>

                <!-- Địa chỉ -->
                <div class="md:col-span-2 space-y-3 input-group">
<label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Địa chỉ thường trú</label>
                    <div class="relative">
                        <div class="icon-box absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 transition-all duration-300">
                            <i data-lucide="map-pin" class="w-4 h-4"></i>
                        </div>
                        <input type="text" name="address" value="${staff.address}" 
                               class="w-full pl-16 pr-6 py-4 bg-gray-50 border border-gray-100 rounded-[1.5rem] focus:outline-none transition-all font-bold text-gray-700" />
                    </div>
                </div>

                <!-- Section 2: Professional Info -->
                <div class="md:col-span-2 flex items-center gap-4 mt-4">
                    <div class="w-10 h-1 bg-[#008751] rounded-full"></div>
                    <h3 class="text-[10px] font-black text-gray-300 uppercase tracking-[0.3em]">Dữ liệu nhân sự</h3>
                </div>

                <!-- Mã nhân viên -->
                <div class="space-y-3 input-group">
                    <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Mã nhân viên (ID)</label>
                    <div class="relative">
                        <div class="icon-box absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 transition-all duration-300">
                            <i data-lucide="fingerprint" class="w-4 h-4"></i>
                        </div>
                        <input type="text" name="employeeCode" value="${staff.employeeCode}" 
                               class="w-full pl-16 pr-6 py-4 bg-gray-50 border border-gray-100 rounded-[1.5rem] focus:outline-none transition-all font-black text-[#008751] tracking-widest" 
                               required />
                    </div>
                </div>

                <!-- Ngày gia nhập -->
                <div class="space-y-3 input-group">
                    <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Ngày gia nhập</label>
                    <div class="relative">
                        <div class="icon-box absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 transition-all duration-300">
                            <i data-lucide="calendar" class="w-4 h-4"></i>
                        </div>
                        <input type="date" name="hireDate" value="${staff.hireDate}" 
                               class="w-full pl-16 pr-6 py-4 bg-gray-50 border border-gray-100 rounded-[1.5rem] focus:outline-none transition-all font-bold text-gray-700" />
                    </div>
                </div>

                <!-- Trạng thái -->
<div class="space-y-3 input-group">
                    <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Trạng thái hệ thống</label>
                    <div class="relative">
                        <div class="icon-box absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 transition-all duration-300">
                            <i data-lucide="activity" class="w-4 h-4"></i>
                        </div>
                        <select name="status" class="w-full pl-16 pr-10 py-4 bg-gray-50 border border-gray-100 rounded-[1.5rem] focus:outline-none transition-all font-black text-sm uppercase tracking-widest cursor-pointer" required>
                            <option value="active" <c:if test="${staff.status == 'active'}">selected</c:if>>ACTIVE</option>
                            <option value="inactive" <c:if test="${staff.status == 'inactive'}">selected</c:if>>INACTIVE</option>
                        </select>
                    </div>
                </div>

                <!-- Cơ sở -->
                <div class="space-y-3 input-group">
                    <label class="text-[10px] font-black text-gray-400 uppercase tracking-widest ml-1">Cơ sở công tác</label>
                    <div class="relative">
                        <div class="icon-box absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 transition-all duration-300">
                            <i data-lucide="building-2" class="w-4 h-4"></i>
                        </div>
                        <select name="locationId" class="w-full pl-16 pr-10 py-4 bg-gray-50 border border-gray-100 rounded-[1.5rem] focus:outline-none transition-all font-bold text-sm text-gray-700 cursor-pointer" required>
                            <option value="">-- Chọn cụm sân --</option>
                            <c:forEach items="${locations}" var="loc">
                                <option value="${loc.locationId}" <c:if test="${loc.locationId == staff.locationId}">selected</c:if>>${loc.locationName}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>

            <!-- Footer Buttons -->
            <div class="pt-12 border-t border-gray-50 flex flex-col md:flex-row gap-4">
                <button type="submit" 
                        class="flex-1 bg-[#008751] hover:bg-[#007043] text-white py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] shadow-2xl shadow-[#008751]/20 transition-all hover:-translate-y-1 flex items-center justify-center gap-3">
                    <i data-lucide="check-circle" class="w-4 h-4"></i>
                    LƯU THAY ĐỔI HỒ SƠ
                </button>
                <a href="${pageContext.request.contextPath}/staff/list"
class="flex-1 bg-gray-50 hover:bg-gray-100 text-gray-400 py-5 rounded-[2rem] font-black text-[10px] uppercase tracking-[0.2em] transition-all hover:-translate-y-1 flex items-center justify-center gap-3">
                    <i data-lucide="x-circle" class="w-4 h-4"></i>
                    HỦY VÀ QUAY LẠI
                </a>
            </div>
        </form>
    </div>
</main>

<jsp:include page="/View/Layout/Footer.jsp"/>

<script>
    lucide.createIcons();
</script>

</body>
</html>