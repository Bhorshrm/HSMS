package com.example.hospital.appointment;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientFirstName,
        String patientLastName,
        Long doctorId,
        String doctorFirstName,
        String doctorLastName,
        LocalDateTime appointmentDateTime
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFirstName(),
                appointment.getPatient().getLastName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentDateTime());
    }
}
