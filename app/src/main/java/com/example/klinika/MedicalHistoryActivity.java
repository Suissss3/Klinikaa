package com.example.klinika;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class MedicalHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicalHistoryAdapter adapter;
    private List<MedicalHistory> historyList;
    private DatabaseReference medicalHistoryRef;
    private String studentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        studentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new MedicalHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        medicalHistoryRef = FirebaseDatabase.getInstance().getReference("MedicalHistory");

        loadMedicalHistory();
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
                            Toast.makeText(MedicalHistoryActivity.this, "No medical history found.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MedicalHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
