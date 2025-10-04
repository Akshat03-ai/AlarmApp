package com.example.alarmapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmsFragment extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadAlarms();
        return view;
    }

    private void loadAlarms() {
        SharedPreferences prefs = requireContext().getSharedPreferences("alarms", Context.MODE_PRIVATE);
        Set<String> alarms = prefs.getStringSet("alarm_times", null);
        List<String> alarmList = new ArrayList<>();
        Set<String> updatedSet = new HashSet<>();

        if (alarms != null) {
            for (String alarmTime : alarms) {
                String[] parts = alarmTime.split(":");
                if (parts.length == 2) {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);

                    Calendar alarmCal = Calendar.getInstance();
                    alarmCal.set(Calendar.HOUR_OF_DAY, hour);
                    alarmCal.set(Calendar.MINUTE, minute);
                    alarmCal.set(Calendar.SECOND, 0);
                    alarmCal.set(Calendar.MILLISECOND, 0);

                    if (alarmCal.getTimeInMillis() > System.currentTimeMillis()) {
                        alarmList.add(alarmTime);
                        updatedSet.add(alarmTime);
                    }
                }
            }

            // Save only future alarms back to SharedPreferences
            prefs.edit().putStringSet("alarm_times", updatedSet).apply();
        }

        recyclerView.setAdapter(new AlarmAdapter(alarmList, requireContext()));
    }


    @Override
    public void onResume() {
        super.onResume();
        loadAlarms(); // This will refresh the list when fragment becomes active
    }

}
