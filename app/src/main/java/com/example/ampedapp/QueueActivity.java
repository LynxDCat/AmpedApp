package com.example.ampedapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class QueueActivity extends AppCompatActivity {

    private final ArrayList<String> selectedEffects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.queue_page);

        LinearLayout navAdd, navQueue;
        ImageButton btnRemoveDelay, btnRemoveReverb, btnRemoveCleantone, btnRemoveDistortion, btnRemoveOverdrive;
        ImageView addIcon, queueIcon;
        TextView addIconText, queueIconText;

        // NAVIGATION BAR
        // Navigation Items
        navAdd = findViewById(R.id.nav_add);
        navQueue = findViewById(R.id.nav_queue);
        addIcon = findViewById(R.id.add_icon);
        queueIcon = findViewById(R.id.queue_icon);
        addIconText = findViewById(R.id.add_icon_text);
        queueIconText = findViewById(R.id.queue_icon_text);
        btnRemoveDelay = findViewById(R.id.remove_icon_delay);
        btnRemoveReverb = findViewById(R.id.remove_icon_reverb);
        btnRemoveCleantone = findViewById(R.id.remove_icon_cleantone);
        btnRemoveDistortion = findViewById(R.id.remove_icon_distortion);
        btnRemoveOverdrive = findViewById(R.id.remove_icon_overdrive);

        // Apply tint color to all icons permanently
        int grayColor = Color.parseColor("#9E9E9E");

        // Setting color for navbar ImageView
        addIcon.setColorFilter(grayColor);
        queueIcon.setColorFilter(Color.parseColor("#FF0000"));

        // Setting color for navbar TextView
        addIconText.setTextColor(grayColor);
        queueIconText.setTextColor(Color.parseColor("#FF0000"));

        // Set click listeners
        navAdd.setOnClickListener(v -> {
            openActivity(LandingPageActivity.class);
        });
        navQueue.setOnClickListener(v -> {
            openActivity(QueueActivity.class);
        });

        btnRemoveDelay.setOnClickListener(v -> {
            ArrayList<String> selectedEffects = EffectManager.getInstance().getSelectedEffects();

            if (selectedEffects.contains("Delay")) {
                selectedEffects.remove("Delay");
                EffectManager.getInstance().setSelectedEffects(selectedEffects);
                findViewById(R.id.button_delay).setVisibility(View.GONE);
            }
        });

        btnRemoveReverb.setOnClickListener(v -> {
            ArrayList<String> selectedEffects = EffectManager.getInstance().getSelectedEffects();

            if (selectedEffects.contains("Reverb")) {
                selectedEffects.remove("Reverb");
                EffectManager.getInstance().setSelectedEffects(selectedEffects);
                findViewById(R.id.button_reverb).setVisibility(View.GONE);
            }
        });

        btnRemoveCleantone.setOnClickListener(v -> {
            ArrayList<String> selectedEffects = EffectManager.getInstance().getSelectedEffects();

            if (selectedEffects.contains("Cleantone")) {
                selectedEffects.remove("Cleantone");
                EffectManager.getInstance().setSelectedEffects(selectedEffects);
                findViewById(R.id.button_cleantone).setVisibility(View.GONE);
            }
        });

        btnRemoveDistortion.setOnClickListener(v -> {
            ArrayList<String> selectedEffects = EffectManager.getInstance().getSelectedEffects();

            if (selectedEffects.contains("Distortion")) {
                selectedEffects.remove("Distortion");
                EffectManager.getInstance().setSelectedEffects(selectedEffects);
                findViewById(R.id.button_distortion).setVisibility(View.GONE);
            }
        });

        btnRemoveOverdrive.setOnClickListener(v -> {
            ArrayList<String> selectedEffects = EffectManager.getInstance().getSelectedEffects();

            if (selectedEffects.contains("Overdrive")) {
                selectedEffects.remove("Overdrive");
                EffectManager.getInstance().setSelectedEffects(selectedEffects);
                findViewById(R.id.button_overdrive).setVisibility(View.GONE);
            }
        });


        // Change status bar color
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#16171B"));

        // Get data from intent
        ArrayList<String> selectedEffects = EffectManager.getInstance().getSelectedEffects();

        if (selectedEffects != null) {

            for(int i = 0; i < selectedEffects.size(); i++){
                switch (selectedEffects.get(i)){
                    case "Delay": {
                        findViewById(R.id.button_delay).setVisibility(View.VISIBLE);
                        break;
                    } case "Reverb": {
                        findViewById(R.id.button_reverb).setVisibility(View.VISIBLE);
                        break;
                    } case "Cleantone": {
                        findViewById(R.id.button_cleantone).setVisibility(View.VISIBLE);
                        break;
                    } case "Distortion": {
                        findViewById(R.id.button_distortion).setVisibility(View.VISIBLE);
                        break;
                    } case "Overdrive": {
                        findViewById(R.id.button_overdrive).setVisibility(View.VISIBLE);
                        break;
                    } default: {
                        break;
                    }
                }
            }
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(QueueActivity.this, activityClass);
        intent.putStringArrayListExtra("selectedEffects", selectedEffects);
        startActivity(intent);
    }
}
