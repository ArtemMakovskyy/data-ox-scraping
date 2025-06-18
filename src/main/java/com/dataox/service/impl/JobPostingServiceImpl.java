package com.dataox.service.impl;

import com.dataox.dto.JobPostingDto;
import com.dataox.model.JobPosting;
import com.dataox.model.Location;
import com.dataox.model.Tag;
import com.dataox.repository.JobPostingRepository;
import com.dataox.repository.TagRepository;
import com.dataox.scraper.JobFetcher;
import com.dataox.service.JobPostingService;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class JobPostingServiceImpl implements JobPostingService {
    private static final List<String> VALID_LABOR_FUNCTIONS = List.of(
            "Accounting & Finance",
            "Administration",
            "Compliance / Regulatory",
            "Customer Service",
            "Data Science",
            "Design",
            "IT",
            "Legal",
            "Marketing & Communications",
            "Operations",
            "Other Engineering",
            "People & HR",
            "Product",
            "Quality Assurance",
            "Sales & Business Development",
            "Software Engineering"
    );

    private final JobPostingRepository jobPostingRepository;
    private final TagRepository tagRepository;
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

        JobPosting jobPosting = new JobPosting();
        jobPosting.setJobPageUrl(dto.getJobPageUrl());
        jobPosting.setPositionName(dto.getPositionName());
        jobPosting.setOrganizationUrl(dto.getOrganizationUrl());
        jobPosting.setLogoUrl(dto.getLogoUrl());
        jobPosting.setOrganizationTitle(dto.getOrganizationTitle());
        jobPosting.setLaborFunction(dto.getLaborFunction());
        jobPosting.setPostedDateUnix(dto.getPostedDateUnix());
        jobPosting.setDescriptionHtml(dto.getDescriptionHtml());

        // Set locations
        Set<Location> locations = new HashSet<>();
        if (dto.getLocations() != null) {
            dto.getLocations().forEach(address -> {
                Location location = new Location();
                location.setAddress(address);
                location.setJobPosting(jobPosting);
                locations.add(location);
            });
        }
        jobPosting.setLocations(locations);

        // Set tags
        Set<Tag> tags = new HashSet<>();
        if (dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                Optional<Tag> existingTag = tagRepository.findByName(tagName);
                Tag tag = existingTag.orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return newTag;
                });
                tags.add(tag);
            }
        }
        jobPosting.setTags(tags);

        return jobPostingRepository.save(jobPosting);
    }

    @Override
    public List<JobPosting> findAll() {
        return jobPostingRepository.findAll();
    }

    @Override
    public Optional<JobPosting> findById(Long id) {
        return jobPostingRepository.findById(id);
    }

    private String normalizeLaborFunction(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Labor function cannot be null or empty");
        }

        String trimmedInput = input.trim();

        for (String validFunction : VALID_LABOR_FUNCTIONS) {
            if (validFunction.equalsIgnoreCase(trimmedInput)) {
                return validFunction;
            }
        }

        throw new IllegalArgumentException("Invalid labor function: " + input
                + ". Valid options are: " + VALID_LABOR_FUNCTIONS);
    }

    private boolean existsByJobPageUrl(String jobPageUrl) {
        return jobPostingRepository.existsByJobPageUrl(jobPageUrl);
    }
}
