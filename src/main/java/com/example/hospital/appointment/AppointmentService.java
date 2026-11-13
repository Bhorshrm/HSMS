package com.example.hospital.appointment;

import com.example.hospital.doctor.Doctor;
import com.example.hospital.doctor.DoctorService;
import com.example.hospital.patient.Patient;
import com.example.hospital.patient.PatientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ZoneId clinicTimeZone;

    public AppointmentService(
            AppointmentRepository repository,
            PatientService patientService,
            DoctorService doctorService,
            @Value("${clinic.time-zone}") String clinicTimeZone) {
        this.repository = repository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.clinicTimeZone = ZoneId.of(clinicTimeZone);
    }
@Transactional
    public AppointmentResponse retrieve(
            Long appointmentId) {
        return repository.findById(appointmentId)
                .map(AppointmentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }
    @Transactional
    public AppointmentResponse create(
            Long patientId, Long doctorId, LocalDateTime appointmentDateTime) {
        if (!appointmentDateTime.isAfter(LocalDateTime.now(clinicTimeZone))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Appointment time must be in the future");
        }

        Patient patient = patientService.findById(patientId);
        LocalDateTime oneHourBefore = appointmentDateTime.minusHours(1);
        LocalDateTime oneHourAfter = appointmentDateTime.plusHours(1);
        if (repository
                .existsByPatient_IdAndAppointmentDateTimeGreaterThanAndAppointmentDateTimeLessThan(
                        patientId, oneHourBefore, oneHourAfter)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This patient already has an appointment during that one-hour slot. "
                            + "Choose another time.");
        }

        Doctor doctor = doctorService.findById(doctorId);
        if (!doctor.getDoctorType().isAvailableAt(appointmentDateTime.toLocalTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, doctor.getDoctorType().getAvailabilityMessage());
        }

        Set<Long> occupiedDoctorIds = repository.findOccupiedDoctorIds(
                oneHourBefore,
                oneHourAfter);
        if (occupiedDoctorIds.contains(doctorId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This doctor already has an appointment during that one-hour slot. "
                            + "Choose another time or doctor.");
        }

        return AppointmentResponse.from(
                repository.save(new Appointment(patient, doctor, appointmentDateTime)));
    }
    
}
