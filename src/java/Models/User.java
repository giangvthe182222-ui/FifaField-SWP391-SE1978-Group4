package Models;

import java.util.UUID;
import java.time.LocalDateTime;

public class User {

    private UUID userId;
    private UUID gmailId;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private String gender;
    private UUID roleId;
    private Role role;
    private String status;
    private LocalDateTime createdAt;

    public User() {}

    

    public UUID getUserId() {
       return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getGmailId() {
        return gmailId;
    }

    public void setGmailId(UUID gmailId) {
        this.gmailId = gmailId;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }

    // ===== roleId (DB) =====
    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    // ===== role (OBJECT) =====
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;

        // đồng bộ ngược lại roleId cho chắc
        if (role != null) {
            this.roleId = role.getRoleId();
        }
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
