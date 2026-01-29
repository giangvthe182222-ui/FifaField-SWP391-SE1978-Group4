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
public class CustomerVoucher {
    private UUID customerId;
    private UUID voucherId;
    private boolean used;

    public CustomerVoucher() {}

    public CustomerVoucher(UUID customerId, UUID voucherId, boolean used) {
        this.customerId = customerId;
        this.voucherId = voucherId;
        this.used = used;
    }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getVoucherId() { return voucherId; }
    public void setVoucherId(UUID voucherId) { this.voucherId = voucherId; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}

