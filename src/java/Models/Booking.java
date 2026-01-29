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
    private UUID customerId;
    private UUID scheduleId;
    private UUID voucherId;
    private LocalDateTime bookingTime;
    private String status;
    private BigDecimal totalPrice;

    public Booking() {}

    public Booking(UUID bookingId, UUID bookerId, UUID customerId,
                   UUID scheduleId, UUID voucherId,
                   LocalDateTime bookingTime, String status,
                   BigDecimal totalPrice) {
        this.bookingId = bookingId;
        this.bookerId = bookerId;
        this.customerId = customerId;
        this.scheduleId = scheduleId;
        this.voucherId = voucherId;
        this.bookingTime = bookingTime;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getBookerId() { return bookerId; }
    public void setBookerId(UUID bookerId) { this.bookerId = bookerId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getScheduleId() { return scheduleId; }
    public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }

    public UUID getVoucherId() { return voucherId; }
    public void setVoucherId(UUID voucherId) { this.voucherId = voucherId; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}

