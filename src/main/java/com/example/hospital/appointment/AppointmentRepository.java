package com.example.hospital.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByPatient_IdAndAppointmentDateTimeGreaterThanAndAppointmentDateTimeLessThan(
            Long patientId,
            LocalDateTime oneHourBefore,
            LocalDateTime oneHourAfter);

    @Query("select appointment.doctor.id from Appointment appointment "
            + "where appointment.appointmentDateTime > :oneHourBefore "
            + "and appointment.appointmentDateTime < :oneHourAfter")
    Set<Long> findOccupiedDoctorIds(
            @Param("oneHourBefore") LocalDateTime oneHourBefore,
            @Param("oneHourAfter") LocalDateTime oneHourAfter);
}
