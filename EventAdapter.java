package com.example.nthabelengmolaoli2333784;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class EventAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> eventTitles;
    private ArrayList<String> eventDates;
    private ArrayList<String> eventImages;
    private ArrayList<Integer> eventIds;
    private OnEventActionListener listener;
    private DatabaseHelper db;

    public interface OnEventActionListener {
        void onWatchVideo(int position);
        void onPlayMusic(int position);
        void onRegister(int position);
        void onShareEvent(int position);
        void onViewOnMap(int position);
    }

    public EventAdapter(Context context, ArrayList<String> eventTitles, ArrayList<String> eventDates, ArrayList<String> eventImages, ArrayList<Integer> eventIds, OnEventActionListener listener) {
        this.context = context;
        this.eventTitles = eventTitles;
        this.eventDates = eventDates;
        this.eventImages = eventImages;
        this.eventIds = eventIds;
        this.listener = listener;
        this.db = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return eventTitles.size();
    }

    @Override
    public Object getItem(int position) {
        return eventTitles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return eventIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imgEvent);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvBadge = convertView.findViewById(R.id.tvTypeBadge);
        TextView tvRegCount = convertView.findViewById(R.id.tvCardRegCount);
        
        Button btnVideo = convertView.findViewById(R.id.btnWatchVideo);
        Button btnMusic = convertView.findViewById(R.id.btnPlayMusic);
        Button btnShare = convertView.findViewById(R.id.btnShareEvent);
        Button btnMap = convertView.findViewById(R.id.btnViewOnMap);

        int eventId = eventIds.get(position);
        tvTitle.setText(eventTitles.get(position));
        tvDate.setText(eventDates.get(position));

        // 1. Fetch Capacity directly for this specific event ID
        int totalCapacity = 0;
        String type = "Future";
        try (Cursor cursor = db.getReadableDatabase().rawQuery("SELECT CAPACITY, TYPE FROM events WHERE ID=?", new String[]{String.valueOf(eventId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                totalCapacity = cursor.getInt(0);
                type = cursor.getString(1);
            }
        }

        // 2. Count actual rows in registrations table for this event ID
        int registeredCount = db.getEventRegistrationCount(eventId);

        // 3. Display logic - CLEAR AND SIMPLE
        if (totalCapacity > 0 && registeredCount >= totalCapacity) {
            tvRegCount.setText("EVENT FULL (" + registeredCount + "/" + totalCapacity + ")");
            tvRegCount.setTextColor(Color.RED);
            tvRegCount.setTypeface(null, Typeface.BOLD);
        } else {
            tvRegCount.setText("Registered: " + registeredCount + " / " + totalCapacity);
            tvRegCount.setTextColor(Color.BLACK);
            tvRegCount.setTypeface(null, Typeface.NORMAL);
        }

        if (type.equalsIgnoreCase("Past")) {
            tvBadge.setText("PAST");
            tvBadge.setBackgroundColor(0xFF757575);
        } else {
            tvBadge.setText("FUTURE");
            tvBadge.setBackgroundColor(0xFFD81B60);
        }

        String imageName = eventImages.get(position);
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId != 0) {
            imageView.setImageResource(resId);
        } else {
            imageView.setImageResource(R.drawable.mapping);
        }

        btnVideo.setOnClickListener(v -> listener.onWatchVideo(position));
        btnMusic.setOnClickListener(v -> listener.onPlayMusic(position));
        btnShare.setOnClickListener(v -> listener.onShareEvent(position));
        btnMap.setOnClickListener(v -> listener.onViewOnMap(position));
        convertView.setOnClickListener(v -> listener.onRegister(position));

        return convertView;
    }
}
