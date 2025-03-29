package com.example.ampedapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        // Change status bar color
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#16171B"));

        // Declaration of Buttons
        Button login;

        // Intialization of Buttons
        login = findViewById(R.id.Guest);

        // Click listeners
        login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LandingPageActivity.class);
            startActivity(intent);
        });

    }


}
