package com.example.klinika;

public class VaccinationRecord {
    public String vaccineName;
    public String dateTaken;
    public String dueDate;

    public VaccinationRecord() {}

    public VaccinationRecord(String vaccineName, String dateTaken, String dueDate) {
        this.vaccineName = vaccineName;
        this.dateTaken = dateTaken;
        this.dueDate = dueDate;
    }
}

