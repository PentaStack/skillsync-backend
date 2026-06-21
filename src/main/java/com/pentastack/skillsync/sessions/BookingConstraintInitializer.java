package com.pentastack.skillsync.sessions;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class BookingConstraintInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbc;

    public BookingConstraintInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            jdbc.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS uq_mentor_slot "
                    + "ON review_sessions (mentor_id, start_time) WHERE status = 'SCHEDULED'");
        } catch (Exception e) {
            // Log warning, do not fail application startup (e.g. on H2 databases in test scope)
            System.out.println("Warning: Could not create partial unique index uq_mentor_slot: " + e.getMessage());
        }
    }
}
