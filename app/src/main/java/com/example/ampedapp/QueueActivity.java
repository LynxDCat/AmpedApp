package com.example.ampedapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
    private final ArrayList<String> effectsListValue = new ArrayList<>();
    private boolean isPlaying;
    private int currentEffectIndex;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice esp32Device;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isConnected = false;

    private final String ESP32_MAC = "CC:DB:A7:2D:AE:7A"; // Main ESP32 address

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
        effectLayouts.put("Clean", R.layout.custom_button_cleantone_queue); // Updated from Cleantone
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
                connectToESP32(() -> sendDataToESP32());
            } else {
                sendDataToESP32();
            }
        });

        playPauseBtn.setOnClickListener(v ->{
            togglePlayPause();
            Log.d("PlayButtons", "Play/Pause button clicked");
        });
        prevBtn.setOnClickListener(v -> {
            playPreviousEffect();
            String effectName = effectsList.get(currentEffectIndex);
            String value = effectsListValue.get(currentEffectIndex);
            Log.d("PlayButtons",  effectName + " " + value);
            Log.d("PlayButtons", "Previous button clicked");
        });
        nextBtn.setOnClickListener(v -> {
            playNextEffect();
            String effectName = effectsList.get(currentEffectIndex);
            String value = effectsListValue.get(currentEffectIndex);
            Log.d("PlayButtons",  effectName + " " + value);
            Log.d("PlayButtons", "Next button clicked");
        });
    }

    private void connectToESP32(Runnable onConnected) {
        // Check if Bluetooth is available and enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        esp32Device = bluetoothAdapter.getRemoteDevice(ESP32_MAC); // Get ESP32 device by MAC address

        // Start a background thread for Bluetooth connection
        new Thread(() -> {
            try {
                // Create Bluetooth socket using a valid service UUID
                bluetoothSocket = esp32Device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                isConnected = true;

                listenForDataFromESP32();  // Start listening for data after connection

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Connected to ESP32", Toast.LENGTH_SHORT).show();
                    onConnected.run();  // Run the callback after connection is successful
                });

            } catch (IOException e) {
                e.printStackTrace();
                isConnected = false;

                // Show a more detailed error message
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to connect to ESP32. Please try again.", Toast.LENGTH_LONG).show();
                    Log.e("QueueActivity", "Bluetooth connection failed: " + e.getMessage());

                    // Optional: Retry the connection by clicking the upload button again
                    Button uploadBtn = findViewById(R.id.uploadButton);
                    uploadBtn.performClick();
                });
            }
        }).start();
    }


    private void sendDataToESP32() {
        // Ensure the Bluetooth socket is connected before sending data
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            Toast.makeText(this, "ESP32 is not connected. Reconnecting...", Toast.LENGTH_SHORT).show();
            connectToESP32(() -> sendDataToESP32());
            return;
        }

        // Validate currentEffectIndex
        if (effectsList.isEmpty() || currentEffectIndex < 0 || currentEffectIndex >= effectsList.size()) {
            //Log.d("QueueActivity", "currentEffectIndex: " + currentEffectIndex + ", effectsList size: " + effectsList.size());
            Toast.makeText(this, "Invalid effect index or empty effects list", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Now it's safe to get the effect from the list
            String effectName = effectsList.get(currentEffectIndex);
            String value = effectsListValue.get(currentEffectIndex);
            //Log.d("QueueActivity", "currentEffectIndex: " + currentEffectIndex + ", effectsList size: " + effectsList.size());
            JSONObject json = new JSONObject();
            json.put("command", effectName);
            json.put("value", value);
            //togglePlayPause();
            if (effectsList.isEmpty()) return;

            if (!isPlaying) {
                // If no effect is playing, this is the first time an effect is being uploaded and played
                if (currentEffectIndex == 0) {
                    // Mark the first effect as "Now Playing"
                    isPlaying = true;
                    playEffect(currentEffectIndex); // Start playing the first effect
                    displayEffects();  // Update the UI to show "Now Playing"
                }
            } else {
                // If an effect is already playing, pause it
                pauseEffect();
            }
            savePlayerState();

            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write((json.toString() + "\n").getBytes(StandardCharsets.UTF_8)); // Send the command
            outputStream.flush();

            //Toast.makeText(this, "Sent " + effectName + " to ESP32", Toast.LENGTH_SHORT).show();
            Log.d("EffectSent", "Sent " + effectName + " to ESP32");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
        }
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
                runOnUiThread(() -> {
                    isConnected = false;
                    Toast.makeText(getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show();
                });
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
                // Ignore unknown commands
                break;
        }
    }

    private void displayEffects() {
        effectContainer.removeAllViews(); // Clear existing effects
        selectedEffects.clear();
        selectedEffects.addAll(effectManager.getSelectedEffects()); // Get selected effects
        effectsListValue.clear();
        effectsListValue.addAll(effectManager.getSelectedEffectsValue());
        effectsList.clear();
        effectsList.addAll(selectedEffects); // Update the effects list

        // Ensure currentEffectIndex is within valid bounds
        if (currentEffectIndex >= effectsList.size()) {
            currentEffectIndex = effectsList.size() - 1; // Reset to the last valid index
        }

        for (int i = 0; i < selectedEffects.size(); i++) {
            String effect = selectedEffects.get(i);
            View view = getLayoutInflater().inflate(effectLayouts.get(effect), effectContainer, false);

            ImageButton removeButton = view.findViewById(R.id.removeEffectButton);
            TextView label = view.findViewById(R.id.effectLabel);

            if (removeButton != null) {
                // Set remove button listener
                removeButton.setOnClickListener(v -> {
                    effectManager.removeEffect(effect);  // Remove effect from manager
                    selectedEffects.remove(effect);  // Remove from selected effects
                    effectsList.remove(effect);  // Remove from effects list
                    displayEffects(); // Re-update the effect list
                });
            }

            if (label != null) {
                if (i == currentEffectIndex && isPlaying) {
                    label.setText("🎵 Now Playing: " + effect);
                    label.setTextColor(Color.GREEN);
                } else if (i == 0) {
                    label.setText(effect);  // Just the effect name
                    label.setTextColor(Color.WHITE);  // White color for the first item if not playing
                } else {
                    label.setText(effect);
                    label.setTextColor(Color.WHITE);  // Default white color
                }
            }

            effectContainer.addView(view);  // Add the view to the container
        }
    }



    private void togglePlayPause() {

        JSONObject json = new JSONObject();
        try {
            json.put("command", "Clean");
            json.put("value", "500");
            Log.d("EffectSent", "Sent Cleantone to ESP32");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void playPreviousEffect() {
        if (currentEffectIndex > 0) currentEffectIndex--;
        playEffect(currentEffectIndex);
        isPlaying = true;
        sendDataToESP32();
    }

    private void playNextEffect() {
        if (currentEffectIndex < effectsList.size() - 1) currentEffectIndex++;
        playEffect(currentEffectIndex);
        isPlaying = true;
        sendDataToESP32();
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

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(QueueActivity.this, activityClass);
        startActivity(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to close the application?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    //clearPlayerState(); // Your method to clear SharedPreferences or queues
                    effectManager.clearEffects();
                    selectedEffects.clear();
                    effectsList.clear();
                    effectContainer.removeAllViews();
                    currentEffectIndex = 0;
                    isPlaying = false;
                    //savePlayerState();
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