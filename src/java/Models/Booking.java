/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
/**
 *
 * @author admin
 */
public class Booking {
    private UUID bookingId;
    private UUID bookerId;
    private String phoneNumber;
    private UUID fieldId;
    private UUID scheduleId;
    private UUID voucherId;
    private UUID weeklyGroupId;
    
    private LocalDateTime bookingTime;
    private String status;
    private String playStatus;
    private String paymentStatus;
    private String extraPaymentStatus;
    private BigDecimal totalPrice;
    private LocalDateTime paymentDeadline;

    public Booking() {}

    public Booking(UUID bookingId, UUID bookerId, UUID fieldId,
                   UUID scheduleId, UUID voucherId,
                   LocalDateTime bookingTime, String status,
                   BigDecimal totalPrice) {
        this.bookingId = bookingId;
        this.bookerId = bookerId;
        this.fieldId = fieldId;
        this.scheduleId = scheduleId;
        this.voucherId = voucherId;
        this.bookingTime = bookingTime;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getBookerId() { return bookerId; }
    public void setBookerId(UUID bookerId) { this.bookerId = bookerId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public UUID getScheduleId() { return scheduleId; }
    public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }

    public UUID getVoucherId() { return voucherId; }
    public void setVoucherId(UUID voucherId) { this.voucherId = voucherId; }

    public UUID getWeeklyGroupId() { return weeklyGroupId; }
    public void setWeeklyGroupId(UUID weeklyGroupId) { this.weeklyGroupId = weeklyGroupId; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlayStatus() { return playStatus; }
    public void setPlayStatus(String playStatus) { this.playStatus = playStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getExtraPaymentStatus() { return extraPaymentStatus; }
    public void setExtraPaymentStatus(String extraPaymentStatus) { this.extraPaymentStatus = extraPaymentStatus; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getPaymentDeadline() { return paymentDeadline; }
    public void setPaymentDeadline(LocalDateTime paymentDeadline) { this.paymentDeadline = paymentDeadline; }
}

