package org.codeup.statiocore.web.mapper;

import org.codeup.statiocore.domain.Building;
import org.codeup.statiocore.web.dto.admin.BuildingResponse;
import org.codeup.statiocore.web.mapper.decorator.BuildingMapperDecorator;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
@DecoratedWith(BuildingMapperDecorator.class)
public interface BuildingMapper {
    @Mapping(target = "totalFloors", ignore = true)
    @Mapping(target = "totalSpots", ignore = true)
    BuildingResponse toResponse(Building building);
}

