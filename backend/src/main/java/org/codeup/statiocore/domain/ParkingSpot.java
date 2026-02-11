package org.codeup.statiocore.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.codeup.statiocore.domain.enums.SpotType;
import org.codeup.statiocore.domain.enums.SpotStatus;

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

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
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

    @ManyToOne
    @JoinColumn(name = "reserved_by_user_id")
    private User reservedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
