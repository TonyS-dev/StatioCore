package org.codeup.parknexus.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.codeup.parknexus.domain.enums.SessionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "parking_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingSession {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    @Column(name = "check_in_time", nullable = false)
    private OffsetDateTime checkInTime;

    @Column(name = "check_out_time")
    private OffsetDateTime checkOutTime;

    @Column(name = "amount_due", precision = 10, scale = 2)
    private BigDecimal amountDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
