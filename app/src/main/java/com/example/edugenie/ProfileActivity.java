package com.example.edugenie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameText, emailText, usernameText;
    private Button backButton, editProfileButton, logoutButton;
    private Switch nightModeSwitch;

    private String userName, userEmail, userUsername;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load night mode preference BEFORE calling super.onCreate
        sharedPreferences = getSharedPreferences("EduGeniePrefs", MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("nightMode", false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getUserData();
        initializeViews();
        setupClickListeners();

        // Set Switch state to match preference
        nightModeSwitch.setChecked(isNightMode);
    }

    private void getUserData() {
        Intent intent = getIntent();

        userName = intent.getStringExtra("name");
        userEmail = intent.getStringExtra("email");
        userUsername = intent.getStringExtra("username");

        if (userName == null) userName = sharedPreferences.getString("name", null);
        if (userEmail == null) userEmail = sharedPreferences.getString("email", null);
        if (userUsername == null) userUsername = sharedPreferences.getString("username", null);

        if (userName == null || userUsername == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void initializeViews() {
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        usernameText = findViewById(R.id.usernameText);
        backButton = findViewById(R.id.backButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
        nightModeSwitch = findViewById(R.id.nightModeSwitch);

        if (userName != null) nameText.setText(userName);
        if (userEmail != null) emailText.setText(userEmail);
        if (userUsername != null) usernameText.setText(userUsername);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        editProfileButton.setOnClickListener(v ->
                Toast.makeText(ProfileActivity.this, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show());

        logoutButton.setOnClickListener(v -> {
            sharedPreferences.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean currentMode = sharedPreferences.getBoolean("nightMode", false);
            if (currentMode == isChecked) return; // prevent unnecessary loop

            sharedPreferences.edit().putBoolean("nightMode", isChecked).apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            // Restart activity to apply theme safely
            startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
            finish();
        });

    }
}

