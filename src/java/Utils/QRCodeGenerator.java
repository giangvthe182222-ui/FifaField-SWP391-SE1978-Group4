package Utils;

import java.util.UUID;

/**
 * VietQR Code Generator - tạo QR code dùng cho thanh toán ngân hàng Using ZXing
 * library (com.google.zxing)
 *
 * Format: https://cdn.qrserver.com/api/render/qr-code/
 */
public class QRCodeGenerator {

    // MB Bank info
    public static final String BANK_CODE = "MB";
    public static final String ACCOUNT_NUMBER = "0974288256";
    public static final String ACCOUNT_NAME = "FIFA FIELD";

    /**
     * Generate VietQR format string for a booking payment Format:
     * "bank=VCB&acc=123456789&amount=500000&content=FFF_BOOKING_<booking_id>"
     */
    public static String generateVietQRString(UUID bookingId, long amountInVND) {
        String content = "FFF_BOOKING_" + bookingId.toString().replace("-", "").toUpperCase();

        StringBuilder qrData = new StringBuilder();
        qrData.append("bank=").append(BANK_CODE);
        qrData.append("&acc=").append(ACCOUNT_NUMBER);
        qrData.append("&amount=").append(amountInVND);
        qrData.append("&content=").append(content);

        return qrData.toString();
    }

    /**
     * Generate QR Code URL using QRServer API This uses free CDN service:
     * https://qrserver.com/
     */
    public static String generateQRCodeURL(String vietQRString) {
        try {
            String encodedQR = java.net.URLEncoder.encode(vietQRString, "UTF-8");
            return "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + encodedQR;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Alternative: Generate QR Code with detailed VietQR info This is the
     * standard VietQR payload format used by Vietnamese banks
     */
    public static String generateDetailedVietQRString(UUID bookingId, long amountInVND, String bankAccount) {
        String content = "FFF_BOOKING_" + bookingId.toString().replace("-", "").toUpperCase();

        // Standard VietQR format: "bank|account|amount|content"
        return BANK_CODE + "|" + bankAccount + "|" + amountInVND + "|" + content;
    }

    /**
     * Test method to generate sample QR
     */
    public static void main(String[] args) {
        UUID testBookingId = UUID.randomUUID();
        long testAmount = 500000; // 500,000 VND

        String vietQRString = generateVietQRString(testBookingId, testAmount);
        System.out.println("VietQR String: " + vietQRString);

        String qrUrl = generateQRCodeURL(vietQRString);
        System.out.println("QR Code URL: " + qrUrl);

        String detailedQR = generateDetailedVietQRString(testBookingId, testAmount, ACCOUNT_NUMBER);
        System.out.println("Detailed VietQR: " + detailedQR);
    }
}
