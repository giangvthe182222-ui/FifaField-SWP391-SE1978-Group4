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
public class Invoice {
    private UUID invoiceId;
    private UUID paymentId;
    private UUID createdBy;
    private LocalDateTime issuedDate;
    private BigDecimal totalAmount;

    public Invoice() {}

    public Invoice(UUID invoiceId, UUID paymentId, UUID createdBy,
                   LocalDateTime issuedDate, BigDecimal totalAmount) {
        this.invoiceId = invoiceId;
        this.paymentId = paymentId;
        this.createdBy = createdBy;
        this.issuedDate = issuedDate;
        this.totalAmount = totalAmount;
    }

    public UUID getInvoiceId() { return invoiceId; }
    public void setInvoiceId(UUID invoiceId) { this.invoiceId = invoiceId; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDateTime issuedDate) { this.issuedDate = issuedDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}

