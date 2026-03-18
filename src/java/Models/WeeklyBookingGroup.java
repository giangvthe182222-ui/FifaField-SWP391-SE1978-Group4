package Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WeeklyBookingGroup {
    private UUID weeklyGroupId;
    private UUID bookerId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime paymentDeadline;
    private LocalDateTime createdAt;

    public UUID getWeeklyGroupId() {
        return weeklyGroupId;
    }

    public void setWeeklyGroupId(UUID weeklyGroupId) {
        this.weeklyGroupId = weeklyGroupId;
    }

    public UUID getBookerId() {
        return bookerId;
    }

    public void setBookerId(UUID bookerId) {
        this.bookerId = bookerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
