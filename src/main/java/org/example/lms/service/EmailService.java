
package org.example.lms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String message) {

        // Skip email if SMTP credentials haven't been configured
        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println("Email skipped: SMTP username is not configured.");
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

        } catch (Exception e) {
            // Keep in-app notifications working even if email fails
            System.err.println("Email could not be sent: " + e.getMessage());
        }
    }
}
