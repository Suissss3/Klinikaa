package com.example.klinika;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentRequestCertificateActivity extends AppCompatActivity {

    private Spinner spinnerCertType;
    private Button btnRequest;
    private DatabaseReference dbCertificates;
    private DatabaseReference notifRef;
    private String uid, studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_request_certificate);

        spinnerCertType = findViewById(R.id.spinnerCertTypeStudent);
        btnRequest = findViewById(R.id.btnRequestCert);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbCertificates = FirebaseDatabase.getInstance().getReference("certificates");
        notifRef = FirebaseDatabase.getInstance().getReference("notifications");

        studentName = "Student";
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("name")
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) studentName = snapshot.getValue(String.class);
                });

        String[] certTypes = {"PE Participation", "Internship Requirement", "Excuse from Class"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, certTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCertType.setAdapter(adapter);

        btnRequest.setOnClickListener(v -> requestCertificate());
    }

    private void requestCertificate() {
        String certType = spinnerCertType.getSelectedItem().toString();
        if (certType.isEmpty()) {
            Toast.makeText(this, "Select a type", Toast.LENGTH_SHORT).show();
            return;
        }
        String key = dbCertificates.push().getKey();
        if (key == null) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        CertificateItem it = new CertificateItem(uid, studentName, certType, "", "", "pending", date);
        dbCertificates.child(key).setValue(it).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Requested", Toast.LENGTH_SHORT).show();
                sendAdminNotification(certType);
            } else Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendAdminNotification(String certType) {
        String key = notifRef.push().getKey();
        if (key == null) return;
        NotificationItem n = new NotificationItem();
        n.type = "certificate_request";
        n.studentUid = uid;
        n.studentName = studentName;
        n.message = "Requested " + certType;
        n.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        n.read = false;
        notifRef.child(key).setValue(n);
    }
}
