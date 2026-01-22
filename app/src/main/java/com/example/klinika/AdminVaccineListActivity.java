package com.example.klinika;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminVaccineListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminVaccineAdapter adapter;
    private List<VaccineItem> vaccineList;
    private Button btnAddVaccine;

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("vaccination_records");
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");

    private String studentUid;
    private String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_vaccine_list);

        studentUid = getIntent().getStringExtra("uid");
        if (studentUid == null || studentUid.isEmpty()) {
            Toast.makeText(this, "No student selected!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerVaccines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vaccineList = new ArrayList<>();
        adapter = new AdminVaccineAdapter(this, vaccineList);
        recyclerView.setAdapter(adapter);

        btnAddVaccine = findViewById(R.id.btnAddVaccine);
        btnAddVaccine.setOnClickListener(v -> {
            Intent intent = new Intent(AdminVaccineListActivity.this, AddVaccinationActivity.class);
            intent.putExtra("uid", studentUid);
            intent.putExtra("name", studentName);
            startActivity(intent);
        });

        loadVaccinesRealtime(); // real-time listener
    }

    private void loadVaccinesRealtime() {
        // First, get student name in real-time
        usersRef.child(studentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentName = snapshot.child("name").getValue(String.class);

                // Now listen for vaccination records in real-time
                dbRef.child(studentUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot vaccineSnap) {
                        vaccineList.clear();
                        for (DataSnapshot recordSnap : vaccineSnap.getChildren()) {
                            VaccinationRecord record = recordSnap.getValue(VaccinationRecord.class);
                            if (record != null) {
                                VaccineItem item = new VaccineItem(
                                        studentName,
                                        record.vaccineName,
                                        record.dateTaken,
                                        record.dueDate
                                );
                                item.studentUid = studentUid;
                                item.recordId = recordSnap.getKey();
                                vaccineList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        // Optional: send a notification when new vaccines appear
                        for (VaccineItem item : vaccineList) {
                            sendNotificationToStudent(item.studentUid, studentName,
                                    "Vaccination record updated: " + item.vaccineName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminVaccineListActivity.this, "Failed to load vaccines", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminVaccineListActivity.this, "Failed to load student info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotificationToStudent(String studentUid, String studentName, String message) {
        String key = notifRef.push().getKey();
        if (key == null) return;

        NotificationItem n = new NotificationItem();
        n.id = key;
        n.studentUid = studentUid;
        n.studentName = studentName;
        n.type = "vaccination_update";
        n.message = message;
        n.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        n.read = false;

        notifRef.child(key).setValue(n);
    }
}
