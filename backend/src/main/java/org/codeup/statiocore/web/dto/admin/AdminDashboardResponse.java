package org.codeup.statiocore.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDashboardResponse {
    private Long totalSpots;
    private Long occupiedSpots;
    private Long availableSpots;
    private BigDecimal totalRevenue;
    private Long totalUsers;
    private Long totalAdmins;
    private Long activeUsers;
    private Long activeSessions;
    private Long totalReservations;
    private Long totalPayments;
}
