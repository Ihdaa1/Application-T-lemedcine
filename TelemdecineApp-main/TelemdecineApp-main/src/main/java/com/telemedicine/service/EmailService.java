package com.telemedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Async
    public void sendAppointmentConfirmationEmail(String patientEmail, String patientName, String doctorName, String specialty, String appointmentDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(patientEmail);
            helper.setSubject("Rendez-vous Médical Confirmé - TeleMed");
            
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("specialty", specialty);
            context.setVariable("appointmentDate", appointmentDate);
            
            String htmlContent = templateEngine.process("appointment-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Appointment confirmation email sent to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("Failed to send appointment confirmation email to: {}", patientEmail, e);
        }
    }
}