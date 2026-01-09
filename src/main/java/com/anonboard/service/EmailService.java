package com.anonboard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("AnonBoard Verification Code: " + otp);

            String htmlContent = String.format(
                    "<div style=\"font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px;\">"
                            +
                            "  <h2 style=\"color: #6366f1; text-align: center;\">AnonBoard Verification</h2>" +
                            "  <p style=\"font-size: 16px; color: #333;\">Hello,</p>" +
                            "  <p style=\"font-size: 16px; color: #333;\">Use the following code to verify your email address. This code is valid for 10 minutes.</p>"
                            +
                            "  <div style=\"background-color: #f3f4f6; padding: 15px; text-align: center; border-radius: 8px; margin: 20px 0;\">"
                            +
                            "    <span style=\"font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1f2937;\">%s</span>"
                            +
                            "  </div>" +
                            "  <p style=\"font-size: 14px; color: #666;\">If you didn't request this code, please ignore this email.</p>"
                            +
                            "</div>",
                    otp);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}", to, e);
            // In production scenarios, you might want to rethrow or handle this
        }
    }
}
