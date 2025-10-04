package com.example.alarmapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class SetAlarmFragment extends Fragment {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    private TimePicker timePicker;
    private AlarmManager alarmManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_alarm, container, false);

        timePicker = view.findViewById(R.id.timePicker);
        Button btnSetAlarm = view.findViewById(R.id.btnSetAlarm);

        Calendar now = Calendar.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(now.get(Calendar.MINUTE));
        }

        timePicker.setIs24HourView(true);
        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        checkBatteryOptimization();
        checkAndRequestPermissions();

        btnSetAlarm.setOnClickListener(v -> setAlarm());

        return view;
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(requireContext().getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void checkAndRequestPermissions() {
        // Android 13+: Notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Android 12+: Exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void setAlarm() {
        int hour, minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(requireContext(), "Cannot schedule exact alarms. Permission not granted.", Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        saveAlarmToPrefs(hour, minute);

        String alarmTime = String.format("%02d:%02d", hour, minute);
        Toast.makeText(getContext(), "Alarm set for " + alarmTime, Toast.LENGTH_SHORT).show();
    }

    private void saveAlarmToPrefs(int hour, int minute) {
        SharedPreferences prefs = requireContext().getSharedPreferences("alarms", Context.MODE_PRIVATE);
        Set<String> alarms = new HashSet<>(prefs.getStringSet("alarm_times", new HashSet<>()));
        alarms.add(String.format("%02d:%02d", hour, minute));
        prefs.edit().putStringSet("alarm_times", alarms).apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
