package com.example.doch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {
    private List<Doctor> doctors = new ArrayList<>();
    private List<Doctor> filteredDoctors = new ArrayList<>();
    private MainViewModel viewModel;

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors != null ? doctors : new ArrayList<>();
        this.filteredDoctors = new ArrayList<>(this.doctors);
        notifyDataSetChanged();
    }

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void filter(String query) {
        filteredDoctors.clear();
        if (query.isEmpty()) {
            filteredDoctors.addAll(doctors);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Doctor doctor : doctors) {
                if (doctor.getFullName().toLowerCase().contains(lowerQuery) ||
                        doctor.getPosition().toLowerCase().contains(lowerQuery)) {
                    filteredDoctors.add(doctor);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = filteredDoctors.get(position);
        holder.tvFullName.setText(doctor.getFullName());
        holder.tvPosition.setText(doctor.getPosition());
        holder.tvOffice.setText("Каб. " + doctor.getOffice());
        holder.tvWorkingHours.setText(doctor.getWorkingHours());

        // Показываем иконку для завершённых приёмов
        holder.ivCheck.setVisibility(doctor.isCompleted() ? View.VISIBLE : View.GONE);
        // Меняем фон карточки для завершённых
        holder.cardView.setCardBackgroundColor(
                doctor.isCompleted() ?
                        holder.itemView.getContext().getResources().getColor(R.color.colorCompletedBackground) :
                        holder.itemView.getContext().getResources().getColor(R.color.white)
        );

        holder.itemView.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(holder.itemView.getContext());
            if (doctor.isCompleted()) {
                builder.setTitle("Вернуть приём?")
                        .setMessage("Вернуть приём для " + doctor.getFullName() + " в текущие приёмы?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            doctor.setCompleted(false);
                            viewModel.updateDoctor(doctor);
                        })
                        .setNegativeButton("Нет", null);
            } else {
                builder.setTitle("Завершить приём?")
                        .setMessage("Вы завершили приём у " + doctor.getFullName() + "?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            doctor.setCompleted(true);
                            viewModel.updateDoctor(doctor);
                        })
                        .setNegativeButton("Нет", null);
            }
            builder.setCancelable(true)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return filteredDoctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvPosition, tvOffice, tvWorkingHours;
        ImageView ivCheck;
        com.google.android.material.card.MaterialCardView cardView;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvPosition = itemView.findViewById(R.id.tv_position);
            tvOffice = itemView.findViewById(R.id.tv_office);
            tvWorkingHours = itemView.findViewById(R.id.tv_working_hours);
            ivCheck = itemView.findViewById(R.id.iv_check);
            cardView = (com.google.android.material.card.MaterialCardView) itemView;
        }
    }
}