package Models;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class Equipment {
    private UUID equipmentId;
    private String name;
    private String equipmentType;
    private String imageUrl;
    private BigDecimal rentalPrice;
    private BigDecimal damageFee;
    private String description;
    private String status;

    public Equipment() {}

    public Equipment(UUID equipmentId, String name, String equipmentType,
                     String imageUrl, BigDecimal rentalPrice,
                     BigDecimal damageFee, String description,
                     String status) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.equipmentType = equipmentType;
        this.imageUrl = imageUrl;
        this.rentalPrice = rentalPrice;
        this.damageFee = damageFee;
        this.description = description;
        this.status = status;
        
    }

    public UUID getEquipmentId() { return equipmentId; }
    public void setEquipmentId(UUID equipmentId) { this.equipmentId = equipmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getRentalPrice() { return rentalPrice; }
    public void setRentalPrice(BigDecimal rentalPrice) { this.rentalPrice = rentalPrice; }

    public BigDecimal getDamageFee() { return damageFee; }
    public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

   
}

