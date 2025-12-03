package org.codeup.parknexus.service;

import org.codeup.parknexus.domain.ActivityLog;
import org.codeup.parknexus.domain.User;

import java.util.List;
import java.util.UUID;

public interface IActivityLogService {
    void log(User user, String action, String details);
    List<ActivityLog> getUserLogs(UUID userId);
}

