package com.kido.corporation.auth.service.impl;

import com.kido.corporation.auth.config.MailProperties;
import com.kido.corporation.auth.constant.MailTemplate;
import com.kido.corporation.auth.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;

    @Override
    @Async
    public void sendMail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(variables);
            
            String html = templateEngine.process("mail/" + templateName, context);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Sent mail with subject '{}' to '{}'", subject, to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    @Override
    public void sendVerifyEmail(String to, String name, String token) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        String link = mailProperties.getFrontendVerifyEmailUrl() + "?email=" + to + "&token=" + token;
        vars.put("verifyLink", link);
        sendMail(to, "Verify your email address", MailTemplate.VERIFY_EMAIL, vars);
    }

    @Override
    public void sendOtpReset(String to, String name, String otp, long validMinutes) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        vars.put("otp", otp);
        vars.put("validMinutes", validMinutes);
        sendMail(to, "Password Reset Verification Code", MailTemplate.OTP_RESET, vars);
    }

    @Override
    public void sendPasswordChanged(String to, String name) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        sendMail(to, "Your password has been changed", MailTemplate.PASSWORD_CHANGED, vars);
    }

    @Override
    public void sendAdminResetPassword(String to, String name, String newPassword) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        vars.put("newPassword", newPassword);
        sendMail(to, "Your password was reset by an administrator", MailTemplate.ADMIN_RESET_PASSWORD, vars);
    }

    @Override
    public void sendWelcomeEmail(String to, String name, String password) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", name);
        vars.put("password", password);
        sendMail(to, "Welcome to our platform", MailTemplate.WELCOME_EMAIL, vars);
    }
}
