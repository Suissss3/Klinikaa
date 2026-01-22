package com.example.klinika;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HealthProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    TextView tvName, tvStudentId, tvCourse, tvYearLevel, tvLastVisit, tvBMI, tvBMICategory;
    EditText edtHeight, edtWeight;
    Spinner spinnerBloodType;
    Button btnSave;
    ImageView imgProfile;
    DatabaseReference userRef;
    String uid;
    Bitmap selectedBitmap = null; // holds chosen profile picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_profile);

        // Initialize views
        tvName = findViewById(R.id.tvName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvCourse = findViewById(R.id.tvCourse);
        tvYearLevel = findViewById(R.id.tvYearLevel);
        tvLastVisit = findViewById(R.id.tvLastVisit);
        tvBMI = findViewById(R.id.tvBMI);
        tvBMICategory = findViewById(R.id.tvBMICategory);
        imgProfile = findViewById(R.id.imgProfile);

        edtHeight = findViewById(R.id.edtHeight);
        edtWeight = findViewById(R.id.edtWeight);
        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        btnSave = findViewById(R.id.btnSave);

        // Blood type spinner
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bloodTypes);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(bloodAdapter);

        // Firebase reference
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        loadUserProfile();

        // Real-time BMI update
        TextWatcher bmiTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String heightStr = edtHeight.getText().toString().trim();
                String weightStr = edtWeight.getText().toString().trim();
                if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
                    calculateAndDisplayBMI(heightStr, weightStr);
                } else {
                    tvBMI.setText("BMI: N/A");
                    tvBMICategory.setText("Category: N/A");
                    tvBMICategory.setTextColor(Color.BLACK);
                }
            }
        };
        edtHeight.addTextChangedListener(bmiTextWatcher);
        edtWeight.addTextChangedListener(bmiTextWatcher);

        // Select profile picture on ImageView click
        imgProfile.setOnClickListener(v -> openImageChooser());

        btnSave.setOnClickListener(v -> saveHealthProfile());
    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvName.setText(snapshot.child("name").getValue(String.class));
                tvStudentId.setText(snapshot.child("studentId").getValue(String.class));
                tvCourse.setText(snapshot.child("course").getValue(String.class));
                tvYearLevel.setText(snapshot.child("yearLevel").getValue(String.class));
                tvLastVisit.setText(snapshot.child("lastClinicVisit").getValue(String.class));
                edtHeight.setText(snapshot.child("height").getValue(String.class));
                edtWeight.setText(snapshot.child("weight").getValue(String.class));

                String bloodType = snapshot.child("bloodType").getValue(String.class);
                if (bloodType != null) {
                    int spinnerPosition = ((ArrayAdapter<String>) spinnerBloodType.getAdapter()).getPosition(bloodType);
                    spinnerBloodType.setSelection(spinnerPosition);
                }

                String height = snapshot.child("height").getValue(String.class);
                String weight = snapshot.child("weight").getValue(String.class);
                if (height != null && weight != null) {
                    calculateAndDisplayBMI(height, weight);
                }

                // Load Base64 profile image
                String base64Image = snapshot.child("profileImage").getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imgProfile.setImageBitmap(decodedBitmap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndDisplayBMI(String heightStr, String weightStr) {
        try {
            double height = Double.parseDouble(heightStr) / 100.0;
            double weight = Double.parseDouble(weightStr);
            double bmi = weight / (height * height);
            tvBMI.setText(String.format("BMI: %.2f", bmi));

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

            tvBMICategory.setText("Category: " + category);
            tvBMICategory.setTextColor(color);

        } catch (NumberFormatException e) {
            tvBMI.setText("BMI: N/A");
            tvBMICategory.setText("Category: N/A");
            tvBMICategory.setTextColor(Color.BLACK);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgProfile.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveHealthProfile() {
        String height = edtHeight.getText().toString().trim();
        String weight = edtWeight.getText().toString().trim();
        String bloodType = spinnerBloodType.getSelectedItem().toString();

        userRef.child("height").setValue(height);
        userRef.child("weight").setValue(weight);
        userRef.child("bloodType").setValue(bloodType);

        // Save profile image as Base64
        if (selectedBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            userRef.child("profileImage").setValue(base64Image);
        }

        Toast.makeText(this, "Health profile updated", Toast.LENGTH_SHORT).show();
    }
}
