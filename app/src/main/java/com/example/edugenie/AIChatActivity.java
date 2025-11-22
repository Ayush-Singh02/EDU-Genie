package com.example.edugenie;

import android.media.MediaRouter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.concurrent.Executor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class AIChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private Button backButton;
    private TextView titleText;

    private String username;
    private DatabaseReference databaseReference;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        FirebaseApp.initializeApp(this);

        username = getIntent().getStringExtra("username");
        if (username == null) {
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        initializeAI();
        initializeViews();
        setupChat();
        setupClickListeners();
        loadChatHistory();
        // Set up send button
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void initializeAI() {
        // AI logic temporarily removed
    }

    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);

        titleText.setText("AI Chat ");
    }

    private void setupChat() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, username);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        ChatMessage welcome = new ChatMessage("AI", "Hello! I'm your AI assistant powered by Gemini 2.5 Flash. I'm here to help you with any questions or tasks you might have. How can I assist you today?", getCurrentTimestamp(), false);
        chatMessages.add(welcome);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> finish());

        // Allow sending message with Enter key
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;

        ChatMessage userMsg = new ChatMessage(username, message, getCurrentTimestamp(), true);
        chatMessages.add(userMsg);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);

        messageInput.setText("");
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        saveMessageToFirebase(userMsg);
        getAIResponse(message); // Call the basic chatbot after user message
    }

    // Class-level executor if doing background work (not touching UI)
    private final Executor executor = Executors.newSingleThreadExecutor();

    private void getAIResponse(String userMessage) {
        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.0-flash");
        GenerativeModelFutures model = GenerativeModelFutures.from(ai);

        Content prompt = new Content.Builder()
                .addText(userMessage)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);

        // If addAIMessage updates the UI, use main executor
        Executor mainExecutor = ContextCompat.getMainExecutor(this);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                addAIMessage(resultText); // Be careful: must run on UI thread
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, mainExecutor); // 🔁 switch to `executor` if UI not involved
    }

    private SpannableStringBuilder formatAIResponse(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new SpannableStringBuilder("");

        // Basic cleanup
        raw = raw.trim()
                .replaceAll("\\n{2,}", "\n\n")                  // Limit newlines
                .replaceAll(" +", " ")                          // Collapse spaces
                .replaceAll("(?m)^- ", "• ")                    // Dash to bullet
                .replaceAll("(?m)^\\* ", "• ")                  // Star to bullet
                .replaceAll("(?m)^\\d+\\. ", "🔹 ");            // Numbers to bullet icon

        // Start applying spans
        SpannableStringBuilder formatted = new SpannableStringBuilder();
        Pattern boldPattern = Pattern.compile("\\*\\*(.+?)\\*\\*"); // Match **bold**
        Matcher matcher = boldPattern.matcher(raw);

        int lastEnd = 0;
        while (matcher.find()) {
            // Add non-bold text before match
            formatted.append(raw.substring(lastEnd, matcher.start()));

            // Add bold section
            String boldText = matcher.group(1); // Get text inside ** **
            int start = formatted.length();
            formatted.append(boldText);         // Append clean bold text
            int end = formatted.length();

            // Apply styling
            formatted.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            formatted.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            lastEnd = matcher.end();
        }

        // Add remaining normal text
        if (lastEnd < raw.length()) {
            formatted.append(raw.substring(lastEnd));
        }

        return formatted;
    }




    private void addAIMessage(String message) {
        ChatMessage aiMsg = new ChatMessage("AI", message, getCurrentTimestamp(), false);
        chatMessages.add(aiMsg);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        saveMessageToFirebase(aiMsg);
    }


    private void saveMessageToFirebase(ChatMessage message) {
        try {
            String messageId = databaseReference.child("users").child(username).child("chatHistory").push().getKey();
            if (messageId != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("sender", message.getSender());
                data.put("message", message.getMessage());
                data.put("timestamp", message.getTimestamp());
                data.put("isUser", message.isUser());
                databaseReference.child("users").child(username).child("chatHistory").child(messageId).setValue(data);
            }
        } catch (Exception e) {
            // Log error but don't crash the app
            e.printStackTrace();
        }
    }

    private void loadChatHistory() {
        databaseReference.child("users").child(username).child("chatHistory")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatMessages.clear();
                    chatMessages.add(new ChatMessage("AI", "Hello! I'm your AI assistant powered by Gemini 2.5 Flash. I'm here to help you with any questions or tasks you might have. How can I assist you today?", getCurrentTimestamp(), false));

                    for (DataSnapshot msg : snapshot.getChildren()) {
                        String sender = msg.child("sender").getValue(String.class);
                        String content = msg.child("message").getValue(String.class);
                        String timestamp = msg.child("timestamp").getValue(String.class);
                        Boolean isUser = msg.child("isUser").getValue(Boolean.class);

                        if (sender != null && content != null && timestamp != null && isUser != null) {
                            chatMessages.add(new ChatMessage(sender, content, timestamp, isUser));
                        }
                    }

                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AIChatActivity.this, "Error loading chat history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static class ChatMessage {
        private String sender;
        private String message;
        private String timestamp;
        private boolean isUser;

        public ChatMessage(String sender, String message, String timestamp, boolean isUser) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
            this.isUser = isUser;
        }

        public String getSender() { return sender; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public boolean isUser() { return isUser; }
    }
}
