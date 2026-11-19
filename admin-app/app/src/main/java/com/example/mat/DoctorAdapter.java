package com.example.mat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {
    private List<Doctor> doctors = new ArrayList<>();

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
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
        Doctor doctor = doctors.get(position);
        holder.tvFullName.setText(doctor.getFullName());
        holder.tvPosition.setText(doctor.getPosition());
        holder.tvOffice.setText("Каб. " + doctor.getOffice());
        holder.tvWorkingHours.setText(doctor.getWorkingHours());

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EditReceptionActivity.class);
            intent.putExtra("DOCTOR_ID", doctor.getId());
            intent.putExtra("DOCTOR_FULL_NAME", doctor.getFullName());
            intent.putExtra("DOCTOR_POSITION", doctor.getPosition());
            intent.putExtra("DOCTOR_OFFICE", doctor.getOffice());
            intent.putExtra("DOCTOR_WORKING_HOURS", doctor.getWorkingHours());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvPosition, tvOffice, tvWorkingHours;
        FloatingActionButton btnEdit;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvPosition = itemView.findViewById(R.id.tv_position);
            tvOffice = itemView.findViewById(R.id.tv_office);
            tvWorkingHours = itemView.findViewById(R.id.tv_working_hours);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }
}