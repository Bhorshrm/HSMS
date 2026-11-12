package com.example.hospital.appointment;

import com.example.hospital.doctor.Doctor;
import com.example.hospital.doctor.DoctorService;
import com.example.hospital.doctor.DoctorType;
import com.example.hospital.patient.Patient;
import com.example.hospital.patient.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Mock
    private DoctorService doctorService;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(repository, patientService, doctorService, "Asia/Kolkata");
    }

    @Test
    void rejectsAppointmentInThePastInClinicTimeZone() {
        LocalDateTime appointmentDateTime =
                LocalDateTime.now(ZoneId.of("Asia/Kolkata")).minusMinutes(1);

        assertThatThrownBy(() -> service.create(7L, 3L, appointmentDateTime))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException =
                            (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode().value()).isEqualTo(400);
                    assertThat(responseStatusException.getReason())
                            .isEqualTo("Appointment time must be in the future");
                });

        verify(patientService, never()).findById(7L);
        verify(doctorService, never()).findById(3L);
        verify(repository, never()).save(any(Appointment.class));
    }

    @Test
    void createsAppointmentWithTheSelectedAvailableDoctor() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 10, 0);
        Patient patient = mock(Patient.class);
        Doctor doctor = mock(Doctor.class);
        when(patient.getId()).thenReturn(7L);
        when(patient.getFirstName()).thenReturn("Jamie");
        when(patient.getLastName()).thenReturn("Doe");
        when(doctor.getId()).thenReturn(3L);
        when(doctor.getFirstName()).thenReturn("Alex");
        when(doctor.getLastName()).thenReturn("Smith");
        when(doctor.getDoctorType()).thenReturn(DoctorType.STANDARD);
        when(patientService.findById(7L)).thenReturn(patient);
        when(doctorService.findById(3L)).thenReturn(doctor);
        when(repository.findOccupiedDoctorIds(
                appointmentDateTime.minusHours(1), appointmentDateTime.plusHours(1)))
                .thenReturn(Set.of(1L, 2L));
        when(repository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = service.create(7L, 3L, appointmentDateTime);

        assertThat(response.patientId()).isEqualTo(7L);
        assertThat(response.doctorId()).isEqualTo(3L);
        assertThat(response.appointmentDateTime()).isEqualTo(appointmentDateTime);
        verify(repository).findOccupiedDoctorIds(
                appointmentDateTime.minusHours(1), appointmentDateTime.plusHours(1));
    }

    @Test
    void rejectsLimitedHoursDoctorOutsideScheduleWithHelpfulMessage() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 13, 30);
        Patient patient = mock(Patient.class);
        Doctor doctor = mock(Doctor.class);
        when(patientService.findById(7L)).thenReturn(mock(Patient.class));
        when(doctor.getDoctorType()).thenReturn(DoctorType.LIMITED_HOURS);
        when(doctorService.findById(3L)).thenReturn(doctor);

        assertThatThrownBy(() -> service.create(7L, 3L, appointmentDateTime))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException response = (ResponseStatusException) exception;
                    assertThat(response.getStatusCode().value()).isEqualTo(400);
                    assertThat(response.getReason()).isEqualTo(
                            "This limited-hours doctor is available only between 2:00 PM and 4:00 PM.");
                });

        verify(repository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsPatientWithAnOverlappingAppointmentEvenForAnotherDoctor() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 10, 20);
        when(patientService.findById(7L)).thenReturn(mock(Patient.class));
        when(repository
                .existsByPatient_IdAndAppointmentDateTimeGreaterThanAndAppointmentDateTimeLessThan(
                        7L, appointmentDateTime.minusHours(1), appointmentDateTime.plusHours(1)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(7L, 4L, appointmentDateTime))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException response = (ResponseStatusException) exception;
                    assertThat(response.getStatusCode().value()).isEqualTo(409);
                    assertThat(response.getReason())
                            .contains("This patient already has an appointment");
                });

        verify(doctorService, never()).findById(4L);
        verify(repository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsSelectedDoctorWhenAppointmentOverlaps() {
        LocalDateTime appointmentDateTime = LocalDateTime.of(2030, 5, 10, 14, 30);
        Patient patient = mock(Patient.class);
        Doctor doctor = mock(Doctor.class);
        when(patientService.findById(7L)).thenReturn(patient);
        when(doctor.getDoctorType()).thenReturn(DoctorType.LIMITED_HOURS);
        when(doctorService.findById(3L)).thenReturn(doctor);
        when(repository.findOccupiedDoctorIds(
                appointmentDateTime.minusHours(1), appointmentDateTime.plusHours(1)))
                .thenReturn(Set.of(3L));

        assertThatThrownBy(() -> service.create(7L, 3L, appointmentDateTime))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException response = (ResponseStatusException) exception;
                    assertThat(response.getStatusCode().value()).isEqualTo(409);
                    assertThat(response.getReason())
                            .contains("already has an appointment");
                });

        verify(repository, never()).save(any(Appointment.class));
    }
}
