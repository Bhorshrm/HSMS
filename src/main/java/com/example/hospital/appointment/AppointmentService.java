package com.example.hospital.appointment;

import com.example.hospital.doctor.Doctor;
import com.example.hospital.patient.Patient;
import com.example.hospital.patient.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository repository, PatientService patientService) {
        this.repository = repository;
        this.patientService = patientService;
    }

    @Transactional
    public AppointmentResponse create(Long patientId, LocalDateTime appointmentDateTime) {
        Patient patient = patientService.findById(patientId);
        Set<Long> occupiedDoctorIds = repository.findOccupiedDoctorIds(appointmentDateTime);
        System.out.println("OCCUPIEDDOCTORIDS");
        System.out.println(occupiedDoctorIds);
        Doctor doctor = patientService.findDoctorForAppointment(
                appointmentDateTime.toLocalTime(), occupiedDoctorIds);

        return AppointmentResponse.from(
                repository.save(new Appointment(patient, doctor, appointmentDateTime)));
    }
}
