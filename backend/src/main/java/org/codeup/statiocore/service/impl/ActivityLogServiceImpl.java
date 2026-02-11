package org.codeup.statiocore.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.statiocore.domain.ActivityLog;
import org.codeup.statiocore.domain.User;
import org.codeup.statiocore.repository.IActivityLogRepository;
import org.codeup.statiocore.service.IActivityLogService;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityLogServiceImpl implements IActivityLogService {
    private final IActivityLogRepository repository;

    @Override
    public void log(User user, String action, String details) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .createdAt(OffsetDateTime.now())
                .build();
        repository.save(log);
    }

    @Override
    public List<ActivityLog> getUserLogs(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

