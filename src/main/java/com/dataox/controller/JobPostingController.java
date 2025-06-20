package com.dataox.controller;

import com.dataox.dto.JobPostingDto;
import com.dataox.service.JobPostingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping
    public ResponseEntity<List<JobPostingDto>> getAllJobs() {
        return ResponseEntity.ok(jobPostingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.getById(id));
    }
}
