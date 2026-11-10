package com.example.hospital.doctor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class DoctorService {
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime STANDARD_END_TIME = LocalTime.of(18, 30);
    private static final String FULLY_BOOKED_MESSAGE =
            "current appointments for doctors are fulfilled , schedule appointment at appropriate time tomorrow";

    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    public List<Doctor> findAll() {
        return repository.findAll();
    }

    public Doctor findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    public Doctor create(Doctor doctor) {
        requireDoctorType(doctor);
        return repository.save(doctor);
    }

    public Doctor allocateStandardDoctor(LocalTime appointmentTime, Set<Long> occupiedDoctorIds) {
        if (appointmentTime == null || occupiedDoctorIds == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment time and occupied doctor IDs are required");
        }
        if (appointmentTime.isBefore(STANDARD_START_TIME) || appointmentTime.isAfter(STANDARD_END_TIME)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Standard doctors are available between 9:00 AM and 6:30 PM");
        }

        return repository.findAll().stream()
                .filter(doctor -> doctor.getDoctorType() == DoctorType.STANDARD)
                .filter(doctor -> !occupiedDoctorIds.contains(doctor.getId()))
                .sorted((first, second) -> first.getId().compareTo(second.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, FULLY_BOOKED_MESSAGE));
    }

    public Doctor update(Long id, Doctor input) {
        requireDoctorType(input);
        Doctor doctor = findById(id);
        doctor.setFirstName(input.getFirstName());
        doctor.setLastName(input.getLastName());
        doctor.setSpecialty(input.getSpecialty());
        doctor.setDoctorType(input.getDoctorType());
        doctor.setEmail(input.getEmail());
        doctor.setPhone(input.getPhone());
        return repository.save(doctor);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void requireDoctorType(Doctor doctor) {
        if (doctor.getDoctorType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor type is required");
        }
    }
}
