package com.example.hospital.appointment;

import com.example.hospital.doctor.Doctor;
import com.example.hospital.patient.Patient;
import com.example.hospital.patient.PatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock
    private AppointmentRepository repository;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private AppointmentService service;

    @Test
    void createsAppointmentWithAnAvailableDoctor() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 10, 0);
        Patient patient = mock(Patient.class);
        Doctor doctor = mock(Doctor.class);
        when(patient.getId()).thenReturn(7L);
        when(patient.getFirstName()).thenReturn("Jamie");
        when(patient.getLastName()).thenReturn("Doe");
        when(doctor.getId()).thenReturn(3L);
        when(doctor.getFirstName()).thenReturn("Alex");
        when(doctor.getLastName()).thenReturn("Smith");
        when(patientService.findById(7L)).thenReturn(patient);
        when(repository.findOccupiedDoctorIds(appointmentDateTime)).thenReturn(Set.of(1L, 2L));
        when(patientService.findDoctorForAppointment(appointmentDateTime.toLocalTime(), Set.of(1L, 2L)))
                .thenReturn(doctor);
        when(repository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = service.create(7L, appointmentDateTime);

        assertThat(response.patientId()).isEqualTo(7L);
        assertThat(response.doctorId()).isEqualTo(3L);
        assertThat(response.appointmentDateTime()).isEqualTo(appointmentDateTime);
        verify(repository).findOccupiedDoctorIds(appointmentDateTime);
    }

    @Test
    void doesNotSaveAppointmentWhenNoDoctorIsAvailable() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 10, 0);
        when(patientService.findById(7L)).thenReturn(mock(Patient.class));
        when(repository.findOccupiedDoctorIds(appointmentDateTime)).thenReturn(Set.of(1L, 2L));
        when(patientService.findDoctorForAppointment(appointmentDateTime.toLocalTime(), Set.of(1L, 2L)))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "No doctor available"));

        assertThatThrownBy(() -> service.create(7L, appointmentDateTime))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(409));

        verify(repository, never()).save(any(Appointment.class));
    }
}
