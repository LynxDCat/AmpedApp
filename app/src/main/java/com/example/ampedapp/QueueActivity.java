package com.example.ampedapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QueueActivity extends AppCompatActivity {

    private final HashMap<String, Integer> effectLayouts = new HashMap<>();
    private LinearLayout effectContainer;
    private EffectManager effectManager;
    private final ArrayList<String> selectedEffects = new ArrayList<>();
    private final ArrayList<String> effectsList = new ArrayList<>();
    private boolean isPlaying;
    private int currentEffectIndex;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice esp32Device;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isConnected = false;

    private final String ESP32_MAC = "CC:DB:A7:2D:AE:7A"; // Replace with your ESP32 address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.queue_page);

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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
            return;
        }

        effectContainer = findViewById(R.id.effectContainer);
        effectManager = EffectManager.getInstance();
        loadPlayerState();

        initEffectLayouts();
        initButtons();
        displayEffects();
    }

    private void initEffectLayouts() {
        effectLayouts.put("Delay", R.layout.custom_button_delay_queue);
        effectLayouts.put("Reverb", R.layout.custom_button_reverb_queue);
        effectLayouts.put("Cleantone", R.layout.custom_button_cleantone_queue);
        effectLayouts.put("Distortion", R.layout.custom_button_distortion_queue);
        effectLayouts.put("Overdrive", R.layout.custom_button_overdrive_queue);
    }

    private void initButtons() {
        Button clearBtn = findViewById(R.id.clearButton);
        Button saveBtn = findViewById(R.id.saveButton);
        Button uploadBtn = findViewById(R.id.uploadButton);
        ImageView playPauseBtn = findViewById(R.id.playPauseButton);
        ImageView prevBtn = findViewById(R.id.prevButton);
        ImageView nextBtn = findViewById(R.id.nextButton);

        clearBtn.setOnClickListener(v -> {
            effectManager.clearEffects();
            selectedEffects.clear();
            effectsList.clear();
            effectContainer.removeAllViews();
            currentEffectIndex = 0;
            isPlaying = false;
            savePlayerState();
        });

        saveBtn.setOnClickListener(v -> showSavePresetDialog());

        uploadBtn.setOnClickListener(v -> {
            if (effectsList.isEmpty()) {
                Toast.makeText(this, "No effects in queue to upload.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isConnected) {
                connectToESP32(() -> sendDataToESP32()); // Pass the Runnable onConnected correctly
            } else {
                sendDataToESP32();
            }
        });

        playPauseBtn.setOnClickListener(v -> togglePlayPause());
        prevBtn.setOnClickListener(v -> playPreviousEffect());
        nextBtn.setOnClickListener(v -> playNextEffect());
    }

    private void connectToESP32(Runnable onConnected) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return;
        }

        esp32Device = bluetoothAdapter.getRemoteDevice(ESP32_MAC);

        new Thread(() -> {
            try {
                bluetoothSocket = esp32Device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                isConnected = true;

                listenForDataFromESP32(); // ✅ Start listening for commands here

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Connected to ESP32", Toast.LENGTH_SHORT).show();
                    onConnected.run();
                });

            } catch (IOException e) {
                e.printStackTrace();
                isConnected = false;
                runOnUiThread(() -> {
                    Button uploadBtn = findViewById(R.id.uploadButton);
                    uploadBtn.performClick(); // Optional: retry logic
                });
            }
        }).start();
    }


    private void sendDataToESP32() {
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            Toast.makeText(this, "ESP32 is not connected. Reconnecting...", Toast.LENGTH_SHORT).show();
            connectToESP32(() -> sendDataToESP32()); // Ensure onConnected is passed properly
            return;
        }

        try {
            String effectName = effectsList.get(currentEffectIndex);  // Get the current effect
            JSONObject json = new JSONObject();
            json.put("command", effectName);
            json.put("value", "500"); // Or use a dynamic value if needed

            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(json.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            Toast.makeText(this, "Data sent to ESP32 for " + effectName, Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
        }
    }



    private void listenForDisconnection() {
        new Thread(() -> {
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                while (isConnected && bluetoothSocket.isConnected()) {
                    int read = inputStream.read(buffer);
                    if (read == -1) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                isConnected = false;
                runOnUiThread(() -> Toast.makeText(this, "Disconnected from ESP32", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // --- UI & Playback ---
    private void displayEffects() {
        effectContainer.removeAllViews();
        selectedEffects.clear();
        selectedEffects.addAll(effectManager.getSelectedEffects());
        effectsList.clear();
        effectsList.addAll(selectedEffects);

        for (int i = 0; i < selectedEffects.size(); i++) {
            String effect = selectedEffects.get(i);
            View view = getLayoutInflater().inflate(effectLayouts.get(effect), effectContainer, false);

            ImageButton removeButton = view.findViewById(R.id.removeEffectButton);
            TextView label = view.findViewById(R.id.effectLabel);

            int finalI = i;
            if (removeButton != null) {
                removeButton.setOnClickListener(v -> {
                    effectManager.removeEffect(effect);
                    selectedEffects.remove(effect);
                    effectsList.remove(effect);
                    displayEffects();
                });
            }

            if (label != null) {
                label.setText(i == currentEffectIndex && isPlaying
                        ? "🎵 Now Playing: " + effect
                        : effect);
                label.setTextColor(i == currentEffectIndex && isPlaying
                        ? Color.GREEN
                        : Color.WHITE);
            }

            effectContainer.addView(view);
        }
    }

    private void togglePlayPause() {
        if (effectsList.isEmpty()) return;
        if (isPlaying) pauseEffect(); else playEffect(currentEffectIndex);
        isPlaying = !isPlaying;
        savePlayerState();
    }

    private void playPreviousEffect() {
        if (currentEffectIndex > 0) currentEffectIndex--;
        playEffect(currentEffectIndex);
        isPlaying = true;
        sendDataToESP32();  // Send data after changing to previous effect
    }

    private void playNextEffect() {
        if (currentEffectIndex < effectsList.size() - 1) currentEffectIndex++;
        playEffect(currentEffectIndex);
        isPlaying = true;
        sendDataToESP32();  // Send data after changing to next effect
    }

    private void playEffect(int index) {
        displayEffects();
        savePlayerState();
    }

    private void pauseEffect() {
        displayEffects();
        savePlayerState();
    }

    private void showSavePresetDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setHint("Enter preset name");
        builder.setTitle("Save Preset").setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        String effectsCsv = android.text.TextUtils.join(",", selectedEffects);
                        PresetDatabaseHelper dbHelper = new PresetDatabaseHelper(this);
                        boolean success = dbHelper.savePreset(name, effectsCsv);
                        Toast.makeText(this, success ? "Preset saved!" : "Save failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void savePlayerState() {
        SharedPreferences sharedPreferences = getSharedPreferences("PlayerState", MODE_PRIVATE);
        sharedPreferences.edit()
                .putInt("currentEffectIndex", currentEffectIndex)
                .putBoolean("isPlaying", isPlaying)
                .apply();
    }

    private void loadPlayerState() {
        SharedPreferences sharedPreferences = getSharedPreferences("PlayerState", MODE_PRIVATE);
        currentEffectIndex = sharedPreferences.getInt("currentEffectIndex", 0);
        isPlaying = sharedPreferences.getBoolean("isPlaying", false);
    }

    private void listenForDataFromESP32() {
        new Thread(() -> {
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytes;

                while (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String received = new String(buffer, 0, bytes).trim();
                        runOnUiThread(() -> handleReceivedData(received));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleReceivedData(String data) {
        switch (data) {
            case "1":
                Toast.makeText(this, "Previous Effect", Toast.LENGTH_SHORT).show();
                playPreviousEffect();
                break;
            case "2":
                Toast.makeText(this, "Toggle Play/Pause", Toast.LENGTH_SHORT).show();
                togglePlayPause();
                break;
            case "3":
                Toast.makeText(this, "Next Effect", Toast.LENGTH_SHORT).show();
                playNextEffect();
                break;

            default:
                //Toast.makeText(this, "Unknown Command: " + data, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(QueueActivity.this, activityClass);
        startActivity(intent);
    }
}