package org.codeup.statiocore.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {
    // Overall Stats
    private Long totalSpots;
    private Long availableSpots;
    private Long occupiedSpots;
    private Double occupancyPercentage;
    
    // User Stats
    private Long activeReservations;
    private Long activeSessions;
    private Long totalReservations;
    private Long totalCompletedSessions;
    
    // Financial Stats
    private BigDecimal totalEarnings;
    private BigDecimal outstandingFees;
    private BigDecimal averageSessionFee;
    
    // Recent Activity
    private List<ActivityRecord> recentActivity;
    
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActivityRecord {
        private String action;
        private String details;
        private String timestamp;
    }
}
