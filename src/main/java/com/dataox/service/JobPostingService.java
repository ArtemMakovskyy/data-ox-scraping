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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final TagRepository tagRepository;
    private final SeleniumHtmlFetcher seleniumHtmlFetcher;

    public void parse(){
        List<JobPostingDTO> fetch = seleniumHtmlFetcher.fetch();
        for (JobPostingDTO jobPostingDTO : fetch) {
            saveJobPosting(jobPostingDTO);
        }
    }

    public JobPosting saveJobPosting(JobPostingDTO dto) {
        if (jobPostingRepository.existsByJobPageUrl(dto.getJobPageUrl())) {
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

        // Locations
        List<Location> locations = new ArrayList<>();
        if (dto.getLocations() != null) {
            dto.getLocations().forEach(locStr -> {
                Location loc = new Location();
                loc.setAddress(locStr);
                loc.setJobPosting(jobPosting);
                locations.add(loc);
            });
        }
        jobPosting.setLocations(locations);

        // Tags
        List<Tag> tags = new ArrayList<>();
        if (dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                Optional<Tag> tagOpt = tagRepository.findByName(tagName);
                Tag tag = tagOpt.orElseGet(() -> {
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

    public List<JobPosting> getAllJobPostings() {
        return jobPostingRepository.findAll();
    }

    public Optional<JobPosting> getJobPostingById(Long id) {
        return jobPostingRepository.findById(id);
    }
}
