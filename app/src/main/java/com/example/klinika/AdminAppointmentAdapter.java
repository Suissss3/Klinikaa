package com.example.klinika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {

    private List<Appointment> list;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onApprove(Appointment appt);
        void onReject(Appointment appt);
        void onDelete(Appointment appt);
    }

    public AdminAppointmentAdapter(List<Appointment> list, OnAppointmentActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appt = list.get(position);

        holder.tvStudent.setText(appt.studentName);
        holder.tvDate.setText(appt.date);
        holder.tvTime.setText(appt.time);
        holder.tvStatus.setText(appt.status);


        holder.tvReason.setText(appt.reason);

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(appt));
        holder.btnReject.setOnClickListener(v -> listener.onReject(appt));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(appt));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudent, tvDate, tvTime, tvStatus, tvReason;
        Button btnApprove, btnReject, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvStudent = itemView.findViewById(R.id.tvStudentName);
            tvDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvStatus = itemView.findViewById(R.id.tvAppointmentStatus);
            tvReason = itemView.findViewById(R.id.tvAppointmentReason);

            btnApprove = itemView.findViewById(R.id.btnApproveAppointment);
            btnReject = itemView.findViewById(R.id.btnRejectAppointment);
            btnDelete = itemView.findViewById(R.id.btnDeleteAppointment);
        }
    }
}
