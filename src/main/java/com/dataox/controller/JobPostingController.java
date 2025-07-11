package com.dataox.controller;

import com.dataox.dto.JobPostingDto;
import com.dataox.service.BatchJobScraperService;
import com.dataox.service.JobPostingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final BatchJobScraperService batchJobScraperService;

    @GetMapping
    public ResponseEntity<List<JobPostingDto>> getAllJobs() {
        return ResponseEntity.ok(jobPostingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostingDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.getById(id));
    }

    @PostMapping("/parse-all")
    public ResponseEntity<String> parseAllJobs() {
        batchJobScraperService.scrapeAllInParallel();
        return ResponseEntity.ok("Batch job scraping started.");
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<JobPostingDto>> getFilteredJobs(
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String organizationTitle,
            @RequestParam(required = false) String laborFunction,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "date") String sort
    ) {
        List<JobPostingDto> result = jobPostingService.findFiltered(
                positionName,
                organizationTitle,
                laborFunction,
                location,
                sort
        );
        return ResponseEntity.ok(result);
    }

}
