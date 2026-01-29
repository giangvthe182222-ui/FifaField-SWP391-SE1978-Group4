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
public class Field {
    private UUID fieldId;
    private String fieldName;
    private String fieldType;
    private String imageUrl;
    private String status;
    private String fieldCondition;
    private LocalDateTime createdAt;
    private UUID locationId;

    public Field() {}

    public Field(UUID fieldId, String fieldName, String fieldType,
                 String imageUrl, String status, String fieldCondition,
                 LocalDateTime createdAt, UUID locationId) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.imageUrl = imageUrl;
        this.status = status;
        this.fieldCondition = fieldCondition;
        this.createdAt = createdAt;
        this.locationId = locationId;
    }

    public UUID getFieldId() { return fieldId; }
    public void setFieldId(UUID fieldId) { this.fieldId = fieldId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFieldCondition() { return fieldCondition; }
    public void setFieldCondition(String fieldCondition) { this.fieldCondition = fieldCondition; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }
}

