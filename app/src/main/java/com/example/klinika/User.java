package com.example.klinika;

public class User {
    private String uid;
    private String name;
    private String studentId;
    private String email;
    private String role;
    private String lastClinicVisit;

    public User() {}

    public User(String uid, String name, String studentId, String email, String role, String lastClinicVisit) {
        this.uid = uid;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.role = role;
        this.lastClinicVisit = lastClinicVisit;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLastClinicVisit() { return lastClinicVisit; }
    public void setLastClinicVisit(String lastClinicVisit) { this.lastClinicVisit = lastClinicVisit; }
}
