package Models;

import java.util.UUID;

public class SupplementaryEquipment {
    private UUID rentalId;
    private UUID equipmentId;
    private int quantity;

    public SupplementaryEquipment() {}

    public SupplementaryEquipment(UUID rentalId, UUID equipmentId, int quantity) {
        this.rentalId = rentalId;
        this.equipmentId = equipmentId;
        this.quantity = quantity;
    }

    public UUID getRentalId() { return rentalId; }
    public void setRentalId(UUID rentalId) { this.rentalId = rentalId; }

    public UUID getEquipmentId() { return equipmentId; }
    public void setEquipmentId(UUID equipmentId) { this.equipmentId = equipmentId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
