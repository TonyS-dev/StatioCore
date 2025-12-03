package org.codeup.parknexus.web.mapper;

import org.codeup.parknexus.domain.Floor;
import org.codeup.parknexus.web.dto.admin.FloorResponse;
import org.codeup.parknexus.web.mapper.decorator.FloorMapperDecorator;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
@DecoratedWith(FloorMapperDecorator.class)
public interface FloorMapper {
    @Mapping(target = "buildingId", source = "building.id")
    @Mapping(target = "buildingName", source = "building.name")
    @Mapping(target = "name", expression = "java(\"Floor \" + floor.getFloorNumber())")
    @Mapping(target = "totalSpots", ignore = true)
    @Mapping(target = "availableSpots", ignore = true)
    FloorResponse toResponse(Floor floor);
}

