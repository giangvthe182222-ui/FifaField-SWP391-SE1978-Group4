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
public class StaffShift {
    private UUID staffId;
    private UUID fieldId;
    private UUID shiftId;
    private LocalDate workingDate;
    private UUID assignedBy;
    private String status;

    public StaffShift() {}

    public StaffShift(UUID staffId, UUID fieldId, UUID shiftId,
                      LocalDate workingDate, UUID assignedBy, String status) {
        this.staffId = staffId;
        this.fieldId = fieldId;
        this.shiftId = shiftId;
        this.workingDate = workingDate;
        this.assignedBy = assignedBy;
        this.status = status;
    }

    public UUID getStaffId() { return staffId; }
    public void setStaffId(UUID staffId) { this.staffId = staffId; }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public UUID getShiftId() { return shiftId; }
    public void setShiftId(UUID shiftId) { this.shiftId = shiftId; }

    public LocalDate getWorkingDate() { return workingDate; }
    public void setWorkingDate(LocalDate workingDate) { this.workingDate = workingDate; }

    public UUID getAssignedBy() { return assignedBy; }
    public void setAssignedBy(UUID assignedBy) { this.assignedBy = assignedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
