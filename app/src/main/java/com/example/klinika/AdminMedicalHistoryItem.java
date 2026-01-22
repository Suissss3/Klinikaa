package com.example.klinika;

public class AdminMedicalHistoryItem {
    private String date;
    private String typeOfVisit;
    private String diagnosis;
    private String treatment;

    public AdminMedicalHistoryItem() { }

    public AdminMedicalHistoryItem(String date, String typeOfVisit, String diagnosis, String treatment) {
        this.date = date;
        this.typeOfVisit = typeOfVisit;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
    }

    public String getDate() { return date; }
    public String getTypeOfVisit() { return typeOfVisit; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatment() { return treatment; }

    public void setDate(String date) { this.date = date; }
    public void setTypeOfVisit(String typeOfVisit) { this.typeOfVisit = typeOfVisit; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
}
