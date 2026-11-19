package com.example.mat;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Doctor.class, MedicalDate.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DoctorDao doctorDao();
    public abstract MedicalDateDao medicalDateDao();
}