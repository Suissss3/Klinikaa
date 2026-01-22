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

public class StudentNotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentNotificationAdapter adapter;
    private List<NotificationItem> list = new ArrayList<>();

    private DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");
    private String uid;

    private Button btnClearNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_notifications);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentNotificationAdapter(this, list);
        recyclerView.setAdapter(adapter);

        btnClearNotifications = findViewById(R.id.btnClearNotifications);
        btnClearNotifications.setOnClickListener(v -> clearNotifications());

        loadStudentNotifications();
    }

    private void loadStudentNotifications() {
        notifRef.orderByChild("studentUid").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            NotificationItem item = ds.getValue(NotificationItem.class);
                            if (item != null) {
                                item.id = ds.getKey();
                                list.add(item);
                            }
                        }

                        // Sort notifications: unread first, then newest first
                        list.sort((n1, n2) -> {
                            if (n1.read == n2.read) {
                                return n2.timestamp.compareTo(n1.timestamp); // newest first
                            } else if (!n1.read && n2.read) {
                                return -1; // unread first
                            } else {
                                return 1;
                            }
                        });

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentNotificationsActivity.this,
                                "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearNotifications() {
        notifRef.orderByChild("studentUid").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue(); // delete each notification
                        }
                        list.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(StudentNotificationsActivity.this,
                                "Notifications cleared", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentNotificationsActivity.this,
                                "Failed to clear notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}