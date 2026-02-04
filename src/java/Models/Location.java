package Models;

import java.util.UUID;
import java.time.LocalDateTime;

public class Location {

    private UUID locationId;
    private String locationName;
    private String address;
    private String phoneNumber;     // ✅ STRING
    private String imageUrl;
    private String status;
//    private LocalDateTime createdAt;
    private UUID managerId;
    private String managerName;

    public Location() {}

    public Location(UUID locationId, String locationName, String address,
                    String phoneNumber, String imageUrl,
                    String status, UUID managerId) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.imageUrl = imageUrl;
        this.status = status;
//        this.createdAt = createdAt;
        this.managerId = managerId;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {          // ✅
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {   // ✅
        this.phoneNumber = phoneNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}
