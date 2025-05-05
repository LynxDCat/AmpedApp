package com.example.ampedapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.help_options);

        LinearLayout htaeLayout = findViewById(R.id.htaeLayout);
        LinearLayout sdpLayout = findViewById(R.id.sdpLayout);
        LinearLayout cauLayout = findViewById(R.id.cauLayout);

        htaeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(HelpActivity.this, HTAE.class);
            startActivity(intent);
        });

        sdpLayout.setOnClickListener(v -> {
            Intent intent = new Intent(HelpActivity.this, SDP.class);
            startActivity(intent);
        });

        cauLayout.setOnClickListener(v -> {
            Intent intent = new Intent(HelpActivity.this, CAU.class);
            startActivity(intent);
        });
    }
}
