package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.Role;
import org.codeup.parknexus.repository.IUserRepository;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.web.dto.admin.AdminDashboardResponse;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements IAdminService {
    private final IUserRepository userRepository;

    @Override
    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long admins = userRepository.findByRole(Role.ADMIN).size();
        long users = userRepository.findByRole(Role.USER).size();
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .adminsCount(admins)
                .usersCount(users)
                .build();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }
}
