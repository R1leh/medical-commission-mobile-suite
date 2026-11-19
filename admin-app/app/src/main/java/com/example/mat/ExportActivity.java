package com.example.mat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class ExportActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private ImageView ivQrCode;
    private TextView tvExportData;
    private TextView tvCheckupDates;
    private TextView tvDoctorsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(MainViewModel.class);
        tvExportData = findViewById(R.id.tv_export_data);
        tvCheckupDates = findViewById(R.id.tvCheckupDates);
        tvDoctorsCount = findViewById(R.id.tvDoctorsCount);
        ivQrCode = findViewById(R.id.iv_qr_code);

        viewModel.getDoctors().observe(this, doctors -> {
            viewModel.getDates().observe(this, dates -> {
                // Set text for checkup dates and doctors count
                String datesText = dates != null && !dates.isEmpty()
                        ? "Медкомиссия: " + dates.get(0).getStartDate() + " - " + dates.get(0).getEndDate()
                        : "Медкомиссия: Даты не указаны";
                String doctorsCountText = "Количество врачей: " + (doctors != null ? doctors.size() : 0);
                tvCheckupDates.setText(datesText);
                tvDoctorsCount.setText(doctorsCountText);

                // Set minimal or no text for tvExportData to avoid duplication
                tvExportData.setText("Данные для экспорта:");

                // Form full data for QR code and sharing
                StringBuilder fullData = new StringBuilder();
                fullData.append("Медкомиссия: ")
                        .append(dates != null && !dates.isEmpty() ? dates.get(0).getStartDate() + " - " + dates.get(0).getEndDate() : "Даты не указаны")
                        .append("\n");
                fullData.append("Количество врачей: ")
                        .append(doctors != null ? doctors.size() : 0)
                        .append("\n");

                if (doctors != null && !doctors.isEmpty()) {
                    for (Doctor doctor : doctors) {
                        fullData.append(doctor.getFullName()).append("; ")
                                .append(doctor.getPosition()).append("; ")
                                .append(doctor.getOffice()).append("; ")
                                .append(doctor.getWorkingHours())
                                .append("\n");
                    }
                }

                String fullString = fullData.toString().trim();

                Button btnGenerateQr = findViewById(R.id.btn_generate_qr);
                btnGenerateQr.setOnClickListener(v -> {
                    try {
                        QRCodeWriter writer = new QRCodeWriter();
                        Map<EncodeHintType, Object> hints = new HashMap<>();
                        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // Поддержка кириллицы
                        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Высокий уровень коррекции ошибок
                        BitMatrix bitMatrix = writer.encode(fullString, BarcodeFormat.QR_CODE, 500, 500, hints);
                        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
                        for (int x = 0; x < 500; x++) {
                            for (int y = 0; y < 500; y++) {
                                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                            }
                        }
                        ivQrCode.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        android.util.Log.e("ExportActivity", "Error generating QR code", e);
                        Toast.makeText(this, "Ошибка при генерации QR-кода: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                Button btnShare = findViewById(R.id.btn_share);
                btnShare.setOnClickListener(v -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, fullString);
                    startActivity(Intent.createChooser(shareIntent, "Поделиться данными"));
                });
            });
        });

        Button btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}