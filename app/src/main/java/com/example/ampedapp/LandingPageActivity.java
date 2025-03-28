package com.example.ampedapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LandingPageActivity extends AppCompatActivity {

    private LinearLayout dropdownDelay, dropdownReverb, dropdownCleantone, dropdownDistortion,
            dropdownOverdrive;
    private ImageButton playButton, playButtonReverb, playButtonCleantone, playButtonDistortion, playButtonOverdrive;
    private SeekBar seekBarDelay, seekBarReverb, seekBarCleantone, seekBarDistortion, seekBarOverdrive;
    private MediaPlayer mediaPlayerDelay, mediaPlayerReverb,  mediaPlayerCleantone, mediaPlayerDistortion, mediaPlayerOverdrive;
    private Handler handler = new Handler();
    private boolean isPlayingDelay = false, isPlayingReverb = false, isPlayingCleantone = false, isPlayingDistortion = false, isPlayingOverdrive = false;
    private boolean delayEnabled = false;
    private int delayTimeMs = 300;
    private float delayFeedback = 0.5f;
    private ExecutorService executorService;
    private Runnable updateSeekBarDelay, updateSeekBarReverb, updateSeekBarCleantone, updateSeekBarDistortion, updateSeekBarOverdrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.landing_page);

        executorService = Executors.newSingleThreadExecutor();
        LinearLayout navAdd, navQueue, buttonDelay, buttonReverb, buttonCleantone, buttonDistortion, buttonOverdrive;

        // NAVIGATION BAR
        // Navigation Items
        navAdd = findViewById(R.id.nav_add);
        navQueue = findViewById(R.id.nav_queue);
        // Set click listeners
        navAdd.setOnClickListener(v -> openActivity(MainActivity.class));
        navQueue.setOnClickListener(v -> openActivity(QueueActivity.class));

        buttonDelay = findViewById(R.id.button_delay);
        buttonReverb = findViewById(R.id.button_reverb);
        buttonCleantone = findViewById(R.id.button_cleantone);
        buttonDistortion = findViewById(R.id.button_distortion);
        buttonOverdrive = findViewById(R.id.button_overdrive);
        dropdownDelay = findViewById(R.id.dropdown_delay);
        dropdownReverb = findViewById(R.id.dropdown_reverb);
        dropdownCleantone = findViewById(R.id.dropdown_cleantone);
        dropdownDistortion = findViewById(R.id.dropdown_distortion);
        dropdownOverdrive = findViewById(R.id.dropdown_overdrive);
        playButton = findViewById(R.id.play_button);
        playButtonReverb = findViewById(R.id.play_button_reverb);
        playButtonCleantone = findViewById(R.id.play_button_cleantone);
        playButtonDistortion = findViewById(R.id.play_button_distortion);
        playButtonOverdrive = findViewById(R.id.play_button_overdrive);
        seekBarDelay = findViewById(R.id.seekbar);
        seekBarReverb = findViewById(R.id.seekbar_reverb);
        seekBarCleantone = findViewById(R.id.seekbar_cleantone);
        seekBarDistortion = findViewById(R.id.seekbar_distortion);
        seekBarOverdrive = findViewById(R.id.seekbar_overdrive);

        Button addButton = findViewById(R.id.add_button);

        // Initialize separate MediaPlayers
        mediaPlayerDelay = MediaPlayer.create(this, R.raw.delay_sample);
        mediaPlayerReverb = MediaPlayer.create(this, R.raw.reverb_sample);
        mediaPlayerCleantone = MediaPlayer.create(this, R.raw.sample); // change this
        mediaPlayerDistortion = MediaPlayer.create(this, R.raw.distortion_sample);
        mediaPlayerOverdrive = MediaPlayer.create(this, R.raw.sample); // change this

        buttonDelay.setOnClickListener(v -> toggleDropdown(dropdownDelay));
        buttonReverb.setOnClickListener(v -> toggleDropdown(dropdownReverb));
        buttonCleantone.setOnClickListener(v -> toggleDropdown(dropdownCleantone));
        buttonDistortion.setOnClickListener(v -> toggleDropdown(dropdownDistortion));
        buttonOverdrive.setOnClickListener(v -> toggleDropdown(dropdownOverdrive));

        addButton.setOnClickListener(v -> {
            delayEnabled = true;
            Toast.makeText(this, "Delay Effect Applied", Toast.LENGTH_SHORT).show();
            dropdownDelay.setVisibility(View.GONE);
        });

        playButton.setOnClickListener(v -> playPauseAudioDelay());
        playButtonReverb.setOnClickListener(v -> playPauseAudioReverb());
        playButtonCleantone.setOnClickListener(v -> playPauseAudioCleantone());
        playButtonDistortion.setOnClickListener(v -> playPauseAudioDistortion());
        playButtonOverdrive.setOnClickListener(v -> playPauseAudioOverdrive());

        initializeSeekBarDelay();
        initializeSeekBarReverb();
        initializeSeekBarCleantone();
        initializeSeekBarDistortion();
        initializeSeekBarOverdrive();
    }

    private void toggleDropdown(View dropdown) {
        dropdown.setVisibility(dropdown.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    // PLAY and PAUSE
    @SuppressLint("ResourceType")
    private void playPauseAudioDelay() {
        if (mediaPlayerDelay == null) {
            mediaPlayerDelay = MediaPlayer.create(this, R.raw.delay_sample);
        }

        if (isPlayingDelay) {
            mediaPlayerDelay.pause();
            playButton.setImageResource(R.raw.play_icon);
            handler.removeCallbacks(updateSeekBarDelay);
        } else {
            // Stop other media players if necessary
            stopAllMediaPlayersExcept(mediaPlayerDelay);

            mediaPlayerDelay.start();
            playButton.setImageResource(R.raw.pause_icon);
            seekBarDelay.setMax(mediaPlayerDelay.getDuration());

            handler.postDelayed(updateSeekBarDelay, 1000);
        }

        isPlayingDelay = !isPlayingDelay;
    }


    @SuppressLint("ResourceType")
    private void playPauseAudioReverb() {
        if (mediaPlayerReverb == null) {
            mediaPlayerReverb = MediaPlayer.create(this, R.raw.reverb_sample);
        }

        if (isPlayingReverb) {
            mediaPlayerReverb.pause();
            playButtonReverb.setImageResource(R.raw.play_icon);
            handler.removeCallbacks(updateSeekBarReverb);
        } else {
            mediaPlayerReverb.start();
            playButtonReverb.setImageResource(R.raw.pause_icon);
            seekBarReverb.setMax(mediaPlayerReverb.getDuration());
            handler.postDelayed(updateSeekBarReverb, 1000);
        }

        isPlayingReverb = !isPlayingReverb;
    }

    @SuppressLint("ResourceType")
    private void playPauseAudioCleantone() {
        if (mediaPlayerCleantone== null) {
            mediaPlayerCleantone = MediaPlayer.create(this, R.raw.sample);
        }

        if (isPlayingCleantone) {
            mediaPlayerCleantone.pause();
            playButtonCleantone.setImageResource(R.raw.play_icon);
            handler.removeCallbacks(updateSeekBarCleantone);
        } else {
            mediaPlayerCleantone.start();
            playButtonCleantone.setImageResource(R.raw.pause_icon);
            seekBarCleantone.setMax(mediaPlayerCleantone.getDuration());
            handler.postDelayed(updateSeekBarCleantone, 1000);
        }

        isPlayingCleantone = !isPlayingCleantone;
    }

    @SuppressLint("ResourceType")
    private void playPauseAudioDistortion() {
        if (mediaPlayerDistortion == null) {
            mediaPlayerDistortion = MediaPlayer.create(this, R.raw.distortion_sample);
        }

        if (isPlayingDistortion) {
            mediaPlayerDistortion.pause();
            playButtonDistortion.setImageResource(R.raw.play_icon);
            handler.removeCallbacks(updateSeekBarDistortion);
        } else {
            mediaPlayerDistortion.start();
            playButtonDistortion.setImageResource(R.raw.pause_icon);
            seekBarDistortion.setMax(mediaPlayerDistortion.getDuration());
            handler.postDelayed(updateSeekBarDistortion, 1000);
        }

        isPlayingDistortion = !isPlayingDistortion;
    }

    @SuppressLint("ResourceType")
    private void playPauseAudioOverdrive() {
        if (mediaPlayerOverdrive == null) {
            mediaPlayerOverdrive = MediaPlayer.create(this, R.raw.sample);
        }

        if (isPlayingOverdrive) {
            mediaPlayerOverdrive.pause();
            playButtonOverdrive.setImageResource(R.raw.play_icon);
            handler.removeCallbacks(updateSeekBarOverdrive);
        } else {
            mediaPlayerOverdrive.start();
            playButtonOverdrive.setImageResource(R.raw.pause_icon);
            seekBarOverdrive.setMax(mediaPlayerOverdrive.getDuration());
            handler.postDelayed(updateSeekBarOverdrive, 1000);
        }

        isPlayingOverdrive = !isPlayingOverdrive;
    }


    private void initializeSeekBarDelay() {
        updateSeekBarDelay = () -> {
            if (mediaPlayerDelay != null && mediaPlayerDelay.isPlaying()) {
                seekBarDelay.setProgress(mediaPlayerDelay.getCurrentPosition());
                handler.postDelayed(updateSeekBarDelay, 1000);
            }
        };
    }

    private void initializeSeekBarReverb() {
        updateSeekBarReverb = () -> {
            if (mediaPlayerReverb != null && mediaPlayerReverb.isPlaying()) {
                seekBarReverb.setProgress(mediaPlayerReverb.getCurrentPosition());
                handler.postDelayed(updateSeekBarReverb, 1000);
            }
        };
    }

    private void initializeSeekBarCleantone() {
        updateSeekBarCleantone = () -> {
            if (mediaPlayerCleantone != null && mediaPlayerCleantone.isPlaying()) {
                seekBarCleantone.setProgress(mediaPlayerCleantone.getCurrentPosition());
                handler.postDelayed(updateSeekBarCleantone, 1000);
            }
        };
    }

    private void initializeSeekBarDistortion() {
        updateSeekBarDistortion = () -> {
            if (mediaPlayerDistortion != null && mediaPlayerDistortion.isPlaying()) {
                seekBarDistortion.setProgress(mediaPlayerDistortion.getCurrentPosition());
                handler.postDelayed(updateSeekBarDistortion, 1000);
            }
        };
    }

    private void initializeSeekBarOverdrive() {
        updateSeekBarOverdrive = () -> {
            if (mediaPlayerOverdrive != null && mediaPlayerOverdrive.isPlaying()) {
                seekBarOverdrive.setProgress(mediaPlayerOverdrive.getCurrentPosition());
                handler.postDelayed(updateSeekBarOverdrive, 1000);
            }
        };
    }

    private void stopAllMediaPlayersExcept(MediaPlayer current) {
        MediaPlayer[] players = { mediaPlayerDelay, mediaPlayerReverb, mediaPlayerCleantone, mediaPlayerDistortion, mediaPlayerOverdrive };

        for (MediaPlayer player : players) {
            if (player != null && player != current && player.isPlaying()) {
                player.pause();
            }
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(LandingPageActivity.this, activityClass);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerDelay != null) {
            mediaPlayerDelay.stop();
            mediaPlayerDelay.release();
            mediaPlayerDelay = null;
        }
        if (mediaPlayerDistortion != null) {
            mediaPlayerDistortion.stop();
            mediaPlayerDistortion.release();
            mediaPlayerDistortion = null;
        }
        if (mediaPlayerReverb != null) {
            mediaPlayerReverb.stop();
            mediaPlayerReverb.release();
            mediaPlayerReverb = null;
        }
        if (mediaPlayerCleantone != null) {
            mediaPlayerCleantone.stop();
            mediaPlayerCleantone.release();
            mediaPlayerCleantone = null;
        }
        if (mediaPlayerOverdrive != null) {
            mediaPlayerOverdrive.stop();
            mediaPlayerOverdrive.release();
            mediaPlayerOverdrive = null;
        }
        handler.removeCallbacks(updateSeekBarCleantone);
        handler.removeCallbacks(updateSeekBarOverdrive);
        handler.removeCallbacks(updateSeekBarDelay);
        handler.removeCallbacks(updateSeekBarDistortion);
        handler.removeCallbacks(updateSeekBarReverb);
        executorService.shutdown();
    }
}
