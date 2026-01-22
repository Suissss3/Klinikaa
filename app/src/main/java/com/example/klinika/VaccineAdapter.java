package com.example.klinika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VaccineAdapter extends RecyclerView.Adapter<VaccineAdapter.ViewHolder> {

    ArrayList<VaccinationRecord> list;

    public VaccineAdapter(ArrayList<VaccinationRecord> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vaccine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VaccinationRecord v = list.get(position);

        holder.name.setText(v.vaccineName);
        holder.dateTaken.setText("Date Taken: " + v.dateTaken);
        holder.dueDate.setText("Due: " + v.dueDate);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, dateTaken, dueDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txtVaccineName);
            dateTaken = itemView.findViewById(R.id.txtDateTaken);
            dueDate = itemView.findViewById(R.id.txtDueDate);
        }
    }
}
