package org.codeup.statiocore.service;

import org.codeup.statiocore.domain.ActivityLog;
import org.codeup.statiocore.domain.User;

import java.util.List;
import java.util.UUID;

public interface IActivityLogService {
    void log(User user, String action, String details);
    List<ActivityLog> getUserLogs(UUID userId);
}

