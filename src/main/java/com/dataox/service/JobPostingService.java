package com.dataox.service;

import com.dataox.dto.JobPostingDto;
import com.dataox.model.JobPosting;
import java.util.List;
import java.util.Optional;

public interface JobPostingService {
    void scrapeAndSaveJobs(String laborFunction);

    JobPosting saveIfNotExists(JobPostingDto dto);

    List<JobPosting> findAll();

    Optional<JobPosting> findById(Long id);
}
