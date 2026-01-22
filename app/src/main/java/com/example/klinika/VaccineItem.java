package com.example.klinika;

public class VaccineItem {
    public String studentName;
    public String vaccineName;
    public String dateTaken;
    public String dueDate;

    // Firebase references for editing/deleting
    public String studentUid;
    public String recordId;

    public VaccineItem() {}

    public VaccineItem(String studentName, String vaccineName, String dateTaken, String dueDate) {
        this.studentName = studentName;
        this.vaccineName = vaccineName;
        this.dateTaken = dateTaken;
        this.dueDate = dueDate;
    }
}
