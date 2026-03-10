package com.mahi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseSchemaInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking and updating database schema...");

        try {
            // Alter 'url' column to TEXT to support long URLs
            log.info("Altering 'url' column to TEXT type...");
            jdbcTemplate.execute("ALTER TABLE monitored_sites ALTER COLUMN url TYPE TEXT");
            log.info("'url' column altered successfully.");
        } catch (Exception e) {
            log.warn("Failed to alter 'url' column (it might already be correct or table missing): {}", e.getMessage());
        }

        try {
            // Alter 'keyword' column to TEXT to support long keyword lists
            log.info("Altering 'keyword' column to TEXT type...");
            jdbcTemplate.execute("ALTER TABLE monitored_sites ALTER COLUMN keyword TYPE TEXT");
            log.info("'keyword' column altered successfully.");
        } catch (Exception e) {
            log.warn("Failed to alter 'keyword' column (it might already be correct or table missing): {}", e.getMessage());
        }
    }
}
