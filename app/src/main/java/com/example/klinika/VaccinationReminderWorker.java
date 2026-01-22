package com.example.klinika;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class VaccinationReminderWorker extends Worker {

    public VaccinationReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        String vaccineName = getInputData().getString("vaccineName");
        String dueDate = getInputData().getString("dueDate");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                "vaccine_channel"
        )
                .setSmallIcon(R.drawable.profile_placeholder)
                .setContentTitle("Vaccination Reminder")
                .setContentText(vaccineName + " is due on " + dueDate)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify((int) System.currentTimeMillis(), builder.build());

        return Result.success();
    }
}
