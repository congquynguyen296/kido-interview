package com.kido.corporation.auth.service;

import java.util.Map;

public interface MailService {

    void sendMail(String to, String subject, String templateName, Map<String, Object> variables);

    void sendVerifyEmail(String to, String name, String token);

    void sendOtpReset(String to, String name, String otp, long validMinutes);

    void sendPasswordChanged(String to, String name);

    void sendAdminResetPassword(String to, String name, String newPassword);

    void sendWelcomeEmail(String to, String name, String password);
}
