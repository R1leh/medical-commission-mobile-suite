package com.example.doch;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private DoctorAdapter activeAdapter;
    private DoctorAdapter completedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvDates = findViewById(R.id.tv_dates);
        TextView tvTotalDoctors = findViewById(R.id.tv_total_doctors);
        TextView tvCompletedDoctors = findViewById(R.id.tv_completed_doctors);
        TextView tvRemainingDoctors = findViewById(R.id.tv_remaining_doctors);
        TextInputEditText etSearch = findViewById(R.id.et_search);
        RecyclerView rvActiveDoctors = findViewById(R.id.rv_active_doctors);
        RecyclerView rvCompletedDoctors = findViewById(R.id.rv_completed_doctors);

        activeAdapter = new DoctorAdapter();
        completedAdapter = new DoctorAdapter();

        rvActiveDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvCompletedDoctors.setLayoutManager(new LinearLayoutManager(this));

        rvActiveDoctors.setAdapter(activeAdapter);
        rvCompletedDoctors.setAdapter(completedAdapter);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(MainViewModel.class);

        activeAdapter.setViewModel(viewModel);
        completedAdapter.setViewModel(viewModel);

        viewModel.getDates().observe(this, dates -> {
            if (dates != null && !dates.isEmpty()) {
                tvDates.setText(dates.get(0).getStartDate() + " - " + dates.get(0).getEndDate());
            } else {
                tvDates.setText("Даты медкомиссии: Не указаны");
            }
        });

        viewModel.getDoctors().observe(this, doctors -> {
            if (doctors == null || doctors.isEmpty()) {
                rvActiveDoctors.setVisibility(android.view.View.GONE);
                rvCompletedDoctors.setVisibility(android.view.View.GONE);
                Toast.makeText(this, R.string.empty_state, Toast.LENGTH_LONG).show();
            } else {
                rvActiveDoctors.setVisibility(android.view.View.VISIBLE);
                rvCompletedDoctors.setVisibility(android.view.View.VISIBLE);
            }
        });

        viewModel.getTotalDoctors().observe(this, total -> tvTotalDoctors.setText(String.valueOf(total)));
        viewModel.getCompletedDoctorsCount().observe(this, completed -> tvCompletedDoctors.setText(String.valueOf(completed)));
        viewModel.getRemainingDoctorsCount().observe(this, remaining -> tvRemainingDoctors.setText(String.valueOf(remaining)));

        viewModel.getActiveDoctors().observe(this, doctors -> activeAdapter.setDoctors(doctors != null ? doctors : new java.util.ArrayList<>()));
        viewModel.getCompletedDoctors().observe(this, doctors -> completedAdapter.setDoctors(doctors != null ? doctors : new java.util.ArrayList<>()));

        viewModel.getImportCompleted().observe(this, completed -> {
            if (completed != null && completed) {
                Toast.makeText(this, "Импорт данных завершён", Toast.LENGTH_SHORT).show();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });

        viewModel.getSearchQuery().observe(this, query -> {
            activeAdapter.filter(query);
            completedAdapter.filter(query);
        });

        findViewById(R.id.btn_import).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ImportConfigActivity.class));
        });

        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            String messageText = "Все данные о врачах и датах будут удалены. Продолжить?";
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
                        startActivity(new Intent(MainActivity.this, ImportConfigActivity.class));
                        finish();
                    })
                    .setNegativeButton("Нет", null)
                    .show();
        });
    }
}