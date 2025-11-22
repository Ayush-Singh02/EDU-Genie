package com.example.edugenie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FlashcardSetAdapter extends RecyclerView.Adapter<FlashcardSetAdapter.FlashcardSetViewHolder> {

    private List<FlashcardsActivity.FlashcardSet> flashcardSets;
    private final OnFlashcardSetClickListener listener;

    public interface OnFlashcardSetClickListener {
        void onSetClick(FlashcardsActivity.FlashcardSet flashcardSet);
        void onEditClick(FlashcardsActivity.FlashcardSet flashcardSet);
        void onDeleteClick(FlashcardsActivity.FlashcardSet flashcardSet);
    }

    public FlashcardSetAdapter(List<FlashcardsActivity.FlashcardSet> flashcardSets, OnFlashcardSetClickListener listener) {
        this.flashcardSets = flashcardSets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_set, parent, false);
        return new FlashcardSetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardSetViewHolder holder, int position) {
        FlashcardsActivity.FlashcardSet flashcardSet = flashcardSets.get(position);
        holder.bind(flashcardSet);
    }

    @Override
    public int getItemCount() {
        return flashcardSets != null ? flashcardSets.size() : 0;
    }

    class FlashcardSetViewHolder extends RecyclerView.ViewHolder {
        private final TextView setNameText, cardCountText;
        private final ImageButton editButton, deleteButton;

        public FlashcardSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNameText = itemView.findViewById(R.id.setNameText);
            cardCountText = itemView.findViewById(R.id.cardCountText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(FlashcardsActivity.FlashcardSet flashcardSet) {
            setNameText.setText(flashcardSet.getName());
            cardCountText.setText(flashcardSet.getCardCount() + " cards");

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onSetClick(flashcardSet);
                }
            });

            editButton.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onEditClick(flashcardSet);
                }
            });

            deleteButton.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(flashcardSet);
                }
            });
        }
    }
}
