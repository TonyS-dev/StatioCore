package org.codeup.parknexus.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {
    private Long activeReservations;
    private Long activeSessions;
    private Long totalReservations;
    private BigDecimal outstandingFees;
}

