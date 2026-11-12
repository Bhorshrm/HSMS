package com.example.hospital.doctor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.time.ZoneId;

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

    // public Doctor allocateStandardDoctor(LocalTime appointmentTime, Set<Long> occupiedDoctorIds) {
    //     if (appointmentTime == null || occupiedDoctorIds == null) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
    //                 "Appointment time and occupied doctor IDs are required");
    //     }
    //     if (appointmentTime.isBefore(STANDARD_START_TIME) || appointmentTime.isAfter(STANDARD_END_TIME)) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
    //                 "Standard doctors are available between 9:00 AM and 6:30 PM");
    //     }

    //     return repository.findAll().stream()
    //             .filter(doctor -> doctor.getDoctorType() == DoctorType.STANDARD)
    //             .filter(doctor -> !occupiedDoctorIds.contains(doctor.getId()))
    //             .sorted((first, second) -> first.getId().compareTo(second.getId()))
    //             .findFirst()
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, FULLY_BOOKED_MESSAGE));
    // }
    @Transactional(readOnly = true)
    public Doctor allocateStandardDoctor(LocalTime appointmentTime, Set<Long> occupiedDoctorIds) {
        if (appointmentTime == null || occupiedDoctorIds == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment time and occupied doctor IDs are required");
        }
        System.out.println("Received appointment time: " + appointmentTime);

        System.out.println("System timezone: " + ZoneId.systemDefault());

        System.out.println("Standard available: " +
                DoctorType.STANDARD.isAvailableAt(appointmentTime));

        System.out.println("Part-time available: " +
                DoctorType.LIMITED_HOURS.isAvailableAt(appointmentTime));
        boolean withinWorkingHours = Arrays.stream(DoctorType.values())
                .anyMatch(type -> type.isAvailableAt(appointmentTime));
        if (!withinWorkingHours) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Doctors are available between 9:00 AM and 6:30 PM");
        }

        return eligibleDoctors(appointmentTime).stream()
                .filter(doctor -> !occupiedDoctorIds.contains(doctor.getId()))
                .min(Comparator
                        .comparingInt((Doctor d) -> d.getDoctorType().getPriority())
                        .thenComparing(Doctor::getId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, FULLY_BOOKED_MESSAGE));
    }

    // All doctors who work at this time, whether or not they are booked
    private List<Doctor> eligibleDoctors(LocalTime time) {
        // return repository.findAll().stream()
        //         .filter(doctor -> doctor.getDoctorType().isAvailableAt(time))
        //         .toList();
          return repository.findAll().stream()
            .filter(doctor -> doctor.getDoctorType() == DoctorType.STANDARD)
            .filter(doctor -> doctor.getDoctorType().isAvailableAt(time))
            .toList();
    }
    @Transactional(readOnly = true)
    public int availableDoctorCount(LocalTime time, Set<Long> occupiedDoctorIds) {
        return (int) eligibleDoctors(time).stream()
                .filter(doctor -> !occupiedDoctorIds.contains(doctor.getId()))
                .count();
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
