package com.dataox.mappper.impl;

import com.dataox.dto.JobPostingDto;
import com.dataox.mappper.JobPostingMapper;
import com.dataox.model.JobPosting;
import com.dataox.model.Location;
import com.dataox.model.Tag;
import com.dataox.repository.TagRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobPostingMapperImpl implements JobPostingMapper {

    private final TagRepository tagRepository;

    @Override
    public JobPosting toEntity(JobPostingDto dto) {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setJobPageUrl(dto.getJobPageUrl());
        jobPosting.setPositionName(dto.getPositionName());
        jobPosting.setOrganizationUrl(dto.getOrganizationUrl());
        jobPosting.setLogoUrl(dto.getLogoUrl());
        jobPosting.setOrganizationTitle(dto.getOrganizationTitle());
        jobPosting.setLaborFunction(dto.getLaborFunction());
        jobPosting.setPostedDateUnix(dto.getPostedDateUnix());
        jobPosting.setDescriptionHtml(dto.getDescriptionHtml());

        Set<Location> locations = dto.getLocations() != null
                ? dto.getLocations().stream()
                .map(address -> {
                    Location location = new Location();
                    location.setAddress(address);
                    location.setJobPosting(jobPosting);
                    return location;
                }).collect(Collectors.toSet())
                : Set.of();
        jobPosting.setLocations(locations);

        Set<Tag> tags = dto.getTags() != null
                ? dto.getTags().stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return newTag;
                        })).collect(Collectors.toSet())
                : Set.of();
        jobPosting.setTags(tags);

        return jobPosting;
    }

    @Override
    public JobPostingDto toDto(JobPosting entity) {
        JobPostingDto dto = new JobPostingDto();
        dto.setJobPageUrl(entity.getJobPageUrl());
        dto.setPositionName(entity.getPositionName());
        dto.setOrganizationUrl(entity.getOrganizationUrl());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setOrganizationTitle(entity.getOrganizationTitle());
        dto.setLaborFunction(entity.getLaborFunction());
        dto.setPostedDateUnix(entity.getPostedDateUnix());
        dto.setDescriptionHtml(entity.getDescriptionHtml());

        dto.setTags(entity.getTags() != null
                ? entity.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet())
                : Set.of());

        dto.setLocations(entity.getLocations() != null
                ? entity.getLocations().stream()
                .map(Location::getAddress)
                .collect(Collectors.toSet())
                : Set.of());

        return dto;
    }
}
