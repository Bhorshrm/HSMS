package com.example.hospital.appointment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {
    @Mock
    private AppointmentService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AppointmentController(service)).build();
    }

    @Test
    void createsAppointmentAndReturnsAssignedDoctor() throws Exception {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 10, 0);
        when(service.create(7L, appointmentDateTime)).thenReturn(new AppointmentResponse(
                20L, 7L, "Jamie", "Doe", 3L, "Alex", "Smith", appointmentDateTime));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 7,
                                  "appointmentDateTime": "2030-05-10T10:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(3))
                .andExpect(jsonPath("$.patientId").value(7));
    }

    @Test
    void returnsConflictMessageWhenNoDoctorIsAvailable() throws Exception {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 10, 0);
        String message = "current appointments for doctors are fulfilled , schedule appointment "
                + "at appropriate time tomorrow";
        when(service.create(7L, appointmentDateTime)).thenThrow(
                new ResponseStatusException(HttpStatus.CONFLICT, message));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 7,
                                  "appointmentDateTime": "2030-05-10T10:00:00"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(message));
    }
}
