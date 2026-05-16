package com.example.nthabelengmolaoli2333784;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StudentDashBoardActivity extends AppCompatActivity implements EventAdapter.OnEventActionListener {

    EditText etSearch;
    GridView gvEvents;
    TextView tvWelcome, tvRegCount;
    LinearLayout llCountdownBanner, llReminderContainer;
    HorizontalScrollView hsvReminders;
    TextView tvCountdownTimer, tvCountdownEventName, tvReminderLabel;
    
    DatabaseHelper db;
    ArrayList<String> eventTitles, eventDates, eventImages, eventTypes, eventLocations;
    ArrayList<Integer> eventIds;
    EventAdapter adapter;
    String currentUsername;
    MediaPlayer mediaPlayer;
    SharedPreferences sp;
    
    Handler countdownHandler = new Handler();
    Runnable countdownRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Student Dashboard");
        }

        db = new DatabaseHelper(this);
        sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        
        etSearch = findViewById(R.id.etSearch);
        gvEvents = findViewById(R.id.gvEvents);
        tvWelcome = findViewById(R.id.tvWelcomeMessage);
        tvRegCount = findViewById(R.id.tvRegCount);
        llCountdownBanner = findViewById(R.id.llCountdownBanner);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);
        tvCountdownEventName = findViewById(R.id.tvCountdownEventName);
        tvReminderLabel = findViewById(R.id.tvReminderLabel);
        hsvReminders = findViewById(R.id.hsvReminders);
        llReminderContainer = findViewById(R.id.llReminderContainer);

        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) currentUsername = sp.getString("USERNAME", "Student");
        tvWelcome.setText("Welcome " + currentUsername + " to Botho University Expo");

        eventTitles = new ArrayList<>();
        eventDates = new ArrayList<>();
        eventImages = new ArrayList<>();
        eventTypes = new ArrayList<>();
        eventIds = new ArrayList<>();
        eventLocations = new ArrayList<>();

        loadEvents("");
        updateRegCount();
        startCountdown();
        checkUpcomingEventsReminders();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadEvents(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkUpcomingEventsReminders() {
        if (llReminderContainer == null) return;
        llReminderContainer.removeAllViews();
        Cursor cursor = db.getAllEvents();
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        boolean hasReminders = false;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("DATE"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("TITLE"));
                    Date eventDate = sdf.parse(dateStr);
                    if (eventDate != null) {
                        long diff = eventDate.getTime() - now;
                        if (diff > 0) {
                            hasReminders = true;
                            long days = diff / (24 * 60 * 60 * 1000);
                            TextView tv = new TextView(this);
                            String reminderText = title + "\n(" + (days == 0 ? "Starting soon!" : days + " days left") + ")";
                            tv.setText(reminderText);
                            tv.setPadding(30, 15, 30, 15);
                            tv.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame);
                            tv.setTextColor(Color.BLACK);
                            tv.setTypeface(null, Typeface.BOLD);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 20, 0);
                            tv.setLayoutParams(lp);
                            llReminderContainer.addView(tv);
                        }
                    }
                } catch (Exception e) {}
            }
            cursor.close();
        }
        tvReminderLabel.setVisibility(hasReminders ? View.VISIBLE : View.GONE);
        hsvReminders.setVisibility(hasReminders ? View.VISIBLE : View.GONE);
    }

    private void startCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdownUI();
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void updateCountdownUI() {
        Cursor cursor = db.getAllEvents();
        long now = System.currentTimeMillis();
        long minDiff = Long.MAX_VALUE;
        String nextEventTitle = "";
        long nextEventTime = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("DATE"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("TITLE"));
                    Date eventDate = sdf.parse(dateStr);
                    if (eventDate != null) {
                        long diff = eventDate.getTime() - now;
                        if (diff > 0 && diff < minDiff) {
                            minDiff = diff;
                            nextEventTitle = title;
                            nextEventTime = eventDate.getTime();
                        }
                    }
                } catch (Exception e) {}
            }
            cursor.close();
        }

        if (nextEventTime > 0) {
            llCountdownBanner.setVisibility(View.VISIBLE);
            tvCountdownEventName.setText(nextEventTitle);
            long diff = nextEventTime - now;
            long days = diff / (24 * 60 * 60 * 1000);
            long hours = (diff / (60 * 60 * 1000)) % 24;
            long minutes = (diff / (60 * 1000)) % 60;
            long seconds = (diff / 1000) % 60;
            String timeLeft = String.format(Locale.getDefault(), "%02dd %02dh %02dm %02ds", days, hours, minutes, seconds);
            tvCountdownTimer.setText(timeLeft);
        } else {
            llCountdownBanner.setVisibility(View.GONE);
        }
    }

    private void loadEvents(String filter) {
        eventTitles.clear();
        eventDates.clear();
        eventImages.clear();
        eventTypes.clear();
        eventIds.clear();
        eventLocations.clear();
        Cursor cursor = db.getAllEvents();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow("TITLE"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("DATE"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("TYPE"));
                String img = cursor.getString(cursor.getColumnIndexOrThrow("IMAGE"));
                String loc = cursor.getString(cursor.getColumnIndexOrThrow("LOCATION"));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID"));
                if (title.toLowerCase().contains(filter.toLowerCase()) || type.toLowerCase().contains(filter.toLowerCase())) {
                    eventTitles.add(title);
                    eventDates.add(date);
                    eventImages.add(img);
                    eventTypes.add(type);
                    eventIds.add(id);
                    eventLocations.add(loc);
                }
            }
            cursor.close();
        }
        adapter = new EventAdapter(this, eventTitles, eventDates, eventImages, eventIds, this);
        gvEvents.setAdapter(adapter);
    }

    private void updateRegCount() {
        int count = db.getEventRegistrationCountForUser(currentUsername);
        tvRegCount.setText("My Reg: " + count);
    }

    @Override public void onWatchVideo(int pos) { stopMusic(); startActivity(new Intent(this, MediaActivity.class)); }
    @Override public void onPlayMusic(int pos) { stopMusic(); mediaPlayer = MediaPlayer.create(this, R.raw.jehova); if (mediaPlayer != null) mediaPlayer.start(); }
    
    @Override public void onRegister(int pos) { 
        int eventId = eventIds.get(pos);
        int status = db.registerForEventWithStatus(currentUsername, eventId);
        
        switch (status) {
            case 0:
                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                updateRegCount();
                loadEvents(etSearch.getText().toString());
                break;
            case 1:
                Toast.makeText(this, "already registered", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "event full", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this, "cant register .past event", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override public void onShareEvent(int pos) { startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "Join me for " + eventTitles.get(pos)), "Share")); }
    @Override public void onViewOnMap(int pos) { Intent i = new Intent(this, MapActivity.class); i.putExtra("LOCATION_NAME", eventLocations.get(pos)); startActivity(i); }

    private void stopMusic() { if (mediaPlayer != null) { if (mediaPlayer.isPlaying()) mediaPlayer.stop(); mediaPlayer.release(); mediaPlayer = null; } }

    @Override public boolean onCreateOptionsMenu(Menu m) { getMenuInflater().inflate(R.menu.student_menu, m); return true; }
    
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile) { 
            startActivity(new Intent(this, ProfileActivity.class).putExtra("USERNAME", currentUsername)); 
            return true; 
        } else if (id == R.id.menu_messenger) {
            showMessenger();
            return true;
        } else if (id == R.id.menu_personal_inbox) {
            showPersonalEmailInbox();
            return true;
        } else if (id == R.id.menu_inbox) {
            showExpoUpdatesInbox();
            return true;
        } else if (id == R.id.menu_map) {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("LOCATION_NAME", "Botho University, Lesotho");
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_media) { 
            stopMusic();
            startActivity(new Intent(this, MediaActivity.class)); 
            return true; 
        } else if (id == R.id.menu_bluetooth) {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return true;
        } else if (id == R.id.menu_go_back) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_logout) { 
            stopMusic();
            SharedPreferences.Editor e = sp.edit(); e.clear(); e.apply(); 
            startActivity(new Intent(this, MainActivity.class)); 
            finish(); 
            return true; 
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPersonalEmailInbox() {
        ArrayList<String> emails = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        try (Cursor cursor = db.getReceivedEmails(currentUsername)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String sender = cursor.getString(cursor.getColumnIndexOrThrow("SENDER"));
                    String subject = cursor.getString(cursor.getColumnIndexOrThrow("SUBJECT"));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("MESSAGE"));
                    long time = cursor.getLong(cursor.getColumnIndexOrThrow("TIMESTAMP"));
                    emails.add("From: " + sender + "\nSubject: " + subject + "\n" + body + "\n(" + sdf.format(new Date(time)) + ")");
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("My Personal Email Inbox");
        if (emails.isEmpty()) {
            builder.setMessage("No in-app emails received.");
        } else {
            builder.setItems(emails.toArray(new String[0]), null);
        }
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void showMessenger() {
        ArrayList<String> students = new ArrayList<>();
        ArrayList<String> schoolEmails = new ArrayList<>();
        ArrayList<String> phones = new ArrayList<>();
        try (Cursor cursor = db.getAllStudents()) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("USERNAME"));
                    if (!name.equals(currentUsername)) {
                        students.add(name);
                        schoolEmails.add(cursor.getString(cursor.getColumnIndexOrThrow("EMAIL")));
                        phones.add(cursor.getString(cursor.getColumnIndexOrThrow("PHONE")));
                    }
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Botho Student Directory");
        
        // Add "Contact Admin" as the first option
        students.add(0, "[SYSTEM ADMIN] - For Bookings/Enquiries");
        schoolEmails.add(0, "admin@botho.ac.bw");
        phones.add(0, "555-0100");

        String[] studentArray = students.toArray(new String[0]);
        builder.setItems(studentArray, (dialog, which) -> {
            if (which == 0) {
                openInAppEmailForm("admin"); // Fixed username for Admin
            } else {
                showContactOptions(studentArray[which], schoolEmails.get(which), phones.get(which));
            }
        });
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void showContactOptions(String name, String email, String phone) {
        String[] options = {"Real-Time Chat", "Send Formal In-App Email", "External School Email", "Call Student"};
        new AlertDialog.Builder(this)
                .setTitle("Contact " + name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openChatRoom(name);
                    else if (which == 1) openInAppEmailForm(name);
                    else if (which == 2) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:" + email));
                        startActivity(Intent.createChooser(intent, "Send External Email"));
                    } else if (which == 3) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phone));
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void openInAppEmailForm(String receiver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String displayTitle = receiver.equals("admin") ? "Enquiry to Botho Admin" : "Email to " + receiver;
        builder.setTitle(displayTitle);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        EditText etSubject = new EditText(this);
        etSubject.setHint("Subject (e.g., Appointment Request)");
        EditText etMsg = new EditText(this);
        etMsg.setHint("Type your message here...");
        etMsg.setMinLines(3);
        layout.addView(etSubject);
        layout.addView(etMsg);
        builder.setView(layout);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String sub = etSubject.getText().toString().trim();
            String msg = etMsg.getText().toString().trim();
            if (!sub.isEmpty() && !msg.isEmpty()) {
                db.sendInAppEmail(currentUsername, receiver, sub, msg);
                Toast.makeText(this, "Enquiry Sent!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please fill subject and body", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openChatRoom(String receiver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chat Room: " + receiver);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        TextView tvHistory = new TextView(this);
        tvHistory.setHeight(500);
        tvHistory.setVerticalScrollBarEnabled(true);
        refreshChatHistory(tvHistory, receiver);
        EditText etMsg = new EditText(this);
        etMsg.setHint("Type chat...");
        Button btnSend = new Button(this);
        btnSend.setText("Send Chat");
        btnSend.setOnClickListener(v -> {
            String m = etMsg.getText().toString().trim();
            if (!m.isEmpty()) {
                db.sendMessage(currentUsername, receiver, m);
                etMsg.setText("");
                refreshChatHistory(tvHistory, receiver);
            }
        });
        layout.addView(tvHistory);
        layout.addView(etMsg);
        layout.addView(btnSend);
        builder.setView(layout);
        builder.setPositiveButton("Done", null);
        builder.show();
    }

    private void refreshChatHistory(TextView tv, String receiver) {
        StringBuilder sb = new StringBuilder();
        try (Cursor c = db.getChatHistory(currentUsername, receiver)) {
            if (c != null) {
                while (c.moveToNext()) {
                    String s = c.getString(c.getColumnIndexOrThrow("SENDER"));
                    String m = c.getString(c.getColumnIndexOrThrow("MESSAGE"));
                    sb.append(s.equals(currentUsername) ? "Me: " : s + ": ").append(m).append("\n");
                }
            }
        }
        tv.setText(sb.toString());
    }

    private void showExpoUpdatesInbox() {
        ArrayList<String> notifications = new ArrayList<>();
        Cursor cursor = db.getAllEvents();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        long now = System.currentTimeMillis();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("TITLE"));
                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("DATE"));
                    long modified = cursor.getLong(cursor.getColumnIndexOrThrow("LAST_MODIFIED"));
                    String timeStr = sdf.format(new Date(modified));
                    Date eventDate = dateParser.parse(dateStr);
                    String status = "[NEW EVENT Notification]";
                    if (eventDate != null && eventDate.getTime() < now) status = "[PAST EVENT]";
                    notifications.add(status + "\nFrom: System Admin\nSent: " + timeStr + "\nEvent: " + title + "\nEvent Date: " + dateStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Expo Admin Updates");
        if (notifications.isEmpty()) {
            builder.setMessage("No expo updates found.");
        } else {
            builder.setItems(notifications.toArray(new String[0]), null);
        }
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override protected void onDestroy() { super.onDestroy(); countdownHandler.removeCallbacks(countdownRunnable); stopMusic(); }
}
