<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh Toán Thiết Bị Bổ Sung - FifaField</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body class="bg-gray-50">
    <div class="min-h-screen py-8 px-4">
        <div class="max-w-2xl mx-auto">
            <!-- Header -->
            <div class="mb-8">
                <h1 class="text-3xl font-bold text-gray-900 flex items-center gap-2">
                    <i class="fas fa-credit-card text-blue-600"></i>
                    THANH TOÁN THIẾT BỊ BỔ SUNG
                </h1>
                <p class="text-gray-600 mt-1">Mã rental: <span class="font-mono font-bold">${rentalId}</span></p>
            </div>

            <!-- Flash Messages -->
            <c:if test="${not empty flash_success}">
                <div class="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-3">
                    <i class="fas fa-check-circle text-green-600 mt-1"></i>
                    <div>
                        <h3 class="font-semibold text-green-900">Thành công</h3>
                        <p class="text-green-800">${flash_success}</p>
                    </div>
                </div>
            </c:if>
            <c:if test="${not empty flash_error}">
                <div class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                    <i class="fas fa-exclamation-circle text-red-600 mt-1"></i>
                    <div>
                        <h3 class="font-semibold text-red-900">Lỗi</h3>
                        <p class="text-red-800">${flash_error}</p>
                    </div>
                </div>
            </c:if>

            <!-- Main Content -->
            <div class="bg-white rounded-lg shadow p-8">
                <c:if test="${not empty rental}">
                    <!-- Rental Summary -->
                    <div class="mb-8 pb-6 border-b-2">
                        <h2 class="text-xl font-bold text-gray-900 mb-4">Chi Tiết Đơn Hàng</h2>
                        
                        <div class="space-y-3">
                            <div class="flex justify-between">
                                <span class="text-gray-700">Sân:</span>
                                <span class="font-semibold text-gray-900">${rental.fieldId}</span>
                            </div>
                            <div class="flex justify-between">
                                <span class="text-gray-700">Ngày Tạo:</span>
                                <span class="font-semibold text-gray-900">
                                    <fmt:formatDate value="${createdTime}" pattern="dd/MM/yyyy HH:mm" />
                                </span>
                            </div>
                            <div class="flex justify-between">
                                <span class="text-gray-700">Trạng Thái:</span>
                                <span>
                                    <c:choose>
                                        <c:when test="${rental.status == 'pending'}">
                                            <span class="inline-block px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm font-semibold">Chờ Thanh Toán</span>
                                        </c:when>
                                        <c:when test="${rental.status == 'paid'}">
                                            <span class="inline-block px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-semibold">Đã Thanh Toán</span>
                                        </c:when>
                                        <c:when test="${rental.status == 'completed'}">
                                            <span class="inline-block px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-semibold">Hoàn Tất</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="inline-block px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-sm font-semibold">${rental.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="flex justify-between items-center pt-2">
                                <span class="text-gray-700 font-semibold">Đã Thêm:</span>
                                <span class="text-3xl font-bold text-blue-600">${equipmentCount}</span>
                                <span class="text-gray-600">dụng cụ</span>
                            </div>
                        </div>
                    </div>

                    <!-- Price Summary -->
                    <div class="mb-8 text-center">
                        <p class="text-sm text-gray-600 mb-2">Tổng Tiền</p>
                        <p class="text-5xl font-bold text-blue-600">
                            <fmt:formatNumber value="${rental.totalPrice}" pattern="#,##0" /> đ
                        </p>
                    </div>

                    <!-- Payment Info -->
                    <div class="bg-blue-50 rounded-lg p-6 mb-8">
                        <h3 class="font-bold text-gray-900 mb-3 flex items-center gap-2">
                            <i class="fas fa-info-circle text-blue-600"></i>
                            Thông Tin Thanh Toán
                        </h3>
                        <ul class="space-y-2 text-sm text-gray-700">
                            <li><strong>Phương thức:</strong> Chuyển khoản ngân hàng</li>
                            <li><strong>Thời hạn:</strong> 15 phút từ lúc tạo đơn</li>
                            <li><strong>Hết hạn:</strong> Tự động hủy đơn nếu không thanh toán</li>
                        </ul>
                    </div>

                    <!-- Action Buttons -->
                    <div class="flex gap-3">
                        <a href="${pageContext.request.contextPath}/payment?bookingId=${rental.originalBookingId}&resetDeadline=1&source=supplementary&rentalId=${rental.rentalId}" 
                           class="flex-1 px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition flex items-center justify-center gap-2">
                            <i class="fas fa-qrcode"></i>
                            Mở Trang Payment Có Sẵn
                        </a>
                        <a href="${pageContext.request.contextPath}/staff/locationBookings" 
                           class="px-6 py-3 bg-gray-300 text-gray-700 font-semibold rounded-lg hover:bg-gray-400 transition flex items-center justify-center gap-2">
                            <i class="fas fa-arrow-left"></i>
                            Quay Lại
                        </a>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</body>
</html>
