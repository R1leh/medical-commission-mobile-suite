package com.example.doch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ImportConfigActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_config);

        setSupportActionBar(findViewById(R.id.toolbar));

        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(MainViewModel.class);

        EditText etLink = findViewById(R.id.etConfigLink);
        Button btnScanQr = findViewById(R.id.btnScanQR);
        Button btnBack = findViewById(R.id.btnBack);

        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                parseAndSaveConfig(result.getContents(), btnScanQr, btnBack);
            } else {
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show();
            }
        });

        btnScanQr.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_CODE);
            } else {
                launchScanner();
            }
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        etLink.setOnEditorActionListener((v, actionId, event) -> {
            String link = etLink.getText().toString();
            if (!link.isEmpty()) {
                parseAndSaveConfig(link, btnScanQr, btnBack);
            } else {
                Toast.makeText(this, "Введите данные", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchScanner();
            } else {
                Toast.makeText(this, "Разрешение на камеру отклонено", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Сканируйте QR-код");
        options.setBeepEnabled(true);
        barcodeLauncher.launch(options);
    }

    private void parseAndSaveConfig(String qrData, Button btnScanQr, Button btnBack) {
        btnScanQr.setEnabled(false);
        btnBack.setEnabled(false);
        Toast.makeText(this, "Обработка данных...", Toast.LENGTH_SHORT).show();
        try {
            android.util.Log.d("ImportConfig", "Raw QR Data: [" + qrData + "]");
            String[] lines = qrData.split("\r?\n");
            for (int i = 0; i < lines.length; i++) {
                android.util.Log.d("ImportConfig", "Line " + i + ": " + lines[i]);
            }

            if (lines.length < 2) {
                Toast.makeText(this, "Некорректный формат данных: недостаточно строк", Toast.LENGTH_SHORT).show();
                btnScanQr.setEnabled(true);
                btnBack.setEnabled(true);
                return;
            }

            if (!lines[0].startsWith("Медкомиссия: ")) {
                Toast.makeText(this, "Некорректный формат: ожидается 'Медкомиссия: '", Toast.LENGTH_SHORT).show();
                btnScanQr.setEnabled(true);
                btnBack.setEnabled(true);
                return;
            }
            String[] dateParts = lines[0].split(": ")[1].split(" - ");
            if (dateParts.length != 2) {
                Toast.makeText(this, "Некорректный формат даты", Toast.LENGTH_SHORT).show();
                btnScanQr.setEnabled(true);
                btnBack.setEnabled(true);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            sdf.setLenient(false);
            try {
                Date startDate = sdf.parse(dateParts[0]);
                Date endDate = sdf.parse(dateParts[1]);
                if (startDate.after(endDate)) {
                    Toast.makeText(this, "Дата начала позже даты окончания", Toast.LENGTH_SHORT).show();
                    btnScanQr.setEnabled(true);
                    btnBack.setEnabled(true);
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Некорректный формат даты: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnScanQr.setEnabled(true);
                btnBack.setEnabled(true);
                return;
            }

            // Проверка количества врачей
            final int doctorCount;
            if (!lines[1].startsWith("Количество врачей: ")) {
                Toast.makeText(this, "Некорректный формат: ожидается 'Количество врачей: '", Toast.LENGTH_SHORT).show();
                btnScanQr.setEnabled(true);
                btnBack.setEnabled(true);
                return;
            }
            String doctorCountStr = lines[1].split(": ")[1].trim();
            try {
                doctorCount = Integer.parseInt(doctorCountStr);
                if (doctorCount < 0 || 2 + doctorCount > lines.length) {
                    Toast.makeText(this, "Некорректное количество врачей: " + doctorCount, Toast.LENGTH_SHORT).show();
                    btnScanQr.setEnabled(true);
                    btnBack.setEnabled(true);
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректное количество врачей: " + doctorCountStr, Toast.LENGTH_SHORT).show();
                btnScanQr.setEnabled(true);
                btnBack.setEnabled(true);
                return;
            }

            // Проверка формата строк врачей
            for (int i = 2; i < 2 + doctorCount && i < lines.length; i++) {
                String[] parts = lines[i].split("; ", 4);
                if (parts.length != 4) {
                    Toast.makeText(this, "Некорректный формат строки врача " + (i - 1), Toast.LENGTH_SHORT).show();
                    btnScanQr.setEnabled(true);
                    btnBack.setEnabled(true);
                    return;
                }
                if (parts[0].trim().isEmpty() || parts[1].trim().isEmpty() || parts[2].trim().isEmpty()) {
                    Toast.makeText(this, "Пустые поля в строке врача " + (i - 1), Toast.LENGTH_SHORT).show();
                    btnScanQr.setEnabled(true);
                    btnBack.setEnabled(true);
                    return;
                }
                if (parts[3].trim().isEmpty()) {
                    Toast.makeText(this, "Пустое поле часов работы в строке врача " + (i - 1), Toast.LENGTH_SHORT).show();
                    btnScanQr.setEnabled(true);
                    btnBack.setEnabled(true);
                    return;
                }
            }

            String messageText = "Импортировать новые данные? Все текущие данные будут удалены.";
            SpannableString spannableMessage = new SpannableString(messageText);
            spannableMessage.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, R.color.black)),
                    0,
                    messageText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                    .setTitle("Подтверждение импорта")
                    .setMessage(spannableMessage)
                    .setPositiveButton("Импортировать", (dialog, which) -> {
                        // Синхронизация очистки данных
                        CountDownLatch latch = new CountDownLatch(1);
                        viewModel.clearData();
                        latch.countDown();
                        try {
                            latch.await(2, TimeUnit.SECONDS); // Ждём завершения очистки
                        } catch (InterruptedException e) {
                            android.util.Log.e("ImportConfig", "Ошибка ожидания очистки данных", e);
                        }

                        MedicalDate date = new MedicalDate(dateParts[0], dateParts[1]);
                        viewModel.addDate(date);
                        android.util.Log.d("ImportConfig", "Даты сохранены: " + dateParts[0] + " - " + dateParts[1]);

                        int importedDoctors = 0;
                        for (int i = 2; i < 2 + doctorCount && i < lines.length; i++) {
                            android.util.Log.d("ImportConfig", "Обработка врача в строке " + i + ": " + lines[i]);
                            String[] parts = lines[i].split("; ", 4);
                            // Проверки уже выполнены, но оставляем на случай ошибок
                            if (parts.length != 4) {
                                android.util.Log.w("ImportConfig", "Пропущена строка врача " + i + ": некорректный формат");
                                continue;
                            }
                            if (parts[0].trim().isEmpty() || parts[1].trim().isEmpty() || parts[2].trim().isEmpty()) {
                                android.util.Log.w("ImportConfig", "Пропущена строка врача " + i + ": пустые поля (ФИО, специальность или кабинет)");
                                continue;
                            }
                            if (parts[3].trim().isEmpty()) {
                                android.util.Log.w("ImportConfig", "Пропущена строка врача " + i + ": пустое поле часов работы");
                                continue;
                            }
                            Doctor doctor = new Doctor(parts[0], parts[1], parts[2], parts[3]);
                            viewModel.addDoctor(doctor);
                            importedDoctors++;
                            android.util.Log.d("ImportConfig", "Врач добавлен: " + parts[0]);
                        }

                        scheduleNotifications(dateParts[0]);
                        Toast.makeText(this, "Импортировано " + importedDoctors + " врачей и даты медкомиссии", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> {
                        btnScanQr.setEnabled(true);
                        btnBack.setEnabled(true);
                    })
                    .show();
        } catch (Exception e) {
            android.util.Log.e("ImportConfig", "Error parsing QR code", e);
            Toast.makeText(this, "Ошибка при обработке данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnScanQr.setEnabled(true);
            btnBack.setEnabled(true);
        }
    }

    private void scheduleNotifications(String startDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date start = sdf.parse(startDate);
            Date now = new Date();
            long diffInMillis = start.getTime() - now.getTime();

            if (diffInMillis > 7 * 24 * 60 * 60 * 1000L) {
                Data data = new Data.Builder()
                        .putString(NotificationWorker.KEY_START_DATE, startDate)
                        .putString(NotificationWorker.KEY_NOTIFICATION_TYPE, "week")
                        .build();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInputData(data)
                        .setInitialDelay(diffInMillis - 7 * 24 * 60 * 60 * 1000L, TimeUnit.MILLISECONDS)
                        .build();
                WorkManager.getInstance(this).enqueue(workRequest);
                android.util.Log.d("ImportConfig", "Scheduled notification for 7 days before " + startDate);
            }

            if (diffInMillis > 3 * 24 * 60 * 60 * 1000L) {
                Data data = new Data.Builder()
                        .putString(NotificationWorker.KEY_START_DATE, startDate)
                        .putString(NotificationWorker.KEY_NOTIFICATION_TYPE, "three_days")
                        .build();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInputData(data)
                        .setInitialDelay(diffInMillis - 3 * 24 * 60 * 60 * 1000L, TimeUnit.MILLISECONDS)
                        .build();
                WorkManager.getInstance(this).enqueue(workRequest);
                android.util.Log.d("ImportConfig", "Scheduled notification for 3 days before " + startDate);
            }

            if (diffInMillis > 24 * 60 * 60 * 1000L) {
                Data data = new Data.Builder()
                        .putString(NotificationWorker.KEY_START_DATE, startDate)
                        .putString(NotificationWorker.KEY_NOTIFICATION_TYPE, "one_day")
                        .build();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInputData(data)
                        .setInitialDelay(diffInMillis - 24 * 60 * 60 * 1000L, TimeUnit.MILLISECONDS)
                        .build();
                WorkManager.getInstance(this).enqueue(workRequest);
                android.util.Log.d("ImportConfig", "Scheduled notification for 1 day before " + startDate);
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Ошибка формата даты: " + startDate, Toast.LENGTH_SHORT).show();
            android.util.Log.e("ImportConfig", "Invalid date format: " + startDate, e);
        } catch (Exception e) {
            android.util.Log.e("ImportConfig", "Error scheduling notifications", e);
            Toast.makeText(this, "Ошибка планирования уведомлений: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}