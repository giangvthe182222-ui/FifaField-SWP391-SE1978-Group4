package Utils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
     * Generate a short transfer content that can be matched back to a booking.
     */
    public static String generateTransferContent(UUID bookingId) {
        String compactId = bookingId.toString().replace("-", "").toUpperCase();
        return "FFFBOOK" + compactId.substring(0, 12);
    }

    /**
     * Generate a bank-scannable VietQR image URL with exact amount and transfer content.
     */
    public static String generateQRCodeURL(BigDecimal amount, String transferContent) {
        try {
            String encodedContent = URLEncoder.encode(transferContent, StandardCharsets.UTF_8.name());
            String encodedAccountName = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8.name());
            long roundedAmount = amount == null ? 0L : amount.longValue();
            return "https://img.vietqr.io/image/"
                    + BANK_CODE + "-" + ACCOUNT_NUMBER + "-compact2.png"
                    + "?amount=" + roundedAmount
                    + "&addInfo=" + encodedContent
                    + "&accountName=" + encodedAccountName;
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
        String content = generateTransferContent(bookingId);

        // Standard VietQR format: "bank|account|amount|content"
        return BANK_CODE + "|" + bankAccount + "|" + amountInVND + "|" + content;
    }

    /**
     * Test method to generate sample QR
     */
    public static void main(String[] args) {
        UUID testBookingId = UUID.randomUUID();
        long testAmount = 500000; // 500,000 VND

        String transferContent = generateTransferContent(testBookingId);
        System.out.println("Transfer content: " + transferContent);

        String qrUrl = generateQRCodeURL(BigDecimal.valueOf(testAmount), transferContent);
        System.out.println("QR Code URL: " + qrUrl);

        String detailedQR = generateDetailedVietQRString(testBookingId, testAmount, ACCOUNT_NUMBER);
        System.out.println("Detailed VietQR: " + detailedQR);
    }
}
