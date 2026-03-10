package com.mahi.notification;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Service for sending email notifications using SendGrid
 */
@Slf4j
@Service
public class EmailNotificationService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.from-email}")
    private String fromEmail;

    /**
     * Send email notification
     *
     * @param to recipient email address
     * @param subject email subject
     * @param message email message body
     */
    public void sendEmail(String to, String subject, String message) {
        sendEmailInternal(to, subject, new Content("text/plain", message));
    }

    /**
     * Send email with HTML content
     *
     * @param to recipient email address
     * @param subject email subject
     * @param htmlMessage HTML message body
     */
    public void sendHtmlEmail(String to, String subject, String htmlMessage) {
        sendEmailInternal(to, subject, new Content("text/html", htmlMessage));
    }

    private void sendEmailInternal(String to, String subject, Content content) {
        if (!StringUtils.hasText(to)) {
            log.warn("Email recipient not specified, skipping email notification");
            return;
        }

        if (!StringUtils.hasText(sendGridApiKey) || "your-sendgrid-api-key".equals(sendGridApiKey)) {
            log.error("SendGrid API key is not configured or is a placeholder");
            throw new RuntimeException("SendGrid API key is missing or not configured");
        }

        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                log.info("Attempt {} to send email to: {} with subject: {}", attempt + 1, to, subject);
                Response response = sg.api(request);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    log.info("Email sent successfully on attempt {}. Status Code: {}", attempt + 1, response.getStatusCode());
                    return; // Success
                } else {
                    log.error("Attempt {} failed to send email. Status Code: {}, Body: {}",
                            attempt + 1, response.getStatusCode(), response.getBody());
                    // Throw an exception to trigger a retry for server-side errors
                    if (response.getStatusCode() >= 500) {
                        throw new IOException("SendGrid server error: " + response.getBody());
                    } else {
                        // Don't retry for client-side errors (4xx)
                        throw new RuntimeException("Failed to send email via SendGrid (client error): " + response.getBody());
                    }
                }
            } catch (IOException ex) {
                log.error("Error on attempt {} sending email to: {}", attempt + 1, to, ex);
                attempt++;
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry delay interrupted", ie);
                    }
                } else {
                    throw new RuntimeException("Failed to send email notification after " + MAX_RETRIES + " attempts", ex);
                }
            }
        }
    }
}
