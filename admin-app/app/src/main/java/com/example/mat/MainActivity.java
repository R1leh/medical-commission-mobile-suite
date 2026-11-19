package com.example.mat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private DoctorAdapter adapter;
    private List<Doctor> allDoctors; // Храним полный список врачей для фильтрации

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rvDoctors = findViewById(R.id.rv_doctors);
        if (rvDoctors == null) {
            throw new IllegalStateException("RecyclerView with ID rv_doctors not found in layout");
        }
        adapter = new DoctorAdapter();
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvDoctors.setAdapter(adapter);

        MaterialTextView tvDates = findViewById(R.id.tvDates);
        if (tvDates == null) {
            throw new IllegalStateException("TextView with ID tvDates not found in layout");
        }

        TextInputEditText searchView = findViewById(R.id.searchView);
        if (searchView == null) {
            throw new IllegalStateException("TextInputEditText with ID searchView not found in layout");
        }

        androidx.work.OneTimeWorkRequest workRequest =
                new androidx.work.OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .build();
        androidx.work.WorkManager.getInstance(this).enqueue(workRequest);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(MainViewModel.class);

        // Наблюдение за списком врачей
        viewModel.getDoctors().observe(this, doctors -> {
            if (doctors != null) {
                allDoctors = new ArrayList<>(doctors); // Сохраняем полный список
                adapter.setDoctors(doctors);
            } else {
                allDoctors = new ArrayList<>();
                adapter.setDoctors(new ArrayList<>());
            }
        });

        // Наблюдение за датами медкомиссии
        viewModel.getDates().observe(this, dates -> {
            StringBuilder datesText = new StringBuilder();
            if (dates != null && !dates.isEmpty()) {
                for (MedicalDate date : dates) {
                    datesText.append(date.getStartDate())
                            .append(" - ")
                            .append(date.getEndDate());
                }
            } else {
                datesText.append("Даты не указаны");
            }
            tvDates.setText(datesText.toString());
        });

        // Функционал поиска
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterDoctors(s.toString());
            }
        });

        FloatingActionButton fabAddDoctor = findViewById(R.id.btnAddCheckup);
        if (fabAddDoctor == null) {
            throw new IllegalStateException("FloatingActionButton with ID btnAddCheckup not found in layout");
        }
        fabAddDoctor.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddDoctorActivity.class)));

        MaterialButton btnManageDates = findViewById(R.id.btnSelectDates);
        if (btnManageDates == null) {
            throw new IllegalStateException("Button with ID btnSelectDates not found in layout");
        }
        btnManageDates.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ManageDatesActivity.class)));

        MaterialButton btnExport = findViewById(R.id.btn_export);
        if (btnExport == null) {
            throw new IllegalStateException("Button with ID btn_export not found in layout");
        }
        btnExport.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ExportActivity.class)));

        MaterialButton btnClear = findViewById(R.id.btn_clear);
        if (btnClear == null) {
            throw new IllegalStateException("Button with ID btn_clear not found in layout");
        }
        btnClear.setOnClickListener(v -> {
            // Создаём SpannableString для цветного сообщения
            String messageText = "Все текущие данные будут удалены. Продолжить?";
            SpannableString spannableMessage = new SpannableString(messageText);
            spannableMessage.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)),
                    0,
                    messageText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                    .setTitle("Очистить данные?")
                    .setMessage(spannableMessage)
                    .setPositiveButton("Да", (dialog, which) -> {
                        viewModel.clearData();
                    })
                    .setNegativeButton("Нет", null)
                    .show();
        });

        MaterialButton btnRefresh = findViewById(R.id.btn_refresh);
        if (btnRefresh == null) {
            throw new IllegalStateException("Button with ID btn_refresh not found in layout");
        }
        btnRefresh.setOnClickListener(v -> viewModel.refreshData());
    }

    private void filterDoctors(String query) {
        if (allDoctors == null) return;
        List<Doctor> filteredList;
        if (query.isEmpty()) {
            filteredList = new ArrayList<>(allDoctors);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            filteredList = allDoctors.stream()
                    .filter(doctor -> doctor.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                            doctor.getPosition().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }
        adapter.setDoctors(filteredList);
    }
}