package com.example.klinika;

public class MedicalCertificate {
    public String studentUid;
    public String studentName;
    public String type;
    public String status; // pending, approved, rejected
    public long requestDate;
    public String fileUrl;

    public MedicalCertificate() {} // required for Firebase

    public MedicalCertificate(String studentUid, String studentName, String type, String status, long requestDate, String fileUrl) {
        this.studentUid = studentUid;
        this.studentName = studentName;
        this.type = type;
        this.status = status;
        this.requestDate = requestDate;
        this.fileUrl = fileUrl;
    }
}
