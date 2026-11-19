package com.example.mat;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MedicalDateDao {
    @Insert
    void insert(MedicalDate medicalDate);

    @Query("SELECT * FROM medical_dates")
    List<MedicalDate> getAllMedicalDates();

    @Query("DELETE FROM medical_dates")
    void deleteAll();
}