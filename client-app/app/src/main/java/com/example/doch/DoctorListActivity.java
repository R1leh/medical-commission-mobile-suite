package com.example.doch;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import java.util.ArrayList;

public class DoctorListActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private DoctorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);



        RecyclerView rvDoctors = findViewById(R.id.rv_active_doctors);
        TextView tvDates = findViewById(R.id.tv_dates);
        adapter = new DoctorAdapter();
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvDoctors.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(MainViewModel.class);

        viewModel.getDoctors().observe(this, doctors -> {
            if (doctors != null) {
                adapter.setDoctors(doctors);
            } else {
                adapter.setDoctors(new ArrayList<>());
            }
        });

        viewModel.getDates().observe(this, dates -> {
            if (dates != null && !dates.isEmpty()) {
                tvDates.setText("Даты медкомиссии: " + dates.get(0).getStartDate() + " - " + dates.get(0).getEndDate());
            } else {
                tvDates.setText("Даты медкомиссии: Не указаны");
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            viewModel.clearData();
            startActivity(new Intent(DoctorListActivity.this, ImportConfigActivity.class));
            finish();
        });

        findViewById(R.id.btn_import).setOnClickListener(v -> {
            startActivity(new Intent(DoctorListActivity.this, ImportConfigActivity.class));
            finish();
        });
    }
}