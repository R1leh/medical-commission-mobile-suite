package com.example.mat;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

// Аннотация @Entity указывает, что этот класс является сущностью базы данных
@Entity(tableName = "doctors") // Название таблицы в базе данных
public class Doctor {
    // Первичный ключ с автоинкрементом
    @PrimaryKey(autoGenerate = true)
    private int id; // Уникальный идентификатор врача

    private String fullName;      // Полное имя врача (например, "Иванов Иван Иванович")
    private String position;      // Должность врача (например, "Терапевт")
    private String office;        // Номер кабинета (например, "Кабинет 101")
    private String workingHours;  // Часы работы (например, "09:00-17:00")

    // Конструктор с id (основной для Room)
    public Doctor(int id, String fullName, String position, String office, String workingHours) {
        this.id = id;
        this.fullName = fullName;
        this.position = position;
        this.office = office;
        this.workingHours = workingHours;
    }

    // Конструктор без id (для создания новых записей, игнорируется Room)
    @Ignore
    public Doctor(String fullName, String position, String office, String workingHours) {
        this.fullName = fullName;
        this.position = position;
        this.office = office;
        this.workingHours = workingHours;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPosition() {
        return position;
    }

    public String getOffice() {
        return office;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    // Сеттеры
    public void setId(int id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    // Переопределение toString для удобного отображения в логах или отладке
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                ", office='" + office + '\'' +
                ", workingHours='" + workingHours + '\'' +
                '}';
    }
}