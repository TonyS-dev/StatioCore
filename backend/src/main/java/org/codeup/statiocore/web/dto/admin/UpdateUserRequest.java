package org.codeup.statiocore.web.dto.admin;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeup.statiocore.domain.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    
    @Email
    private String email;
    
    private Role role;
    
    private Boolean isActive;
}
