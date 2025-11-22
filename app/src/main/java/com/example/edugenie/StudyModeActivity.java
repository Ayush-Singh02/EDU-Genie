package com.example.edugenie;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;


import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudyModeActivity extends AppCompatActivity {

    private TextView questionText, answerText, progressText;
    private Button showAnswerButton, nextButton, backButton;

    private String username, setId, setName;
    private List<FlashcardSetActivity.Flashcard> flashcards;
    private int currentIndex = 0;
    private boolean showingAnswer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_mode);

        // Get data from intent
        username = getIntent().getStringExtra("username");
        setId = getIntent().getStringExtra("setId");
        setName = getIntent().getStringExtra("setName");
        String flashcardsJson = getIntent().getStringExtra("flashcardsJson");
        if (username == null || setId == null || setName == null || flashcardsJson == null) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load flashcards from JSON
        loadFlashcardsFromJson(flashcardsJson);
    }

    private void initializeViews() {
        questionText = findViewById(R.id.questionText);
        answerText = findViewById(R.id.answerText);
        progressText = findViewById(R.id.progressText);
        showAnswerButton = findViewById(R.id.showAnswerButton);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        showAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAnswer();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextCard();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadFlashcardsFromJson(String flashcardsJson) {
        Type listType = new TypeToken<ArrayList<FlashcardSetActivity.Flashcard>>(){}.getType();
        flashcards = new Gson().fromJson(flashcardsJson, listType);
        Toast.makeText(this, "Loaded " + flashcards.size() + " cards", Toast.LENGTH_SHORT).show();
        shuffleFlashcards();
        if (!flashcards.isEmpty()) {
            displayCurrentCard();
        } else {
            Toast.makeText(StudyModeActivity.this, "No flashcards to study", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void shuffleFlashcards() {
        Random random = new Random();
        for (int i = flashcards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            FlashcardSetActivity.Flashcard temp = flashcards.get(i);
            flashcards.set(i, flashcards.get(j));
            flashcards.set(j, temp);
        }
    }

    private void displayCurrentCard() {
        if (flashcards.isEmpty() || currentIndex >= flashcards.size()) {
            return;
        }

        FlashcardSetActivity.Flashcard currentCard = flashcards.get(currentIndex);
        questionText.setText("Question: " + currentCard.getQuestion());
        answerText.setText("Answer: " + currentCard.getAnswer());
        answerText.setVisibility(View.GONE);

        progressText.setText((currentIndex + 1) + " / " + flashcards.size());
        showingAnswer = false;

        showAnswerButton.setText("Show Answer");
        nextButton.setEnabled(false);
    }

    private void toggleAnswer() {
        if (showingAnswer) {
            answerText.setVisibility(View.GONE);
            showAnswerButton.setText("Show Answer");
            nextButton.setEnabled(false);
            showingAnswer = false;
        } else {
            answerText.setVisibility(View.VISIBLE);
            showAnswerButton.setText("Hide Answer");
            nextButton.setEnabled(true);
            showingAnswer = true;
        }
    }

    private void nextCard() {
        currentIndex++;
        if (currentIndex >= flashcards.size()) {
            // Completed all cards
            Toast.makeText(this, "Study session completed! Great job!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            displayCurrentCard();
        }
    }
}