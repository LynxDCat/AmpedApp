package com.example.ampedapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QueueActivity extends AppCompatActivity {

    private ArrayList<String> selectedEffects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.queue_page);

        LinearLayout navAdd, navQueue;

        // NAVIGATION BAR
        // Navigation Items
        navAdd = findViewById(R.id.nav_add);
        navQueue = findViewById(R.id.nav_queue);
        // Set click listeners
        navAdd.setOnClickListener(v -> openActivity(LandingPageActivity.class));
        navQueue.setOnClickListener(v -> openActivity(QueueActivity.class));

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
