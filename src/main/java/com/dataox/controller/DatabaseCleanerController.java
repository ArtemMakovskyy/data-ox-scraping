package com.dataox.controller;

import com.dataox.service.DataCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/db")
@RequiredArgsConstructor
public class DatabaseCleanerController {
    private final DataCleaner dataCleaner;


    @PostMapping("/clean")
    public ResponseEntity<String> cleanDatabase() {
        dataCleaner.cleanDatabase();
        return ResponseEntity.ok("Database cleaned and migrations applied.");
    }
}
