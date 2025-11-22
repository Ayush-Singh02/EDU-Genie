package com.example.edugenie;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.AlertDialog;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText, userNameText, flashcardCountText, chatCountText;
    private CardView aiChatCard, flashcardsCard, profileCard;

    private String userName, userEmail, userUsername;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private Button setReminderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply night mode setting
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Get user data from intent
        getUserData();

        // Set up click listeners
        setupClickListeners();

        // Load user stats
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        userNameText = findViewById(R.id.userNameText);
        flashcardCountText = findViewById(R.id.flashcardCountText);
        chatCountText = findViewById(R.id.chatCountText);
        aiChatCard = findViewById(R.id.aiChatCard);
        flashcardsCard = findViewById(R.id.flashcardsCard);
        profileCard = findViewById(R.id.profileCard);
    }

    private void getUserData() {
        Intent intent = getIntent();
        SharedPreferences prefs = getSharedPreferences("EduGeniePrefs", MODE_PRIVATE);

        // Try to get from Intent first
        userName = intent.getStringExtra("name");
        userEmail = intent.getStringExtra("email");
        userUsername = intent.getStringExtra("username");

        // Fallback to SharedPreferences
        if (userName == null) userName = prefs.getString("name", null);
        if (userEmail == null) userEmail = prefs.getString("email", null);
        if (userUsername == null) userUsername = prefs.getString("username", null);

        // If still null, session expired → redirect to login
        if (userName == null || userUsername == null || userEmail == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            prefs.edit().clear().apply();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        // Set UI
        welcomeText.setText("Welcome, " + userName + "!");
        userNameText.setText("Your AI Study Buddy");
    }


    private void setupClickListeners() {
        // AI Chat Card
        aiChatCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AIChatActivity.class);
                intent.putExtra("username", userUsername);
                startActivity(intent);
            }
        });

        // Flashcards Card
        flashcardsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FlashcardsActivity.class);
                intent.putExtra("username", userUsername);
                startActivity(intent);
            }
        });

        // Profile Card
        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("name", userName);
                intent.putExtra("email", userEmail);
                intent.putExtra("username", userUsername);
                startActivity(intent);
            }
        });
    }
}