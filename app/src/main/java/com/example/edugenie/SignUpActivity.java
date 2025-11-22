package com.example.edugenie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    EditText signupName, signupUsername, signupEmail, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);

        // Test Firebase connectivity
        testFirebaseConnection();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    registerUser();
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void testFirebaseConnection() {
        try {
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("test");
            
            reference.setValue("connection_test")
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Firebase connected successfully!");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Firebase connection failed: " + e.getMessage());
                });
        } catch (Exception e) {
            System.out.println("Firebase error: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        String name = signupName.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String username = signupUsername.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();

        if (name.isEmpty()) {
            signupName.setError("Name cannot be empty");
            return false;
        }

        if (email.isEmpty()) {
            signupEmail.setError("Email cannot be empty");
            return false;
        }

        if (username.isEmpty()) {
            signupUsername.setError("Username cannot be empty");
            return false;
        }

        if (password.isEmpty()) {
            signupPassword.setError("Password cannot be empty");
            return false;
        }

        if (password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void registerUser() {
        try {
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("users");

            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String username = signupUsername.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            HelperClass helperClass = new HelperClass(name, email, username, password);
            
            // Show loading message
            Toast.makeText(SignUpActivity.this, "Creating account...", Toast.LENGTH_SHORT).show();
            
            // Use username as the key for easier login lookup
            reference.child(username).setValue(helperClass)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    System.out.println("User registered: " + username);
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    getSharedPreferences("EduGeniePrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("loggedIn", true)
                            .putString("username", username)
                            .putString("name", name)
                            .putString("email", email)
                            .apply();

                    startActivity(intent);
                    finish(); // Close signup activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    System.out.println("Signup error: " + e.getMessage());
                });
        } catch (Exception e) {
            Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}