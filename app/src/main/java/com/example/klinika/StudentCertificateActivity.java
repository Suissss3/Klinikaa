package com.example.klinika;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class StudentCertificateActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentCertificateAdapter adapter;
    private List<CertificateItem> list = new ArrayList<>();

    private Button btnRequestCertificate;

    private DatabaseReference db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_certificates);

        recyclerView = findViewById(R.id.recyclerStudentCertificates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentCertificateAdapter(this, list);
        recyclerView.setAdapter(adapter);

        btnRequestCertificate = findViewById(R.id.btnRequestCertificate);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance().getReference("certificates");

        loadCertificates();

        // Navigate to request activity when button is clicked
        btnRequestCertificate.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, StudentRequestCertificateActivity.class));
        });
    }

    private void loadCertificates() {
        db.orderByChild("studentUid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ch : snapshot.getChildren()) {
                    CertificateItem it = ch.getValue(CertificateItem.class);
                    if (it != null) {
                        it.id = ch.getKey();
                        list.add(it);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentCertificateActivity.this, "Load error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}