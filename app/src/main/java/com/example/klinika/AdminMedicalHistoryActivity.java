package com.example.klinika;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.*;

public class AdminMedicalHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminMedicalHistoryAdapter adapter;
    private List<MedicalHistory> historyList;
    private DatabaseReference medicalHistoryRef;
    private DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");

    private String studentUid;
    private String studentName; // Set from AdminDashboard click

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_medical_history);

        studentUid = getIntent().getStringExtra("uid");
        studentName = getIntent().getStringExtra("name"); // Optional

        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new AdminMedicalHistoryAdapter(historyList, new AdminMedicalHistoryAdapter.OnHistoryActionListener() {
            @Override
            public void onEdit(MedicalHistory history) {
                openEditHistoryDialog(history);
            }

            @Override
            public void onDelete(MedicalHistory history) {
                deleteHistory(history);
            }
        });

        recyclerView.setAdapter(adapter);

        medicalHistoryRef = FirebaseDatabase.getInstance().getReference("MedicalHistory");

        loadMedicalHistory();
        findViewById(R.id.btnAddHistory).setOnClickListener(v -> openAddHistoryDialog());
    }

    private void loadMedicalHistory() {
        medicalHistoryRef.orderByChild("patientUid").equalTo(studentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            MedicalHistory item = data.getValue(MedicalHistory.class);
                            if (item != null) historyList.add(item);
                        }
                        adapter.notifyDataSetChanged();
                        if (historyList.isEmpty())
                            Toast.makeText(AdminMedicalHistoryActivity.this, "No medical history found.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminMedicalHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAddHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medical_history, null);
        builder.setView(view);

        EditText etDate = view.findViewById(R.id.etDate);
        EditText etType = view.findViewById(R.id.etType);
        EditText etDiagnosis = view.findViewById(R.id.etDiagnosis);
        EditText etTreatment = view.findViewById(R.id.etTreatment);
        Button btnSave = view.findViewById(R.id.btnSaveHistory);

        // Date picker
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (dp, year, month, day) ->
                    etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String type = etType.getText().toString().trim();
            String diagnosis = etDiagnosis.getText().toString().trim();
            String treatment = etTreatment.getText().toString().trim();

            if (date.isEmpty() || type.isEmpty() || diagnosis.isEmpty() || treatment.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = medicalHistoryRef.push().getKey();
            if (id == null) return;
            long timestamp = System.currentTimeMillis();

            MedicalHistory item = new MedicalHistory(id, studentUid, studentName, date, type, diagnosis, treatment, timestamp);
            medicalHistoryRef.child(id).setValue(item)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sendNotificationToStudent(studentUid, studentName, "A new medical history has been added.");
                        }
                    });

            dialog.dismiss();
        });
    }

    private void openEditHistoryDialog(MedicalHistory history) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medical_history, null);
        builder.setView(view);

        EditText etDate = view.findViewById(R.id.etDate);
        EditText etType = view.findViewById(R.id.etType);
        EditText etDiagnosis = view.findViewById(R.id.etDiagnosis);
        EditText etTreatment = view.findViewById(R.id.etTreatment);
        Button btnSave = view.findViewById(R.id.btnSaveHistory);

        etDate.setText(history.date);
        etType.setText(history.type);
        etDiagnosis.setText(history.diagnosis);
        etTreatment.setText(history.treatment);

        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (dp, year, month, day) ->
                    etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            history.date = etDate.getText().toString().trim();
            history.type = etType.getText().toString().trim();
            history.diagnosis = etDiagnosis.getText().toString().trim();
            history.treatment = etTreatment.getText().toString().trim();

            medicalHistoryRef.child(history.id).setValue(history)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sendNotificationToStudent(studentUid, studentName, "Your medical history was updated.");
                        }
                    });

            dialog.dismiss();
        });
    }

    private void deleteHistory(MedicalHistory history) {
        medicalHistoryRef.child(history.id).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendNotificationToStudent(studentUid, studentName, "A medical history record was deleted.");
                    }
                });
    }

    // Notification helper
    private void sendNotificationToStudent(String studentUid, String studentName, String message) {
        String key = notifRef.push().getKey();
        if (key == null) return;

        NotificationItem n = new NotificationItem();
        n.id = key;
        n.studentUid = studentUid;
        n.studentName = studentName;
        n.type = "medical_history_update";
        n.message = message;
        n.timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        n.read = false;

        notifRef.child(key).setValue(n);
    }
}
