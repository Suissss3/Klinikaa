package com.example.klinika;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList;
    private List<User> filteredList;
    private DatabaseReference usersRef;

    private EditText edtSearch;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        toolbar = findViewById(R.id.toolbar);
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

        navigationView.setNavigationItemSelectedListener(this::onDrawerItemSelected);

        // SEARCH BAR
        edtSearch = findViewById(R.id.edtSearchUser);
        btnSearch = findViewById(R.id.btnSearchUser);

        btnSearch.setOnClickListener(v -> searchStudents());

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new AdminUserAdapter(this, filteredList, this::onStudentClick);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadUsers();
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String role = child.child("role").getValue(String.class);
                    if ("student".equals(role)) {
                        User user = child.getValue(User.class);
                        if (user != null) {
                            user.setUid(child.getKey());
                            userList.add(user);
                        }
                    }
                }

                // When data loads, show everything first
                filteredList.clear();
                filteredList.addAll(userList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔍 SEARCH FUNCTION
    private void searchStudents() {
        String query = edtSearch.getText().toString().trim().toLowerCase();

        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(userList); // Show full list
        } else {
            for (User u : userList) {
                if (u.getName() != null && u.getName().toLowerCase().contains(query)) {
                    filteredList.add(u);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No matching students found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean onDrawerItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_appointments) {
            startActivity(new Intent(this, AdminAppointmentListActivity.class));
        } else if (id == R.id.nav_admin_notifications) {
            startActivity(new Intent(this, AdminNotificationsActivity.class));
        } else if (id == R.id.nav_admin_certificates) {
            startActivity(new Intent(this, AdminCertificatesActivity.class));
        } else if (id == R.id.nav_admin_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Bottom Sheet
    private void onStudentClick(User user) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_dialog, null);
        bottomSheetDialog.setContentView(sheetView);

        Button btnUpdateLastVisit = sheetView.findViewById(R.id.btnUpdateLastVisit);
        Button btnViewProfile = sheetView.findViewById(R.id.btnViewProfile);
        Button btnViewMedicalHistory = sheetView.findViewById(R.id.btnViewMedicalHistory);
        Button btnDeleteUser = sheetView.findViewById(R.id.btnDeleteUser);
        Button btnVaccinationRecords = sheetView.findViewById(R.id.btnVaccinationRecords);



        btnUpdateLastVisit.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showUpdateLastVisitDialog(user);
        });

        btnViewProfile.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(this, ViewStudentActivity.class);
            intent.putExtra("uid", user.getUid());
            startActivity(intent);
        });

        btnViewMedicalHistory.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(this, AdminMedicalHistoryActivity.class);
            intent.putExtra("uid", user.getUid());
            startActivity(intent);
        });

        btnVaccinationRecords.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(this, AdminVaccineListActivity.class);
            intent.putExtra("uid", user.getUid());
            startActivity(intent);
        });

        btnDeleteUser.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            deleteUser(user);
        });

        bottomSheetDialog.show();
    }



    private void showUpdateLastVisitDialog(User user) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_set_last_visit, null);
        EditText edtLastVisit = view.findViewById(R.id.edtLastVisitDate);

        edtLastVisit.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(AdminDashboardActivity.this,
                    (dp, year, month, day) -> edtLastVisit.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Update Last Clinic Visit for " + user.getName())
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String date = edtLastVisit.getText().toString().trim();
                    if (date.isEmpty()) {
                        Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    usersRef.child(user.getUid()).child("lastClinicVisit").setValue(date);
                    Toast.makeText(this, "Last visit updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    usersRef.child(user.getUid()).removeValue();
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
