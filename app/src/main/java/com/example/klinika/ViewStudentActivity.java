package com.example.klinika;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class ViewStudentActivity extends AppCompatActivity {

        ImageView ivProfile;
        TextView tvName, tvStudentId, tvEmail, tvAge, tvYear, tvCourse;
        TextView tvHeight, tvWeight, tvBloodType, tvBMI, tvBMICategory;
        DatabaseReference userRef;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_view_student);

                ivProfile = findViewById(R.id.ivProfile);
                tvName = findViewById(R.id.tvName);
                tvStudentId = findViewById(R.id.tvStudentId);
                tvEmail = findViewById(R.id.tvEmail);
                tvAge = findViewById(R.id.tvAge);
                tvYear = findViewById(R.id.tvYearLevel);
                tvCourse = findViewById(R.id.tvCourse);

                tvHeight = findViewById(R.id.tvHeight);
                tvWeight = findViewById(R.id.tvWeight);
                tvBloodType = findViewById(R.id.tvBloodType);
                tvBMI = findViewById(R.id.tvBMI);
                tvBMICategory = findViewById(R.id.tvBMICategory);

                String uid = getIntent().getStringExtra("uid");
                userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

                loadStudentData();
        }

        private void loadStudentData() {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                // Profile Picture
                                String base64Image = snapshot.child("profileImage").getValue(String.class);
                                if (base64Image != null && !base64Image.isEmpty()) {
                                        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                        ivProfile.setImageBitmap(bitmap);
                                } else {
                                        ivProfile.setImageResource(R.drawable.profile_placeholder); // default image
                                }

                                tvName.setText(snapshot.child("name").getValue(String.class));
                                tvStudentId.setText(snapshot.child("studentId").getValue(String.class));
                                tvEmail.setText(snapshot.child("email").getValue(String.class));
                                tvAge.setText(snapshot.child("age").getValue(String.class));
                                tvYear.setText(snapshot.child("yearLevel").getValue(String.class));
                                tvCourse.setText(snapshot.child("course").getValue(String.class));

                                String heightStr = snapshot.child("height").getValue(String.class);
                                String weightStr = snapshot.child("weight").getValue(String.class);
                                String bloodType = snapshot.child("bloodType").getValue(String.class);

                                tvHeight.setText(heightStr != null ? heightStr + " cm" : "-");
                                tvWeight.setText(weightStr != null ? weightStr + " kg" : "-");
                                tvBloodType.setText(bloodType != null ? bloodType : "-");

                                if (heightStr != null && weightStr != null) {
                                        try {
                                                double heightM = Double.parseDouble(heightStr) / 100;
                                                double weightKg = Double.parseDouble(weightStr);
                                                double bmi = weightKg / (heightM * heightM);
                                                tvBMI.setText(String.format("%.2f", bmi));

                                                String category;
                                                int color;

                                                if (bmi < 18.5) {
                                                        category = "Underweight"; color = Color.BLUE;
                                                } else if (bmi < 25) {
                                                        category = "Normal"; color = Color.GREEN;
                                                } else if (bmi < 30) {
                                                        category = "Overweight"; color = Color.YELLOW;
                                                } else {
                                                        category = "Obese"; color = Color.RED;
                                                }

                                                tvBMICategory.setText(category);
                                                tvBMICategory.setTextColor(color);

                                        } catch (NumberFormatException e) {
                                                tvBMI.setText("-");
                                                tvBMICategory.setText("-");
                                                tvBMICategory.setTextColor(Color.BLACK);
                                        }
                                } else {
                                        tvBMI.setText("-");
                                        tvBMICategory.setText("-");
                                        tvBMICategory.setTextColor(Color.BLACK);
                                }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ViewStudentActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        }
                });
        }
}
