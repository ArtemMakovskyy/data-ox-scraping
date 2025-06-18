package com.dataox.service.impl;

import com.dataox.service.DatabaseCleaner;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataCleaner implements DatabaseCleaner {
    private final Flyway flyway;

    @Override
    public void cleanDatabase() {
        flyway.clean();
        flyway.migrate();
    }
}
