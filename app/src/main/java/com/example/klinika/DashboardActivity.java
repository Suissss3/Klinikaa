package com.example.klinika;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageView imgProfile;
    private TextView tvName, tvID, tvEmail, tvCourse, tvYear, tvAge, tvLastVisit;
    private TextView tvHeight, tvWeight, tvBloodType, tvBMI, tvBMICategory;

    private DatabaseReference userRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Notification channel
        createNotificationChannel();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        imgProfile = findViewById(R.id.imgDashboardProfile);

        tvName = findViewById(R.id.tvDashboardName);
        tvID = findViewById(R.id.tvDashboardID);
        tvEmail = findViewById(R.id.tvDashboardEmail);
        tvCourse = findViewById(R.id.tvDashboardCourse);
        tvYear = findViewById(R.id.tvDashboardYear);
        tvAge = findViewById(R.id.tvDashboardAge);
        tvLastVisit = findViewById(R.id.tvDashboardLastVisit);

        tvHeight = findViewById(R.id.tvDashboardHeight);
        tvWeight = findViewById(R.id.tvDashboardWeight);
        tvBloodType = findViewById(R.id.tvDashboardBloodType);
        tvBMI = findViewById(R.id.tvDashboardBMI);
        tvBMICategory = findViewById(R.id.tvDashboardBMICategory);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        loadUserData();

        // Drawer menu clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_health_profile) {
                startActivity(new Intent(DashboardActivity.this, HealthProfileActivity.class));

            } else if (id == R.id.nav_medical_history) {
                Intent intent = new Intent(DashboardActivity.this, MedicalHistoryActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);

            } else if (id == R.id.nav_vaccination) {
                Intent intent = new Intent(DashboardActivity.this, VaccineListActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);

            } else if (id == R.id.nav_certificates) {
                Intent intent = new Intent(DashboardActivity.this, StudentCertificateActivity.class);
                intent.putExtra("uid", uid); // pass student UID
                startActivity(intent);

            } else if (id == R.id.nav_notifications) {
                Intent intent = new Intent(DashboardActivity.this, StudentNotificationsActivity.class);
                intent.putExtra("uid", uid); // send student UID
                startActivity(intent);

            } else if (id == R.id.nav_appointment) {
                Intent intent = new Intent(DashboardActivity.this, StudentAppointmentActivity.class);
                intent.putExtra("uid", uid); // send student UID
                startActivity(intent);

            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                finish();

            } else {
                Toast.makeText(this, "Feature not implemented", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel("vaccine_channel",
                            "Vaccination Reminders",
                            NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvName.setText(snapshot.child("name").getValue(String.class));
                tvID.setText(snapshot.child("studentId").getValue(String.class));
                tvEmail.setText(snapshot.child("email").getValue(String.class));
                tvCourse.setText(snapshot.child("course").getValue(String.class));
                tvYear.setText(snapshot.child("yearLevel").getValue(String.class));
                tvAge.setText(snapshot.child("age").getValue(String.class));
                tvLastVisit.setText(snapshot.child("lastClinicVisit").getValue(String.class));

                String heightStr = snapshot.child("height").getValue(String.class);
                String weightStr = snapshot.child("weight").getValue(String.class);
                String bloodType = snapshot.child("bloodType").getValue(String.class);

                tvHeight.setText(heightStr != null ? heightStr + " cm" : "-");
                tvWeight.setText(weightStr != null ? weightStr + " kg" : "-");
                tvBloodType.setText(bloodType != null ? bloodType : "-");

                String base64Image = snapshot.child("profileImage").getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imgProfile.setImageBitmap(bitmap);
                } else {
                    imgProfile.setImageResource(R.drawable.profile_placeholder);
                }

                // BMI calculation
                if (heightStr != null && weightStr != null) {
                    try {
                        double heightM = Double.parseDouble(heightStr) / 100;
                        double weightKg = Double.parseDouble(weightStr);
                        double bmi = weightKg / (heightM * heightM);

                        tvBMI.setText(String.format("%.1f", bmi));

                        String category;
                        int color;

                        if (bmi < 18.5) {
                            category = "Underweight";
                            color = getResources().getColor(R.color.underweight);
                        } else if (bmi < 25) {
                            category = "Normal";
                            color = getResources().getColor(R.color.normal);
                        } else if (bmi < 30) {
                            category = "Overweight";
                            color = getResources().getColor(R.color.overweight);
                        } else {
                            category = "Obese";
                            color = getResources().getColor(R.color.obese);
                        }

                        tvBMICategory.setText(category);
                        tvBMICategory.setTextColor(color);

                    } catch (NumberFormatException e) {
                        tvBMI.setText("-");
                        tvBMICategory.setText("-");
                    }
                } else {
                    tvBMI.setText("-");
                    tvBMICategory.setText("-");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
