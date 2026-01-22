package com.example.klinika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminCertificateAdapter extends RecyclerView.Adapter<AdminCertificateAdapter.ViewHolder> {

    public interface OnActionClickListener {
        void onApprove(CertificateItem item);
        void onReject(CertificateItem item);
        void onUploadBase64(CertificateItem item); // triggers file picker in Activity
    }

    private Context context;
    private List<CertificateItem> list;
    private OnActionClickListener listener;

    public AdminCertificateAdapter(Context context, List<CertificateItem> list, OnActionClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminCertificateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_certificate, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCertificateAdapter.ViewHolder holder, int position) {
        CertificateItem item = list.get(position);
        holder.tvType.setText(item.certType);
        holder.tvStudent.setText(item.studentName != null ? item.studentName : item.studentUid);
        holder.tvStatus.setText(item.status != null ? item.status : "pending");

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(item));
        holder.btnReject.setOnClickListener(v -> listener.onReject(item));
        holder.btnUpload.setOnClickListener(v -> listener.onUploadBase64(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvStatus, tvStudent;
        Button btnApprove, btnReject, btnUpload;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudent = itemView.findViewById(R.id.tvCertStudent);
            tvType = itemView.findViewById(R.id.tvCertType);
            tvStatus = itemView.findViewById(R.id.tvCertStatus);
            btnApprove = itemView.findViewById(R.id.btnApproveCert);
            btnReject = itemView.findViewById(R.id.btnRejectCert);
            btnUpload = itemView.findViewById(R.id.btnUploadCert);
        }
    }
}
