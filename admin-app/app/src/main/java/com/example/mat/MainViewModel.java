package com.example.mat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final MutableLiveData<List<MedicalDate>> dates = new MutableLiveData<>();
    private final MutableLiveData<List<Doctor>> doctors = new MutableLiveData<>();
    private final AppDatabase database;
    private final DoctorDao doctorDao;
    private final MedicalDateDao medicalDateDao;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private static final String PREFS_NAME = "MedicalCommissionPrefs";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = Room.databaseBuilder(application, AppDatabase.class, "medical_database")
                .fallbackToDestructiveMigration()
                .build();
        doctorDao = database.doctorDao();
        medicalDateDao = database.medicalDateDao();

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        executorService.execute(() -> {
            // Загрузка врачей
            List<Doctor> doctorList = doctorDao.getAllDoctors();
            if (doctorList.isEmpty()) {
                doctorList = initializeDefaultDoctors();
                for (Doctor doctor : doctorList) {
                    doctorDao.insert(doctor);
                }
                doctorList = doctorDao.getAllDoctors();
            }
            doctors.postValue(doctorList);

            // Загрузка дат
            List<MedicalDate> dateList = medicalDateDao.getAllMedicalDates();
            if (dateList.isEmpty()) {
                dateList = initializeDefaultDates();
                for (MedicalDate date : dateList) {
                    medicalDateDao.insert(date);
                }
                dateList = medicalDateDao.getAllMedicalDates();
            }

            // Загрузка сохраненных дат из SharedPreferences
            SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedStartDate = prefs.getString(KEY_START_DATE, null);
            String savedEndDate = prefs.getString(KEY_END_DATE, null);
            if (savedStartDate != null && savedEndDate != null) {
                dateList = new ArrayList<>();
                dateList.add(new MedicalDate(savedStartDate, savedEndDate));
            }
            dates.postValue(dateList);
        });
    }

    private List<Doctor> initializeDefaultDoctors() {
        List<Doctor> defaultDoctors = new ArrayList<>();
        return defaultDoctors;
    }

    private List<MedicalDate> initializeDefaultDates() {
        List<MedicalDate> defaultDates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        return defaultDates;
    }

    public void addDoctor(Doctor doctor) {
        executorService.execute(() -> {
            doctorDao.insert(doctor);
            doctors.postValue(doctorDao.getAllDoctors());
        });
    }

    public void updateDoctor(Doctor doctor) {
        executorService.execute(() -> {
            doctorDao.update(doctor);
            doctors.postValue(doctorDao.getAllDoctors());
        });
    }

    public void deleteDoctor(Doctor doctor) {
        executorService.execute(() -> {
            doctorDao.delete(doctor);
            doctors.postValue(doctorDao.getAllDoctors());
        });
    }

    public void addDate(MedicalDate date) {
        executorService.execute(() -> {
            medicalDateDao.insert(date);
            List<MedicalDate> updatedDates = medicalDateDao.getAllMedicalDates();
            dates.postValue(updatedDates);

            SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_START_DATE, date.getStartDate());
            editor.putString(KEY_END_DATE, date.getEndDate());
            editor.apply();
        });
    }

    public void clearData() {
        executorService.execute(() -> {
            doctorDao.deleteAll();
            medicalDateDao.deleteAll();
            doctors.postValue(new ArrayList<>());
            dates.postValue(new ArrayList<>());

            SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
        });
    }

    public void setDates(List<MedicalDate> newDates) {
        executorService.execute(() -> {
            medicalDateDao.deleteAll();
            for (MedicalDate date : newDates) {
                medicalDateDao.insert(date);
            }
            dates.postValue(newDates);

            if (!newDates.isEmpty()) {
                SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_START_DATE, newDates.get(0).getStartDate());
                editor.putString(KEY_END_DATE, newDates.get(0).getEndDate());
                editor.apply();
            }
        });
    }

    public void setDoctors(List<Doctor> newDoctors) {
        executorService.execute(() -> {
            doctorDao.deleteAll();
            for (Doctor doctor : newDoctors) {
                doctorDao.insert(doctor);
            }
            doctors.postValue(newDoctors);
        });
    }

    public void refreshData() {
        executorService.execute(() -> {
            // Перезагрузка врачей
            List<Doctor> doctorList = doctorDao.getAllDoctors();
            doctors.postValue(doctorList);

            // Перезагрузка дат
            List<MedicalDate> dateList = medicalDateDao.getAllMedicalDates();
            // Загрузка сохраненных дат из SharedPreferences
            SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedStartDate = prefs.getString(KEY_START_DATE, null);
            String savedEndDate = prefs.getString(KEY_END_DATE, null);
            if (savedStartDate != null && savedEndDate != null) {
                dateList = new ArrayList<>();
                dateList.add(new MedicalDate(savedStartDate, savedEndDate));
            }
            dates.postValue(dateList);
        });
    }

    public LiveData<List<MedicalDate>> getDates() {
        return dates;
    }

    public LiveData<List<Doctor>> getDoctors() {
        return doctors;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}