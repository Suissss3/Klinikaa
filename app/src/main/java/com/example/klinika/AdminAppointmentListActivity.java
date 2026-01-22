package com.example.klinika;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminAppointmentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminAppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();
    private DatabaseReference appointmentRef;
    private DatabaseReference notifRef;

    private String adminUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_appointment_list);

        recyclerView = findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminAppointmentAdapter(appointmentList, new AdminAppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onApprove(Appointment appt) {
                updateAppointmentStatus(appt, "approved");
            }

            @Override
            public void onReject(Appointment appt) {
                updateAppointmentStatus(appt, "rejected");
            }

            @Override
            public void onDelete(Appointment appt) {
                deleteAppointment(appt);
            }
        });

        recyclerView.setAdapter(adapter);

        adminUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentRef = FirebaseDatabase.getInstance().getReference("appointments");
        notifRef = FirebaseDatabase.getInstance().getReference("notifications");

        loadAppointments();
    }

    private void loadAppointments() {
        // Show only appointments assigned to this admin
        appointmentRef.orderByChild("adminUid").equalTo(adminUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Appointment appt = data.getValue(Appointment.class);
                            if (appt != null) {
                                appt.id = data.getKey(); // save the Firebase key
                                appointmentList.add(appt);
                            }
                        }

                        // Sort appointments by date and time (earliest first)
                        appointmentList.sort((a1, a2) -> {
                            try {
                                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                                Date dt1 = sdfDateTime.parse(a1.date + " " + a1.time);
                                Date dt2 = sdfDateTime.parse(a2.date + " " + a2.time);

                                return dt1.compareTo(dt2); // earliest first
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        });

                        adapter.notifyDataSetChanged();

                        if (appointmentList.isEmpty()) {
                            Toast.makeText(AdminAppointmentListActivity.this, "No appointments assigned to you", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminAppointmentListActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Update appointment status and send notification to student
    private void updateAppointmentStatus(Appointment appt, String status) {
        appointmentRef.child(appt.id).child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "Your appointment on " + appt.date + " at " + appt.time + " was " + status;
                        sendNotification(appt.studentUid, message, "appointment_update");
                        Toast.makeText(this, "Appointment " + status, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update appointment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Delete appointment and notify student
    private void deleteAppointment(Appointment appt) {
        appointmentRef.child(appt.id).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = "Your appointment on " + appt.date + " at " + appt.time + " has been deleted by admin.";
                        sendNotification(appt.studentUid, message, "appointment_deleted");
                        Toast.makeText(this, "Appointment deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete appointment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Send notification to a user
    private void sendNotification(String uid, String message, String type) {
        String key = notifRef.push().getKey();
        if (key == null) return;

        NotificationItem n = new NotificationItem();
        n.id = key;
        n.studentUid = uid;
        n.type = type;
        n.message = message;
        n.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        n.read = false;

        notifRef.child(key).setValue(n);
    }
}
