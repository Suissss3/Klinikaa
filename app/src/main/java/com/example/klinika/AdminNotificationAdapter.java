package com.example.klinika;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder> {

    private List<NotificationItem> notifications;

    public AdminNotificationAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
        sortNotificationsNewestFirst(); // Sort when adapter is created
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem n = notifications.get(position);
        holder.tvMessage.setText(n.message);
        holder.tvType.setText(n.type);
        holder.tvTimestamp.setText(n.timestamp);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // ---------------- Sorting Helper ----------------
    private void sortNotificationsNewestFirst() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Collections.sort(notifications, new Comparator<NotificationItem>() {
            @Override
            public int compare(NotificationItem n1, NotificationItem n2) {
                try {
                    // newest first
                    return sdf.parse(n2.timestamp).compareTo(sdf.parse(n1.timestamp));
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
    }

    // Optional: update notifications dynamically
    public void updateNotifications(List<NotificationItem> newNotifications) {
        this.notifications = newNotifications;
        sortNotificationsNewestFirst(); // sort again
        notifyDataSetChanged();
    }

    // ---------------- ViewHolder ----------------
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvType, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvType = itemView.findViewById(R.id.tvNotificationType);
            tvTimestamp = itemView.findViewById(R.id.tvNotificationTimestamp);
        }
    }
}
