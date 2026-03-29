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

/**
 * HTTP client wrapper for PayOS (Vietnamese payment gateway).
 * Handles auth headers, HMAC signing, and response parsing — all in one place.
 */
public class PayOSClient {

    private static final String API_BASE_URL = "https://api-merchant.payos.vn";

    // Everything PayOS needs 
    private final String clientId;     
    private final String apiKey;       
    private final String checksumKey; 
    private final String returnUrl;    
    private final String cancelUrl;    

    /**
     * Reads all credentials from env vars.
     * Two fallback names per key (e.g. PAYOS_CLIENT_ID → FFF_PAYOS_CLIENT_ID)
     * so different deploy environments can use different naming conventions.
     */
    public PayOSClient() {
        this.clientId    = getConfig("PAYOS_CLIENT_ID",    "FFF_PAYOS_CLIENT_ID");
        this.apiKey      = getConfig("PAYOS_API_KEY",      "FFF_PAYOS_API_KEY");
        this.checksumKey = getConfig("PAYOS_CHECKSUM_KEY", "FFF_PAYOS_CHECKSUM_KEY");
        this.returnUrl   = getConfig("PAYOS_RETURN_URL",   "FFF_PAYOS_RETURN_URL");
        this.cancelUrl   = getConfig("PAYOS_CANCEL_URL",   "FFF_PAYOS_CANCEL_URL");
    }

    // Returns false if the 3 required keys are missing — call this before doing anything
    public boolean isConfigured() {
        return notBlank(clientId)
                && notBlank(apiKey)
                && notBlank(checksumKey);
    }

    // Tells you exactly which env vars are missing — handy when a new deploy breaks
    public String getMissingConfigSummary() {
        List<String> missing = new ArrayList<>();
        if (!notBlank(clientId))    missing.add("PAYOS_CLIENT_ID");
        if (!notBlank(apiKey))      missing.add("PAYOS_API_KEY");
        if (!notBlank(checksumKey)) missing.add("PAYOS_CHECKSUM_KEY");
        if (!notBlank(returnUrl))   missing.add("PAYOS_RETURN_URL");
        if (!notBlank(cancelUrl))   missing.add("PAYOS_CANCEL_URL");
        return missing.isEmpty() ? "none" : String.join(", ", missing);
    }

    // Shorthand — uses return/cancel URLs from env
    public PaymentLinkResponse createPaymentLink(long orderCode,
                                                 BigDecimal amount,
                                                 String description,
                                                 UUID bookingId,
                                                 LocalDateTime paymentDeadline) {
        return createPaymentLink(orderCode, amount, description, bookingId, paymentDeadline, null, null);
    }

    /**
     * Creates a PayOS payment link. This is the main method you'll call.
     *
     * Steps:
     *  1. Validate config + amount
     *  2. Build return/cancel URLs with bookingId attached as query param
     *  3. Sign the request with HMAC-SHA256
     *  4. POST to PayOS, parse response
     *  5. Return checkout URL + QR code (or a failure object)
     */
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

        // PayOS only accepts whole numbers (VND has no fractional units)
        int amountInt;
        try {
            amountInt = amount.intValueExact();
        } catch (ArithmeticException ex) {
            amountInt = amount.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }

        // PayOS needs a Unix timestamp (seconds). Default: 15 min from now.
        long expiredAt = paymentDeadline == null
                ? LocalDateTime.now().plusMinutes(15).atZone(ZoneId.systemDefault()).toEpochSecond()
                : paymentDeadline.atZone(ZoneId.systemDefault()).toEpochSecond();

        // Override URLs take priority over env defaults
        String returnBase = firstNonBlank(returnUrlOverride, returnUrl);
        String cancelBase = firstNonBlank(cancelUrlOverride, cancelUrl);
        if (!notBlank(returnBase) || !notBlank(cancelBase)) {
            return PaymentLinkResponse.failure("Missing payOS return/cancel URL configuration");
        }

        // Append ?bookingId=xxx so we know which booking the redirect belongs to
        String returnUrlResolved = appendBookingQueryParam(returnBase, bookingId);
        String cancelUrlResolved = appendBookingQueryParam(cancelBase, bookingId);

        // Fields must be sorted alphabetically and joined as key=value pairs.
        // PayOS will recompute this on their end — any mismatch = rejected request.
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

        // Hand-rolled JSON — avoids pulling in Jackson/Gson just for this
        String payload = "{"
                + "\"orderCode\":"     + orderCode + ","
                + "\"amount\":"        + amountInt + ","
                + "\"description\":\""  + escapeJson(description) + "\","
                + "\"returnUrl\":\""    + escapeJson(returnUrlResolved) + "\","
                + "\"cancelUrl\":\""    + escapeJson(cancelUrlResolved) + "\","
                + "\"expiredAt\":"      + expiredAt + ","
                + "\"signature\":\""    + signature + "\""
                + "}";

        HttpResponse response = sendRequest("POST", API_BASE_URL + "/v2/payment-requests", payload);
        if (response.statusCode < 200 || response.statusCode >= 300) {
            return PaymentLinkResponse.failure("HTTP " + response.statusCode + ": " + response.body);
        }

        // PayOS uses "code": "00" for success — separate from HTTP status
        String code = extractJsonString(response.body, "code");
        if (code != null && !"00".equals(code)) {
            String desc = firstNonBlank(extractJsonString(response.body, "desc"), "payOS rejected request");
            return PaymentLinkResponse.failure(desc);
        }

