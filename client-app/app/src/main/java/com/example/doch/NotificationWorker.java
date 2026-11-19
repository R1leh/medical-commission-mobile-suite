package com.example.doch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {
    public static final String CHANNEL_ID = "medical_channel";
    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_NOTIFICATION_TYPE = "notification_type";
    private static final int NOTIFICATION_ID_WEEK = 1;
    private static final int NOTIFICATION_ID_THREE_DAYS = 2;
    private static final int NOTIFICATION_ID_ONE_DAY = 3;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (!manager.areNotificationsEnabled()) {
                android.util.Log.w("NotificationWorker", "Notifications permission not granted");
                return Result.failure();
            }
        }

        createNotificationChannel();
        String startDate = getInputData().getString(KEY_START_DATE);
        String notificationType = getInputData().getString(KEY_NOTIFICATION_TYPE);
        showNotification(notificationType, startDate);
        return Result.success();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Medical Commission",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String notificationType, String startDate) {
        int notificationId;
        String message;
        switch (notificationType) {
            case "week":
                notificationId = NOTIFICATION_ID_WEEK;
                message = "Медкомиссия начнется через неделю (" + startDate + ")!";
                break;
            case "three_days":
                notificationId = NOTIFICATION_ID_THREE_DAYS;
                message = "Медкомиссия начнется через 3 дня (" + startDate + ")!";
                break;
            case "one_day":
                notificationId = NOTIFICATION_ID_ONE_DAY;
                message = "Медкомиссия начнется завтра (" + startDate + ")!";
                break;
            default:
                notificationId = 0;
                message = "Медкомиссия начнется скоро!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Напоминание о медкомиссии")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
        manager.notify(notificationId, builder.build());
    }
}