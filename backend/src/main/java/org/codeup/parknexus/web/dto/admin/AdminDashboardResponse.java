package org.codeup.parknexus.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDashboardResponse {
    private Long totalSpots;
    private Long occupiedSpots;
    private Double totalRevenue;
    private Long totalUsers;
    private Long adminsCount;
    private Long usersCount;
}
