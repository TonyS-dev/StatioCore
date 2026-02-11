package org.codeup.statiocore.repository;

import org.codeup.statiocore.domain.User;
import org.codeup.statiocore.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // For Login (AuthService)
    Optional<User> findByEmail(String email);

    // For Registration Validation (Prevent duplicates)
    boolean existsByEmail(String email);

    // For Admin Dashboard (Filter by role)
    List<User> findByRole(Role role);
    
    // Count active users
    long countByIsActive(boolean isActive);
}

