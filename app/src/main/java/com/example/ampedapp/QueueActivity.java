package com.example.ampedapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;

public class QueueActivity extends AppCompatActivity {

    private final HashMap<String, Integer> effectLayouts = new HashMap<>(); // Map effect names to layouts
    private LinearLayout effectContainer;
    private EffectManager effectManager;
    private final ArrayList<String> selectedEffects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.queue_page);

        effectContainer = findViewById(R.id.effectContainer);
        effectManager = EffectManager.getInstance();

        LinearLayout navAdd, navQueue, navSettings;
        ImageView addIcon, queueIcon, settingsIcon;
        TextView addIconText, queueIconText, settingsText;

        // NAVIGATION BAR
        // Navigation Items
        navAdd = findViewById(R.id.nav_add);
        navQueue = findViewById(R.id.nav_queue);
        navSettings = findViewById(R.id.nav_settings);
        addIcon = findViewById(R.id.add_icon);
        queueIcon = findViewById(R.id.queue_icon);
        settingsIcon = findViewById(R.id.settings_icon);
        addIconText = findViewById(R.id.add_icon_text);
        queueIconText = findViewById(R.id.queue_icon_text);
        settingsText = findViewById(R.id.settings_text);

        // Apply tint color to all icons permanently
        int grayColor = Color.parseColor("#9E9E9E");

        // Setting color for navbar ImageView
        addIcon.setColorFilter(grayColor);
        queueIcon.setColorFilter(Color.parseColor("#FF0000"));
        settingsIcon.setColorFilter(grayColor);

        // Setting color for navbar TextView
        addIconText.setTextColor(grayColor);
        queueIconText.setTextColor(Color.parseColor("#FF0000"));
        settingsText.setTextColor(grayColor);

        // Set click listeners
        navAdd.setOnClickListener(v -> openActivity(LandingPageActivity.class));
        navQueue.setOnClickListener(v -> openActivity(QueueActivity.class));
        navSettings.setOnClickListener(v -> openActivity(SettingsActivity.class));

        // Initialize effect layout mapping
        effectLayouts.put("Delay", R.layout.custom_button_delay_queue);
        effectLayouts.put("Reverb", R.layout.custom_button_reverb_queue);
        effectLayouts.put("Cleantone", R.layout.custom_button_cleantone_queue);
        effectLayouts.put("Distortion", R.layout.custom_button_distortion_queue);
        effectLayouts.put("Overdrive", R.layout.custom_button_overdrive_queue);

        // Change status bar color
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#16171B"));

        // Display effects in queue
        displayEffects();
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(QueueActivity.this, activityClass);
        intent.putStringArrayListExtra("selectedEffects", selectedEffects);
        startActivity(intent);
    }

    private void displayEffects() {
        effectContainer.removeAllViews(); // Clear previous views
        ArrayList<String> selectedEffects = effectManager.getSelectedEffects();

        for (String effect : selectedEffects) {
            if (effectLayouts.containsKey(effect)) {
                View effectView = getLayoutInflater().inflate(effectLayouts.get(effect), effectContainer, false);

                // Find "X" button in the effect layout
                ImageButton removeButton = effectView.findViewById(R.id.removeEffectButton);
                if (removeButton != null) {
                    removeButton.setOnClickListener(v -> removeEffect(effect, effectView));
                }

                effectContainer.addView(effectView);
            }
        }
    }

    private void removeEffect(String effect, View effectView) {
        // Remove effect from EffectManager
        effectManager.removeEffect(effect);

        // Remove effect view from UI
        effectContainer.removeView(effectView);

        // Update the file (if applicable)
        processAudioFileAfterRemoval();
    }

    private void processAudioFileAfterRemoval() {
        // TODO: Implement logic to update the audio file (e.g., reprocess with remaining effects)
        // Example:
        // effectManager.applyEffectsToAudio();
    }
}
