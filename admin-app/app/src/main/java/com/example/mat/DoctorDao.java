package com.example.mat;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DoctorDao {
    @Insert
    void insert(Doctor doctor);

    @Update
    void update(Doctor doctor);

    @Delete
    void delete(Doctor doctor);

    @Query("SELECT * FROM doctors")
    List<Doctor> getAllDoctors();

    @Query("DELETE FROM doctors")
    void deleteAll();
}