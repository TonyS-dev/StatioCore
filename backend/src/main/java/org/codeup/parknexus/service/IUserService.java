package org.codeup.parknexus.service;

import org.codeup.parknexus.web.dto.user.DashboardResponse;

import java.util.UUID;

public interface IUserService {
    DashboardResponse getDashboard(UUID userId);
}
