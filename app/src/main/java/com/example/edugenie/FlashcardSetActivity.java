package com.example.edugenie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.gson.Gson;





import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Color;
//import com.google.gson.Gson;

public class FlashcardSetActivity extends AppCompatActivity {

    private LinearLayout flashcardsContainer;
    private FloatingActionButton addCardButton;
    private Button backButton, studyButton;
    private TextView titleText, emptyStateText;

    private String username, setId, setName;
    private List<Flashcard> flashcards;
    private FlashcardsActivity.FlashcardSet flashcardSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_set);

        // Get data from intent
        username = getIntent().getStringExtra("username");
        setId = getIntent().getStringExtra("setId");
        setName = getIntent().getStringExtra("setName");

        if (username == null || setId == null || setName == null) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views FIRST
        initializeViews();

        // Set up click listeners SECOND
        setupClickListeners();

        // Load flashcards THIRD
        loadFlashcards();
    }

    private void initializeViews() {
        flashcardsContainer = findViewById(R.id.flashcardsContainer);
        addCardButton = findViewById(R.id.addCardButton);
        backButton = findViewById(R.id.backButton);
        studyButton = findViewById(R.id.studyButton);
        titleText = findViewById(R.id.titleText);
        emptyStateText = findViewById(R.id.emptyStateText);

        titleText.setText(setName);
        flashcards = new ArrayList<>();
    }

    private void setupClickListeners() {
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateCardDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        studyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FlashcardSetActivity.this, "Study button clicked", Toast.LENGTH_SHORT).show();
                if (flashcards.isEmpty()) {
                    Toast.makeText(FlashcardSetActivity.this, "No flashcards to study", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(FlashcardSetActivity.this, "Starting study with " + flashcards.size() + " cards", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(FlashcardSetActivity.this, StudyModeActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("setId", setId);
                intent.putExtra("setName", setName);
                String flashcardsJson = new Gson().toJson(flashcards);
                intent.putExtra("flashcardsJson", flashcardsJson);
                startActivity(intent);
            }
        });
    }

    private void showCreateCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Flashcard");

        final android.widget.EditText questionInput = new android.widget.EditText(this);
        questionInput.setHint("Enter question");

        final android.widget.EditText answerInput = new android.widget.EditText(this);
        answerInput.setHint("Enter answer");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(questionInput);
        layout.addView(answerInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String question = questionInput.getText().toString().trim();
            String answer = answerInput.getText().toString().trim();

            if (!question.isEmpty() && !answer.isEmpty()) {
                createFlashcard(question, answer);
            } else {
                Toast.makeText(this, "Please enter both question and answer", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createFlashcard(String question, String answer) {
        String cardId = java.util.UUID.randomUUID().toString();
        Flashcard card = new Flashcard(cardId, question, answer, System.currentTimeMillis());
        flashcards.add(card);
        flashcardSet.cards = flashcards;
        flashcardSet.cardCount = flashcards.size();
        Map<String, FlashcardsActivity.FlashcardSet> sets = HelperClass.loadFlashcardSets(this);
        sets.put(setId, flashcardSet);
        HelperClass.saveFlashcardSets(this, sets);
        loadFlashcards();
        Toast.makeText(this, "Flashcard added successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadFlashcards() {
        Map<String, FlashcardsActivity.FlashcardSet> sets = HelperClass.loadFlashcardSets(this);
        flashcardSet = sets.get(setId);
        if (flashcardSet != null && flashcardSet.cards != null) {
            flashcards = new ArrayList<>(flashcardSet.cards);
        } else {
            flashcards = new ArrayList<>();
        }
        flashcardsContainer.removeAllViews();
        for (Flashcard card : flashcards) {
            createFlashcardView(card);
        }
        updateEmptyState();
    }

    private void createFlashcardView(Flashcard flashcard) {
        // Create card container
        android.widget.LinearLayout cardContainer = new android.widget.LinearLayout(this);
        cardContainer.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        cardContainer.setPadding(16, 16, 16, 16);
        cardContainer.setBackgroundResource(R.drawable.white_background);

        android.widget.LinearLayout.LayoutParams marginParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        marginParams.setMargins(0, 0, 0, 16);
        cardContainer.setLayoutParams(marginParams);

        // Use theme colors for card backgrounds and text
        int cardBgColor = HelperClass.getThemeColor(this, android.R.attr.colorBackground);
        int textColor = HelperClass.getThemeColor(this, android.R.attr.textColorPrimary);

        // Question
        TextView questionText = new TextView(this);
        questionText.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        questionText.setText("Q: " + flashcard.getQuestion());
        questionText.setTextSize(16);
        questionText.setTextColor(Color.BLACK);
        questionText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        questionText.setPadding(0, 0, 0, 8);

        // Answer
        TextView answerText = new TextView(this);
        answerText.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        answerText.setText("A: " + flashcard.getAnswer());
        answerText.setTextSize(14);
        answerText.setTextColor(Color.BLACK);
        answerText.setAlpha(0.8f);
        answerText.setPadding(0, 0, 0, 12);

        // Buttons
        android.widget.LinearLayout buttonLayout = new android.widget.LinearLayout(this);
        buttonLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        buttonLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.END);

        // Edit button
        Button editButton = new Button(this);
        editButton.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        editButton.setText("Edit");
        editButton.setTextSize(12);
        editButton.setBackgroundResource(R.drawable.lavender_border);
        editButton.setTextColor(Color.BLACK);
        editButton.setPadding(16, 8, 16, 8);

        // Delete button
        Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        deleteButton.setText("Delete");
        deleteButton.setTextSize(12);
        deleteButton.setBackgroundResource(R.drawable.lavender_border);
        deleteButton.setTextColor(Color.BLACK);
        deleteButton.setPadding(16, 8, 16, 8);

        // Set click listeners
        editButton.setOnClickListener(v -> editFlashcard(flashcard));
        deleteButton.setOnClickListener(v -> deleteFlashcard(flashcard));

        buttonLayout.addView(editButton);
        buttonLayout.addView(deleteButton);

        // Add views to card
        cardContainer.addView(questionText);
        cardContainer.addView(answerText);
        cardContainer.addView(buttonLayout);

        // Add card to container
        flashcardsContainer.addView(cardContainer);
    }

    private void editFlashcard(Flashcard flashcard) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Flashcard");

        final android.widget.EditText questionInput = new android.widget.EditText(this);
        questionInput.setText(flashcard.getQuestion());

        final android.widget.EditText answerInput = new android.widget.EditText(this);
        answerInput.setText(flashcard.getAnswer());

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(questionInput);
        layout.addView(answerInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String question = questionInput.getText().toString().trim();
            String answer = answerInput.getText().toString().trim();

            if (!question.isEmpty() && !answer.isEmpty()) {
                updateFlashcard(flashcard, question, answer);
            } else {
                Toast.makeText(this, "Please enter both question and answer", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateFlashcard(Flashcard flashcard, String question, String answer) {
        flashcard.question = question;
        flashcard.answer = answer;
        Map<String, FlashcardsActivity.FlashcardSet> sets = HelperClass.loadFlashcardSets(this);
        sets.put(setId, flashcardSet);
        HelperClass.saveFlashcardSets(this, sets);
        loadFlashcards();
        Toast.makeText(this, "Flashcard updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void deleteFlashcard(Flashcard flashcard) {
        flashcards.remove(flashcard);
        flashcardSet.cards = flashcards;
        flashcardSet.cardCount = flashcards.size();
        Map<String, FlashcardsActivity.FlashcardSet> sets = HelperClass.loadFlashcardSets(this);
        sets.put(setId, flashcardSet);
        HelperClass.saveFlashcardSets(this, sets);
        loadFlashcards();
        Toast.makeText(this, "Flashcard deleted successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateCardCount() {
        // This method is no longer needed as cardCount is managed locally
    }

    private void updateEmptyState() {
        if (flashcards.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            flashcardsContainer.setVisibility(View.GONE);
            studyButton.setEnabled(false);
        } else {
            emptyStateText.setVisibility(View.GONE);
            flashcardsContainer.setVisibility(View.VISIBLE);
            studyButton.setEnabled(true);
        }
    }

    // Flashcard data class
    public static class Flashcard {
        public String id;
        public String question;
        public String answer;
        public long createdAt;
        public Flashcard(String id, String question, String answer, long createdAt) {
            this.id = id;
            this.question = question;
            this.answer = answer;
            this.createdAt = createdAt;
        }
        public String getId() { return id; }
        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
    }
}