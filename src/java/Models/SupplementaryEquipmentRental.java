package Models;

import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class SupplementaryEquipmentRental {
    private UUID rentalId;
    private UUID originalBookingId;
    private UUID customerId;
    private UUID fieldId;
    private UUID locationId;
    private LocalDateTime createdTime;
    private String status;
    private BigDecimal totalPrice;

    public SupplementaryEquipmentRental() {}

    public SupplementaryEquipmentRental(UUID originalBookingId, UUID customerId, UUID fieldId, UUID locationId) {
        this.rentalId = UUID.randomUUID();
        this.originalBookingId = originalBookingId;
        this.customerId = customerId;
        this.fieldId = fieldId;
        this.locationId = locationId;
        this.createdTime = LocalDateTime.now();
        this.status = "pending";
        this.totalPrice = BigDecimal.ZERO;
    }

    public UUID getRentalId() { return rentalId; }
    public void setRentalId(UUID rentalId) { this.rentalId = rentalId; }

    public UUID getOriginalBookingId() { return originalBookingId; }
    public void setOriginalBookingId(UUID originalBookingId) { this.originalBookingId = originalBookingId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
