package org.codeup.statiocore.web.mapper.decorator;

import org.codeup.statiocore.domain.Building;
import org.codeup.statiocore.repository.IFloorRepository;
import org.codeup.statiocore.repository.IParkingSpotRepository;
import org.codeup.statiocore.web.dto.admin.BuildingResponse;
import org.codeup.statiocore.web.mapper.BuildingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class BuildingMapperDecorator implements BuildingMapper {

    @Autowired
    @Qualifier("delegate")
    private BuildingMapper delegate;

    @Autowired
    private IFloorRepository floorRepository;

    @Autowired
    private IParkingSpotRepository spotRepository;

    @Override
    public BuildingResponse toResponse(Building building) {
        BuildingResponse response = delegate.toResponse(building);

        // Populate count fields
        if (building != null && building.getId() != null) {
            response.setTotalFloors((int) floorRepository.countByBuildingId(building.getId()));
            response.setTotalSpots((int) spotRepository.countByFloorBuildingId(building.getId()));
        }

        return response;
    }
}

