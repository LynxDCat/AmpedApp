package com.example.ampedapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override   
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings_page);

        LinearLayout navAdd, navQueue, navPreset, navSettings;
        ImageView addIcon, queueIcon, presetIcon, settingsIcon;
        TextView addIconText, queueIconText, presetText, settingsText;

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
        presetIcon.setColorFilter(grayColor);
        settingsIcon.setColorFilter(Color.parseColor("#FF0000"));

        // Setting color for navbar TextView
        addIconText.setTextColor(grayColor);
        queueIconText.setTextColor(grayColor);
        presetText.setTextColor(grayColor);
        settingsText.setTextColor(Color.parseColor("#FF0000"));

        LinearLayout htuLayout = findViewById(R.id.htuLayout);

        htuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(SettingsActivity.this, activityClass);
        startActivity(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to close the application?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    //selectedEffects.clear();
                    clearPlayerState(); // Your method to clear SharedPreferences or queues
                    finishAffinity();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss(); // Dismisses the dialog
                })
                .setCancelable(true)
                .show();
    }

    private void clearPlayerState() {
        SharedPreferences preferences = getSharedPreferences("PlayerState", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
