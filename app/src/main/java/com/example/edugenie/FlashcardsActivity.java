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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Color;

public class FlashcardsActivity extends AppCompatActivity {

    private LinearLayout flashcardSetsContainer;
    private FloatingActionButton addSetButton;
    private Button backButton;
    private TextView titleText, emptyStateText;

    private String username;
    private Map<String, FlashcardSet> flashcardSetsMap;

    private static final int MAX_FLASHCARD_SETS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        // Get username from intent
        username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Error: User not found. Please login again.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load flashcard sets
        loadFlashcardSets();
    }

    private void initializeViews() {
        flashcardSetsContainer = findViewById(R.id.flashcardSetsContainer);
        addSetButton = findViewById(R.id.addSetButton);
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        emptyStateText = findViewById(R.id.emptyStateText);

        titleText.setText("My Flashcards");
        flashcardSetsMap = new HashMap<>();
    }

    private void setupClickListeners() {
        addSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flashcardSetsMap.size() >= MAX_FLASHCARD_SETS) {
                    Toast.makeText(FlashcardsActivity.this,
                        "You can only create up to " + MAX_FLASHCARD_SETS + " flashcard sets",
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                showCreateSetDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showCreateSetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Flashcard Set");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter set name (e.g., Math Formulas, History Dates)");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String setName = input.getText().toString().trim();
            if (!setName.isEmpty()) {
                createFlashcardSet(setName);
            } else {
                Toast.makeText(this, "Please enter a set name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createFlashcardSet(String setName) {
        String setId = java.util.UUID.randomUUID().toString();
        FlashcardSet set = new FlashcardSet(setId, setName, 0);
        set.cards = new ArrayList<>();
        flashcardSetsMap.put(setId, set);
        HelperClass.saveFlashcardSets(this, flashcardSetsMap);
        loadFlashcardSets();
        Toast.makeText(this, "Flashcard set created successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadFlashcardSets() {
        flashcardSetsMap = HelperClass.loadFlashcardSets(this);
        flashcardSetsContainer.removeAllViews();
        for (FlashcardSet set : flashcardSetsMap.values()) {
            createFlashcardSetView(set);
        }
        updateEmptyState();
    }

    private void createFlashcardSetView(FlashcardSet flashcardSet) {
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

        cardContainer.setBackgroundColor(cardBgColor);

        // Create title and info
        android.widget.LinearLayout headerLayout = new android.widget.LinearLayout(this);
        headerLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        headerLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Set name
        TextView setNameText = new TextView(this);
        setNameText.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            0,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        ));
        setNameText.setText(flashcardSet.getName());
        setNameText.setTextSize(18);
        setNameText.setTextColor(Color.BLACK);
        setNameText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        // Card count
        TextView cardCountText = new TextView(this);
        cardCountText.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardCountText.setText(flashcardSet.getCardCount() + " cards");
        cardCountText.setTextSize(14);
        cardCountText.setTextColor(Color.BLACK);
        cardCountText.setAlpha(0.7f);

        headerLayout.addView(setNameText);
        headerLayout.addView(cardCountText);

        // Create buttons
        android.widget.LinearLayout buttonLayout = new android.widget.LinearLayout(this);
        buttonLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        buttonLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(android.view.Gravity.END);

        // Study button
        Button studyButton = new Button(this);
        studyButton.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        studyButton.setText("Study");
        studyButton.setTextSize(12);
        studyButton.setBackgroundResource(R.drawable.lavender_border);
        studyButton.setTextColor(Color.BLACK);
        studyButton.setPadding(16, 8, 16, 8);
        studyButton.setEnabled(flashcardSet.getCardCount() > 0);

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

        // Add Card button
        Button addCardButton = new Button(this);
        addCardButton.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        addCardButton.setText("Add Card");
        addCardButton.setTextSize(12);
        addCardButton.setBackgroundResource(R.drawable.lavender_border);
        addCardButton.setTextColor(Color.BLACK);
        addCardButton.setPadding(16, 8, 16, 8);
        addCardButton.setOnClickListener(v -> showAddCardDialog(flashcardSet));

        // Set click listeners
        studyButton.setOnClickListener(v -> openFlashcardSet(flashcardSet));
        editButton.setOnClickListener(v -> editFlashcardSet(flashcardSet));
        deleteButton.setOnClickListener(v -> deleteFlashcardSet(flashcardSet));

        buttonLayout.addView(addCardButton, 0);
        buttonLayout.addView(studyButton);
        buttonLayout.addView(editButton);
        buttonLayout.addView(deleteButton);

        // Add views to card
        cardContainer.addView(headerLayout);
        cardContainer.addView(buttonLayout);

        // Add card to container
        flashcardSetsContainer.addView(cardContainer);
    }

    private void openFlashcardSet(FlashcardSet flashcardSet) {
        Intent intent = new Intent(this, FlashcardSetActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("setId", flashcardSet.getId());
        intent.putExtra("setName", flashcardSet.getName());
        startActivity(intent);
    }

    private void editFlashcardSet(FlashcardSet flashcardSet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Flashcard Set");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(flashcardSet.getName());
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateFlashcardSet(flashcardSet, newName);
            } else {
                Toast.makeText(this, "Please enter a set name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateFlashcardSet(FlashcardSet flashcardSet, String newName) {
        flashcardSet.name = newName;
        HelperClass.saveFlashcardSets(this, flashcardSetsMap);
        loadFlashcardSets();
        Toast.makeText(this, "Flashcard set updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void deleteFlashcardSet(FlashcardSet flashcardSet) {
        flashcardSetsMap.remove(flashcardSet.id);
        HelperClass.saveFlashcardSets(this, flashcardSetsMap);
        loadFlashcardSets();
        Toast.makeText(this, "Flashcard set deleted successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyState() {
        if (flashcardSetsMap.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            flashcardSetsContainer.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            flashcardSetsContainer.setVisibility(View.VISIBLE);
        }
    }

    // Add this method to show the add card dialog for a set
    private void showAddCardDialog(FlashcardSet flashcardSet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Flashcard to " + flashcardSet.getName());

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
                addCardToSet(flashcardSet, question, answer);
            } else {
                Toast.makeText(this, "Please enter both question and answer", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Add this method to add a card to a set in Firebase
    private void addCardToSet(FlashcardSet flashcardSet, String question, String answer) {
        String cardId = java.util.UUID.randomUUID().toString();
        FlashcardSetActivity.Flashcard card = new FlashcardSetActivity.Flashcard(cardId, question, answer, System.currentTimeMillis());
        flashcardSet.cards.add(card);
        flashcardSet.cardCount = flashcardSet.cards.size();
        flashcardSetsMap.put(flashcardSet.id, flashcardSet);
        HelperClass.saveFlashcardSets(this, flashcardSetsMap);
        loadFlashcardSets();
        Toast.makeText(this, "Flashcard added successfully", Toast.LENGTH_SHORT).show();
    }


    // Add this method to update the card count for a set
    private void updateCardCount(FlashcardSet flashcardSet) {
        // This method is no longer needed as cardCount is managed locally
    }

    // FlashcardSet data class
    public static class FlashcardSet {
        public String id;
        public String name;
        public int cardCount;
        public List<FlashcardSetActivity.Flashcard> cards;

        public FlashcardSet(String id, String name, int cardCount) {
            this.id = id;
            this.name = name;
            this.cardCount = cardCount;
            this.cards = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCardCount() {
            return cardCount;
        }

        public List<FlashcardSetActivity.Flashcard> getCards() {
            return cards;
        }
    }

}