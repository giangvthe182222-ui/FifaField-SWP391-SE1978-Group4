package Models;

import java.math.BigDecimal;
import java.util.UUID;

public class LocationEquipmentViewModel {
    private UUID equipmentId;
    private UUID locationId;
    private String name;
    private String equipmentType;
    private BigDecimal rentalPrice;
    private BigDecimal damageFee;
    private String imageUrl;
    
    private int quantity;
    private String status;

    public UUID getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(UUID equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getRentalPrice() {
        return rentalPrice;
    }

    public void setRentalPrice(BigDecimal rentalPrice) {
        this.rentalPrice = rentalPrice;
    }

    public BigDecimal getDamageFee() {
        return damageFee;
    }

    public void setDamageFee(BigDecimal damageFee) {
        this.damageFee = damageFee;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationID) {
        this.locationId = locationID;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
