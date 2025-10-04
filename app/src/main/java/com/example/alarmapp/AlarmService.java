package com.example.alarmapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.HashSet;
import java.util.Set;

public class AlarmService extends Service {
    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 101;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private boolean isAlarmActive = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isAlarmActive) {
            startAlarm();
            isAlarmActive = true;
        }
        return START_STICKY;
    }

    private void startAlarm() {

        // 2. Remove triggered alarm from storage
        removeTriggeredAlarm();

        // 3. Acquire wake lock
        acquireWakeLock();

        // 4. Start vibration (1s on/1s off pattern)
        startVibration();

        // 5. Start alarm sound (only once)
        startAlarmSound();

        // 6. Create persistent notification
        createNotification();
    }

    private void removeTriggeredAlarm() {
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        Set<String> alarms = prefs.getStringSet("alarm_times", null);
        if (alarms != null) {
            Set<String> updatedAlarms = new HashSet<>(alarms);
            updatedAlarms.remove(String.valueOf(System.currentTimeMillis()));
            prefs.edit().putStringSet("alarm_times", updatedAlarms).apply();
        }
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "AlarmApp::AlarmServiceWakeLock"
            );
            wakeLock.acquire(60 * 1000); // Hold for 1 minute
        }
    }

    private void startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
                vibrator = vm.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 1000, 1000}; // 1s on, 1s off
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
        } catch (Exception e) {
            Log.e("AlarmService", "Vibration error", e);
        }
    }

    private void startAlarmSound() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.alarm_sound);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("AlarmService", "Sound error", e);
            playFallbackSound();
        }
    }

    private void playFallbackSound() {
        try {
            Uri alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            Ringtone ringtone = RingtoneManager.getRingtone(this, alertUri);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e("AlarmService", "Fallback sound error", e);
        }
    }

    private void createNotification() {
        createNotificationChannel();

        Intent fullscreenIntent = new Intent(this, FullscreenAlarmActivity.class);
        fullscreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, fullscreenIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Alarm is ringing")
                .setContentText("Tap to open")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alarm notifications");
            channel.enableVibration(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        isAlarmActive = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}