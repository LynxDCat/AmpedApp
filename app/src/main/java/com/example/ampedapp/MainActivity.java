package com.example.ampedapp;

import android.media.MediaPlayer;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private LinearLayout buttonDelay, dropdownDelay, buttonReverb, buttonDistortion;
    private ImageButton playButton;
    private SeekBar seekBar;
    private Button addButton;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private boolean delayEnabled = false;
    private int delayTimeMs = 300;
    private float delayFeedback = 0.5f;
    private RadioGroup delayOptionsGroup;
    private ExecutorService executorService;
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.landing_page);

        executorService = Executors.newSingleThreadExecutor();

        buttonDelay = findViewById(R.id.button_delay);
        buttonDistortion = findViewById(R.id.button_distortion);
        dropdownDelay = findViewById(R.id.dropdown_delay);
        buttonReverb = findViewById(R.id.button_reverb);
        playButton = findViewById(R.id.play_button);
        seekBar = findViewById(R.id.seekbar);
        addButton = findViewById(R.id.add_button);
        delayOptionsGroup = findViewById(R.id.delay_options_group);

        mediaPlayer = MediaPlayer.create(this, R.raw.delay_sample);

        buttonDelay.setOnClickListener(v -> toggleDropdown(dropdownDelay));
        buttonDistortion.setOnClickListener(v -> toggleDropdown(dropdownDelay));

        setupDelayOptions();
        addButton.setOnClickListener(v -> {
            delayEnabled = true;
            Toast.makeText(this, "Delay Effect Applied", Toast.LENGTH_SHORT).show();
            dropdownDelay.setVisibility(View.GONE);
        });
        buttonReverb.setOnClickListener(v -> Toast.makeText(this, "Reverb Effect Applied", Toast.LENGTH_SHORT).show());

        playButton.setOnClickListener(v -> playPauseAudio());
        initializeSeekBar();
    }

    private void toggleDropdown(View dropdown) {
        dropdown.setVisibility(dropdown.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    private void setupDelayOptions() {
        delayOptionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
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

    private void playPauseAudio() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.delay_sample);
        }

        if (isPlaying) {
            mediaPlayer.pause();
            playButton.setImageResource(R.raw.play_icon);
            handler.removeCallbacks(updateSeekBar);
        } else {
            mediaPlayer.start();
            playButton.setImageResource(R.raw.pause_icon);
            seekBar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(updateSeekBar, 1000);
        }

        isPlaying = !isPlaying;
    }

    private void initializeSeekBar() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 1000);
                }
            }
        };

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
        executorService.shutdown();
    }
}