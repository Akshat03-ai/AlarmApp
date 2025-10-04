package com.example.alarmapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private final String[] tabTitles = {"Set Alarm", "Alarms"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // This must come FIRST
        if (checkSelfPermission(android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.VIBRATE}, 1001);
        }
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
        intent.putExtra("extra_pkgname", getPackageName());
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // fallback to standard settings
            Intent fallbackIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            fallbackIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(fallbackIntent);
        }


        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        TabPagerAdapter adapter = new TabPagerAdapter(this);
        viewPager.setAdapter(adapter);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }


        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

    }
}
