# Keyword Monitor & Notification Service

A production-ready Spring Boot backend application that monitors URLs for specific keywords and sends notifications via Email or Telegram when the keyword appears. The system checks monitored websites every 5 minutes.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Troubleshooting](#troubleshooting)

## Features

✅ **Website Monitoring**: Monitor multiple URLs for specific keywords  
✅ **Email Notifications**: Send alerts via email when keywords are found  
✅ **Telegram Notifications**: Send alerts via Telegram Bot API  
✅ **Scheduled Checking**: Automated checks every 5 minutes  
✅ **Duplicate Prevention**: Smart alert history to prevent duplicate notifications  
✅ **REST API**: Full-featured REST API for managing monitors  
✅ **Global Exception Handling**: Comprehensive error handling  
✅ **Logging**: Detailed logging using SLF4J  
✅ **PostgreSQL Database**: Persistent storage with JPA/Hibernate  

## Tech Stack

- **Java 17** - Programming Language
- **Spring Boot 3.2** - Framework
- **PostgreSQL** - Database (via Neon)
- **Spring Data JPA** - ORM
- **Spring Mail** - Email notifications
- **Jsoup** - HTML parsing and scraping
- **Spring WebFlux** - Async HTTP client for Telegram API
- **Lombok** - Boilerplate reduction
- **Maven** - Build tool

## Project Structure

```
keyword-check-hub/
├── src/main/java/com/notificationhub/keyword_check_hub/
│   ├── controller/
│   │   ├── MonitorController.java          # REST API for monitors
│   │   └── AlertHistoryController.java     # REST API for alerts
│   ├── service/
│   │   ├── MonitoredSiteService.java       # Business logic for sites
│   │   └── WebsiteMonitorService.java      # Website monitoring logic
│   ├── notification/
│   │   ├── EmailNotificationService.java   # Email service
│   │   └── TelegramNotificationService.java # Telegram service
│   ├── repository/
│   │   ├── MonitoredSiteRepository.java    # Database operations
│   │   └── AlertHistoryRepository.java     # Alert history DB operations
│   ├── entity/
│   │   ├── MonitoredSite.java              # JPA entity
│   │   └── AlertHistory.java               # JPA entity
│   ├── dto/
│   │   ├── CreateMonitorRequest.java       # Request DTO
│   │   ├── MonitoredSiteResponse.java      # Response DTO
│   │   ├── AlertHistoryResponse.java       # Response DTO
│   │   └── ApiResponse.java                # Generic API response
│   ├── scheduler/
│   │   └── MonitoringScheduler.java        # Scheduled tasks
│   ├── exception/
│   │   ├── KeywordMonitorException.java    # Base exception
│   │   ├── MonitoredSiteNotFoundException.java
│   │   ├── WebsiteScrapingException.java
│   │   └── NotificationException.java
│   ├── config/
│   │   ├── GlobalExceptionHandler.java     # Global error handling
│   │   └── WebClientConfig.java            # WebClient configuration
│   └── KeywordCheckHubApplication.java     # Main class
├── src/main/resources/
│   ├── application.yaml                    # Configuration file
│   └── static/                             # Static files
├── pom.xml                                 # Maven dependencies
└── README.md                               # This file
```

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17 or higher** - [Download](https://adoptium.net/)
- **Maven 3.8.1 or higher** - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL** - For local development (or use Neon cloud)
- **Git** - [Download](https://git-scm.com/)

### For Notifications:
- **Gmail Account** with App Password for email notifications
- **Telegram Bot Token** (create via @BotFather on Telegram)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/keyword-check-hub.git
cd keyword-check-hub
```

### 2. Set Up PostgreSQL Database

**Option A: Using Neon (Cloud)**

The project is pre-configured for Neon. Update the credentials in `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-neon-endpoint/neondb
    username: your-username
    password: your-password
```

**Option B: Local PostgreSQL**

```bash
# Create database
createdb keyword_monitor_db

# Update application.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/keyword_monitor_db
    username: postgres
    password: your-password
```

### 3. Configure Email (Gmail)

1. Enable 2FA on your Gmail account
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Update `application.yaml`:

```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-app-password  # 16-character password from step 2
```

### 4. Configure Telegram Bot

1. Create a bot via @BotFather on Telegram
2. Copy the Bot Token
3. Set environment variable or update `application.yaml`:

```yaml
app:
  telegram:
    bot-token: your-bot-token-here
```

### 5. Build the Project

```bash
mvn clean install
```

## Configuration

### Environment Variables

Set these environment variables for production:

```bash
# Database
export DATABASE_URL=jdbc:postgresql://...
export DATABASE_USER=...
export DATABASE_PASSWORD=...

# Email
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Telegram
export TELEGRAM_BOT_TOKEN=your-bot-token
```

### application.yaml Configuration

Key properties:

```yaml
# Monitoring interval (in milliseconds)
app.monitor.check-interval: 300000  # 5 minutes

# Connection timeouts
app.monitor.connection-timeout: 10000  # 10 seconds
app.monitor.read-timeout: 10000

# Logging level
logging.level.com.notificationhub: DEBUG

# Server port
server.port: 8080
```

## Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Production Mode

```bash
mvn clean package -DskipTests
java -jar target/keyword-check-hub-1.0.0.jar
```

### Docker (Optional)

```bash
docker build -t keyword-monitor:latest .
docker run -p 8080:8080 \
  -e TELEGRAM_BOT_TOKEN=your-token \
  -e MAIL_USERNAME=your-email \
  -e MAIL_PASSWORD=your-password \
  keyword-monitor:latest
```

The application will start on `http://localhost:8080`

## API Documentation

### Monitored Sites API

#### 1. Create a Monitored Site

```http
POST /api/monitor
Content-Type: application/json

{
  "url": "https://example.com/jobs",
  "keyword": "Java Developer",
  "email": "your-email@gmail.com",
  "telegramChatId": "123456789"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Monitored site created successfully",
  "data": {
    "id": 1,
    "url": "https://example.com/jobs",
    "keyword": "Java Developer",
    "email": "your-email@gmail.com",
    "telegramChatId": "123456789",
    "createdAt": "2026-03-10T10:30:00",
    "lastChecked": null,
    "isActive": true
  }
}
```

#### 2. Get All Monitored Sites

```http
GET /api/monitor
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Retrieved 2 monitored sites",
  "data": [
    {
      "id": 1,
      "url": "https://example.com/jobs",
      "keyword": "Java Developer",
      "email": "your-email@gmail.com",
      "telegramChatId": "123456789",
      "createdAt": "2026-03-10T10:30:00",
      "lastChecked": "2026-03-10T10:35:00",
      "isActive": true
    }
  ]
}
```

#### 3. Get Monitored Site by ID

```http
GET /api/monitor/1
```

#### 4. Update Monitored Site

```http
PUT /api/monitor/1
Content-Type: application/json

{
  "url": "https://newexample.com/jobs",
  "keyword": "Senior Java Developer",
  "email": "newemail@gmail.com",
  "telegramChatId": "987654321"
}
```

#### 5. Delete Monitored Site

```http
DELETE /api/monitor/1
```

#### 6. Trigger Manual Scan

```http
POST /api/monitor/1/scan
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Website scanned successfully for keyword",
  "data": "Manual scan completed for site 1"
}
```

#### 7. Get Site Statistics

```http
GET /api/monitor/1/stats
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "siteInfo": {
      "id": 1,
      "url": "https://example.com/jobs",
      "keyword": "Java Developer",
      "totalAlerts": 5,
      "keywordFoundCount": 3
    }
  }
}
```

#### 8. Health Check

```http
GET /api/monitor/health
```

### Alert History API

#### 1. Get Alerts for a Site

```http
GET /api/alerts/site/1
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Retrieved 5 alerts",
  "data": [
    {
      "id": 1,
      "siteId": 1,
      "keywordFound": true,
      "message": "Keyword 'Java Developer' found on https://example.com/jobs",
      "alertTime": "2026-03-10T10:35:00",
      "emailSent": true,
      "telegramSent": true
    }
  ]
}
```

#### 2. Get Latest Alert for a Site

```http
GET /api/alerts/site/1/latest
```

## Database Schema

### MonitoredSite Table

```sql
CREATE TABLE monitored_sites (
  id BIGSERIAL PRIMARY KEY,
  url VARCHAR(500) NOT NULL,
  keyword VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  telegram_chat_id VARCHAR(255),
  last_checked TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  is_active BOOLEAN DEFAULT TRUE,
  INDEX idx_url (url),
  INDEX idx_created_at (created_at)
);
```

### AlertHistory Table

```sql
CREATE TABLE alert_history (
  id BIGSERIAL PRIMARY KEY,
  site_id BIGINT NOT NULL,
  keyword_found BOOLEAN NOT NULL,
  message VARCHAR(1000),
  alert_time TIMESTAMP NOT NULL,
  email_sent BOOLEAN,
  telegram_sent BOOLEAN,
  FOREIGN KEY (site_id) REFERENCES monitored_sites(id),
  INDEX idx_site_id (site_id),
  INDEX idx_alert_time (alert_time)
);
```

## Error Handling

The API returns consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2026-03-10T10:30:00"
}
```

### HTTP Status Codes

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid input or validation error
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### Validation Errors

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "url": "URL must start with http:// or https://",
    "keyword": "Keyword is required"
  }
}
```

## Monitoring & Logging

The application logs all important events:

```
[2026-03-10 10:35:00] - INFO - Starting scheduled website monitoring
[2026-03-10 10:35:01] - INFO - Checking URL: https://example.com/jobs
[2026-03-10 10:35:02] - INFO - Keyword 'Java Developer' found on https://example.com/jobs!
[2026-03-10 10:35:03] - INFO - Email notification sent to: your-email@gmail.com
[2026-03-10 10:35:04] - INFO - Telegram notification sent to chat ID: 123456789
```

View logs in `logs/keyword-monitor.log`

## Testing the Application

### Using cURL

```bash
# Create a monitor
curl -X POST http://localhost:8080/api/monitor \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com",
    "keyword": "Java",
    "email": "test@gmail.com",
    "telegramChatId": "123456"
  }'

# Get all monitors
curl http://localhost:8080/api/monitor

# Trigger manual scan
curl -X POST http://localhost:8080/api/monitor/1/scan

# Get alerts
curl http://localhost:8080/api/alerts/site/1
```

### Using Postman

Import the provided Postman collection (if available) or manually create requests following the API documentation above.

## Performance Optimization

- **Database Indexes**: Indexed on `url`, `created_at`, and `alert_time`
- **Connection Pooling**: HikariCP is used for database connections
- **Async Telegram API**: WebFlux for non-blocking Telegram API calls
- **Duplicate Prevention**: 1-hour window to prevent duplicate alerts
- **Batch Processing**: Sites are checked sequentially to avoid overload

## Security Considerations

1. **Store sensitive data in environment variables** (not in code)
2. **Use HTTPS** in production
3. **Validate all inputs** (already implemented via validation annotations)
4. **Limit API access** (add Spring Security if needed)
5. **Use strong database passwords**
6. **Enable PostgreSQL SSL** for remote connections

## Troubleshooting

### Issue: "Connection refused" for PostgreSQL

**Solution**: 
- Verify PostgreSQL is running
- Check database URL and credentials
- For Neon: Verify network access is allowed

### Issue: Email not sending

**Solution**:
- Verify App Password is correct (not regular password)
- Check Gmail security settings allow app access
- Verify SMTP credentials in `application.yaml`

### Issue: Telegram messages not received

**Solution**:
- Verify bot token is correct
- Ensure Telegram chat ID is valid
- Check bot is not marked as inactive

### Issue: Keyword detection not working

**Solution**:
- Verify website is accessible (check URL)
- Check website doesn't require JavaScript
- Verify keyword capitalization matches (search is case-insensitive)
- Check website HTML structure with browser inspector

### Issue: Scheduler not running

**Solution**:
- Verify `@EnableScheduling` is present on main class
- Check logs for scheduler startup
- Verify active monitored sites exist in database

## Future Enhancements

- [ ] WebSocket support for real-time alerts
- [ ] Advanced scheduling (cron expressions)
- [ ] Webhook notifications
- [ ] Dashboard UI
- [ ] Slack integration
- [ ] SMS notifications via Twilio
- [ ] User authentication and multi-tenant support
- [ ] Advanced filtering and regex support

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:

- **Issues**: GitHub Issues
- **Email**: support@example.com
- **Documentation**: Check the [Wiki](https://github.com/yourusername/keyword-check-hub/wiki)

---

**Happy Monitoring! 🚀**

