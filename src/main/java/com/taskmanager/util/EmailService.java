package com.taskmanager.util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "yuvarajsinghtagore@gmail.com";
    private static final String SMTP_PASSWORD = "kzzxuxklnndiovkq";

    public static void sendOTP(String toEmail, String otp) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("TaskManager - Email Verification OTP");
        message.setText("Your OTP for email verification is: " + otp + "\n\nThis OTP will expire in 10 minutes.\n\nIf you did not request this, please ignore this email.");

        Transport.send(message);
    }
}
