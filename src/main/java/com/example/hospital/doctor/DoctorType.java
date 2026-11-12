package com.example.hospital.doctor;

import java.time.LocalTime;

public enum DoctorType {
    // lower priority number = preferred when several doctors are free
    LIMITED_HOURS(
            LocalTime.of(14, 0), LocalTime.of(16, 0), 1,
            "This limited-hours doctor is available only between 2:00 PM and 4:00 PM."),
    STANDARD(
            LocalTime.of(9, 0), LocalTime.of(18, 30), 2,
            "This doctor is available between 9:00 AM and 6:30 PM.");

    private final LocalTime start;
    private final LocalTime end;
    private final int priority;
    private final String availabilityMessage;

    DoctorType(LocalTime start, LocalTime end, int priority, String availabilityMessage) {
        this.start = start;
        this.end = end;
        this.priority = priority;
        this.availabilityMessage = availabilityMessage;
    }

    // The full one-hour appointment must fit inside the doctor's working hours.
    public boolean isAvailableAt(LocalTime time) {
        return !time.isBefore(start) && !time.plusHours(1).isAfter(end);
    }

    public int getPriority() {
        return priority;
    }

    public String getAvailabilityMessage() {
        return availabilityMessage;
    }
}
