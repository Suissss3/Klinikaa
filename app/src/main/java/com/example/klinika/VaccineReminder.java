package com.example.klinika;

public class VaccineReminder {
    public String vaccineName;
    public String dueDate;
    public long dueTimestamp;
    public boolean notified;

    public VaccineReminder() {}

    public VaccineReminder(String vaccineName, String dueDate, long dueTimestamp, boolean notified) {
        this.vaccineName = vaccineName;
        this.dueDate = dueDate;
        this.dueTimestamp = dueTimestamp;
        this.notified = notified;
    }
}
