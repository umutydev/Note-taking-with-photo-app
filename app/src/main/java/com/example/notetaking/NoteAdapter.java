package com.example.notetaking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private OnNoteListener onNoteListener;

    public NoteAdapter(List<Note> noteList, OnNoteListener onNoteListener) {
        this.noteList = noteList;
        this.onNoteListener = onNoteListener;
    }

    // Arama filtreleme için gerekli metod
    public void filterList(List<Note> filteredList) {
        this.noteList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());

        // --- TARİH DÖNÜŞTÜRME ---
        // "long" tipindeki zamanı okunabilir tarihe çeviriyoruz
        if (note.getDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateString = sdf.format(new Date(note.getDate()));
            holder.date.setText(dateString);
        } else {
            holder.date.setText("");
        }
        // -------------------------

        // Resim İşlemleri
        if (note.getImage() != null && !note.getImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(note.getImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.image.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Tıklamalar
        holder.itemView.setOnClickListener(v -> {
            if (onNoteListener != null) onNoteListener.onNoteClick(note, position);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onNoteListener != null) onNoteListener.onDeleteClick(note, position);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, date; // "date" eklendi
        ImageView image;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvItemTitle);
            content = itemView.findViewById(R.id.tvItemContent);
            date = itemView.findViewById(R.id.tvItemDate); // Tarih kutusunu bulduk
            image = itemView.findViewById(R.id.ivItemImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteNote);
        }
    }

    public interface OnNoteListener {
        void onDeleteClick(Note note, int position);
        void onNoteClick(Note note, int position);
    }
}