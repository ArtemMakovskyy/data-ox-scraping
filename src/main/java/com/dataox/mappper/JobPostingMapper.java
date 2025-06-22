package com.dataox.mappper;

import com.dataox.config.MapperConfig;
import com.dataox.dto.JobPostingDto;
import com.dataox.model.JobPosting;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LocationMapper.class, TagMapper.class}
)
public interface JobPostingMapper {

    JobPostingDto toDto(JobPosting entity);

    JobPosting toEntity(JobPostingDto dto);

}
