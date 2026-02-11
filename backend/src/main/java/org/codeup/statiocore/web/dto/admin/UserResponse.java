package org.codeup.statiocore.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}

