package com.example.klinika;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VaccineListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VaccineAdapter adapter;
    private ArrayList<VaccinationRecord> vaccineList = new ArrayList<>();
    private DatabaseReference ref;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_list);

        uid = getIntent().getStringExtra("uid");

        recyclerView = findViewById(R.id.recyclerVaccines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VaccineAdapter(vaccineList);
        recyclerView.setAdapter(adapter);

        // Correct Firebase path
        ref = FirebaseDatabase.getInstance().getReference("vaccination_records").child(uid);

        loadVaccinationRecords();
    }

    private void loadVaccinationRecords() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vaccineList.clear();

                for (DataSnapshot recordSnap : snapshot.getChildren()) {
                    VaccinationRecord record = recordSnap.getValue(VaccinationRecord.class);
                    if (record != null) {
                        vaccineList.add(record);

                        // Schedule reminder safely
                        if (record.dueDate != null && !record.dueDate.isEmpty()) {
                            scheduleVaccinationReminder(record.vaccineName, record.dueDate, recordSnap.getKey());
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("VaccineListActivity", "Failed to load records: " + error.getMessage());
            }
        });
    }

    private void scheduleVaccinationReminder(String vaccineName, String dueDateString, String recordId) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDate = sdf.parse(dueDateString);
            if (dueDate == null) return;

            // 3 days before due date
            long reminderTime = dueDate.getTime() - (3L * 24 * 60 * 60 * 1000);
            long delay = reminderTime - System.currentTimeMillis();
            if (delay <= 0) return; // too late

            // Use recordId as unique tag to prevent duplicate workers
            Data data = new Data.Builder()
                    .putString("vaccineName", vaccineName)
                    .putString("dueDate", dueDateString)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(VaccinationReminderWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag(recordId) // unique tag per record
                    .build();

            WorkManager.getInstance(this).enqueue(request);

            Log.d("VACCINE_REMINDER", "Reminder scheduled for " + vaccineName + " on " + dueDateString);

        } catch (Exception e) {
            Log.e("VACCINE_REMINDER", "Error scheduling reminder: " + e.getMessage());
        }
    }
}
