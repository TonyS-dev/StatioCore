package org.codeup.statiocore.repository;

import org.codeup.statiocore.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

@Repository
public interface IActivityLogRepository extends JpaRepository<ActivityLog, UUID>, JpaSpecificationExecutor<ActivityLog> {
    // View history for a specific user
    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // View the last N system events (For Admin Dashboard)
    // We use Pageable to limit to the last 10 or 20
    @Override
    @NonNull
    Page<ActivityLog> findAll(@NonNull Pageable pageable);
}

