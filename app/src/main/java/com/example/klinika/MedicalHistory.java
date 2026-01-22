package com.example.klinika;

public class MedicalHistory {
    public String id;
    public String patientUid;
    public String patientName;
    public String date;
    public String type;
    public String diagnosis;
    public String treatment;
    public long timestamp;

    public MedicalHistory() {}

    public MedicalHistory(String id, String patientUid, String patientName,
                          String date, String type, String diagnosis, String treatment, long timestamp) {
        this.id = id;
        this.patientUid = patientUid;
        this.patientName = patientName;
        this.date = date;
        this.type = type;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.timestamp = timestamp;
    }
}
