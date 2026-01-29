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
public class GmailAccount {
    private UUID gmailId;
    private String googleSub;
    private String email;

    public GmailAccount() {}

    public GmailAccount(UUID gmailId, String googleSub, String email) {
        this.gmailId = gmailId;
        this.googleSub = googleSub;
        this.email = email;
    }

    public UUID getGmailId() { return gmailId; }
    public void setGmailId(UUID gmailId) { this.gmailId = gmailId; }

    public String getGoogleSub() { return googleSub; }
    public void setGoogleSub(String googleSub) { this.googleSub = googleSub; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
