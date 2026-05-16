package com.example.nthabelengmolaoli2333784;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity implements AdminEventAdapter.OnEventEditListener {

    EditText etTitle, etDesc, etDate, etTime, etLoc, etImg, etCap;
    Spinner spnCategory;
    Button btnAddUpdate;
    GridView gvEvents;
    DatabaseHelper db;
    NotificationHelper notificationHelper;
    ArrayList<String> eventTitles, eventDates, eventImages;
    ArrayList<Integer> eventIds;
    AdminEventAdapter adapter;
    int currentEditingId = -1;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        db = new DatabaseHelper(this);
        notificationHelper = new NotificationHelper(this);
        sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        
        etTitle = findViewById(R.id.etAdminEventTitle);
        etDesc = findViewById(R.id.etAdminEventDesc);
        etDate = findViewById(R.id.etAdminEventDate);
        etTime = findViewById(R.id.etAdminEventTime); 
        etLoc = findViewById(R.id.etAdminEventLocation);
        etImg = findViewById(R.id.etAdminEventImage);
        etCap = findViewById(R.id.etAdminEventCapacity);
        spnCategory = findViewById(R.id.spnAdminCategory);
        btnAddUpdate = findViewById(R.id.btnAdminAddUpdate);
        gvEvents = findViewById(R.id.gvAdminEvents);

        String[] categories = {"Academic", "Social", "Career", "Business", "Culture", "Sports"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(catAdapter);

        eventTitles = new ArrayList<>();
        eventDates = new ArrayList<>();
        eventImages = new ArrayList<>();
        eventIds = new ArrayList<>();
        
        btnAddUpdate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String loc = etLoc.getText().toString().trim();
            String img = etImg.getText().toString().trim();
            String capStr = etCap.getText().toString().trim();
            String category = spnCategory.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty() || date.isEmpty() || time.isEmpty() || loc.isEmpty() || img.isEmpty() || capStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidFutureDate(date)) {
                Toast.makeText(this, "Cannot create event in the past. Use a future date (YYYY-MM-DD)", Toast.LENGTH_LONG).show();
                return;
            }

            if (!isValidLocation(loc)) {
                Toast.makeText(this, "Invalid location name. Please enter a real place.", Toast.LENGTH_LONG).show();
                return;
            }

            int cap = Integer.parseInt(capStr);
            if (cap > 100) {
                Toast.makeText(this, "Maximum capacity is 100 students.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentEditingId == -1) {
                if (db.addEvent(title, desc, date, time, loc, "Future", img, cap, category)) {
                    sendBroadcastToStudents("NEW EVENT: " + title + " on " + date);
                    Toast.makeText(this, "Event Created!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    viewEvents();
                }
            } else {
                if (db.updateEvent(currentEditingId, title, desc, date, time, loc, "Future", img, cap, category)) {
                    sendBroadcastToStudents("EVENT UPDATED: " + title + " details changed.");
                    Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show();
                    currentEditingId = -1;
                    btnAddUpdate.setText("Add Event");
                    clearFields();
                    viewEvents();
                }
            }
        });

        viewEvents();
    }

    private void sendBroadcastToStudents(String message) {
        // Send System Notification
        notificationHelper.sendInstantNotification("Botho Expo Update", message);
        
        // Simulate/Send SMS to real numbers in Database
        try (Cursor cursor = db.getAllStudents()) {
            while (cursor != null && cursor.moveToNext()) {
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("PHONE"));
                if (phone != null && phone.length() > 5) {
                    // Placeholder for real SMS sending (requires carrier settings)
                    // SmsManager.getDefault().sendTextMessage(phone, null, message, null, null);
                    System.out.println("SMS sent to: " + phone + " Content: " + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidFutureDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date eventDate = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            return eventDate != null && !eventDate.before(cal.getTime());
        } catch (ParseException e) { return false; }
    }

    private boolean isValidLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            return addresses != null && !addresses.isEmpty();
        } catch (IOException e) { return locationName.length() > 3; }
    }

    private void clearFields() {
        etTitle.setText(""); etDesc.setText(""); etDate.setText(""); etTime.setText("");
        etLoc.setText(""); etImg.setText(""); etCap.setText("100");
        spnCategory.setSelection(0);
    }

    public void viewEvents() {
        eventTitles.clear(); eventDates.clear(); eventImages.clear(); eventIds.clear();
        Cursor cursor = db.getAllEvents();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                eventTitles.add(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));
                eventDates.add(cursor.getString(cursor.getColumnIndexOrThrow("DATE")));
                eventImages.add(cursor.getString(cursor.getColumnIndexOrThrow("IMAGE")));
                eventIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
            }
            cursor.close();
        }
        adapter = new AdminEventAdapter(this, eventTitles, eventDates, eventImages, eventIds, db, this, this::viewEvents);
        gvEvents.setAdapter(adapter);
    }

    @Override
    public void onEdit(int eventId) {
        currentEditingId = eventId;
        btnAddUpdate.setText("Update Event");
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM events WHERE ID=?", new String[]{String.valueOf(eventId)});
        if (cursor != null && cursor.moveToFirst()) {
            etTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));
            etDesc.setText(cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPTION")));
            etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("DATE")));
            etTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("TIME")));
            etLoc.setText(cursor.getString(cursor.getColumnIndexOrThrow("LOCATION")));
            etImg.setText(cursor.getString(cursor.getColumnIndexOrThrow("IMAGE")));
            etCap.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("CAPACITY"))));
            cursor.close();
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu m) { getMenuInflater().inflate(R.menu.admin_menu, m); return true; }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home || id == R.id.menu_go_back) { finish(); return true; }
        else if (id == R.id.menu_summary) { showSummary(); return true; }
        else if (id == R.id.menu_logout_admin) { logout(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showSummary() {
        int ev = 0, reg = 0, students = 0;
        try (Cursor c1 = db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM events", null)) { if (c1.moveToFirst()) ev = c1.getInt(0); }
        try (Cursor c2 = db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM registrations", null)) { if (c2.moveToFirst()) reg = c2.getInt(0); }
        try (Cursor c3 = db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM users WHERE ROLE='Student'", null)) { if (c3.moveToFirst()) students = c3.getInt(0); }
        String report = "Expo Analytics:\n\nTotal Events: " + ev + "\nSign-ups: " + reg + "\nStudents: " + students;
        new AlertDialog.Builder(this).setTitle("Summary Report").setMessage(report).setPositiveButton("OK", null).show();
    }

    private void logout() {
        SharedPreferences.Editor e = sp.edit(); e.clear(); e.apply();
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
