package com.example.notetaking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etRegEmail, etRegPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Başlat
        mAuth = FirebaseAuth.getInstance();

        // Görünümleri Tanımla
        etName = findViewById(R.id.etName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Kayıt Ol Butonu
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Giriş Yap Linki (Geri döner)
        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Bu sayfayı kapatır, alttaki Login sayfası görünür
            }
        });
    }

    private void registerUser() {
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etRegEmail.setError("Email gerekli");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etRegPassword.setError("Şifre gerekli");
            return;
        }
        if (password.length() < 6) {
            etRegPassword.setError("Şifre en az 6 karakter olmalı");
            return;
        }

        // Firebase'de Kullanıcı Oluştur
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Başarılı
                            Toast.makeText(RegisterActivity.this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();

                            // Direkt uygulamaya (Main) git
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            // Geri tuşuna basınca tekrar register'a dönmesin diye geçmişi temizle
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Hata
                            Toast.makeText(RegisterActivity.this, "Hata: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}