package com.example.notetaking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    EditText etTitle, etContent; // Artık EditText oldu
    ImageView ivImage;
    Button btnBack, btnUpdate;

    FirebaseFirestore db;
    String documentId; // Güncellenecek notun kimliği

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etDetailTitle);
        etContent = findViewById(R.id.etDetailContent);
        ivImage = findViewById(R.id.ivDetailImage);
        btnBack = findViewById(R.id.btnBack);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Verileri Al
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String imageString = getIntent().getStringExtra("image");
        documentId = getIntent().getStringExtra("docId"); // ID'yi alıyoruz

        // Kutulara Doldur
        etTitle.setText(title);
        etContent.setText(content);

        // Resmi Göster
        if (imageString != null && !imageString.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivImage.setImageBitmap(decodedByte);
                ivImage.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivImage.setVisibility(View.GONE);
            }
        }

        // Geri Dön Butonu
        btnBack.setOnClickListener(v -> finish());

        // GÜNCELLEME BUTONU
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNote();
            }
        });
    }

    private void updateNote() {
        String newTitle = etTitle.getText().toString();
        String newContent = etContent.getText().toString();

        if (newTitle.isEmpty()) {
            etTitle.setError("Başlık boş olamaz");
            return;
        }

        // Firebase'e gönderilecek paket
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("title", newTitle);
        noteData.put("content", newContent);
        // İstersen "date" alanını da güncelleyip notu en üste taşıyabilirsin:
        noteData.put("date", System.currentTimeMillis());

        // Belirli ID'ye sahip belgeyi güncelle
        db.collection("notes").document(documentId)
                .update(noteData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailActivity.this, "Not Güncellendi!", Toast.LENGTH_SHORT).show();
                    finish(); // Sayfayı kapat, listeye dön
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}