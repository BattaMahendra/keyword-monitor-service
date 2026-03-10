package com.mahi.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahi.exception.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Service for sending Telegram notifications via Telegram Bot API
 */
@Slf4j
@Service
public class TelegramNotificationService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds

    @Value("${app.telegram.bot-token}")
    private String botToken;

    @Value("${app.telegram.api-url}")
    private String telegramApiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public TelegramNotificationService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Send message via Telegram Bot API with retry logic
     *
     * @param chatId telegram chat ID
     * @param message message text
     */
    public void sendMessage(String chatId, String message) {
        if (!StringUtils.hasText(chatId)) {
            log.warn("Telegram chat ID not specified, skipping Telegram notification");
            return;
        }

        if (!isValidBotToken()) {
            log.warn("Telegram bot token is not configured properly, skipping Telegram notification");
            return;
        }

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                String url = telegramApiUrl + botToken + "/sendMessage";
                String payload = "{\"chat_id\": \"" + chatId + "\", \"text\": \"" + escapeJson(message) + "\"}";

                String responseBody = webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(payload))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                // Parse response to check if it was actually successful
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                boolean ok = jsonResponse.path("ok").asBoolean();

                if (ok) {
                    log.info("Telegram message sent successfully to chat ID: {} on attempt {}", chatId, attempt + 1);
                    return; // Success, exit the loop
                } else {
                    String description = jsonResponse.path("description").asText();
                    log.error("Telegram API returned error: {}", description);
                    throw new NotificationException("Telegram API error: " + description);
                }

            } catch (WebClientResponseException e) {
                log.error("Telegram API returned HTTP error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
                // Don't retry for client errors (4xx)
                if (e.getStatusCode().is4xxClientError()) {
                    throw new NotificationException("Telegram API client error: " + e.getResponseBodyAsString(), e);
                }
                // Retry for server errors (5xx)
                attempt++;
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new NotificationException("Retry delay interrupted", ie);
                    }
                } else {
                    throw new NotificationException("Failed to send Telegram notification after " + MAX_RETRIES + " attempts", e);
                }
            } catch (Exception e) {
                log.error("Attempt {} failed to send Telegram message to chat ID: {}. Error: {}", attempt + 1, chatId, e.getMessage());
                attempt++;
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new NotificationException("Retry delay interrupted", ie);
                    }
                } else {
                    throw new NotificationException("Failed to send Telegram notification after " + MAX_RETRIES + " attempts", e);
                }
            }
        }
    }

    /**
     * Check if bot token is configured
     */
    private boolean isValidBotToken() {
        return StringUtils.hasText(botToken);
    }

    /**
     * Escape special characters in JSON
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
