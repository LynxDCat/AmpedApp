package com.example.ampedapp;

import java.util.ArrayList;

public class EffectManager {
    private static final EffectManager instance = new EffectManager();
    private final ArrayList<String> selectedEffects = new ArrayList<>();
    private final ArrayList<String> selectedEffectsValue = new ArrayList<>();
    private int currentPlayingIndex = -1;
    private boolean isPlaying = false;

    private EffectManager() { } // Private constructor

    public static EffectManager getInstance() {
        return instance;
    }

    // Allow multiple and repeated effects
    public void addEffect(String effect) {
        selectedEffects.add(effect);
    }

    public void addValue(String value){
        selectedEffectsValue.add(value);
    }


    public ArrayList<String> getSelectedEffects() {
        return new ArrayList<>(selectedEffects); // Return a copy to prevent modification outside
    }

    public ArrayList<String> getSelectedEffectsValue() {
        return new ArrayList<>(selectedEffectsValue); // Return a copy to prevent modification
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

    public int getCurrentPlayingIndex() {
        return currentPlayingIndex;
    }

    public void setCurrentPlayingIndex(int index) {
        this.currentPlayingIndex = index;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }


}