        PaymentLinkResponse result = new PaymentLinkResponse();
        result.success       = true;
        result.orderCode     = orderCode;
        result.qrCode        = extractJsonString(response.body, "qrCode");
        result.checkoutUrl   = extractJsonString(response.body, "checkoutUrl");
        result.accountNumber = extractJsonString(response.body, "accountNumber");
        result.bankCode      = extractJsonString(response.body, "bin");

        // qrCode is the whole point — if it's missing, something went wrong
        if (!notBlank(result.qrCode)) {
            return PaymentLinkResponse.failure("payOS did not return qrCode");
        }

        return result;
    }

    /**
     * Checks the current status of an order.
     * Don't rely on redirect URLs alone — users close browsers, connections drop.
     * Always verify payment server-side with this.
     * Typical statuses: PENDING / PAID / CANCELLED / EXPIRED
     */
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
        result.success       = true;
        result.status        = firstNonBlank(extractJsonString(response.body, "status"), "PENDING");
        result.checkoutUrl   = extractJsonString(response.body, "checkoutUrl");
        result.qrCode        = extractJsonString(response.body, "qrCode");
        result.accountNumber = extractJsonString(response.body, "accountNumber");
        result.bankCode      = extractJsonString(response.body, "bin");
        return result;
    }

    /**
     * Raw HTTP call — the engine behind everything above.
     * Sets the required PayOS auth headers (x-client-id, x-api-key).
     * On 4xx/5xx, reads from error stream (that's where PayOS puts the error body).
     */
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
            // 4xx/5xx errors come through getErrorStream(), not getInputStream()
            InputStream is = statusCode >= 200 && statusCode < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            return new HttpResponse(statusCode, readAll(is));

        } catch (Exception ex) {
            return new HttpResponse(500, ex.getMessage() == null ? "payOS request failed" : ex.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // Reads an InputStream to String. Returns "" if stream is null.
    private static String readAll(InputStream is) {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        } catch (Exception ignored) {}
        return sb.toString();
    }

    // Tacks ?bookingId=xxx onto the URL so redirect handlers know which booking to update
    private String appendBookingQueryParam(String baseUrl, UUID bookingId) {
        try {
            String encoded = URLEncoder.encode(bookingId.toString(), StandardCharsets.UTF_8.name());
            String separator = baseUrl.contains("?") ? "&" : "?";
            return baseUrl + separator + "bookingId=" + encoded;
        } catch (Exception ex) {
            return baseUrl; // safe fallback — URL still works, just missing the param
        }
    }

    /**
     * Signs a string with HMAC-SHA256 and returns it as a lowercase hex string.
     * The checksumKey is our shared secret with PayOS — keep it out of source control.
     * 0xff & b converts signed Java byte to unsigned before hex formatting.
     */
    private String hmacSha256Hex(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String item = Integer.toHexString(0xff & b);
            if (item.length() == 1) hex.append('0'); // zero-pad single chars
            hex.append(item);
        }
        return hex.toString();
    }

    /**
     * Pulls a string value out of JSON by field name using regex.
     * No Jackson/Gson dependency — works fine for PayOS's flat response structure.
     * Won't handle escaped quotes inside values, but PayOS doesn't send those.
     */
    private String extractJsonString(String json, String fieldName) {
        if (!notBlank(json) || !notBlank(fieldName)) return null;
        Pattern p = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    // Escapes \ and " so hand-built JSON strings don't break
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Returns the first non-blank string in the list — great for "use override if present, else fall back to env"
    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (notBlank(value)) return value.trim();
        }
        return null;
    }

    /**
     * Looks up a config value by trying multiple key names.
     * Order: env var → JVM system property → lowercase system property
     * This lets different environments (Docker, local, CI) use their own naming conventions.
     */
    private String getConfig(String... keys) {
        if (keys == null) return null;
        for (String key : keys) {
            String value = firstNonBlank(
                    System.getenv(key),
                    System.getProperty(key),
                    System.getProperty(key.toLowerCase())
            );
            if (notBlank(value)) return value;
        }
        return null;
    }

    // Just a null + blank check — used everywhere
    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Wraps raw HTTP response — internal use only
    private static class HttpResponse {
        private final int statusCode;
        private final String body;

        private HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    /**
     * Result object for createPaymentLink().
     * Check isSuccess() first. If false, getMessage() tells you why it failed.
     * If true, getQrCode() and getCheckoutUrl() have what you need.
     */
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

        public boolean isSuccess()       { return success; }
        public String getMessage()       { return message; }
        public long getOrderCode()       { return orderCode; }
        public String getCheckoutUrl()   { return checkoutUrl; }
        public String getQrCode()        { return qrCode; }
        public String getBankCode()      { return bankCode; }
        public String getAccountNumber() { return accountNumber; }
    }

    /**
     * Result object for getPaymentStatus().
     * Same pattern as PaymentLinkResponse.
     * getStatus() returns: PENDING / PAID / CANCELLED / EXPIRED
     */
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

        public boolean isSuccess()       { return success; }
        public String getMessage()       { return message; }
        public String getStatus()        { return status; }
        public String getCheckoutUrl()   { return checkoutUrl; }
        public String getQrCode()        { return qrCode; }
        public String getBankCode()      { return bankCode; }
        public String getAccountNumber() { return accountNumber; }
    }
}