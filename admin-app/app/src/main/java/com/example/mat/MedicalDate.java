package com.example.mat;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

// Аннотация @Entity указывает, что этот класс является сущностью базы данных
@Entity(tableName = "medical_dates") // Название таблицы в базе данных
public class MedicalDate {
    // Первичный ключ с автоинкрементом
    @PrimaryKey(autoGenerate = true)
    private int id; // Уникальный идентификатор записи

    private String startDate;  // Начальная дата медкомиссии (например, "15.05.2025")
    private String endDate;    // Конечная дата медкомиссии (например, "20.05.2025")

    // Конструктор с id (основной для Room)
    public MedicalDate(int id, String startDate, String endDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Конструктор без id (для создания новых записей, игнорируется Room)
    @Ignore
    public MedicalDate(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    // Сеттеры
    public void setId(int id) {
        this.id = id;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    // Переопределение toString для удобного отображения в логах или отладке
    @Override
    public String toString() {
        return "MedicalDate{" +
                "id=" + id +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}