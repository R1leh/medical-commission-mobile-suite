package com.example.doch;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DoctorDao {
    @Query("SELECT * FROM doctors")
    LiveData<List<Doctor>> getAllDoctors();

    @Query("SELECT * FROM doctors WHERE isCompleted = 0")
    LiveData<List<Doctor>> getActiveDoctors();

    @Query("SELECT * FROM doctors WHERE isCompleted = 1")
    LiveData<List<Doctor>> getCompletedDoctors();

    @Insert
    void insert(Doctor doctor);

    @Update
    void update(Doctor doctor);

    @Query("DELETE FROM doctors")
    void deleteAll();
}