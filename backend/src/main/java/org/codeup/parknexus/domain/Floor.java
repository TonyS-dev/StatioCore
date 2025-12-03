package org.codeup.parknexus.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "floors", uniqueConstraints = {
    @UniqueConstraint(name = "uk_building_floor", columnNames = {"building_id", "floor_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Floor {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 0;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
