package com.example.edugenie;

import android.content.Context;
import android.content.SharedPreferences;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.TypedValue;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;




public class HelperClass {

    String name, email, username, password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HelperClass(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public HelperClass() {
    }

    /**
     * Utility to resolve a theme color attribute to its actual color value.
     */
    public static int getThemeColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private static final String FLASHCARD_PREFS = "flashcard_prefs";
    private static final String FLASHCARD_SETS_KEY = "flashcard_sets";

    public static Map<String, FlashcardsActivity.FlashcardSet> loadFlashcardSets(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(FLASHCARD_PREFS, Context.MODE_PRIVATE);
        String json = prefs.getString(FLASHCARD_SETS_KEY, null);
        if (json == null) return new HashMap<>();
        Type type = new TypeToken<Map<String, FlashcardsActivity.FlashcardSet>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void saveFlashcardSets(Context context, Map<String, FlashcardsActivity.FlashcardSet> sets) {
        SharedPreferences prefs = context.getSharedPreferences(FLASHCARD_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(sets);
        editor.putString(FLASHCARD_SETS_KEY, json);
        editor.apply();
    }

    public static List<FlashcardSetActivity.Flashcard> loadCardsForSet(Context context, String setId) {
        Map<String, FlashcardsActivity.FlashcardSet> sets = loadFlashcardSets(context);
        FlashcardsActivity.FlashcardSet set = sets.get(setId);
        if (set != null && set.cards != null) {
            return set.cards;
        }
        return new ArrayList<>();
    }

    public static void saveCardsForSet(Context context, String setId, List<FlashcardSetActivity.Flashcard> cards) {
        Map<String, FlashcardsActivity.FlashcardSet> sets = loadFlashcardSets(context);
        FlashcardsActivity.FlashcardSet set = sets.get(setId);
        if (set != null) {
            set.cards = cards;
            set.cardCount = cards.size();
            saveFlashcardSets(context, sets);
        }
    }
}