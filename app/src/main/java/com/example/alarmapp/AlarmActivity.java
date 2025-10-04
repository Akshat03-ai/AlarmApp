package com.example.alarmapp;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.TextView;

public class AlarmActivity extends Activity {

    Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen, show over lockscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        TextView textView = new TextView(this);
        textView.setText("⏰ Alarm Ringing!");
        textView.setTextSize(30f);
        textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        textView.setPadding(50, 300, 50, 50);
        setContentView(textView);

        // Vibrate
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) vibrator.vibrate(1000);

        // Ringtone
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        ringtone.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
    }
}
