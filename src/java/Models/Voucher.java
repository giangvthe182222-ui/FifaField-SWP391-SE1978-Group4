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
public class Voucher {
    private UUID voucherId;
    private UUID locationId;
    private String code;
    private BigDecimal discountValue;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int usedCount;
    private String status;
    private LocalDateTime createdAt;

    public Voucher() {}

    public Voucher(UUID voucherId, UUID locationId, String code,
                   BigDecimal discountValue, String description,
                   LocalDate startDate, LocalDate endDate,
                   int usedCount, String status, LocalDateTime createdAt) {
        this.voucherId = voucherId;
        this.locationId = locationId;
        this.code = code;
        this.discountValue = discountValue;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.usedCount = usedCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getVoucherId() { return voucherId; }
    public void setVoucherId(UUID voucherId) { this.voucherId = voucherId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
