package com.example.klinika;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminVaccineAdapter extends RecyclerView.Adapter<AdminVaccineAdapter.ViewHolder> {

    private Context context;
    private List<VaccineItem> vaccineList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(VaccineItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AdminVaccineAdapter(Context context, List<VaccineItem> vaccineList) {
        this.context = context;
        this.vaccineList = vaccineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_vaccine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VaccineItem item = vaccineList.get(position);
        holder.tvStudentName.setText(item.studentName);
        holder.tvVaccineName.setText(item.vaccineName);
        holder.tvDateTaken.setText(item.dateTaken);
        holder.tvDueDate.setText(item.dueDate);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);

            // Optional: Open AddVaccinationActivity directly for edit
            Intent intent = new Intent(context, AddVaccinationActivity.class);
            intent.putExtra("uid", item.studentUid);
            intent.putExtra("name", item.studentName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return vaccineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvVaccineName, tvDateTaken, tvDueDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvVaccineName = itemView.findViewById(R.id.tvVaccineName);
            tvDateTaken = itemView.findViewById(R.id.tvDateTaken);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
        }
    }
}
