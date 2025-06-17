package com.dataox.service;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataCleaner {
    private final Flyway flyway;

    public void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }
}
