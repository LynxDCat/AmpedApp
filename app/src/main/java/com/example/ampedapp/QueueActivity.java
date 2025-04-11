package com.example.ampedapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class QueueActivity extends AppCompatActivity {

    private final HashMap<String, Integer> effectLayouts = new HashMap<>();
    private LinearLayout effectContainer;
    private EffectManager effectManager;

    private final ArrayList<String> selectedEffects = new ArrayList<>();
    private boolean isPlaying;
    private int currentEffectIndex;

    private ArrayList<String> effectsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.queue_page);

        effectContainer = findViewById(R.id.effectContainer);
        effectManager = EffectManager.getInstance();

        loadPlayerState();

        ImageView playPauseBtn, prevBtn, nextBtn;

        // NAVIGATION BAR
        LinearLayout navAdd = findViewById(R.id.nav_add);
        LinearLayout navQueue = findViewById(R.id.nav_queue);
        LinearLayout navPreset = findViewById(R.id.nav_preset);
        LinearLayout navSettings = findViewById(R.id.nav_settings);

        ImageView addIcon = findViewById(R.id.add_icon);
        ImageView queueIcon = findViewById(R.id.queue_icon);
        ImageView presetIcon = findViewById(R.id.preset_icon);
        ImageView settingsIcon = findViewById(R.id.settings_icon);

        TextView addIconText = findViewById(R.id.add_icon_text);
        TextView queueIconText = findViewById(R.id.queue_icon_text);
        TextView presetText = findViewById(R.id.preset_icon_text);
        TextView settingsText = findViewById(R.id.settings_text);

        int grayColor = Color.parseColor("#9E9E9E");

        addIcon.setColorFilter(grayColor);
        queueIcon.setColorFilter(Color.parseColor("#FF0000"));
        presetIcon.setColorFilter(grayColor);
        settingsIcon.setColorFilter(grayColor);

        addIconText.setTextColor(grayColor);
        queueIconText.setTextColor(Color.parseColor("#FF0000"));
        presetText.setTextColor(grayColor);
        settingsText.setTextColor(grayColor);

        navAdd.setOnClickListener(v -> openActivity(LandingPageActivity.class));
        navQueue.setOnClickListener(v -> openActivity(QueueActivity.class));
        navPreset.setOnClickListener(v -> openActivity(PresetActivity.class));
        navSettings.setOnClickListener(v -> openActivity(SettingsActivity.class));

        effectLayouts.put("Delay", R.layout.custom_button_delay_queue);
        effectLayouts.put("Reverb", R.layout.custom_button_reverb_queue);
        effectLayouts.put("Cleantone", R.layout.custom_button_cleantone_queue);
        effectLayouts.put("Distortion", R.layout.custom_button_distortion_queue);
        effectLayouts.put("Overdrive", R.layout.custom_button_overdrive_queue);

        Button clearBtn = findViewById(R.id.clearButton);
        Button savePresetButton = findViewById(R.id.saveButton);
        playPauseBtn = findViewById(R.id.playPauseButton);
        prevBtn = findViewById(R.id.prevButton);
        nextBtn = findViewById(R.id.nextButton);

        clearBtn.setOnClickListener(v -> {
            effectManager.clearEffects();
            selectedEffects.clear();
            effectsList.clear();
            effectContainer.removeAllViews();
            currentEffectIndex = 0;
            isPlaying = false;
            savePlayerState();
        });

        playPauseBtn.setOnClickListener(v -> togglePlayPause());
        prevBtn.setOnClickListener(v -> playPreviousEffect());
        nextBtn.setOnClickListener(v -> playNextEffect());

        savePresetButton.setOnClickListener(v -> showSavePresetDialog());

        displayEffects();
    }

    private void openActivity(Class<?> activityClass) {
        savePlayerState();
        Intent intent = new Intent(QueueActivity.this, activityClass);
        intent.putStringArrayListExtra("selectedEffects", selectedEffects);
        startActivity(intent);
    }

    private void displayEffects() {
        effectContainer.removeAllViews();
        selectedEffects.clear();
        selectedEffects.addAll(effectManager.getSelectedEffects());
        effectsList.clear();
        effectsList.addAll(selectedEffects);

        for (int i = 0; i < selectedEffects.size(); i++) {
            String effect = selectedEffects.get(i);

            if (effectLayouts.containsKey(effect)) {
                View effectView = getLayoutInflater().inflate(effectLayouts.get(effect), effectContainer, false);

                ImageButton removeButton = effectView.findViewById(R.id.removeEffectButton);
                if (removeButton != null) {
                    removeButton.setOnClickListener(v -> removeEffect(effect, effectView));
                }

                TextView label = effectView.findViewById(R.id.effectLabel);
                if (label != null) {
                    if (i == currentEffectIndex && isPlaying) {
                        label.setText("\uD83C\uDFB5 Now Playing: " + effect);
                        label.setTextColor(Color.GREEN);
                    } else {
                        label.setText(effect);
                        label.setTextColor(Color.WHITE);
                    }
                }

                effectContainer.addView(effectView);
            }
        }
    }

    private void removeEffect(String effect, View effectView) {
        effectManager.removeEffect(effect);
        effectContainer.removeView(effectView);

        selectedEffects.remove(effect);
        effectsList.remove(effect);

        if (currentEffectIndex >= effectsList.size()) {
            currentEffectIndex = Math.max(0, effectsList.size() - 1);
        }

        processAudioFileAfterRemoval();
        savePlayerState();
    }

    private void showSavePresetDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Save Preset");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter preset name");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String presetName = input.getText().toString().trim();
            if (!presetName.isEmpty()) {
                String effectsCsv = android.text.TextUtils.join(",", selectedEffects);
                PresetDatabaseHelper dbHelper = new PresetDatabaseHelper(this);
                boolean success = dbHelper.savePreset(presetName, effectsCsv);
                Toast.makeText(this, success ? "Preset saved!" : "Failed to save preset.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Preset name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void togglePlayPause() {
        if (effectsList.isEmpty()) {
            Toast.makeText(this, "No effects to play.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPlaying) {
            pauseEffect();
        } else {
            playEffect(currentEffectIndex);
        }
        isPlaying = !isPlaying;
        savePlayerState();
    }

    private void playPreviousEffect() {
        if (currentEffectIndex > 0) {
            currentEffectIndex--;
            playEffect(currentEffectIndex);
            isPlaying = true;
        }
    }

    private void playNextEffect() {
        if (currentEffectIndex < effectsList.size() - 1) {
            currentEffectIndex++;
            playEffect(currentEffectIndex);
            isPlaying = true;
        }
    }

    private void playEffect(int index) {
        for (int i = 0; i < effectContainer.getChildCount(); i++) {
            View child = effectContainer.getChildAt(i);
            TextView label = child.findViewById(R.id.effectLabel);
            if (label != null) {
                if (i == index) {
                    label.setText("\uD83C\uDFB5 Now Playing: " + effectsList.get(i));
                    label.setTextColor(Color.GREEN);
                } else {
                    label.setText(effectsList.get(i));
                    label.setTextColor(Color.WHITE);
                }
            }
        }
        savePlayerState();
    }

    private void pauseEffect() {
        for (int i = 0; i < effectContainer.getChildCount(); i++) {
            View child = effectContainer.getChildAt(i);
            TextView label = child.findViewById(R.id.effectLabel);
            if (label != null) {
                label.setText(effectsList.get(i));
                label.setTextColor(Color.WHITE);
            }
        }
        savePlayerState();
    }

    private void savePlayerState() {
        SharedPreferences sharedPreferences = getSharedPreferences("PlayerState", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentEffectIndex", currentEffectIndex);
        editor.putBoolean("isPlaying", isPlaying);
        editor.apply();
    }

    private void loadPlayerState() {
        SharedPreferences sharedPreferences = getSharedPreferences("PlayerState", MODE_PRIVATE);
        currentEffectIndex = sharedPreferences.getInt("currentEffectIndex", 0);
        isPlaying = sharedPreferences.getBoolean("isPlaying", false);
    }

    private void processAudioFileAfterRemoval() {
        // TODO: Implement logic to update audio file after removal
    }
}