package com.example.klinika;

public class CertificateItem {
    public String id;
    public String studentUid;
    public String studentName;
    public String certType;
    public String base64File; // base64 encoded file (image/pdf etc.)
    public String fileMime;   // e.g. "image/jpeg" or "application/pdf"
    public String status;     // pending, approved, rejected
    public String requestedAt;

    public CertificateItem() {}

    public CertificateItem(String studentUid, String studentName, String certType, String base64File, String fileMime, String status, String requestedAt) {
        this.studentUid = studentUid;
        this.studentName = studentName;
        this.certType = certType;
        this.base64File = base64File;
        this.fileMime = fileMime;
        this.status = status;
        this.requestedAt = requestedAt;
    }
}
