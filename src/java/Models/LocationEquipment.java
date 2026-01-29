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
public class LocationEquipment {
    private UUID locationId;
    private UUID equipmentId;
    private String status;
    private String equipmentCondition;

    public LocationEquipment() {}

    public LocationEquipment(UUID locationId, UUID equipmentId,
                             String status, String equipmentCondition) {
        this.locationId = locationId;
        this.equipmentId = equipmentId;
        this.status = status;
        this.equipmentCondition = equipmentCondition;
    }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public UUID getEquipmentId() { return equipmentId; }
    public void setEquipmentId(UUID equipmentId) { this.equipmentId = equipmentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEquipmentCondition() { return equipmentCondition; }
    public void setEquipmentCondition(String equipmentCondition) {
        this.equipmentCondition = equipmentCondition;
    }
}

