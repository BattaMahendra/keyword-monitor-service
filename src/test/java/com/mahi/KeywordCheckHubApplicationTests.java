package com.mahi;

import com.mahi.notification.EmailNotificationService;
import com.mahi.notification.TelegramNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")

class KeywordCheckHubApplicationTests {

	@MockBean
	private EmailNotificationService emailNotificationService;

	@MockBean
	private TelegramNotificationService telegramNotificationService;

	@Test
	void contextLoads() {
	}

}
