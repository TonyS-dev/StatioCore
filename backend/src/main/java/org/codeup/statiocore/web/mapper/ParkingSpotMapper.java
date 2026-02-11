package org.codeup.statiocore.web.mapper;

import org.codeup.statiocore.domain.ParkingSpot;
import org.codeup.statiocore.web.dto.admin.SpotResponse;
import org.codeup.statiocore.web.dto.user.ParkingSpotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParkingSpotMapper {
    @Mapping(target = "floorName", expression = "java(\"Floor \" + spot.getFloor().getFloorNumber())")
    @Mapping(target = "floorNumber", source = "floor.floorNumber")
    @Mapping(target = "buildingName", source = "floor.building.name")
    @Mapping(target = "buildingAddress", source = "floor.building.address")
    @Mapping(target = "type", expression = "java(spot.getType().name())")
    @Mapping(target = "status", expression = "java(spot.getStatus().name())")
    @Mapping(target = "hourlyRate", expression = "java(spot.getType() == org.codeup.statiocore.domain.enums.SpotType.VIP ? 15.0 : 10.0)")
    ParkingSpotResponse toUserResponse(ParkingSpot spot);
    List<ParkingSpotResponse> toUserResponses(List<ParkingSpot> spots);

    @Mapping(target = "floorId", source = "floor.id")
    @Mapping(target = "floorNumber", source = "floor.floorNumber")
    @Mapping(target = "buildingName", source = "floor.building.name")
    @Mapping(target = "type", expression = "java(spot.getType().name())")
    @Mapping(target = "status", expression = "java(spot.getStatus().name())")
    SpotResponse toAdminResponse(ParkingSpot spot);
    List<SpotResponse> toAdminResponses(List<ParkingSpot> spots);
}

