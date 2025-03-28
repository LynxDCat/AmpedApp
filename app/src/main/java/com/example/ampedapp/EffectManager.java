package com.example.ampedapp;

import java.util.ArrayList;

public class EffectManager {
    private static final EffectManager instance = new EffectManager();
    private ArrayList<String> selectedEffects = new ArrayList<>();

    private EffectManager() { } // Private constructor

    public static EffectManager getInstance() {
        return instance;
    }

    public void addEffect(String effect) {
        if (!selectedEffects.contains(effect)) {
            selectedEffects.add(effect);
        }
    }

    public ArrayList<String> getSelectedEffects() {
        return selectedEffects;
    }
}
