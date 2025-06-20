package com.dataox.mappper;

import com.dataox.dto.JobPostingDto;
import com.dataox.model.JobPosting;

public interface JobPostingMapper {
    JobPosting toEntity(JobPostingDto dto);

    JobPostingDto toDto(JobPosting entity);
}
