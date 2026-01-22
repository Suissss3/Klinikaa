package com.example.klinika;

public class HealthProfile {
    public String height;
    public String weight;
    public String bloodType;
    public String bmi;
    public String lastClinicVisit;

    public HealthProfile() {} // default constructor for Firebase

    public HealthProfile(String height, String weight, String bloodType, String bmi, String lastClinicVisit) {
        this.height = height;
        this.weight = weight;
        this.bloodType = bloodType;
        this.bmi = bmi;
        this.lastClinicVisit = lastClinicVisit;
    }
}
