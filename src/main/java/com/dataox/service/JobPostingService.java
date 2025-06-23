package com.dataox.service;

import com.dataox.dto.JobPostingDto;
import com.dataox.model.JobPosting;
import java.util.List;

public interface JobPostingService {
    void scrapeAndSaveJobs(String laborFunction);

    JobPosting saveIfNotExists(JobPostingDto dto);

    List<JobPostingDto> findAll();

    JobPostingDto getById(Long id);

    List<JobPostingDto> findFiltered(
            String positionName,
            String organizationTitle,
            String laborFunction,
            String location,
            String sort
    );
}
