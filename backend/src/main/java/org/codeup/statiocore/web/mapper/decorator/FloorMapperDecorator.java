package org.codeup.statiocore.web.mapper.decorator;

import org.codeup.statiocore.domain.Floor;
import org.codeup.statiocore.domain.enums.SpotStatus;
import org.codeup.statiocore.repository.IParkingSpotRepository;
import org.codeup.statiocore.web.dto.admin.FloorResponse;
import org.codeup.statiocore.web.mapper.FloorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class FloorMapperDecorator implements FloorMapper {

    @Autowired
    @Qualifier("delegate")
    private FloorMapper delegate;

    @Autowired
    private IParkingSpotRepository spotRepository;

    @Override
    public FloorResponse toResponse(Floor floor) {
        FloorResponse response = delegate.toResponse(floor);

        // Populate count fields
        if (floor != null && floor.getId() != null) {
            response.setTotalSpots((int) spotRepository.countByFloorId(floor.getId()));
            response.setAvailableSpots((int) spotRepository.countByFloorIdAndStatus(floor.getId(), SpotStatus.AVAILABLE));
        }

        return response;
    }
}

