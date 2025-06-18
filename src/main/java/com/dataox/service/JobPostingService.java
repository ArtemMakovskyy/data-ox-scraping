package com.dataox.service;

import com.dataox.dto.JobPostingDTO;
import com.dataox.model.JobPosting;
import com.dataox.model.Location;
import com.dataox.model.Tag;
import com.dataox.repository.JobPostingRepository;
import com.dataox.repository.TagRepository;
import com.dataox.scraper.SeleniumHtmlFetcher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class JobPostingService {
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
    private final SeleniumHtmlFetcher seleniumHtmlFetcher;

    /**
     * Validates and normalizes the labor function string.
     * Throws IllegalArgumentException if input is invalid.
     */
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

        throw new IllegalArgumentException("Invalid labor function: " + input +
                ". Valid options are: " + VALID_LABOR_FUNCTIONS);
    }

    /**
     * Fetches job postings for a validated labor function and saves them if they don't already exist.
     */
    public void scrapeAndSaveJobs(String laborFunction) {
        String normalizedLaborFunction = normalizeLaborFunction(laborFunction);
        List<JobPostingDTO> fetchedJobs = seleniumHtmlFetcher.fetch(normalizedLaborFunction);

        for (JobPostingDTO dto : fetchedJobs) {
            try {
                saveIfNotExists(dto);
            } catch (Exception e) {
                log.error("Failed to save job posting [URL={}], position='{}'",
                        dto.getJobPageUrl(), dto.getPositionName(), e);
            }
        }
    }
    /**
     * Saves the job posting if it doesn't already exist by job page URL.
     * Returns the saved JobPosting or null if it already exists.
     */
    public JobPosting saveIfNotExists(JobPostingDTO dto) {
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
        List<Location> locations = new ArrayList<>();
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
        List<Tag> tags = new ArrayList<>();
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

    /**
     * Retrieves all saved job postings.
     */
    public List<JobPosting> findAll() {
        return jobPostingRepository.findAll();
    }

    /**
     * Finds a job posting by its ID.
     */
    public Optional<JobPosting> findById(Long id) {
        return jobPostingRepository.findById(id);
    }

    /**
     * Checks if a job posting exists by its job page URL.
     */
    private boolean existsByJobPageUrl(String jobPageUrl) {
        return jobPostingRepository.existsByJobPageUrl(jobPageUrl);
    }
}
