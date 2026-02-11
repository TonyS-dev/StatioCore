package org.codeup.statiocore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeup.statiocore.service.IAdminService;
import org.codeup.statiocore.service.IReservationService;
import org.codeup.statiocore.service.IParkingService;
import org.codeup.statiocore.service.IUserService;
import org.codeup.statiocore.web.controller.UserController;
import org.codeup.statiocore.web.mapper.ParkingSpotMapper;
import org.codeup.statiocore.web.mapper.ParkingSessionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @MockBean
    private IParkingService parkingService;

    @MockBean
    private IReservationService reservationService;

    @MockBean
    private IAdminService adminService;

    @MockBean
    private ParkingSpotMapper parkingSpotMapper;

    @MockBean
    private ParkingSessionMapper parkingSessionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void availableSpots_shouldReturnList() throws Exception {
        // Mock parking service to return empty list
        Mockito.when(parkingService.getAvailableSpots(null, null, null, null)).thenReturn(List.of());
        Mockito.when(parkingSpotMapper.toUserResponses(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/spots/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void getActiveSessions_shouldReturnList() throws Exception {
        Mockito.when(parkingService.getActiveSessions(any())).thenReturn(List.of());
        Mockito.when(parkingSessionMapper.toResponses(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/parking/sessions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
