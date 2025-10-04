package com.example.alarmapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

public class AlarmAlertActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make it fullscreen even if screen is locked
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView textView = new TextView(this);
        textView.setText("⏰ Wake up! Your alarm is ringing.");
        textView.setTextSize(24f);
        textView.setPadding(40, 200, 40, 40);
        setContentView(textView);

        // Auto close after 10 seconds (optional)
        new Handler().postDelayed(this::finish, 10000);
    }
}
