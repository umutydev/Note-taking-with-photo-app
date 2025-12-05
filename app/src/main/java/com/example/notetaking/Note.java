package com.example.notetaking;

import com.google.firebase.firestore.Exclude;

public class Note {
    String title;
    String content;
    String image;
    String userId;
    long date;

    // Veritabanı kimliğini tutacak değişken (Firebase'e kaydedilmez, sadece biz kullanırız)
    @Exclude
    String documentId;

    public Note() {} // Boş kurucu zorunlu

    public Note(String title, String content, String image, String userId, long date) {
        this.title = title;
        this.content = content;
        this.image = image;
        this.userId = userId;
        this.date = date;
    }

    // Getter ve Setterlar
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImage() { return image; }
    public long getDate() { return date; }

    // ID işlemleri
    @Exclude
    public String getDocumentId() { return documentId; }
    @Exclude
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}