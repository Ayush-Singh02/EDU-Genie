package com.example.edugenie;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private List<AIChatActivity.ChatMessage> chatMessages;
    private String currentUsername;

    public ChatAdapter(List<AIChatActivity.ChatMessage> chatMessages, String currentUsername) {
        this.chatMessages = chatMessages;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        AIChatActivity.ChatMessage message = chatMessages.get(position);
        holder.messageText.setText(message.getMessage());
        holder.timestampText.setText(message.getTimestamp());

        if (message.isUser()) {
            holder.messageText.setText(message.getMessage());
        } else {
            holder.messageText.setText(formatAIResponse(message.getMessage())); // apply bold + big
        }

    }

    private SpannableStringBuilder formatAIResponse(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new SpannableStringBuilder();

        raw = raw.trim()
                .replaceAll("\\n{3,}", "\n\n")            // Normalize excess newlines
                .replaceAll("(?m)^- ", "• ")              // Dash bullets
                .replaceAll("(?m)^\\* ", "• ")            // Star bullets
                .replaceAll("(?m)^\\d+\\. ", "🔹 ");      // Number bullets

        SpannableStringBuilder builder = new SpannableStringBuilder();

        // Regex to match: ```lang\n<code>\n```
        Pattern codeBlockPattern = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```");
        Matcher matcher = codeBlockPattern.matcher(raw);

        int lastEnd = 0;

        while (matcher.find()) {
            // Add normal text before code block and format bold
            String beforeCode = raw.substring(lastEnd, matcher.start());
            builder.append(applyBoldFormatting(beforeCode));

            // Capture code content
            String codeContent = matcher.group(2).trim(); // get only the code
            int start = builder.length();
            builder.append(codeContent);
            int end = builder.length();

            // Apply styles for code block
            builder.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BackgroundColorSpan(0xFFEFEFEF), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new ForegroundColorSpan(0xFF222222), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.95f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            lastEnd = matcher.end();
        }

        // Append remaining non-code content
        if (lastEnd < raw.length()) {
            builder.append(applyBoldFormatting(raw.substring(lastEnd)));
        }

        return builder;
    }

    private SpannableStringBuilder applyBoldFormatting(String text) {
        SpannableStringBuilder result = new SpannableStringBuilder();
        Pattern boldPattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
        Matcher matcher = boldPattern.matcher(text);

        int last = 0;
        while (matcher.find()) {
            result.append(text.substring(last, matcher.start()));
            String boldText = matcher.group(1);
            int start = result.length();
            result.append(boldText);
            int end = result.length();
            result.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            result.setSpan(new RelativeSizeSpan(1.2f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            last = matcher.end();
        }

        if (last < text.length()) {
            result.append(text.substring(last));
        }

        return result;
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        AIChatActivity.ChatMessage message = chatMessages.get(position);
        return message.isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText, senderText;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
            senderText = itemView.findViewById(R.id.senderText);
        }
    }
}