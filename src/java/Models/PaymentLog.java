package Models;

import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Payment Log Model - Track payment verification attempts
 */
public class PaymentLog {

    private int logId;
    private UUID paymentId;
    private LocalDateTime checkTime;
    private String status;
    private String responseMessage;

    public PaymentLog() {
    }

    public PaymentLog(UUID paymentId, String status, String responseMessage) {
        this.paymentId = paymentId;
        this.status = status;
        this.responseMessage = responseMessage;
        this.checkTime = LocalDateTime.now();
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
