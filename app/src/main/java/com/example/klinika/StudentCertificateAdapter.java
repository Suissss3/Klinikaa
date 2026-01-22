package com.example.klinika;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.OutputStream;
import java.util.List;

public class StudentCertificateAdapter extends RecyclerView.Adapter<StudentCertificateAdapter.ViewHolder> {

    private Context context;
    private List<CertificateItem> list;

    public StudentCertificateAdapter(Context context, List<CertificateItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public StudentCertificateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_certificate_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentCertificateAdapter.ViewHolder holder, int position) {
        CertificateItem item = list.get(position);
        holder.tvType.setText(item.certType);
        holder.tvStatus.setText(item.status);

        boolean downloadable = "approved".equalsIgnoreCase(item.status) && item.base64File != null && !item.base64File.isEmpty();
        holder.btnDownload.setEnabled(downloadable);
        if (downloadable) {
            holder.btnDownload.setOnClickListener(v -> downloadCertificate(item));
        } else {
            holder.btnDownload.setOnClickListener(null);
        }
    }

    private void downloadCertificate(CertificateItem item) {
        try {
            byte[] decoded = Base64.decode(item.base64File, Base64.DEFAULT);
            String ext = "bin";
            String mime = item.fileMime != null ? item.fileMime : "application/octet-stream";

            // choose sensible extension from mime
            if (mime.contains("jpeg") || mime.contains("jpg")) ext = "jpg";
            else if (mime.contains("png")) ext = "png";
            else if (mime.contains("pdf")) ext = "pdf";
            else if (mime.contains("gif")) ext = "gif";

            String filename = item.certType.replaceAll("\\s+", "_") + "_" + (System.currentTimeMillis()/1000) + "." + ext;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, mime);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                android.net.Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show();
                    return;
                }
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                if (os != null) {
                    os.write(decoded);
                    os.close();
                    Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to write file", Toast.LENGTH_SHORT).show();
                }
            } else {
                java.io.File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) dir = context.getFilesDir();
                java.io.File file = new java.io.File(dir, filename);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                fos.write(decoded);
                fos.close();
                Toast.makeText(context, "Saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvStatus;
        Button btnDownload;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvCertType);
            tvStatus = itemView.findViewById(R.id.tvCertStatus);
            btnDownload = itemView.findViewById(R.id.btnDownloadCert);
        }
    }
}
