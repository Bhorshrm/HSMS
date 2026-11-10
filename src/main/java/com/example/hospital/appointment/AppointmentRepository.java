package com.example.hospital.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Set;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("select appointment.doctor.id from Appointment appointment "
            + "where appointment.appointmentDateTime = :appointmentDateTime")
    Set<Long> findOccupiedDoctorIds(@Param("appointmentDateTime") LocalDateTime appointmentDateTime);
}
