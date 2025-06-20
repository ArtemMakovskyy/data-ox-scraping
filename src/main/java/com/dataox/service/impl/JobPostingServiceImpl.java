package com.dataox.service.impl;

import com.dataox.dto.JobPostingDto;
import com.dataox.exception.EntityNotFoundException;
import com.dataox.mappper.JobPostingMapper;
import com.dataox.model.JobPosting;
import com.dataox.repository.JobPostingRepository;
import com.dataox.scraper.JobFetcher;
import com.dataox.service.JobPostingService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class JobPostingServiceImpl implements JobPostingService {

    private static final List<String> VALID_LABOR_FUNCTIONS = List.of(
            "Accounting & Finance", "Administration", "Compliance / Regulatory", "Customer Service",
            "Data Science", "Design", "IT", "Legal", "Marketing & Communications", "Operations",
            "Other Engineering", "People & HR", "Product", "Quality Assurance",
            "Sales & Business Development", "Software Engineering"
    );

    private final JobPostingRepository jobPostingRepository;
    private final JobPostingMapper jobPostingMapper;
    private final JobFetcher jobFetcher;

    @Override
    public void scrapeAndSaveJobs(String laborFunction) {
        String normalizedLaborFunction = normalizeLaborFunction(laborFunction);
        List<JobPostingDto> fetchedJobs = jobFetcher.fetch(normalizedLaborFunction);

        for (JobPostingDto dto : fetchedJobs) {
            try {
                saveIfNotExists(dto);
            } catch (Exception e) {
                log.error("Failed to save job posting [URL={}], position='{}'",
                        dto.getJobPageUrl(), dto.getPositionName(), e);
            }
        }
    }

    @Override
    public JobPosting saveIfNotExists(JobPostingDto dto) {
        if (existsByJobPageUrl(dto.getJobPageUrl())) {
            return null;
        }

        JobPosting jobPosting = jobPostingMapper.toEntity(dto);
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

    private String normalizeLaborFunction(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Labor function cannot be null or empty");
        }

        String trimmedInput = input.trim();
        return VALID_LABOR_FUNCTIONS.stream()
                .filter(valid -> valid.equalsIgnoreCase(trimmedInput))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid labor function: " + input
                        + ". Valid options are: " + VALID_LABOR_FUNCTIONS));
    }

    private boolean existsByJobPageUrl(String jobPageUrl) {
        return jobPostingRepository.existsByJobPageUrl(jobPageUrl);
    }
}
