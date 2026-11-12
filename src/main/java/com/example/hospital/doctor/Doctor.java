package com.example.hospital.doctor;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank @Size(max = 50)
    @Column(nullable = false)
    private String firstName;

    @NotBlank @Size(max = 50)
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Column(nullable = false)
    private String specialty;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DoctorType doctorType;

    @Email
    @Column(unique = true)
    private String email;
    @NotBlank @Size(max = 20)
    private String phone;
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "exited_at")
    private LocalDateTime exitedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        joinedAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    protected Doctor() {}

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public DoctorType getDoctorType() { return doctorType; }
    public void setDoctorType(DoctorType doctorType) { this.doctorType = doctorType; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public LocalDateTime getExitedAt() { return exitedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setExitedAt(LocalDateTime exitedAt) { this.exitedAt = exitedAt; }
}
