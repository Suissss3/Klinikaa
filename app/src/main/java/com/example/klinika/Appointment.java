package com.example.klinika;

public class Appointment {
    public String id;
    public String studentUid;
    public String studentName;
    public String adminUid;
    public String adminName;
    public String date;
    public String time;
    public String reason;
    public String status;
    public long timestamp;

    public Appointment() {} // required for Firebase

    public Appointment(String id, String studentUid, String studentName, String adminUid, String adminName,
                       String date, String time, String reason, String status, long timestamp) {
        this.id = id;
        this.studentUid = studentUid;
        this.studentName = studentName;
        this.adminUid = adminUid;
        this.adminName = adminName;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = status;
        this.timestamp = timestamp;
    }
}
