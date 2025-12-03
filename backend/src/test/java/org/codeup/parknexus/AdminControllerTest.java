package org.codeup.parknexus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.repository.IActivityLogRepository;
import org.codeup.parknexus.repository.IUserRepository;
import org.codeup.parknexus.web.controller.AdminController;
import org.codeup.parknexus.web.mapper.ActivityLogMapper;
import org.codeup.parknexus.web.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.notNullValue;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAdminService adminService;

    @MockBean
    private IActivityLogRepository activityLogRepository;

    @MockBean
    private IUserRepository userRepository;

    @MockBean
    private ActivityLogMapper activityLogMapper;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void dashboard_shouldReturnOk() throws Exception {
        Mockito.when(adminService.getDashboard()).thenReturn(null);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    public void getBuildings_shouldReturnOk() throws Exception {
        Mockito.when(adminService.getAllBuildings()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }
}

