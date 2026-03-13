package Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PayOSClient {

    private static final String API_BASE_URL = "https://api-merchant.payos.vn";

    private final String clientId;
    private final String apiKey;
    private final String checksumKey;
    private final String returnUrl;
    private final String cancelUrl;

    public PayOSClient() {
        this.clientId = getConfig("PAYOS_CLIENT_ID", "FFF_PAYOS_CLIENT_ID");
        this.apiKey = getConfig("PAYOS_API_KEY", "FFF_PAYOS_API_KEY");
        this.checksumKey = getConfig("PAYOS_CHECKSUM_KEY", "FFF_PAYOS_CHECKSUM_KEY");
        this.returnUrl = getConfig("PAYOS_RETURN_URL", "FFF_PAYOS_RETURN_URL");
        this.cancelUrl = getConfig("PAYOS_CANCEL_URL", "FFF_PAYOS_CANCEL_URL");
    }

    public boolean isConfigured() {
        return notBlank(clientId)
                && notBlank(apiKey)
                && notBlank(checksumKey);
    }

    public String getMissingConfigSummary() {
        List<String> missing = new ArrayList<>();
        if (!notBlank(clientId)) {
            missing.add("PAYOS_CLIENT_ID");
        }
        if (!notBlank(apiKey)) {
            missing.add("PAYOS_API_KEY");
        }
        if (!notBlank(checksumKey)) {
            missing.add("PAYOS_CHECKSUM_KEY");
        }
        if (!notBlank(returnUrl)) {
            missing.add("PAYOS_RETURN_URL");
        }
        if (!notBlank(cancelUrl)) {
            missing.add("PAYOS_CANCEL_URL");
        }
        if (missing.isEmpty()) {
            return "none";
        }
        return String.join(", ", missing);
    }

    public PaymentLinkResponse createPaymentLink(long orderCode,
                                                 BigDecimal amount,
                                                 String description,
                                                 UUID bookingId,
                                                 LocalDateTime paymentDeadline) {
        return createPaymentLink(orderCode, amount, description, bookingId, paymentDeadline, null, null);
    }

    public PaymentLinkResponse createPaymentLink(long orderCode,
                                                 BigDecimal amount,
                                                 String description,
                                                 UUID bookingId,
                                                 LocalDateTime paymentDeadline,
                                                 String returnUrlOverride,
                                                 String cancelUrlOverride) {
        if (!isConfigured()) {
            return PaymentLinkResponse.failure("Missing payOS configuration");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentLinkResponse.failure("Invalid amount");
        }

        int amountInt;
        try {
            amountInt = amount.intValueExact();
        } catch (ArithmeticException ex) {
            amountInt = amount.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }

        long expiredAt = paymentDeadline == null
                ? LocalDateTime.now().plusMinutes(15).atZone(ZoneId.systemDefault()).toEpochSecond()
                : paymentDeadline.atZone(ZoneId.systemDefault()).toEpochSecond();

        String returnBase = firstNonBlank(returnUrlOverride, returnUrl);
        String cancelBase = firstNonBlank(cancelUrlOverride, cancelUrl);
        if (!notBlank(returnBase) || !notBlank(cancelBase)) {
            return PaymentLinkResponse.failure("Missing payOS return/cancel URL configuration");
        }

        String returnUrlResolved = appendBookingQueryParam(returnBase, bookingId);
        String cancelUrlResolved = appendBookingQueryParam(cancelBase, bookingId);

        String signatureData = "amount=" + amountInt
                + "&cancelUrl=" + cancelUrlResolved
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + returnUrlResolved;

        String signature;
        try {
            signature = hmacSha256Hex(signatureData, checksumKey);
        } catch (Exception ex) {
            return PaymentLinkResponse.failure("Cannot sign payOS request");
        }

        String payload = "{"
                + "\"orderCode\":" + orderCode + ","
                + "\"amount\":" + amountInt + ","
                + "\"description\":\"" + escapeJson(description) + "\","
                + "\"returnUrl\":\"" + escapeJson(returnUrlResolved) + "\","
                + "\"cancelUrl\":\"" + escapeJson(cancelUrlResolved) + "\","
                + "\"expiredAt\":" + expiredAt + ","
                + "\"signature\":\"" + signature + "\""
                + "}";

        HttpResponse response = sendRequest("POST", API_BASE_URL + "/v2/payment-requests", payload);
        if (response.statusCode < 200 || response.statusCode >= 300) {
            return PaymentLinkResponse.failure("HTTP " + response.statusCode + ": " + response.body);
        }

        String code = extractJsonString(response.body, "code");
        if (code != null && !"00".equals(code)) {
            String desc = firstNonBlank(extractJsonString(response.body, "desc"), "payOS rejected request");
            return PaymentLinkResponse.failure(desc);
        }

        PaymentLinkResponse result = new PaymentLinkResponse();
        result.success = true;
        result.orderCode = orderCode;
        result.qrCode = extractJsonString(response.body, "qrCode");
        result.checkoutUrl = extractJsonString(response.body, "checkoutUrl");
        result.accountNumber = extractJsonString(response.body, "accountNumber");
        result.bankCode = extractJsonString(response.body, "bin");

        if (!notBlank(result.qrCode)) {
            return PaymentLinkResponse.failure("payOS did not return qrCode");
        }

        return result;
    }

    public PaymentStatusResponse getPaymentStatus(long orderCode) {
        if (!isConfigured()) {
            return PaymentStatusResponse.failure("Missing payOS configuration");
        }

        HttpResponse response = sendRequest("GET", API_BASE_URL + "/v2/payment-requests/" + orderCode, null);
        if (response.statusCode < 200 || response.statusCode >= 300) {
            return PaymentStatusResponse.failure("HTTP " + response.statusCode);
        }

        String code = extractJsonString(response.body, "code");
        if (code != null && !"00".equals(code)) {
            String desc = firstNonBlank(extractJsonString(response.body, "desc"), "payOS status check failed");
            return PaymentStatusResponse.failure(desc);
        }

        PaymentStatusResponse result = new PaymentStatusResponse();
        result.success = true;
        result.status = firstNonBlank(extractJsonString(response.body, "status"), "PENDING");
        result.checkoutUrl = extractJsonString(response.body, "checkoutUrl");
        result.qrCode = extractJsonString(response.body, "qrCode");
        result.accountNumber = extractJsonString(response.body, "accountNumber");
        result.bankCode = extractJsonString(response.body, "bin");
        return result;
    }

    private HttpResponse sendRequest(String method, String endpoint, String body) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-client-id", clientId);
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            if (body != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int statusCode = conn.getResponseCode();
            InputStream is = statusCode >= 200 && statusCode < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            String responseBody = readAll(is);
            return new HttpResponse(statusCode, responseBody);

        } catch (Exception ex) {
            return new HttpResponse(500, ex.getMessage() == null ? "payOS request failed" : ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String readAll(InputStream is) {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception ignored) {
        }
        return sb.toString();
    }

    private String appendBookingQueryParam(String baseUrl, UUID bookingId) {
        try {
            String encoded = URLEncoder.encode(bookingId.toString(), StandardCharsets.UTF_8.name());
            String separator = baseUrl.contains("?") ? "&" : "?";
            return baseUrl + separator + "bookingId=" + encoded;
        } catch (Exception ex) {
            return baseUrl;
        }
    }

    private String hmacSha256Hex(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String item = Integer.toHexString(0xff & b);
            if (item.length() == 1) {
                hex.append('0');
            }
            hex.append(item);
        }
        return hex.toString();
    }

    private String extractJsonString(String json, String fieldName) {
        if (!notBlank(json) || !notBlank(fieldName)) {
            return null;
        }
        Pattern p = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (notBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String getConfig(String... keys) {
        if (keys == null) {
            return null;
        }
        for (String key : keys) {
            String value = firstNonBlank(
                    System.getenv(key),
                    System.getProperty(key),
                    System.getProperty(key.toLowerCase())
            );
            if (notBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static class HttpResponse {
        private final int statusCode;
        private final String body;

        private HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    public static class PaymentLinkResponse {
        private boolean success;
        private String message;
        private long orderCode;
        private String checkoutUrl;
        private String qrCode;
        private String bankCode;
        private String accountNumber;

        static PaymentLinkResponse failure(String message) {
            PaymentLinkResponse r = new PaymentLinkResponse();
            r.success = false;
            r.message = message;
            return r;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public long getOrderCode() {
            return orderCode;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }

        public String getQrCode() {
            return qrCode;
        }

        public String getBankCode() {
            return bankCode;
        }

        public String getAccountNumber() {
            return accountNumber;
        }
    }

    public static class PaymentStatusResponse {
        private boolean success;
        private String message;
        private String status;
        private String checkoutUrl;
        private String qrCode;
        private String bankCode;
        private String accountNumber;

        static PaymentStatusResponse failure(String message) {
            PaymentStatusResponse r = new PaymentStatusResponse();
            r.success = false;
            r.message = message;
            return r;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }

        public String getQrCode() {
            return qrCode;
        }

        public String getBankCode() {
            return bankCode;
        }

        public String getAccountNumber() {
            return accountNumber;
        }
    }
}
