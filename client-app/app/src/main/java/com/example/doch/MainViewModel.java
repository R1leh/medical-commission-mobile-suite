package com.example.doch;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.WorkManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final DoctorDao doctorDao;
    private final MedicalDateDao medicalDateDao;
    private final LiveData<List<Doctor>> doctors;
    private final LiveData<List<Doctor>> activeDoctors;
    private final LiveData<List<Doctor>> completedDoctors;
    private final LiveData<List<MedicalDate>> dates;
    private final MutableLiveData<Integer> totalDoctors = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> completedDoctorsCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> remainingDoctorsCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> importCompleted = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MainViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        doctorDao = db.doctorDao();
        medicalDateDao = db.medicalDateDao();
        doctors = doctorDao.getAllDoctors();
        activeDoctors = doctorDao.getActiveDoctors();
        completedDoctors = doctorDao.getCompletedDoctors();
        dates = medicalDateDao.getAllDates();

        doctors.observeForever(doctorList -> {
            if (doctorList == null || doctorList.isEmpty()) {
                totalDoctors.setValue(0);
                completedDoctorsCount.setValue(0);
                remainingDoctorsCount.setValue(0);
            } else {
                totalDoctors.setValue(doctorList.size());
                completedDoctorsCount.setValue((int) doctorList.stream().filter(Doctor::isCompleted).count());
                remainingDoctorsCount.setValue((int) doctorList.stream().filter(d -> !d.isCompleted()).count());
            }
        });
    }

    public LiveData<List<Doctor>> getDoctors() {
        return doctors;
    }

    public LiveData<List<Doctor>> getActiveDoctors() {
        return activeDoctors;
    }

    public LiveData<List<Doctor>> getCompletedDoctors() {
        return completedDoctors;
    }

    public LiveData<List<MedicalDate>> getDates() {
        return dates;
    }

    public LiveData<Integer> getTotalDoctors() {
        return totalDoctors;
    }

    public LiveData<Integer> getCompletedDoctorsCount() {
        return completedDoctorsCount;
    }

    public LiveData<Integer> getRemainingDoctorsCount() {
        return remainingDoctorsCount;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<Boolean> getImportCompleted() {
        return importCompleted;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void addDoctor(Doctor doctor) {
        executorService.execute(() -> {
            doctorDao.insert(doctor);
            android.util.Log.d("MainViewModel", "Врач добавлен: " + doctor.getFullName());
        });
    }

    public void updateDoctor(Doctor doctor) {
        executorService.execute(() -> doctorDao.update(doctor));
    }

    public void addDate(MedicalDate date) {
        executorService.execute(() -> {
            medicalDateDao.insert(date);
            android.util.Log.d("MainViewModel", "Даты добавлены: " + date.getStartDate() + " - " + date.getEndDate());
            importCompleted.postValue(true);
        });
    }

    public void clearData() {
        executorService.execute(() -> {
            doctorDao.deleteAll();
            medicalDateDao.deleteAll();
            WorkManager.getInstance(getApplication()).cancelAllWork();
            android.util.Log.d("MainViewModel", "Все данные очищены");
            importCompleted.postValue(false);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}