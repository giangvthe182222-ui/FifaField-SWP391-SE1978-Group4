package Models;

import java.util.UUID;
import java.math.BigDecimal;

public class BookingEquipmentViewModel {
    private UUID equipmentId;
    private String name;
    private int quantity;
    private BigDecimal rentalPrice;

    public BookingEquipmentViewModel() {}

    public UUID getEquipmentId() { return equipmentId; }
    public void setEquipmentId(UUID equipmentId) { this.equipmentId = equipmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getRentalPrice() { return rentalPrice; }
    public void setRentalPrice(BigDecimal rentalPrice) { this.rentalPrice = rentalPrice; }
}
