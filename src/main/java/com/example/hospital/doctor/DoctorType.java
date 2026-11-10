package com.example.hospital.doctor;

import java.time.LocalTime;

public enum DoctorType {
    // lower priority number = preferred when several doctors are free
    PART_TIME(LocalTime.of(14, 0), LocalTime.of(16, 0), 1),
    STANDARD(LocalTime.of(9, 0), LocalTime.of(18, 30), 2);

    private final LocalTime start;
    private final LocalTime end;
    private final int priority;

    DoctorType(LocalTime start, LocalTime end, int priority) {
        this.start = start;
        this.end = end;
        this.priority = priority;
    }

    // start inclusive, end exclusive
    public boolean isAvailableAt(LocalTime time) {
        return !time.isBefore(start) && time.isBefore(end);
    }

    public int getPriority() {
        return priority;
    }
}
