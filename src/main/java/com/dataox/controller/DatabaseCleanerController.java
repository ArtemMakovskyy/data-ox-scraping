package com.dataox.controller;

import com.dataox.service.DatabaseCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/db")
@RequiredArgsConstructor
public class DatabaseCleanerController {
    private final DatabaseCleaner databaseCleaner;

    @PostMapping("/clean")
    public ResponseEntity<String> cleanDatabase() {
        databaseCleaner.cleanDatabase();
        return ResponseEntity.ok("Database cleaned.");
    }
}
