package com.example.alarmapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {

    public TabPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new SetAlarmFragment() : new AlarmsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
