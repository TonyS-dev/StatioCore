package org.codeup.statiocore.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityLogResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String details;
    private OffsetDateTime createdAt;
}

