package org.codeup.statiocore.service;

import org.codeup.statiocore.web.dto.user.DashboardResponse;

import java.util.UUID;

public interface IUserService {
    DashboardResponse getDashboard(UUID userId);
}
