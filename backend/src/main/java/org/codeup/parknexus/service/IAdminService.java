package org.codeup.parknexus.service;

import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.Role;
import org.codeup.parknexus.web.dto.admin.AdminDashboardResponse;
import org.codeup.parknexus.web.dto.admin.BuildingResponse;

import java.util.List;
import java.util.UUID;

public interface IAdminService {
    AdminDashboardResponse getDashboard();
    List<User> getAllUsers();
    User updateUserRole(UUID userId, Role role);
    List<BuildingResponse> getAllBuildings();
}

