package com.example.mat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class AddDoctorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor);

        setSupportActionBar(findViewById(R.id.toolbar));

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        EditText etFullName = findViewById(R.id.et_full_name);
        EditText etPosition = findViewById(R.id.et_position);
        EditText etOffice = findViewById(R.id.et_office);
        EditText etWorkingHours = findViewById(R.id.et_working_hours);

        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnSave = findViewById(R.id.btn_save);

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString();
            String position = etPosition.getText().toString();
            String office = etOffice.getText().toString();
            String workingHours = etWorkingHours.getText().toString();

            if (!fullName.isEmpty() && !position.isEmpty() && !office.isEmpty() && !workingHours.isEmpty()) {
                Doctor doctor = new Doctor(fullName, position, office, workingHours);
                viewModel.addDoctor(doctor);
                finish();
            }
        });
    }
}