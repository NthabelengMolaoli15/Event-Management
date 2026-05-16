package com.example.nthabelengmolaoli2333784;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class AdminEventAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<String> eventTitles;
    private final ArrayList<String> eventDates;
    private final ArrayList<String> eventImages;
    private final ArrayList<Integer> eventIds;
    private final DatabaseHelper db;
    private final OnEventEditListener editListener;
    private final Runnable refreshCallback;

    public interface OnEventEditListener {
        void onEdit(int eventId);
    }

    public AdminEventAdapter(Context context, ArrayList<String> titles, ArrayList<String> dates, ArrayList<String> images, ArrayList<Integer> ids, DatabaseHelper db, OnEventEditListener editListener, Runnable refreshCallback) {
        this.context = context;
        this.eventTitles = titles;
        this.eventDates = dates;
        this.eventImages = images;
        this.eventIds = ids;
        this.db = db;
        this.editListener = editListener;
        this.refreshCallback = refreshCallback;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_event_admin, parent, false);
        }

        ImageView ivEvent = convertView.findViewById(R.id.ivAdminEventImage);
        TextView tvTitle = convertView.findViewById(R.id.tvAdminEventTitle);
        TextView tvDate = convertView.findViewById(R.id.tvAdminEventDate);
        TextView tvStats = convertView.findViewById(R.id.tvAdminRegStats);
        
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteEvent);
        ImageButton btnUpdate = convertView.findViewById(R.id.btnUpdateEvent);
        ImageButton btnViewStudents = convertView.findViewById(R.id.btnViewStudents);
        ImageButton btnMap = convertView.findViewById(R.id.btnAdminMap);

        tvTitle.setText(eventTitles.get(position));
        tvDate.setText(eventDates.get(position));
        
        int eventId = eventIds.get(position);
        int count = db.getEventRegistrationCount(eventId);
        
        // Get capacity
        int capacity = 50;
        try (Cursor c = db.getReadableDatabase().rawQuery("SELECT CAPACITY FROM events WHERE ID=?", new String[]{String.valueOf(eventId)})) {
            if (c != null && c.moveToFirst()) capacity = c.getInt(0);
        }
        tvStats.setText("Reg: " + count + "/" + capacity);

        // Load image from drawable
        String imgName = eventImages.get(position);
        int resId = context.getResources().getIdentifier(imgName, "drawable", context.getPackageName());
        if (resId != 0) {
            ivEvent.setImageResource(resId);
        } else {
            ivEvent.setImageResource(R.drawable.assignment); // Fallback
        }

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.deleteEvent(String.valueOf(eventId));
                    Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                    refreshCallback.run();
                })
                .setNegativeButton("No", null)
                .show();
        });

        btnUpdate.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(eventId);
            }
        });

        btnViewStudents.setOnClickListener(v -> showRegisteredStudents(eventId));

        btnMap.setOnClickListener(v -> {
            Cursor cursor = db.getReadableDatabase().rawQuery("SELECT LOCATION FROM events WHERE ID=?", new String[]{String.valueOf(eventId)});
            if (cursor != null && cursor.moveToFirst()) {
                String loc = cursor.getString(0);
                cursor.close();
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("LOCATION_NAME", loc);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private void showRegisteredStudents(int eventId) {
        ArrayList<String> studentList = new ArrayList<>();
        try (Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT r.USERNAME FROM registrations r WHERE r.EVENT_ID=?",
                new String[]{String.valueOf(eventId)})) {

            while (cursor.moveToNext()) {
                studentList.add(cursor.getString(0));
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Registered Students");

        if (studentList.isEmpty()) {
            builder.setMessage("No students registered yet.");
        } else {
            String[] studentsArray = studentList.toArray(new String[0]);
            builder.setItems(studentsArray, null);
        }

        builder.setPositiveButton("Close", null);
        builder.show();
    }
}
