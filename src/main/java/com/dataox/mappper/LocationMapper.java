package com.dataox.mappper;

import com.dataox.config.MapperConfig;
import com.dataox.dto.LocationDto;
import com.dataox.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(config = MapperConfig.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {
    LocationDto toDto(Location entity);

    Location toEntity(LocationDto entity);
}
