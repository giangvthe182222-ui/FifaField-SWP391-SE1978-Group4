package Models;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

public class BookingViewModel {
    private UUID bookingId;
    private UUID bookerId;
    private UUID fieldId;
    private UUID scheduleId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String fieldName;
    private String customerName;
    private String status;
    private BigDecimal totalPrice;

    public BookingViewModel() {}

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getBookerId() { return bookerId; }
    public void setBookerId(UUID bookerId) { this.bookerId = bookerId; }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public UUID getScheduleId() { return scheduleId; }
    public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
