#  Keyword Monitor Service 🚀

[![CI/CD](https://github.com/BattaMahendra/keyword-monitor-service/actions/workflows/maven.yml/badge.svg)](https://github.com/BattaMahendra/keyword-monitor-service/actions/workflows/maven.yml)

Tired of manually checking websites for job openings or other important updates? Keyword Monitor is here to help! This service automates the process of tracking keywords on websites and notifies you as soon as they appear.   

**Live Application:** [https://keyword-monitor-service.onrender.com/](https://keyword-monitor-service.onrender.com/)

![Website Screenshot](src/main/resources/Images%20of%20Website/img.png)
![Website Screenshot 2](src/main/resources/Images%20of%20Website/img_1.png)

## How It Works 📝

Keyword Monitor allows you to specify a URL and a set of keywords to track. The service will periodically check the website for those keywords and send you a notification via email or Telegram if the count of any keyword increases.

### A Real-World Example: Job Hunting 🎯

I developed this tool to streamline my job search for Java backend positions. Instead of manually checking company career portals every day, I configured Keyword Monitor to watch the careers pages of companies like Accenture for keywords such as "Java," "Spring Boot," and "microservices."

When a new job matching my criteria was posted, I received an immediate notification, allowing me to be one of the first to apply. This saved me a significant amount of time and effort, and it can do the same for you!

## Technologies Used 💻

- **Backend:** Java, Spring Boot, Spring Data JPA
- **Database:** PostgreSQL
- **Web Scraping:** Playwright
- **Notifications:** Email (SMTP), Telegram Bot API
- **Caching:** Spring Cache
- **Frontend:** HTML, CSS, JavaScript, Bootstrap
- **Deployment:** Render

## Performance and Optimizations ⚡️

I've implemented several optimizations to ensure the application is fast and efficient:

- **Caching:** Website content is cached to avoid redundant scraping and reduce latency.
- **Asynchronous Processing:** Notifications are sent asynchronously to avoid blocking the main application thread.
- **Efficient Web Scraping:** Playwright is used for robust and efficient web scraping, even on dynamic websites.
- **Optimized Database Queries:** Database queries are optimized for performance, with indexes on frequently queried columns.

## Architecture 🏗️

### API Interaction Flow 🌐

```
+-------------------+     HTTP Request (e.g., POST /api/monitor)     +--------------------------+     JPA     +------------------+
|     User Browser  | ---------------------------------------------> | Spring Boot Application  | <---------> |    PostgreSQL    |
| (index.html/JS)   |                                                | (REST Controllers)       |             | (Monitored Sites)|
+-------------------+                                                +--------------------------+             +------------------+
        ^                                                                      |
        |                                                                      | API Responses (JSON)
        +------------------------------------------------------------------------+
```


### Scheduled Monitoring Flow ⏰

```
+---------------------+
| Spring Scheduler    |
| (Every X minutes)   |
+---------------------+
        |
        | 1. Fetch all Monitored Sites
        v
+--------------------------+
| Spring Boot Application  |
| (WebsiteMonitorService)  |
+--------------------------+
        |
        | 2. For each Site:
        |    a. Fetch Website Content (Playwright + Cache)
        |    b. Count Keyword Occurrences
        |    c. Compare with Last Count
        |    d. If count increased:
        |       i. Record Alert (DB)
        |       ii. Send Email Notification (Async)
        |       iii. Send Telegram Notification (Async)
        v
+------------------+    +-----------------+    +-------------------+
|    PostgreSQL    |    | Email Service   |    | Telegram Bot API  |
| (Alert History)  |    | (SMTP)          |    |                   |
+------------------+    +-----------------+    +-------------------+
```

### Database Schema Design 🗄️

```
+---------------------+       +---------------------+       +---------------------+
|    Monitored_Site   |       |    Keyword_Detail   |       |     Alert_History   |
+---------------------+       +---------------------+       +---------------------+
| id (PK)             | <-----| site_id (FK)        |       | id (PK)             |
| url                 |       | keyword             |       | site_id (FK)        |
| email               |       | last_count          |       | keyword             |
| telegram_chat_id    |       +---------------------+       | message             |
| last_checked        |                                     | alert_time          |
+---------------------+                                     | email_sent          |
                                                            | telegram_sent       |
                                                            +---------------------+
```

## How to Skip GitHub Actions 🤫

When pushing changes to your `README.md` file, you can add `[skip ci]` to your commit message to prevent the GitHub Actions pipeline from running. For example:

```bash
git commit -m "Update README.md [skip ci]"
```

---

<p align="center">
  Developed by Batta Mahendra &mdash; <em>Curiosity in Code.</em>
</p>
