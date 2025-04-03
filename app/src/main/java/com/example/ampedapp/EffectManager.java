package com.example.ampedapp;

import java.util.ArrayList;

public class EffectManager {
    private static final EffectManager instance = new EffectManager();
    private final ArrayList<String> selectedEffects = new ArrayList<>();

    private EffectManager() { } // Private constructor

    public static EffectManager getInstance() {
        return instance;
    }

    // Allow multiple and repeated effects
    public void addEffect(String effect) {
        selectedEffects.add(effect);
    }

    public ArrayList<String> getSelectedEffects() {
        return new ArrayList<>(selectedEffects); // Return a copy to prevent modification outside
    }

    public void setSelectedEffects(ArrayList<String> effects) {
        selectedEffects.clear();  // Clear existing list
        selectedEffects.addAll(effects); // Add all new effects
    }

    // Optional: Remove an effect (all occurrences)
    public void removeEffect(String effect) {
        selectedEffects.remove(effect);
        //applyEffectsToAudio(); // Reprocess the file after removal
    }

    // Optional: Clear all effects
    public void clearEffects() {
        selectedEffects.clear();
    }


}
