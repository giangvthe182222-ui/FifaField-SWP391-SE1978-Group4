package Models;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

public class BookingViewModel {
    private UUID bookingId;
    private UUID bookerId;
    private UUID fieldId;
    private UUID locationId;
    private UUID scheduleId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String fieldName;
    private String locationName;
    private String customerName;
    private String customerPhone;
    private String status;
    private String playStatus;
    private String paymentStatus;
    private String extraPaymentStatus;
    private BigDecimal fieldPrice;
    private BigDecimal equipmentPrice;
    private BigDecimal totalPrice;
    private BigDecimal outstandingAmount;
    private boolean equipmentBookingAllowed;

    public BookingViewModel() {}

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public UUID getBookerId() { return bookerId; }
    public void setBookerId(UUID bookerId) { this.bookerId = bookerId; }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

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

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlayStatus() { return playStatus; }
    public void setPlayStatus(String playStatus) { this.playStatus = playStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getExtraPaymentStatus() { return extraPaymentStatus; }
    public void setExtraPaymentStatus(String extraPaymentStatus) { this.extraPaymentStatus = extraPaymentStatus; }

    public BigDecimal getFieldPrice() { return fieldPrice; }
    public void setFieldPrice(BigDecimal fieldPrice) { this.fieldPrice = fieldPrice; }

    public BigDecimal getEquipmentPrice() { return equipmentPrice; }
    public void setEquipmentPrice(BigDecimal equipmentPrice) { this.equipmentPrice = equipmentPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal outstandingAmount) { this.outstandingAmount = outstandingAmount; }

    public boolean isEquipmentBookingAllowed() { return equipmentBookingAllowed; }
    public void setEquipmentBookingAllowed(boolean equipmentBookingAllowed) { this.equipmentBookingAllowed = equipmentBookingAllowed; }
}
