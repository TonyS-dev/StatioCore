package org.codeup.parknexus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeup.parknexus.domain.ParkingSpot;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.service.IReservationService;
import org.codeup.parknexus.service.IParkingService;
import org.codeup.parknexus.service.IUserService;
import org.codeup.parknexus.web.controller.UserController;
import org.codeup.parknexus.web.dto.user.ParkingSpotResponse;
import org.codeup.parknexus.web.dto.user.ParkingSessionResponse;
import org.codeup.parknexus.web.mapper.ParkingSpotMapper;
import org.codeup.parknexus.web.mapper.ParkingSessionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(controllers = UserController.class)
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

