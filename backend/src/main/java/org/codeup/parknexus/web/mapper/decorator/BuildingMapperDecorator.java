package org.codeup.parknexus.web.mapper.decorator;

import org.codeup.parknexus.domain.Building;
import org.codeup.parknexus.repository.IFloorRepository;
import org.codeup.parknexus.repository.IParkingSpotRepository;
import org.codeup.parknexus.web.dto.admin.BuildingResponse;
import org.codeup.parknexus.web.mapper.BuildingMapper;
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

