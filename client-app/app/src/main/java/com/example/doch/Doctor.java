package com.example.doch;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "doctors")
public class Doctor {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String fullName;
    private String position;
    private String office;
    private String workingHours;
    private boolean isCompleted; // Новое поле для статуса завершения

    public Doctor(String fullName, String position, String office, String workingHours) {
        this.fullName = fullName;
        this.position = position;
        this.office = office;
        this.workingHours = workingHours;
        this.isCompleted = false; // По умолчанию приём не завершён
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}