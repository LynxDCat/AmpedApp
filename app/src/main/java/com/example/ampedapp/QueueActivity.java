package com.example.ampedapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class QueueActivity extends AppCompatActivity {

    private final HashMap<String, Integer> effectLayouts = new HashMap<>();
    private LinearLayout effectContainer;
    private EffectManager effectManager;

    private final ArrayList<String> selectedEffects = new ArrayList<>();
    private boolean isPlaying;
    private int currentEffectIndex;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice esp32Device;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ArrayList<String> effectsList = new ArrayList<>();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.queue_page);

        effectContainer = findViewById(R.id.effectContainer);
        effectManager = EffectManager.getInstance();

        loadPlayerState();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
        }

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
        Button uploadButton = findViewById(R.id.uploadButton);
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

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToESP32();
                /*if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    try {
                        String jsonData = "{\"command\": \"blink\", \"value\": \"500\"}";
                        OutputStream outputStream = bluetoothSocket.getOutputStream();
                        outputStream.write(jsonData.getBytes());
                        outputStream.flush();
                        Toast.makeText(getApplicationContext(), "Data sent to ESP32", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed to send data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Bluetooth not connected to ESP32", Toast.LENGTH_SHORT).show();
                }*/
            }
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

    public void connectToESP32() {
        if (bluetoothAdapter.isEnabled()) {
            // Replace with the MAC address of your ESP32 device
            String esp32Address = "EC:64:C9:5E:CD:D6"; // Replace with your ESP32's Bluetooth address
            esp32Device = bluetoothAdapter.getRemoteDevice(esp32Address);

            new Thread(new Runnable() {
                @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                @Override
                public void run() {
                    try {
                        bluetoothSocket = esp32Device.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();

                        // If connection is successful, display Toast
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Connected to ESP32", Toast.LENGTH_SHORT).show();
                                sendDataToESP32();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        // If connection fails, display Toast
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Connection Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        } else {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendDataToESP32() {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                String jsonData = "{\"command\": \"blink\", \"value\": \"500\"}";
                OutputStream outputStream = bluetoothSocket.getOutputStream();

                // Write the data to the OutputStream
                outputStream.write(jsonData.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();

                // Notify user that data has been sent
                Toast.makeText(getApplicationContext(), "Data sent to ESP32", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not connected to ESP32", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to send data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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