package com.example.ampedapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class PresetActivity extends AppCompatActivity {

    private LinearLayout presetContainer;
    private EffectManager effectManager;
    private PresetDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.preset_page);

        LinearLayout navAdd, navQueue, navPreset, navSettings;
        ImageView addIcon, queueIcon, presetIcon, settingsIcon;
        TextView addIconText, queueIconText, presetText, settingsText;
        ImageButton removeButton;

        // NAVIGATION BAR
        // Navigation Items
        navAdd = findViewById(R.id.nav_add);
        navQueue = findViewById(R.id.nav_queue);
        navPreset = findViewById(R.id.nav_preset);
        navSettings = findViewById(R.id.nav_settings);

        addIcon = findViewById(R.id.add_icon);
        queueIcon = findViewById(R.id.queue_icon);
        presetIcon = findViewById(R.id.preset_icon);
        settingsIcon = findViewById(R.id.settings_icon);

        addIconText = findViewById(R.id.add_icon_text);
        queueIconText = findViewById(R.id.queue_icon_text);
        presetText = findViewById(R.id.preset_icon_text);
        settingsText = findViewById(R.id.settings_text);

        // Set click listeners
        navAdd.setOnClickListener(v -> openActivity(LandingPageActivity.class));
        navQueue.setOnClickListener(v -> openActivity(QueueActivity.class));
        navPreset.setOnClickListener(v -> openActivity(PresetActivity.class));
        navSettings.setOnClickListener(v -> openActivity(SettingsActivity.class));

        // Apply tint color to all icons permanently
        int grayColor = Color.parseColor("#9E9E9E");

        // Setting color for navbar ImageView
        addIcon.setColorFilter(grayColor);
        queueIcon.setColorFilter(grayColor);
        presetIcon.setColorFilter(Color.parseColor("#FF0000"));
        settingsIcon.setColorFilter(grayColor);

        // Setting color for navbar TextView
        addIconText.setTextColor(grayColor);
        queueIconText.setTextColor(grayColor);
        presetText.setTextColor(Color.parseColor("#FF0000"));
        settingsText.setTextColor(grayColor); // END OF NAVIGATION BAR

        presetContainer = findViewById(R.id.presetContainer);
        effectManager = EffectManager.getInstance();
        dbHelper = new PresetDatabaseHelper(this);

        loadPresets();
    }

    private void loadPresets() {
        ArrayList<Preset> presets = dbHelper.getAllPresets();

        for (Preset preset : presets) {
            View presetView = getLayoutInflater().inflate(R.layout.preset_item_layout, presetContainer, false);

            TextView presetName = presetView.findViewById(R.id.presetNameText);
            Button useButton = presetView.findViewById(R.id.usePresetButton);
            ImageButton removeButton = presetView.findViewById(R.id.removeToDBButton); // You should have this in your layout

            presetName.setText(preset.getName());

            useButton.setOnClickListener(v -> {
                effectManager.setSelectedEffects(new ArrayList<>(preset.getEffects()));
                Intent intent = new Intent(PresetActivity.this, QueueActivity.class);
                startActivity(intent);
            });

            removeButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(PresetActivity.this);
                builder.setTitle("Delete Preset");

                final EditText input = new EditText(PresetActivity.this);
                input.setHint("Type \"" + preset.getName() + "\" to confirm");
                builder.setView(input);

                builder.setPositiveButton("Delete", (dialog, which) -> {
                    String enteredName = input.getText().toString().trim();
                    if (enteredName.equals(preset.getName())) {
                        boolean deleted = dbHelper.deletePreset(preset.getName());
                        if (deleted) {
                            Toast.makeText(PresetActivity.this, "Preset deleted", Toast.LENGTH_SHORT).show();
                            presetContainer.removeView(presetView); // Remove this specific view
                        } else {
                            Toast.makeText(PresetActivity.this, "Failed to delete preset", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PresetActivity.this, "Name did not match", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            });

            presetContainer.addView(presetView);
        }
    }


    private void applyEffectsToQueue(List<String> effects) {
        EffectManager.getInstance().setSelectedEffects(new ArrayList<>(effects));
        Intent intent = new Intent(PresetActivity.this, QueueActivity.class);
        startActivity(intent);
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(PresetActivity.this, activityClass);
        startActivity(intent);
    }
}
