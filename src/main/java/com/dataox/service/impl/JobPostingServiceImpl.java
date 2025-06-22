package com.dataox.service.impl;

import com.dataox.dto.JobPostingDto;
import com.dataox.exception.EntityNotFoundException;
import com.dataox.mappper.JobPostingMapper;
import com.dataox.model.JobPosting;
import com.dataox.model.LaborFunction;
import com.dataox.model.Tag;
import com.dataox.repository.JobPostingRepository;
import com.dataox.scraper.JobFetcher;
import com.dataox.service.JobPostingService;
import com.dataox.service.TagService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobPostingMapper jobPostingMapper;
    private final TagService tagService;
    private final JobFetcher jobFetcher;

    @Override
    public void scrapeAndSaveJobs(String laborFunction) {
        String normalizedLaborFunction = normalizeLaborFunction(laborFunction);
        List<JobPostingDto> fetchedJobs = jobFetcher.fetch(normalizedLaborFunction);

        for (JobPostingDto dto : fetchedJobs) {
            try {
                saveIfNotExists(dto);
            } catch (Exception e) {
                log.error("Failed to save job posting [URL={}], position='{}']",
                        dto.getJobPageUrl(), dto.getPositionName(), e);
            }
        }
    }

    @Override
    public JobPosting saveIfNotExists(JobPostingDto dto) {
        if (existsByJobPageUrl(dto.getJobPageUrl())) {
            return null;
        }

        JobPosting jobPosting = mapAndResolveTags(dto);
        return jobPostingRepository.save(jobPosting);
    }

    @Override
    public List<JobPostingDto> findAll() {
        return jobPostingRepository.findAll().stream()
                .map(jobPostingMapper::toDto)
                .toList();
    }

    @Override
    public JobPostingDto getById(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find JobPosting by id: " + id));
        return jobPostingMapper.toDto(jobPosting);
    }

    private JobPosting mapAndResolveTags(JobPostingDto dto) {
        JobPosting jobPosting = jobPostingMapper.toEntity(dto);
        Set<Tag> resolvedTags = tagService.resolveTags(jobPosting.getTags());
        jobPosting.setTags(resolvedTags);
        return jobPosting;
    }

    private String normalizeLaborFunction(String input) {
        return LaborFunction.fromString(input).getLabel();
    }

    private boolean existsByJobPageUrl(String jobPageUrl) {
        return jobPostingRepository.existsByJobPageUrl(jobPageUrl);
    }
}
