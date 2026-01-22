package com.example.klinika;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import android.app.DatePickerDialog;

public class AddVaccinationActivity extends AppCompatActivity {

    private TextView tvStudentName;
    private Spinner spinnerVaccineName;
    private EditText etDateTaken, etDueDate, etNotes;
    private Button btnSave;

    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("vaccination_records");

    private String studentUid;
    private String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vaccination);

        tvStudentName = findViewById(R.id.tvStudentName);
        spinnerVaccineName = findViewById(R.id.spinnerVaccineName);
        etDateTaken = findViewById(R.id.etDateTaken);
        etDueDate = findViewById(R.id.etDueDate);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);

        // Get data from intent
        studentUid = getIntent().getStringExtra("uid");
        studentName = getIntent().getStringExtra("name");

        tvStudentName.setText(studentName != null ? studentName : "Student");

        // Vaccine spinner
        String[] vaccines = {"COVID-19", "Flu Shot", "Tetanus", "Hepatitis B", "MMR"};
        ArrayAdapter<String> vaccineAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, vaccines);
        vaccineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVaccineName.setAdapter(vaccineAdapter);

        etDateTaken.setOnClickListener(v -> showDatePicker(etDateTaken));
        etDueDate.setOnClickListener(v -> showDatePicker(etDueDate));

        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        editText.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveRecord() {
        String vaccineName = spinnerVaccineName.getSelectedItem().toString();
        String dateTaken = etDateTaken.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (studentUid == null || studentUid.isEmpty() || vaccineName.isEmpty() || dateTaken.isEmpty()) {
            Toast.makeText(this, "Complete required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String recordId = db.push().getKey();
        VaccinationRecord record = new VaccinationRecord(vaccineName, dateTaken, dueDate);
        db.child(studentUid).child(recordId).setValue(record)
                .addOnSuccessListener(a -> Toast.makeText(this, "Vaccination saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
