package com.example.notetaking; // <-- PAKET İSMİNİ DÜZELT

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etNoteTitle, etNoteContent;
    private ImageView ivNoteImage;
    private Button btnSelectImage, btnSaveNote, btnTakePhoto;
    private ImageButton btnBackAdd;

    private Bitmap selectedBitmap;
    private String encodedImageString = "";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        ivNoteImage = findViewById(R.id.ivNoteImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnBackAdd = findViewById(R.id.btnBackAdd);

        // Geri Dön
        btnBackAdd.setOnClickListener(v -> finish());

        // --- 1. RESMİN KENDİSİNE TIKLAYINCA (Galeri Aç) ---
        ivNoteImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        // --------------------------------------------------

        // 2. Galeri Butonu
        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 3. Kamera Butonu
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(AddNoteActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddNoteActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                cameraLauncher.launch(null);
            }
        });

        // 4. Kaydet Butonu
        btnSaveNote.setOnClickListener(v -> saveNoteToFirebase());
    }

    // Kamera Sonucu
    ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    ivNoteImage.setImageBitmap(selectedBitmap);
                    encodedImageString = encodeImage(selectedBitmap);
                }
            }
    );

    // Galeri Sonucu
    ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                            selectedBitmap = ImageDecoder.decodeBitmap(source);
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        }
                        ivNoteImage.setImageBitmap(selectedBitmap);
                        encodedImageString = encodeImage(selectedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    // Sıkıştırma Fonksiyonu
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 800;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void saveNoteToFirebase() {
        String title = etNoteTitle.getText().toString();
        String content = etNoteContent.getText().toString();

        if (TextUtils.isEmpty(title)) {
            etNoteTitle.setError("Başlık gerekli");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> noteMap = new HashMap<>();
        noteMap.put("title", title);
        noteMap.put("content", content);
        noteMap.put("image", encodedImageString);
        noteMap.put("userId", user.getUid());
        noteMap.put("date", System.currentTimeMillis());

        db.collection("notes")
                .add(noteMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddNoteActivity.this, "Not Kaydedildi!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddNoteActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}