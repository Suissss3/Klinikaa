package com.example.klinika;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText edtUser, edtPassword;
    Button btnLogin, btnRegisterRedirect;
    FirebaseAuth auth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUser = findViewById(R.id.edtStudentId);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterRedirect = findViewById(R.id.btnRegisterRedirect);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> loginUser());

        btnRegisterRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String userInput = edtUser.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (userInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(userInput).matches()) {
            // Login using email
            auth.signInWithEmailAndPassword(userInput, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            checkUserRole(auth.getCurrentUser().getUid());
                        } else {
                            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Login using student ID
            usersRef.orderByChild("studentId").equalTo(userInput)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                Toast.makeText(LoginActivity.this, "Student ID not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (DataSnapshot child : snapshot.getChildren()) {
                                String email = child.child("email").getValue(String.class);
                                auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                checkUserRole(auth.getCurrentUser().getUid());
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(LoginActivity.this, "Login Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkUserRole(String uid) {
        usersRef.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if (role == null) role = "student";

                if (role.equals("admin")) {
                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                }
                finish();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Failed to get role", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
