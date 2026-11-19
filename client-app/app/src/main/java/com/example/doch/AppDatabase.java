package com.example.doch;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Doctor.class, MedicalDate.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract DoctorDao doctorDao();
    public abstract MedicalDateDao medicalDateDao();

    public static synchronized AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "child-app-database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}