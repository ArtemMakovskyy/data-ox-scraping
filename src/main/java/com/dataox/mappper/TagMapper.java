package com.dataox.mappper;

import com.dataox.config.MapperConfig;
import com.dataox.dto.TagDto;
import com.dataox.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    TagDto toDto(Tag entity);

    Tag toEntity(TagDto entity);
}
