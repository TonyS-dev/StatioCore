package org.codeup.parknexus.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.codeup.parknexus.domain.enums.SpotType;
import org.codeup.parknexus.domain.enums.SpotStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "parking_spots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_floor_spot", columnNames = {"floor_id", "spot_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingSpot {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(name = "spot_number", nullable = false, length = 20)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SpotType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SpotStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    private Long version;
}
