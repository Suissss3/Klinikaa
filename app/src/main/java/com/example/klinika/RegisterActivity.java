package com.example.klinika;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtStudentId, edtName, edtEmail, edtPassword, edtAge;
    Spinner spinnerYearLevel, spinnerCourse;
    Button btnRegister;
    FirebaseAuth auth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        edtStudentId = findViewById(R.id.edtStudentId);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtAge = findViewById(R.id.edtAge);
        spinnerYearLevel = findViewById(R.id.spinnerYearLevel);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        btnRegister = findViewById(R.id.btnRegister);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Year Level Spinner
        String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearLevels);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYearLevel.setAdapter(yearAdapter);

        // Course Spinner
        String[] courses = {"College of Computer Studies", "College of Communication Arts",
                "College of Nursing", "College of Criminal Justice", "College of Business Administration"};
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String studentId = edtStudentId.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String age = edtAge.getText().toString().trim();
        String yearLevel = spinnerYearLevel.getSelectedItem().toString();
        String course = spinnerCourse.getSelectedItem().toString();

        if (studentId.isEmpty() || name.isEmpty() || email.isEmpty() ||
                password.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();

                        // Save user info with role "student"
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("studentId", studentId);
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("age", age);
                        userMap.put("yearLevel", yearLevel);
                        userMap.put("course", course);
                        userMap.put("role", "student"); // <-- role field

                        usersRef.child(uid).setValue(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to save user info: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
