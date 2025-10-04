package com.example.alarmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Get system services with proper context
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final Vibrator vibrator;

        // 2. Proper vibrator initialization for all Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager != null ? vibratorManager.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        // 3. Wake lock acquisition
        final PowerManager.WakeLock wakeLock = powerManager != null ?
                powerManager.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE,
                        "AlarmApp::AlarmWakeLock"
                ) : null;

        if (wakeLock != null) {
            wakeLock.acquire(30 * 1000);
        }

        // 4. Start vibration (1s on/1s off pattern)
        if (vibrator != null && vibrator.hasVibrator()) {
            Log.d(TAG, "Vibrator detected. Starting vibration...");
            try {
                long[] pattern = {0, 1000, 1000}; // vibrate 1s, pause 1s
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            } catch (Exception e) {
                Log.e(TAG, "Vibration failed: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "No vibrator found on device.");
        }


        Intent activityIntent = new Intent(context, FullscreenAlarmActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(activityIntent);


        // 5. Launch fullscreen activity
        Intent fullscreenIntent = new Intent(context, FullscreenAlarmActivity.class);
        fullscreenIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        );

        // 6. Start foreground service
        Intent serviceIntent = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // 7. Proper handler for wake lock release
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }, 5000);

        // 8. Start activity last
        context.startActivity(fullscreenIntent);
    }
}