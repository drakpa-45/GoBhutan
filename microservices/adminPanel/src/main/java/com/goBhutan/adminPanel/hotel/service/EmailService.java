package com.goBhutan.adminPanel.hotel.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("GoBhutan - Password Reset OTP");
        message.setText(
                "Your OTP for password reset is: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n" +
                        "If you did not request this, please ignore this email."
        );
        mailSender.send(message);
    }
}