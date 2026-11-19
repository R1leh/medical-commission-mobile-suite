package com.example.doch;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MedicalDateDao {
    @Query("SELECT * FROM medical_dates")
    LiveData<List<MedicalDate>> getAllDates();

    @Insert
    void insert(MedicalDate date);

    @Query("DELETE FROM medical_dates")
    void deleteAll();
}