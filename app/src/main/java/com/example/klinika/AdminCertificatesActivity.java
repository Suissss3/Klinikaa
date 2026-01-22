package com.example.klinika;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminCertificatesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminCertificateAdapter adapter;
    private List<CertificateItem> list = new ArrayList<>();
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("certificates");
    private DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications");

    private CertificateItem pendingUploadFor; // item selected for upload

    // FILE PICKER
    private final ActivityResultLauncher<String> pickFileLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null && pendingUploadFor != null) {
                        uploadUriAsBase64(pendingUploadFor, uri);
                    } else {
                        Toast.makeText(AdminCertificatesActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_certificates);

        recyclerView = findViewById(R.id.recyclerCertificatesAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCertificateAdapter(this, list, new AdminCertificateAdapter.OnActionClickListener() {
            @Override
            public void onApprove(CertificateItem item) { approve(item); }
            @Override
            public void onReject(CertificateItem item) { reject(item); }
            @Override
            public void onUploadBase64(CertificateItem item) { pickFileForItem(item); }
        });
        recyclerView.setAdapter(adapter);

        loadRequestsRealtime();
    }

    private void loadRequestsRealtime() {
        db.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ch : snapshot.getChildren()) {
                    CertificateItem it = ch.getValue(CertificateItem.class);
                    if (it != null) {
                        it.id = ch.getKey();
                        list.add(it);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminCertificatesActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickFileForItem(CertificateItem item) {
        pendingUploadFor = item;
        pickFileLauncher.launch("*/*"); // accepts images & PDFs
    }

    private void uploadUriAsBase64(CertificateItem item, Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] bytes = toByteArray(is);
            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);

            String mime = getContentResolver().getType(uri);
            if (mime == null) mime = "application/octet-stream";

            Map<String, Object> updates = new HashMap<>();
            updates.put("base64File", encoded);
            updates.put("fileMime", mime);
            updates.put("status", "approved");

            db.child(item.id).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminCertificatesActivity.this, "Uploaded & approved", Toast.LENGTH_SHORT).show();
                    sendNotification(item.studentUid,
                            "Your certificate is ready for download",
                            "certificate_update");
                } else {
                    Toast.makeText(AdminCertificatesActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pendingUploadFor = null;
        }
    }

    private byte[] toByteArray(InputStream is) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int n;
        byte[] data = new byte[4096];
        while ((n = is.read(data, 0, data.length)) != -1) buffer.write(data, 0, n);
        return buffer.toByteArray();
    }

    private void approve(CertificateItem item) {
        if (item.base64File == null || item.base64File.isEmpty()) {
            Toast.makeText(this, "Upload file before approving", Toast.LENGTH_SHORT).show();
            return;
        }
        db.child(item.id).child("status").setValue("approved");
        sendNotification(item.studentUid,
                "Your certificate request has been approved",
                "certificate_update");
    }

    private void reject(CertificateItem item) {
        db.child(item.id).child("status").setValue("rejected");
        sendNotification(item.studentUid,
                "Your certificate request has been rejected",
                "certificate_update");
    }

    // ✔ NEW CLEAN NOTIFICATION METHOD
    private void sendNotification(String studentUid, String message, String type) {
        String key = notifRef.push().getKey();
        if (key == null) return;

        NotificationItem n = new NotificationItem();
        n.id = key;
        n.studentUid = studentUid;
        n.type = type;
        n.message = message;
        n.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        n.read = false;

        notifRef.child(key).setValue(n);
    }
}
