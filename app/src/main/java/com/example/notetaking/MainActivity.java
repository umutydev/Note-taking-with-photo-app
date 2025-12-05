package com.example.notetaking; // <-- BURAYA KENDİ PAKET İSMİNİ YAZ

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener {

    FloatingActionButton fabAdd;
    ImageButton btnLogout;
    RecyclerView recyclerView;
    SearchView searchView;

    NoteAdapter noteAdapter;
    List<Note> noteList;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Başlat
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Görünümleri Tanımla
        fabAdd = findViewById(R.id.fabAdd);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

        // RecyclerView Ayarları
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);

        // --- ARAMA KUTUSU AYARLARI (HEM RENK HEM TIKLAMA ÇÖZÜMÜ) ---
        searchView.setIconifiedByDefault(false); // Kutuyu hep açık tut
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.clearFocus(); // İlk açılışta klavye fırlamasın diye

        try {
            // Yazı rengini SİYAH yap (Görünmez yazı sorunu için)
            EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchEditText.setTextColor(Color.BLACK);
            searchEditText.setHintTextColor(Color.GRAY);

            // Tıklayınca Klavyeyi ZORLA Aç (Tıklanmama sorunu için)
            searchEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchView.setIconified(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Genel Tıklama Dinleyicisi
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        // Arama Dinleyicisi
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText); // Yazdıkça filtrele
                return true;
            }
        });

        // Butonlar
        fabAdd.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddNoteActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    // --- FİLTRELEME ---
    private void filter(String text) {
        List<Note> filteredList = new ArrayList<>();
        for (Note item : noteList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                    item.getContent().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (noteAdapter != null) {
            noteAdapter.filterList(filteredList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    // --- VERİ ÇEKME ---
    private void loadNotes() {
        if (mAuth.getCurrentUser() == null) return;

        db.collection("notes")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        noteList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Note note = doc.toObject(Note.class);
                            if (note != null) {
                                note.setDocumentId(doc.getId());
                                noteList.add(note);
                            }
                        }
                        noteAdapter.notifyDataSetChanged();
                    } else {
                        noteList.clear();
                        noteAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- TIKLAMA VE SİLME ---
    @Override
    public void onNoteClick(Note note, int position) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", note.getContent());
        intent.putExtra("image", note.getImage());
        intent.putExtra("docId", note.getDocumentId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Note note, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Notu Sil")
                .setMessage("Silmek istediğine emin misin?")
                .setPositiveButton("Evet", (dialog, which) -> deleteNoteFromFirebase(note.getDocumentId()))
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void deleteNoteFromFirebase(String docId) {
        db.collection("notes").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Not Silindi", Toast.LENGTH_SHORT).show();
                    loadNotes(); // Listeyi yenile
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Silinemedi!", Toast.LENGTH_SHORT).show());
    }
}