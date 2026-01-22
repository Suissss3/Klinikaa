package com.example.klinika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminMedicalHistoryAdapter extends RecyclerView.Adapter<AdminMedicalHistoryAdapter.ViewHolder> {

    public interface OnHistoryActionListener {
        void onEdit(MedicalHistory history);
        void onDelete(MedicalHistory history);
    }

    private List<MedicalHistory> historyList;
    private OnHistoryActionListener listener;

    public AdminMedicalHistoryAdapter(List<MedicalHistory> historyList, OnHistoryActionListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_medical_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalHistory history = historyList.get(position);

        holder.tvDate.setText("Date: " + history.date);
        holder.tvType.setText("Type: " + history.type);
        holder.tvDiagnosis.setText("Diagnosis: " + history.diagnosis);
        holder.tvTreatment.setText("Treatment: " + history.treatment);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(history));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(history));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvType, tvDiagnosis, tvTreatment;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvType = itemView.findViewById(R.id.tvType);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
            tvTreatment = itemView.findViewById(R.id.tvTreatment);
            btnEdit = itemView.findViewById(R.id.btnEditHistory);
            btnDelete = itemView.findViewById(R.id.btnDeleteHistory);
        }
    }
}
