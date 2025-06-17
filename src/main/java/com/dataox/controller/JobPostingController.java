package com.dataox.controller;

import com.dataox.dto.JobPostingDTO;
import com.dataox.model.JobPosting;
import com.dataox.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping("/parse")
    public ResponseEntity<String> parse(@RequestParam String laborFunction) {
        jobPostingService.scrapeAndSaveJobs(laborFunction);
        return ResponseEntity.ok("ok");
    }

    @PostMapping
    public ResponseEntity<JobPosting> createJob(@RequestBody JobPostingDTO dto) {
        JobPosting savedJob = jobPostingService.saveIfNotExists(dto);
        if (savedJob == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedJob);
    }

    @GetMapping
    public ResponseEntity<List<JobPosting>> getAllJobs() {
        List<JobPosting> jobs = jobPostingService.findAll();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPosting> getJobById(@PathVariable Long id) {
        return jobPostingService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
