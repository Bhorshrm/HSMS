package com.example.hospital.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Page<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String first, String last, Pageable pageable);
  @Query("""
    SELECT p FROM Patient p
    WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(p.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(p.email)     LIKE LOWER(CONCAT('%', :search, '%'))
    """)
Page<Patient> search(@Param("search") String search, Pageable pageable);
}
