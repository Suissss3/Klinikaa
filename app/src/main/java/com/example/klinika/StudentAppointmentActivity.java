package com.example.klinika;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class StudentAppointmentActivity extends AppCompatActivity {

    private Spinner spinnerAdmins;
    private EditText etDate, etTime, etReason;
    private Button btnBook;

    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference appointmentRef = FirebaseDatabase.getInstance().getReference("appointments");
    private DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");

    private String studentUid, studentName;
    private List<String> adminNames = new ArrayList<>();
    private List<String> adminUids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_appointment);

        spinnerAdmins = findViewById(R.id.spinnerAdmins);
        etDate = findViewById(R.id.etAppointmentDate);
        etTime = findViewById(R.id.etAppointmentTime);
        etReason = findViewById(R.id.etAppointmentReason);
        btnBook = findViewById(R.id.btnBookAppointment);

        studentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(studentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentName = snapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        loadAdmins();

        // Date picker
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(StudentAppointmentActivity.this, (dp, year, month, day) ->
                    etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time picker
        etTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(StudentAppointmentActivity.this, (tp, hour, minute) ->
                    etTime.setText(String.format("%02d:%02d", hour, minute)),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        btnBook.setOnClickListener(v -> bookAppointment());
    }

    private void loadAdmins() {
        // Load all users with role = "admin"
        usersRef.orderByChild("role").equalTo("admin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adminNames.clear();
                        adminUids.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String name = data.child("name").getValue(String.class);
                            String uid = data.getKey();
                            if (name != null && uid != null) {
                                adminNames.add(name);
                                adminUids.add(uid);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(StudentAppointmentActivity.this,
                                android.R.layout.simple_spinner_item, adminNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerAdmins.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void bookAppointment() {
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String reason = etReason.getText().toString().trim();
        int pos = spinnerAdmins.getSelectedItemPosition();

        if (date.isEmpty() || time.isEmpty() || reason.isEmpty() || pos < 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String adminUid = adminUids.get(pos);
        String adminName = adminNames.get(pos);
        String id = appointmentRef.push().getKey();
        if (id == null) return;

        long timestamp = System.currentTimeMillis();
        Appointment appt = new Appointment(id, studentUid, studentName, adminUid, adminName, date, time, reason, "pending", timestamp);
        appointmentRef.child(id).setValue(appt).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(StudentAppointmentActivity.this, "Appointment booked", Toast.LENGTH_SHORT).show();
                sendNotification(adminUid, "New appointment request from " + studentName, "Appointment Request");
                finish();
            } else {
                Toast.makeText(StudentAppointmentActivity.this, "Failed to book appointment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String uid, String message, String type) {
        String key = notifRef.push().getKey();
        if (key == null) return;

        NotificationItem n = new NotificationItem();
        n.id = key;
        n.studentUid = uid; // recipient
        n.type = type;
        n.message = message;
        n.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        n.read = false;

        notifRef.child(key).setValue(n);
    }
}
