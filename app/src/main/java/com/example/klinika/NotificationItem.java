package com.example.klinika;

public class NotificationItem {
    public String id;
    public String studentUid;
    public String studentName;
    public String type;
    public String message;
    public String timestamp;
    public boolean read;

    public NotificationItem() {}

    public NotificationItem(String studentUid, String message, String type, String timestamp) {
        this.studentUid = studentUid;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.read = false;
    }
}
