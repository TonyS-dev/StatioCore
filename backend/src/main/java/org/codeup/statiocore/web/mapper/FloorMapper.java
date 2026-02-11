package org.codeup.statiocore.web.mapper;

import org.codeup.statiocore.domain.Floor;
import org.codeup.statiocore.web.dto.admin.FloorResponse;
import org.codeup.statiocore.web.mapper.decorator.FloorMapperDecorator;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
@DecoratedWith(FloorMapperDecorator.class)
public interface FloorMapper {
    @Mapping(target = "buildingId", source = "building.id")
    @Mapping(target = "buildingName", source = "building.name")
    @Mapping(target = "floorNumber", source = "floorNumber")
    @Mapping(target = "spotCount", ignore = true)
    @Mapping(target = "totalSpots", ignore = true)
    @Mapping(target = "availableSpots", ignore = true)
    FloorResponse toResponse(Floor floor);
}

