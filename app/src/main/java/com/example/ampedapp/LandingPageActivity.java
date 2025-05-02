package com.example.ampedapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LandingPageActivity extends AppCompatActivity {

    private LinearLayout dropdownDelay, dropdownReverb, dropdownCleantone, dropdownDistortion,
            dropdownOverdrive;
    private ImageButton playButton, playButtonReverb, playButtonCleantone, playButtonDistortion, playButtonOverdrive;
    private SeekBar seekBarDelay, seekBarReverb, seekBarCleantone, seekBarDistortion, seekBarOverdrive;
    private MediaPlayer mediaPlayerDelay, mediaPlayerReverb,  mediaPlayerCleantone, mediaPlayerDistortion, mediaPlayerOverdrive;
    private final Handler handler = new Handler();
    private boolean isPlayingDelay = false, isPlayingReverb = false, isPlayingCleantone = false, isPlayingDistortion = false, isPlayingOverdrive = false;
    private ExecutorService executorService;
    private Runnable updateSeekBarDelay, updateSeekBarReverb, updateSeekBarCleantone, updateSeekBarDistortion, updateSeekBarOverdrive;

    private final ArrayList<String> selectedEffects = new ArrayList<>();

    Boolean isOpenDropDelay = false, isOpenDropReverb = false, isOpenDropCleantone = false, isOpenDropDistortion = false, isOpenDropOverdrive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.landing_page);

        executorService = Executors.newSingleThreadExecutor();
        LinearLayout buttonDelay, buttonReverb, buttonCleantone, buttonDistortion, buttonOverdrive;
        Button addButton, addButtonReverb, addButtonCleantone, addButtonDistortion, addButtonOverdrive;
        LinearLayout navAdd, navQueue, navPreset, navSettings;
        ImageView addIcon, queueIcon, presetIcon, settingsIcon;
        TextView addIconText, queueIconText, presetText, settingsText;

        // NAVIGATION BAR
        // Navigation Items
        navAdd = findViewById(R.id.nav_add);
        navQueue = findViewById(R.id.nav_queue);
        navPreset = findViewById(R.id.nav_preset);
        navSettings = findViewById(R.id.nav_settings);

        addIcon = findViewById(R.id.add_icon);
        queueIcon = findViewById(R.id.queue_icon);
        presetIcon = findViewById(R.id.preset_icon);
        settingsIcon = findViewById(R.id.settings_icon);

        addIconText = findViewById(R.id.add_icon_text);
        queueIconText = findViewById(R.id.queue_icon_text);
        presetText = findViewById(R.id.preset_icon_text);
        settingsText = findViewById(R.id.settings_text);

        // Apply tint color to all icons permanently
        int grayColor = Color.parseColor("#9E9E9E");

        // Setting color for navbar ImageView
        addIcon.setColorFilter(Color.parseColor("#FF0000"));
        queueIcon.setColorFilter(grayColor);
        presetIcon.setColorFilter(grayColor);
        settingsIcon.setColorFilter(grayColor);

        // Setting color for navbar TextView
        addIconText.setTextColor(Color.parseColor("#FF0000"));
        queueIconText.setTextColor(grayColor);
        presetText.setTextColor(grayColor);
        settingsText.setTextColor(grayColor);

        // Set click listeners
        navAdd.setOnClickListener(v -> openActivity(LandingPageActivity.class));
        navQueue.setOnClickListener(v -> openActivity(QueueActivity.class));
        navSettings.setOnClickListener(v -> openActivity(SettingsActivity.class));
        navPreset.setOnClickListener(v -> openActivity(PresetActivity.class));

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

        addButton = findViewById(R.id.add_button);
        addButtonReverb = findViewById(R.id.add_button_reverb);
        addButtonCleantone = findViewById(R.id.add_button_cleantone);
        addButtonDistortion = findViewById(R.id.add_button_distortion);
        addButtonOverdrive = findViewById(R.id.add_button_overdrive);

        // Initialize separate MediaPlayers
        mediaPlayerDelay = MediaPlayer.create(this, R.raw.delay_sample);
        mediaPlayerReverb = MediaPlayer.create(this, R.raw.reverb_sample);
        mediaPlayerCleantone = MediaPlayer.create(this, R.raw.sample); // change this
        mediaPlayerDistortion = MediaPlayer.create(this, R.raw.distortion_sample);
        mediaPlayerOverdrive = MediaPlayer.create(this, R.raw.sample); // change this

        buttonDelay.setOnClickListener(v -> {
            if(!isOpenDropDelay){
                Log.d("LandingPageActivity", "Delay drop down button open!");
                isOpenDropDelay = true;
            } else {
                Log.d("LandingPageActivity", "Delay drop down button closed!");
                isOpenDropDelay = false;
            }

            toggleDropdown(dropdownDelay);
        });
        buttonReverb.setOnClickListener(v -> {
            if(!isOpenDropReverb){
                Log.d("LandingPageActivity", "Reverb drop down button open!");
                isOpenDropReverb = true;
            } else {
                Log.d("LandingPageActivity", "Reverb drop down button closed!");
                isOpenDropReverb = false;
            }

            toggleDropdown(dropdownReverb);
        });
        buttonCleantone.setOnClickListener(v -> {
            if(!isOpenDropCleantone){
                Log.d("LandingPageActivity", "Cleantone drop down button open!");
                isOpenDropCleantone = true;
            } else {
                Log.d("LandingPageActivity", "Cleantone drop down button closed!");
                isOpenDropCleantone = false;
            }

            toggleDropdown(dropdownCleantone);
        });
        buttonDistortion.setOnClickListener(v -> {
            if(!isOpenDropDistortion){
                Log.d("LandingPageActivity", "Distortion drop down button open!");
                isOpenDropDistortion = true;
            } else {
                Log.d("LandingPageActivity", "Distortion drop down button closed!");
                isOpenDropDistortion = false;
            }

            toggleDropdown(dropdownDistortion);
        });
        buttonOverdrive.setOnClickListener(v -> {
            if(!isOpenDropOverdrive){
                Log.d("LandingPageActivity", "Overdrive drop down button open!");
                isOpenDropOverdrive = true;
            } else {
                Log.d("LandingPageActivity", "Overdrive drop down button closed!");
                isOpenDropOverdrive = false;
            }

            toggleDropdown(dropdownOverdrive);
        });

        playButton.setOnClickListener(v -> playPauseAudioDelay());
        playButtonReverb.setOnClickListener(v -> playPauseAudioReverb());
        playButtonCleantone.setOnClickListener(v -> playPauseAudioCleantone());
        playButtonDistortion.setOnClickListener(v -> playPauseAudioDistortion());
        playButtonOverdrive.setOnClickListener(v -> playPauseAudioOverdrive());

        addButton.setOnClickListener(v -> {
            if (!selectedEffects.contains("Delay")) {
                EffectManager.getInstance().addEffect("Delay");
                Log.d("LandingPageActivity","Delay Added to the Queue");
                isOpenDropDelay = false;
                Log.d("LandingPageActivity", "Delay drop down button closed!");
                //Toast.makeText(this, "Delay Added to the Queue", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("LandingPageActivity","Delay is already in the Queue");
                Toast.makeText(this, "Delay is already in the Queue", Toast.LENGTH_SHORT).show();
            }
            dropdownDelay.setVisibility(View.GONE);
        });

        addButtonReverb.setOnClickListener(v -> {
            if (!selectedEffects.contains("Reverb")) {
                EffectManager.getInstance().addEffect("Reverb");
                Log.d("LandingPageActivity","Reverb Added to the Queue");
                isOpenDropReverb = false;
                Log.d("LandingPageActivity", "Reverb drop down button closed!");
                //Toast.makeText(this, "Reverb Added to the Queue", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("LandingPageActivity","Reverb is already in the Queue");
                //Toast.makeText(this, "Reverb is already in the Queue", Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, "Reverb is already in the Queue", Toast.LENGTH_SHORT).show();
            }
            dropdownReverb.setVisibility(View.GONE);
        });

        addButtonCleantone.setOnClickListener(v -> {
            if (!selectedEffects.contains("Clean")) {
                EffectManager.getInstance().addEffect("Clean");
                Log.d("LandingPageActivity","Cleantone Added to the Queue");
                isOpenDropCleantone = false;
                Log.d("LandingPageActivity", "Cleantone drop down button closed!");
                //Toast.makeText(this, "Cleantone Added to the Queue", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("LandingPageActivity","Cleantone is already in the Queue");
                //Toast.makeText(this, "Cleantone is already in the Queue", Toast.LENGTH_SHORT).show();
            }
            dropdownCleantone.setVisibility(View.GONE);
        });

        addButtonDistortion.setOnClickListener(v -> {
            if (!selectedEffects.contains("Distortion")) {
                EffectManager.getInstance().addEffect("Distortion");
                Log.d("LandingPageActivity","Distortion Added to the Queue");
                isOpenDropDistortion = false;
                Log.d("LandingPageActivity", "Distortion drop down button closed!");
                //Toast.makeText(this, "Distortion Added to the Queue", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("LandingPageActivity","Distortion is already in the Queue");
                //Toast.makeText(this, "Distortion is already in the Queue", Toast.LENGTH_SHORT).show();
            }
            dropdownDistortion.setVisibility(View.GONE);
        });

        addButtonOverdrive.setOnClickListener(v -> {
            if (!selectedEffects.contains("Overdrive")) {
                EffectManager.getInstance().addEffect("Overdrive");
                Log.d("LandingPageActivity","Overdrive Added to the Queue");
                isOpenDropOverdrive = false;
                Log.d("LandingPageActivity", "Overdrive drop down button closed!");
                //Toast.makeText(this, "Overdrive Added to the Queue", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("LandingPageActivity","Overdrive is already in the Queue");
                //Toast.makeText(this, "Overdrive is already in the Queue", Toast.LENGTH_SHORT).show();
            }
            dropdownOverdrive.setVisibility(View.GONE);
        });

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
            stopAllMediaPlayersExcept(mediaPlayerDelay);  // Stop other players
            mediaPlayerDelay.start();
            playButton.setImageResource(R.raw.pause_icon);
            seekBarDelay.setMax(mediaPlayerDelay.getDuration());
            handler.postDelayed(updateSeekBarDelay, 1000);  // Start updating seekbar
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
            stopAllMediaPlayersExcept(mediaPlayerReverb);  // Stop other players
            mediaPlayerReverb.start();
            playButtonReverb.setImageResource(R.raw.pause_icon);
            seekBarReverb.setMax(mediaPlayerReverb.getDuration());
            handler.postDelayed(updateSeekBarReverb, 1000);  // Start updating seekbar
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
            stopAllMediaPlayersExcept(mediaPlayerCleantone);
            mediaPlayerCleantone.start();
            playButtonCleantone.setImageResource(R.raw.pause_icon);
            seekBarCleantone.setMax(mediaPlayerCleantone.getDuration());
            handler.postDelayed(updateSeekBarReverb, 1000);
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
            stopAllMediaPlayersExcept(mediaPlayerDistortion);
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
            stopAllMediaPlayersExcept(mediaPlayerOverdrive);
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
        MediaPlayer[] players = {mediaPlayerDelay, mediaPlayerReverb, mediaPlayerCleantone, mediaPlayerDistortion, mediaPlayerOverdrive};

        for (MediaPlayer player : players) {
            if (player != null && player != current && player.isPlaying()) {
                player.pause();
                // Ensure the seek bar is reset if the player is stopped
                if (player == mediaPlayerDelay) {
                    seekBarDelay.setProgress(0);
                } else if (player == mediaPlayerReverb) {
                    seekBarReverb.setProgress(0);
                } else if (player == mediaPlayerCleantone) {
                    seekBarCleantone.setProgress(0);
                } else if (player == mediaPlayerDistortion) {
                    seekBarDistortion.setProgress(0);
                } else if (player == mediaPlayerOverdrive) {
                    seekBarOverdrive.setProgress(0);
                }
            }
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(LandingPageActivity.this, activityClass);
        intent.putStringArrayListExtra("selectedEffects", selectedEffects);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release all media players
        releaseMediaPlayer(mediaPlayerDelay);
        releaseMediaPlayer(mediaPlayerReverb);
        releaseMediaPlayer(mediaPlayerCleantone);
        releaseMediaPlayer(mediaPlayerDistortion);
        releaseMediaPlayer(mediaPlayerOverdrive);

        // Remove all handler callbacks
        handler.removeCallbacks(updateSeekBarCleantone);
        handler.removeCallbacks(updateSeekBarOverdrive);
        handler.removeCallbacks(updateSeekBarDelay);
        handler.removeCallbacks(updateSeekBarDistortion);
        handler.removeCallbacks(updateSeekBarReverb);

        // Shutdown executor service
        executorService.shutdown();
    }
    private void releaseMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
