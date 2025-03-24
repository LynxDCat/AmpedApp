package com.example.ampedapp;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private LinearLayout buttonDelay, dropdownDelay, buttonReverb, buttonDistortion;
    private ImageButton playButton;
    private SeekBar seekBar;
    private Button addButton;
    private MediaPlayer mediaPlayer;
    private MediaPlayer delayedMediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private boolean delayEnabled = false;
    private int delayTimeMs = 300; // Default delay time in milliseconds
    private float delayFeedback = 0.5f; // Default feedback amount (0-1)
    private RadioGroup delayOptionsGroup;
    private ExecutorService executorService;

    private LinearLayout navFeed, navSearch, navRecord, navTracks, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.landing_page);

        // Initialize executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Find Views by ID
        buttonDelay = findViewById(R.id.button_delay);
        buttonDistortion = findViewById(R.id.button_distortion);
        dropdownDelay = findViewById(R.id.dropdown_delay);
        buttonReverb = findViewById(R.id.button_reverb);
        playButton = findViewById(R.id.play_button);
        seekBar = findViewById(R.id.seekbar);
        addButton = findViewById(R.id.add_button);
        delayOptionsGroup = findViewById(R.id.delay_options_group);

        // Find Navbar IDs from bottom_navbar.xml
        // These should match the IDs in your bottom_navbar.xml
        View bottomNavbar = findViewById(R.id.bottomNav);
        navFeed = bottomNavbar.findViewById(R.id.nav_feed);
        navSearch = bottomNavbar.findViewById(R.id.nav_search);
        navRecord = bottomNavbar.findViewById(R.id.nav_record);
        navTracks = bottomNavbar.findViewById(R.id.nav_tracks);
        navProfile = bottomNavbar.findViewById(R.id.nav_profile);

        // Initialize MediaPlayer with sample audio
        mediaPlayer = MediaPlayer.create(this, R.raw.sample);

        // Toggle Dropdown on Delay Button Click
        buttonDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dropdownDelay.getVisibility() == View.GONE) {
                    dropdownDelay.setVisibility(View.VISIBLE);
                } else {
                    dropdownDelay.setVisibility(View.GONE);
                }
            }
        });

        // Toggle Dropdown on Delay Button Click
        buttonDistortion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dropdownDelay.getVisibility() == View.GONE) {
                    dropdownDelay.setVisibility(View.VISIBLE);
                } else {
                    dropdownDelay.setVisibility(View.GONE);
                }
            }
        });

        // Toggle Dropdown on Delay Button Click
        buttonDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dropdownDelay.getVisibility() == View.GONE) {
                    dropdownDelay.setVisibility(View.VISIBLE);
                } else {
                    dropdownDelay.setVisibility(View.GONE);
                }
            }
        });

        // Set up delay options radio group
        setupDelayOptions();

        // Apply Effects on Button Click
        addButton.setOnClickListener(v -> {
            delayEnabled = true;
            applyDelayEffect();
            dropdownDelay.setVisibility(View.GONE);
        });

        buttonReverb.setOnClickListener(v -> applyReverbEffect());

        // Set Click Listeners for Navbar
        setupNavbar();

        // Play/Pause Audio on Play Button Click
        playButton.setOnClickListener(v -> {
            if (!isPlaying) {
                if (delayEnabled) {
                    playAudioWithDelay();
                } else {
                    playAudio();
                }
            } else {
                pauseAudio();
            }
        });

        // SeekBar Change Listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    if (delayedMediaPlayer != null) {
                        delayedMediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupDelayOptions() {
        delayOptionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Get selected radio button
            RadioButton selectedOption = findViewById(checkedId);
            String option = selectedOption.getText().toString();

            if (option.contains("Short")) {
                delayTimeMs = 150;
                delayFeedback = 0.3f;
            } else if (option.contains("Medium")) {
                delayTimeMs = 300;
                delayFeedback = 0.5f;
            } else if (option.contains("Long")) {
                delayTimeMs = 500;
                delayFeedback = 0.7f;
            }

            Toast.makeText(MainActivity.this, "Selected " + option, Toast.LENGTH_SHORT).show();
        });
    }

    // Method to Play Audio
    private void playAudio() {
        mediaPlayer.start();
        isPlaying = true;
        playButton.setImageResource(R.raw.pause_icon);
        seekBar.setMax(mediaPlayer.getDuration());

        // Update SeekBar in Real-Time
        handler.postDelayed(updateSeekBar, 1000);
    }

    // Method to Play Audio with Delay Effect
    private void playAudioWithDelay() {
        Toast.makeText(this, "Playing with " + delayTimeMs + "ms delay", Toast.LENGTH_SHORT).show();

        // Start original playback
        mediaPlayer.start();

        // Recursive Echo Effect using Handler
        handler.postDelayed(new Runnable() {
            int echoCount = 5; // Number of echoes
            int currentEcho = 1;

            @Override
            public void run() {
                if (currentEcho <= echoCount && isPlaying) {
                    if (delayedMediaPlayer != null) {
                        delayedMediaPlayer.release();
                    }
                    delayedMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.sample);
                    delayedMediaPlayer.setVolume(delayFeedback, delayFeedback);
                    delayedMediaPlayer.start();

                    currentEcho++;
                    handler.postDelayed(this, delayTimeMs); // Repeat for next echo
                }
            }
        }, delayTimeMs);

        isPlaying = true;
        playButton.setImageResource(R.raw.pause_icon);
        seekBar.setMax(mediaPlayer.getDuration());

        // Update SeekBar in Real-Time
        handler.postDelayed(updateSeekBar, 1000);
    }


    // Method to Pause Audio
    private void pauseAudio() {
        mediaPlayer.pause();
        if (delayedMediaPlayer != null) {
            delayedMediaPlayer.pause();
        }
        isPlaying = false;
        playButton.setImageResource(R.raw.play_icon);
        handler.removeCallbacks(updateSeekBar);
    }

    private void applyDelayEffect() {
        Toast.makeText(this, "Delay Effect Ready (" + delayTimeMs + "ms)", Toast.LENGTH_SHORT).show();

        // For more advanced implementations, we could use AudioTrack or external libraries
        // But for demonstration, we'll use the simple multi-player approach

        // Create a copy of the original MediaPlayer for delayed playback
        if (delayedMediaPlayer != null) {
            delayedMediaPlayer.release();
        }
        delayedMediaPlayer = MediaPlayer.create(this, R.raw.sample);
    }

    private void applyReverbEffect() {
        Toast.makeText(this, "Reverb Effect Applied", Toast.LENGTH_SHORT).show();
        // Placeholder for actual reverb DSP processing
    }

    // For more advanced implementations: processing audio with real-time delay
    private void processAudioWithDelay(String inputPath, String outputPath) {
        executorService.execute(() -> {
            try {
                // Read the input audio file (assuming it's a raw PCM file)
                FileInputStream fis = new FileInputStream(inputPath);
                FileOutputStream fos = new FileOutputStream(outputPath);

                // Sample rate and bits per sample for calculations
                int sampleRate = 44100; // Standard sample rate
                int bytesPerSample = 2; // 16-bit audio = 2 bytes per sample

                // Calculate delay buffer size
                int delayBufferSize = (int)(delayTimeMs * sampleRate / 1000) * bytesPerSample;
                byte[] delayBuffer = new byte[delayBufferSize];

                // Buffer for reading
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Fill delay buffer with zeros initially
                for (int i = 0; i < delayBufferSize; i++) {
                    delayBuffer[i] = 0;
                }

                // Process the audio with delay
                int delayBufferPos = 0;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    // Process each sample with delay
                    for (int i = 0; i < bytesRead; i += bytesPerSample) {
                        // Get the current sample (16-bit, little endian)
                        short currentSample = ByteBuffer.wrap(buffer, i, bytesPerSample)
                                .order(ByteOrder.LITTLE_ENDIAN).getShort();

                        // Get the delayed sample
                        short delayedSample = ByteBuffer.wrap(delayBuffer, delayBufferPos, bytesPerSample)
                                .order(ByteOrder.LITTLE_ENDIAN).getShort();

                        // Mix current and delayed sample
                        short mixedSample = (short)(currentSample + (delayedSample * delayFeedback));

                        // Write the mixed sample to output
                        ByteBuffer mixedBuffer = ByteBuffer.allocate(bytesPerSample)
                                .order(ByteOrder.LITTLE_ENDIAN).putShort(mixedSample);
                        fos.write(mixedBuffer.array());

                        // Update delay buffer with current sample
                        ByteBuffer currentBuffer = ByteBuffer.allocate(bytesPerSample)
                                .order(ByteOrder.LITTLE_ENDIAN).putShort(currentSample);

                        delayBuffer[delayBufferPos] = currentBuffer.array()[0];
                        delayBuffer[delayBufferPos + 1] = currentBuffer.array()[1];

                        // Update delay buffer position
                        delayBufferPos = (delayBufferPos + bytesPerSample) % delayBufferSize;
                    }
                }

                fis.close();
                fos.close();

                // Notify processing complete
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Audio processing complete!", Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Error processing audio: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Runnable to Update SeekBar
    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (delayedMediaPlayer != null) {
            delayedMediaPlayer.stop();
            delayedMediaPlayer.release();
            delayedMediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
        executorService.shutdown();
    }

    private void setupNavbar() {
        if (navFeed != null) {
            navFeed.setOnClickListener(view ->
                    Toast.makeText(MainActivity.this, "Feed Clicked", Toast.LENGTH_SHORT).show()
            );
        }

        if (navSearch != null) {
            navSearch.setOnClickListener(view ->
                    Toast.makeText(MainActivity.this, "Search Clicked", Toast.LENGTH_SHORT).show()
            );
        }

        if (navRecord != null) {
            navRecord.setOnClickListener(view ->
                    Toast.makeText(MainActivity.this, "Record Clicked", Toast.LENGTH_SHORT).show()
            );
        }

        if (navTracks != null) {
            navTracks.setOnClickListener(view ->
                    Toast.makeText(MainActivity.this, "My Tracks Clicked", Toast.LENGTH_SHORT).show()
            );
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(view ->
                    Toast.makeText(MainActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show()
            );
        }
    }
}