package com.example.klinika;

import android.os.Bundle;
import android.view.View;
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

public class AdminNotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminNotificationAdapter adapter;
    private List<NotificationItem> notifications = new ArrayList<>();
    private DatabaseReference notifRef;
    private String adminUid;
    private Button btnClearNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnClearNotifications = findViewById(R.id.btnClearNotifications);

        adapter = new AdminNotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);

        adminUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notifRef = FirebaseDatabase.getInstance().getReference("notifications");

        loadNotifications();

        // Clear notifications button
        btnClearNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Query only notifications for the current admin
                notifRef.orderByChild("studentUid").equalTo(adminUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    data.getRef().removeValue();
                                }
                                notifications.clear();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(AdminNotificationsActivity.this,
                                        "Notifications cleared", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(AdminNotificationsActivity.this,
                                        "Failed to clear notifications", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void loadNotifications() {
        notifRef.orderByChild("studentUid").equalTo(adminUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<NotificationItem> tempList = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            NotificationItem n = data.getValue(NotificationItem.class);
                            if (n != null) tempList.add(n);
                        }

                        // Use adapter's updateNotifications to sort newest-first
                        adapter.updateNotifications(tempList);

                        if (tempList.isEmpty()) {
                            Toast.makeText(AdminNotificationsActivity.this, "No notifications", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminNotificationsActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
