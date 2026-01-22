package com.example.klinika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {

    private final List<MedicalHistory> historyList;

    public MedicalHistoryAdapter(List<MedicalHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medical_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalHistory history = historyList.get(position);

        holder.tvDate.setText("Date: " + history.date);
        holder.tvType.setText("Type of Visit: " + history.type);
        holder.tvDiagnosis.setText("Diagnosis: " + history.diagnosis);
        holder.tvTreatment.setText("Treatment: " + history.treatment);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvType, tvDiagnosis, tvTreatment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvType = itemView.findViewById(R.id.tvType);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
            tvTreatment = itemView.findViewById(R.id.tvTreatment);
        }
    }
}
