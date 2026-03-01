package Utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailUtil {

    private static final String SMTP_USER = "fifafoodballfield@gmail.com";
    private static final String SMTP_PASS = "rruihknjbumlwaib";

    public static void sendOtp(String toEmail, String otp) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SMTP_USER));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        String digits = String.join("&nbsp;&nbsp;&nbsp;", otp.split(""));
        String html = "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background:#f0f0f0;font-family:Arial,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f0f0f0;padding:40px 0;'>"
                + "<tr><td align='center'>"
                + "<table width='520' cellpadding='0' cellspacing='0' style='background:#f0f0f0;'>"
                + // Logo circle
                "<tr><td align='center' style='padding-bottom:24px;'>"
                + "<div style='display:inline-block;background:#d32f2f;border-radius:50%;width:72px;height:72px;line-height:72px;text-align:center;'>"
                + "<span style='color:#fff;font-size:36px;'>&#9917;</span>"
                + "</div></td></tr>"
                + // Title
                "<tr><td align='center' style='font-size:24px;font-weight:bold;color:#111;padding-bottom:24px;'>"
                + "Mã Xác Minh</td></tr>"
                + // White card
                "<tr><td>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#fff;border-radius:8px;padding:36px 40px;'>"
                + "<tr><td align='center' style='font-size:14px;color:#444;padding-bottom:20px;'>"
                + "Đây là mã xác minh của bạn:</td></tr>"
                + // OTP digits
                "<tr><td align='center' style='font-size:42px;font-weight:bold;letter-spacing:12px;color:#111;padding:12px 0 20px;'>"
                + digits + "</td></tr>"
                + // Expiry note
                "<tr><td align='center' style='font-size:13px;color:#888;padding-top:8px;'>"
                + "Mã này sẽ sớm hết hạn.</td></tr>"
                + "</table></td></tr>"
                + // Footer
                "<tr><td align='center' style='font-size:11px;color:#aaa;padding-top:20px;'>"
                + "&copy; FIFA FIELD. Đừng chia sẻ mã này cho bất kỳ ai.\n"
                + "Đây là dịch vụ thư thông báo.\n"
                + "Tập Đoàn Fifa Foodball Field, 13666 Đại Lộ Thăng Long. Đại học FPT Hà Nội, Hola, Việt Nam © 2026 Fifa Field. Đã đăng ký bản quyền</td></tr>"
                + "</table></td></tr></table>"
                + "</body></html>";

        msg.setSubject("[FIFA FIELD] Mã xác minh: " + otp);
        msg.setContent(html, "text/html; charset=UTF-8");
        Transport.send(msg);
    }
}
