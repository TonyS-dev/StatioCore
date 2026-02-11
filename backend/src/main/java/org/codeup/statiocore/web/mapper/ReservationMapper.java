package org.codeup.statiocore.web.mapper;

import org.codeup.statiocore.domain.Reservation;
import org.codeup.statiocore.web.dto.user.ReservationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "spotId", source = "spot.id")
    @Mapping(target = "spotNumber", source = "spot.spotNumber")
    @Mapping(target = "buildingName", source = "spot.floor.building.name")
    @Mapping(target = "floorNumber", source = "spot.floor.floorNumber")
    @Mapping(target = "vehicleNumber", source = "vehicleNumber")
    @Mapping(target = "status", expression = "java(reservation.getStatus().name())")
    @Mapping(target = "createdAt", source = "createdAt")
    ReservationResponse toResponse(Reservation reservation);
}
