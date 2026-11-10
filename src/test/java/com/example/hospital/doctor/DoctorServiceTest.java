package com.example.hospital.doctor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {
    private static final String FULLY_BOOKED_MESSAGE =
            "current appointments for doctors are fulfilled , schedule appointment at appropriate time tomorrow";

    @Mock
    private DoctorRepository repository;

    @InjectMocks
    private DoctorService service;

    private Doctor standardDoctorOne;
    private Doctor standardDoctorTwo;
    private Doctor limitedHoursDoctor;

    @BeforeEach
    void setUp() {
        standardDoctorOne = doctor(1L, DoctorType.STANDARD);
        standardDoctorTwo = doctor(2L, DoctorType.STANDARD);
        limitedHoursDoctor = doctor(3L, DoctorType.PART_TIME);
        lenient().when(repository.findAll()).thenReturn(
                List.of(limitedHoursDoctor, standardDoctorTwo, standardDoctorOne));
    }

    @Test
    void allocatesOneAvailableStandardDoctorInStableOrder() {
        Doctor assigned = service.allocateStandardDoctor(LocalTime.of(9, 0), Set.of(1L));

        assertThat(assigned).isSameAs(standardDoctorTwo);
    }

    @Test
    void doesNotAllocateLimitedHoursDoctorWhenStandardDoctorsAreFull() {
        assertThatThrownBy(() ->
                service.allocateStandardDoctor(LocalTime.of(14, 0), Set.of(1L, 2L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException response = (ResponseStatusException) exception;
                    assertThat(response.getStatusCode().value()).isEqualTo(409);
                    assertThat(response.getReason()).isEqualTo(FULLY_BOOKED_MESSAGE);
                });
    }

    @Test
    void acceptsStandardDoctorWorkingHourBoundaries() {
        assertThat(service.allocateStandardDoctor(LocalTime.of(9, 0), Set.of()))
                .isSameAs(standardDoctorOne);
        assertThat(service.allocateStandardDoctor(LocalTime.of(18, 30), Set.of()))
                .isSameAs(standardDoctorOne);
    }

    @Test
    void rejectsAppointmentOutsideStandardDoctorWorkingHours() {
        assertThatThrownBy(() ->
                service.allocateStandardDoctor(LocalTime.of(18, 31), Set.of()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException response = (ResponseStatusException) exception;
                    assertThat(response.getStatusCode().value()).isEqualTo(400);
                });

        verify(repository, never()).findAll();
    }

    @Test
    void requiresDoctorTypeWhenCreatingDoctor() {
        Doctor unclassified = doctor(4L, null);

        assertThatThrownBy(() -> service.create(unclassified))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Doctor type is required");
    }

    private static Doctor doctor(Long id, DoctorType type) {
        Doctor doctor = new Doctor();
        ReflectionTestUtils.setField(doctor, "id", id);
        doctor.setDoctorType(type);
        return doctor;
    }
}
