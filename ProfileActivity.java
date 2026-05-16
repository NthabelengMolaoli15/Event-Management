package com.example.nthabelengmolaoli2333784;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    EditText etUsername, etEmail, etPassword, etStudentID, etPhone;
    Button btnUpdate, btnClearReg;
    GridView gvMyRegs;
    DatabaseHelper db;
    String currentUsername;
    ArrayList<String> regEventList;
    ArrayList<Integer> regEventIds;
    ArrayAdapter<String> regAdapter;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        db = new DatabaseHelper(this);
        sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        
        etUsername = findViewById(R.id.etProfileUsername);
        etEmail = findViewById(R.id.etProfileEmail);
        etPassword = findViewById(R.id.etProfilePassword);
        etStudentID = findViewById(R.id.etProfileStudentID);
        etPhone = findViewById(R.id.etProfilePhone);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        btnClearReg = findViewById(R.id.btnClearRegistrations);
        gvMyRegs = findViewById(R.id.gvMyRegistrations);

        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) {
            currentUsername = sp.getString("USERNAME", "");
        }

        regEventList = new ArrayList<>();
        regEventIds = new ArrayList<>();

        loadProfileData();
        loadMyRegistrations();

        btnUpdate.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();
            String studentId = etStudentID.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty() || newPassword.isEmpty() || studentId.isEmpty() || phone.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Invalid email format. Use example@botho.ac.bw", Toast.LENGTH_SHORT).show();
            } else if (!isPasswordStrong(newPassword)) {
                Toast.makeText(this, "Weak password! Use at least 8 characters with letters and numbers", Toast.LENGTH_LONG).show();
            } else {
                boolean success = db.updateUserProfile(currentUsername, newUsername, newEmail, newPassword, studentId, phone);
                if (success) {
                    Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    
                    if (!currentUsername.equals(newUsername)) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("USERNAME", newUsername);
                        editor.apply();
                        currentUsername = newUsername;
                    }
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Update Failed. Username might be taken.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnClearReg.setOnClickListener(v -> {
            if (db.clearUserRegistrations(currentUsername)) {
                loadMyRegistrations();
                Toast.makeText(this, "All registrations cleared", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No registrations to clear", Toast.LENGTH_SHORT).show();
            }
        });

        gvMyRegs.setOnItemLongClickListener((parent, view, position, id) -> {
            db.unregisterFromEvent(currentUsername, regEventIds.get(position));
            loadMyRegistrations();
            Toast.makeText(this, "Unregistered from " + regEventList.get(position), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 && password.matches(".*[a-zA-Z].*") && password.matches(".*[0-9].*");
    }

    private void loadProfileData() {
        try (Cursor cursor = db.getUserData(currentUsername)) {
            if (cursor != null && cursor.moveToFirst()) {
                etUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow("USERNAME")));
                etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("EMAIL")));
                etPassword.setText(cursor.getString(cursor.getColumnIndexOrThrow("PASSWORD")));
                etStudentID.setText(cursor.getString(cursor.getColumnIndexOrThrow("STUDENT_ID")));
                etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("PHONE")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMyRegistrations() {
        regEventList.clear();
        regEventIds.clear();
        try (Cursor cursor = db.getRegisteredEvents(currentUsername)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    regEventList.add(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));
                    regEventIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        regAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, regEventList);
        gvMyRegs.setAdapter(regAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home || id == R.id.menu_go_back) {
            finish();
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
            startActivity(new Intent(this, MapActivity.class).putExtra("LOCATION_NAME", "Botho University, Lesotho"));
            return true;
        } else if (id == R.id.menu_media) {
            startActivity(new Intent(this, MediaActivity.class));
            return true;
        } else if (id == R.id.menu_bluetooth) {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return true;
        } else if (id == R.id.menu_logout) {
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPersonalEmailInbox() {
        ArrayList<String> emails = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        try (Cursor cursor = db.getReceivedEmails(currentUsername)) {
            while (cursor != null && cursor.moveToNext()) {
                String sender = cursor.getString(cursor.getColumnIndexOrThrow("SENDER"));
                String subject = cursor.getString(cursor.getColumnIndexOrThrow("SUBJECT"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("MESSAGE"));
                long time = cursor.getLong(cursor.getColumnIndexOrThrow("TIMESTAMP"));
                emails.add("From: " + sender + "\nSub: " + subject + "\n" + body + "\n(" + sdf.format(new Date(time)) + ")");
            }
        }
        new AlertDialog.Builder(this).setTitle("Personal Inbox").setItems(emails.toArray(new String[0]), null).setPositiveButton("Close", null).show();
    }

    private void showMessenger() {
        ArrayList<String> students = new ArrayList<>();
        ArrayList<String> emails = new ArrayList<>();
        ArrayList<String> phones = new ArrayList<>();
        try (Cursor c = db.getAllStudents()) {
            while (c != null && c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow("USERNAME"));
                if (!name.equals(currentUsername)) {
                    students.add(name);
                    emails.add(c.getString(c.getColumnIndexOrThrow("EMAIL")));
                    phones.add(c.getString(c.getColumnIndexOrThrow("PHONE")));
                }
            }
        }
        students.add(0, "[ADMIN]"); emails.add(0, "admin@botho.ac.bw"); phones.add(0, "555-0100");
        new AlertDialog.Builder(this).setTitle("Messenger").setItems(students.toArray(new String[0]), (d, w) -> {
            if (w == 0) openInAppEmailForm("admin");
            else showContactOptions(students.get(w), emails.get(w), phones.get(w));
        }).setPositiveButton("Close", null).show();
    }

    private void showContactOptions(String name, String email, String phone) {
        String[] opts = {"Chat", "In-App Email", "External Email", "Call"};
        new AlertDialog.Builder(this).setTitle(name).setItems(opts, (d, w) -> {
            if (w == 0) openChatRoom(name);
            else if (w == 1) openInAppEmailForm(name);
            else if (w == 2) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                startActivity(intent);
            }
            else if (w == 3) startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
        }).show();
    }

    private void openInAppEmailForm(String rec) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(30, 30, 30, 30);
        EditText sub = new EditText(this); sub.setHint("Subject");
        EditText msg = new EditText(this); msg.setHint("Message"); msg.setMinLines(3);
        l.addView(sub); l.addView(msg);
        b.setTitle("To: " + rec).setView(l).setPositiveButton("Send", (d, w) -> {
            if (db.sendInAppEmail(currentUsername, rec, sub.getText().toString(), msg.getText().toString())) Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show();
        }).setNegativeButton("Cancel", null).show();
    }

    private void openChatRoom(String rec) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(30, 30, 30, 30);
        TextView h = new TextView(this); h.setHeight(400); refreshChat(h, rec);
        EditText m = new EditText(this); m.setHint("Chat...");
        Button s = new Button(this); s.setText("Send");
        s.setOnClickListener(v -> { if (db.sendMessage(currentUsername, rec, m.getText().toString())) { m.setText(""); refreshChat(h, rec); } });
        l.addView(h); l.addView(m); l.addView(s);
        b.setTitle("Chat: " + rec).setView(l).setPositiveButton("Done", null).show();
    }

    private void refreshChat(TextView tv, String rec) {
        StringBuilder sb = new StringBuilder();
        try (Cursor c = db.getChatHistory(currentUsername, rec)) {
            while (c != null && c.moveToNext()) {
                String snd = c.getString(c.getColumnIndexOrThrow("SENDER"));
                sb.append(snd.equals(currentUsername) ? "Me: " : snd + ": ").append(c.getString(c.getColumnIndexOrThrow("MESSAGE"))).append("\n");
            }
        }
        tv.setText(sb.toString());
    }

    private void showExpoUpdatesInbox() {
        ArrayList<String> updates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        try (Cursor c = db.getAllEvents()) {
            while (c != null && c.moveToNext()) {
                updates.add("[NOTIFICATION]\nEvent: " + c.getString(c.getColumnIndexOrThrow("TITLE")) + "\nDate: " + c.getString(c.getColumnIndexOrThrow("DATE")) + "\nModified: " + sdf.format(new Date(c.getLong(c.getColumnIndexOrThrow("LAST_MODIFIED")))));
            }
        }
        new AlertDialog.Builder(this).setTitle("Expo Updates").setItems(updates.toArray(new String[0]), null).setPositiveButton("Close", null).show();
    }
}
