package com.example.alarmapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class FullscreenAlarmActivity extends AppCompatActivity {
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Force wake-up and show over lockscreen
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.fullscreen_activity_alarm);


        // 2. Initialize vibration (long pattern)
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 800}; // Vibrate 1s, pause 0.8s
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0); // 0 = no repeat
            }
        }

        // 4. Stop button functionality
        Button stopButton = findViewById(R.id.stopAlarmButton);
        stopButton.setOnClickListener(v -> stopAlarm());
    }

    private void stopAlarm() {
        // 5. Stop all alarm effects
        if (vibrator != null) {
            vibrator.cancel();
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // 6. Stop the AlarmService
        stopService(new Intent(this, AlarmService.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        // 7. Cleanup resources
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}