package com.example.alarmapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private final List<String> alarmList;
    private final Context context;

    public AlarmAdapter(List<String> alarmList, Context context) {
        this.alarmList = alarmList;
        this.context = context;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        String time = alarmList.get(position);
        holder.tvTime.setText(time);

        holder.btnDelete.setOnClickListener(v -> {
            // Remove from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
            Set<String> storedAlarms = new HashSet<>(prefs.getStringSet("alarm_times", new HashSet<>()));
            storedAlarms.remove(time);
            prefs.edit().putStringSet("alarm_times", storedAlarms).apply();

            // Remove from RecyclerView list
            alarmList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, alarmList.size());
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        Button btnDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
