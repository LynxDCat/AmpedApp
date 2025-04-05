package com.example.ampedapp;

import java.util.List;

public class Preset {
    private String name;
    private List<String> effects;

    public Preset(String name, List<String> effects) {
        this.name = name;
        this.effects = effects;
    }

    public String getName() {
        return name;
    }

    public List<String> getEffects() {
        return effects;
    }
}
