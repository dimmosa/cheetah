package view;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Transport;


import java.util.Properties;

/**
 * Email service using Gmail SMTP
 * Automatically falls back to demo mode if email fails
 */
public class EmailService {
    
    // ✅ Configure your Gmail credentials here
    private static final String GMAIL_ADDRESS = "jojo.joje.oe@gmail.com";
    private static final String GMAIL_APP_PASSWORD = "uhaazilxdzfuhuth";
    
    /**
     * Send password reset code to user's email
     * Returns: "EMAIL_SENT" if successful, "DEMO_MODE" if failed/no internet
     */
    public static String sendPasswordResetCode(String recipientEmail, String code) {
        
        // Check if configured
        if (GMAIL_ADDRESS.isEmpty() || GMAIL_APP_PASSWORD.isEmpty() ||
            GMAIL_ADDRESS.equals("your-email@gmail.com")) {
            System.out.println("⚠️ Email not configured - using demo mode");
            return "DEMO_MODE";
        }
        
        // Try to send email with timeout
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
        props.put("mail.smtp.timeout", "10000");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_ADDRESS, GMAIL_APP_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(GMAIL_ADDRESS, "Minesweeper Game"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Minesweeper - Password Reset Code");
            
            String htmlContent = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #0A0E1A; padding: 40px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #0F1922 0%%, #1A2332 100%%); padding: 40px; border-radius: 10px;'>" +
                "<h2 style='color: #00C6FF; text-align: center;'>🔒 Password Reset Request</h2>" +
                "<p style='color: #FFFFFF; font-size: 16px;'>Hello,</p>" +
                "<p style='color: #B0C4DE; font-size: 14px;'>You requested to reset your password for your Minesweeper account.</p>" +
                "<p style='color: #B0C4DE; font-size: 14px;'>Your verification code is:</p>" +
                "<div style='background: #0F1922; padding: 30px; border-radius: 8px; text-align: center; margin: 20px 0;'>" +
                "<h1 style='color: #00FFB3; font-size: 48px; letter-spacing: 12px; margin: 0;'>%s</h1>" +
                "</div>" +
                "<p style='color: #B0C4DE; font-size: 14px;'>This code will expire in 10 minutes.</p>" +
                "<p style='color: #B0C4DE; font-size: 14px;'>If you didn't request this, please ignore this email.</p>" +
                "<hr style='border: 1px solid #2A3F5F; margin: 30px 0;'>" +
                "<p style='color: #808080; font-size: 12px; text-align: center;'>Minesweeper Game © 2025</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                code
            );
            
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            Transport.send(message);
            System.out.println("✅ Email sent successfully to: " + recipientEmail);
            return "EMAIL_SENT";
            
        } catch (Exception e) {
            // Silently fallback to demo mode (don't spam console with scary errors)
            System.out.println("ℹ️  Email unavailable - using offline mode");
            return "DEMO_MODE";
        }
    }
}