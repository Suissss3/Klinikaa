package com.example.klinika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentNotificationAdapter extends RecyclerView.Adapter<StudentNotificationAdapter.ViewHolder> {

    private Context context;
    private List<NotificationItem> list;

    public StudentNotificationAdapter(Context context, List<NotificationItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);

        holder.tvMessage.setText(item.message);
        holder.tvTime.setText(item.timestamp);

        switch (item.type) {
            case "appointment":
                holder.tvType.setText("Appointment Reminder");
                break;
            case "vaccination":
                holder.tvType.setText("Vaccination Reminder");
                break;
            case "certificate_update":
                holder.tvType.setText("Certificate Update");
                break;
            case "results":
                holder.tvType.setText("Test Result Notice");
                break;
            default:
                holder.tvType.setText("Notification");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvMessage, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvNotifType);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
        }
    }
}
