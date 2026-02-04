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
        msg.setSubject("[FIFA FOODBALL FIELD] Mã xác minh: " + otp);
        msg.setContent("Mã xác minh của bé là: " + otp + "\nMã có hiệu lực trong vòng 5 phút, bé nhanh chóng nhập sớm nhé.\nYêu bé<3!", "text/plain; charset=UTF-8");
        Transport.send(msg);
    }
}
