package com.example.edugenie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<FlashcardSetActivity.Flashcard> flashcards;
    private OnFlashcardClickListener listener;

    public interface OnFlashcardClickListener {
        void onEditClick(FlashcardSetActivity.Flashcard flashcard);
        void onDeleteClick(FlashcardSetActivity.Flashcard flashcard);
    }

    public FlashcardAdapter(List<FlashcardSetActivity.Flashcard> flashcards, OnFlashcardClickListener listener) {
        this.flashcards = flashcards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        FlashcardSetActivity.Flashcard flashcard = flashcards.get(position);
        holder.bind(flashcard);
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    class FlashcardViewHolder extends RecyclerView.ViewHolder {
        private TextView questionText, answerText;
        private ImageButton editButton, deleteButton;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            answerText = itemView.findViewById(R.id.answerText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(FlashcardSetActivity.Flashcard flashcard) {
            questionText.setText("Q: " + flashcard.getQuestion());
            answerText.setText("A: " + flashcard.getAnswer());

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(flashcard);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(flashcard);
                }
            });
        }
    }
}