package com.example.mat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageDatesActivity extends AppCompatActivity {
    private Button btnStartDate, btnEndDate, btnCancel, btnSave;
    private TextView tvMedicalDates;
    private MainViewModel viewModel;
    private Calendar calendar;
    private String selectedStartDate = "";
    private String selectedEndDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_dates);

        // Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        calendar = Calendar.getInstance();

        // Инициализация элементов интерфейса
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        tvMedicalDates = findViewById(R.id.tvMedicalDates);

        // Обновление списка дат
        viewModel.getDates().observe(this, dates -> {
            StringBuilder datesText = new StringBuilder("Добавленные даты:\n");
            if (dates != null) {
                for (MedicalDate date : dates) {
                    datesText.append(date.getStartDate())
                            .append(" - ")
                            .append(date.getEndDate())
                            .append("\n");
                }
            }
            tvMedicalDates.setText(datesText.toString());
        });

        // Обработчик для выбора начальной даты
        btnStartDate.setOnClickListener(v -> showDatePickerDialog(true));

        // Обработчик для выбора конечной даты
        btnEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Обработчик для отмены
        btnCancel.setOnClickListener(v -> finish());

        // Обработчик для сохранения дат
        btnSave.setOnClickListener(v -> {
            if (!selectedStartDate.isEmpty() && !selectedEndDate.isEmpty()) {
                if (isValidDateRange(selectedStartDate, selectedEndDate)) {
                    MedicalDate date = new MedicalDate(selectedStartDate, selectedEndDate);
                    viewModel.addDate(date);
                    selectedStartDate = "";
                    selectedEndDate = "";
                    btnStartDate.setText("Выбрать дату");
                    btnEndDate.setText("Выбрать дату");
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка: дата завершения не может быть раньше даты начала", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Ошибка: выберите обе даты", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    String selectedDate = sdf.format(calendar.getTime());
                    if (isStartDate) {
                        selectedStartDate = selectedDate;
                        btnStartDate.setText(selectedDate);
                    } else {
                        selectedEndDate = selectedDate;
                        btnEndDate.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            sdf.setLenient(false); // Строгая проверка формата
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (start == null || end == null) {
                return false;
            }
            return !end.before(start); // Возвращает true, если endDate не раньше startDate
        } catch (ParseException e) {
            Toast.makeText(this, "Ошибка: неверный формат даты", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}