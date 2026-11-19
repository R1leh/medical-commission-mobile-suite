package com.example.mat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


public class EditReceptionActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private EditText etDoctorName, etPosition, etRoomNumber, etWorkingHours;
    private Button btnCancel, btnDelete, btnSave;
    private int doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reception);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Инициализация ViewModel
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(MainViewModel.class);

        // Инициализация полей ввода и кнопок
        etDoctorName = findViewById(R.id.etDoctorName);
        etPosition = findViewById(R.id.etPosition);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etWorkingHours = findViewById(R.id.etWorkingHours);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);
        btnSave = findViewById(R.id.btnSave);

        // Получение данных врача из Intent
        doctorId = getIntent().getIntExtra("DOCTOR_ID", -1);
        String fullName = getIntent().getStringExtra("DOCTOR_FULL_NAME");
        String position = getIntent().getStringExtra("DOCTOR_POSITION");
        String office = getIntent().getStringExtra("DOCTOR_OFFICE");
        String workingHours = getIntent().getStringExtra("DOCTOR_WORKING_HOURS");

        // Заполнение полей текущими данными
        etDoctorName.setText(fullName);
        etPosition.setText(position);
        etRoomNumber.setText(office);
        etWorkingHours.setText(workingHours);

        // Обработчик кнопки "Отмена"
        btnCancel.setOnClickListener(v -> finish());

        // Обработчик кнопки "Удалить"
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Подтверждение удаления")
                    .setMessage("Вы уверены, что хотите удалить врача?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        if (doctorId != -1) {
                            Doctor doctor = new Doctor(doctorId, fullName, position, office, workingHours);
                            viewModel.deleteDoctor(doctor);
                            finish();
                        }
                    })
                    .setNegativeButton("Нет", null)
                    .show();
        });

        // Обработчик кнопки "Сохранить"
        btnSave.setOnClickListener(v -> {
            String updatedFullName = etDoctorName.getText().toString();
            String updatedPosition = etPosition.getText().toString();
            String updatedOffice = etRoomNumber.getText().toString();
            String updatedWorkingHours = etWorkingHours.getText().toString();

            if (!updatedFullName.isEmpty() && !updatedPosition.isEmpty() && !updatedOffice.isEmpty() && !updatedWorkingHours.isEmpty()) {
                Doctor updatedDoctor = new Doctor(doctorId, updatedFullName, updatedPosition, updatedOffice, updatedWorkingHours);
                viewModel.updateDoctor(updatedDoctor);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}