package com.example.hospital.patient;

import com.example.hospital.doctor.Doctor;
import com.example.hospital.doctor.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
@Service
public class PatientService {
    private final PatientRepository repository;
    private final DoctorService doctorService;

    public PatientService(PatientRepository repository, DoctorService doctorService) {
        this.repository = repository;
        this.doctorService = doctorService;
    }

    public List<Patient> findAll() {
        return repository.findAll();
    }

    public Patient findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    public Patient create(Patient patient) {
        return repository.save(patient);
    }

    public Doctor findDoctorForAppointment(LocalTime appointmentTime, Set<Long> occupiedDoctorIds) {
        return doctorService.allocateStandardDoctor(appointmentTime, occupiedDoctorIds);
    }

    public Patient update(Long id, Patient input) {
        Patient patient = findById(id);
        patient.setFirstName(input.getFirstName());
        patient.setLastName(input.getLastName());
        patient.setDateOfBirth(input.getDateOfBirth());
        patient.setEmail(input.getEmail());
        patient.setPhone(input.getPhone());
        return repository.save(patient);
    }
    @Transactional(readOnly = true)
    public Page<Patient> search(String search, Pageable pageable) {
        String q = search == null ? "" : search.trim();
        return repository.search(q, pageable);
    }

    @Transactional(readOnly = true)
    public List<Patient> listAll() {
        return repository.findAll(Sort.by("firstName", "lastName"));
    }
    public void delete(Long id) {
        repository.delete(findById(id));
         Patient patient = findById(id);
       try {
           repository.delete(patient);
       } catch (DataIntegrityViolationException e) {
           throw new ResponseStatusException(HttpStatus.CONFLICT,
                   "This patient has appointments. Delete those appointments first.");
       }
    }
}
