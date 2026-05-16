package com.example.nthabelengmolaoli2333784;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "EventExpo.db";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_REGISTRATIONS = "registrations";
    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_EMAILS = "student_emails";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 150); 
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, EMAIL TEXT, PASSWORD TEXT, ROLE TEXT, STUDENT_ID TEXT, PHONE TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT, DESCRIPTION TEXT, DATE TEXT, TIME TEXT, LOCATION TEXT, TYPE TEXT, IMAGE TEXT, CAPACITY INTEGER DEFAULT 100, CATEGORY TEXT, LAST_MODIFIED INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_REGISTRATIONS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, EVENT_ID INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, SENDER TEXT, RECEIVER TEXT, MESSAGE TEXT, TIMESTAMP INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_EMAILS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, SENDER TEXT, RECEIVER TEXT, SUBJECT TEXT, MESSAGE TEXT, TIMESTAMP INTEGER)");

        ContentValues adminValues = new ContentValues();
        adminValues.put("USERNAME", "admin");
        adminValues.put("EMAIL", "admin@botho.ac.bw");
        adminValues.put("PASSWORD", "admin123");
        adminValues.put("ROLE", "Admin");
        adminValues.put("STUDENT_ID", "ADM001");
        adminValues.put("PHONE", "555-0100");
        db.insert(TABLE_USERS, null, adminValues);

        addDefaultEvent(db, "Assignment Expo", "Showcase of student assignments.", "2026-06-10", "10:00", "Botho Campus, Lesotho", "Future", "assignment", 100, "Academic");
        addDefaultEvent(db, "Model Showcase", "Fashion and modeling event.", "2026-07-15", "14:00", "Botho Campus, Lesotho", "Future", "blackmodel", 100, "Social");
        addDefaultEvent(db, "Farmer's Market", "Organic products from local farmers.", "2024-11-20", "09:00", "Botho Campus, Lesotho", "Past", "canvafarmer", 100, "Business");
    }

    private void addDefaultEvent(SQLiteDatabase db, String title, String desc, String date, String time, String loc, String type, String img, int cap, String cat) {
        ContentValues values = new ContentValues();
        values.put("TITLE", title);
        values.put("DESCRIPTION", desc);
        values.put("DATE", date);
        values.put("TIME", time);
        values.put("LOCATION", loc);
        values.put("TYPE", type);
        values.put("IMAGE", img);
        values.put("CAPACITY", cap);
        values.put("CATEGORY", cat);
        values.put("LAST_MODIFIED", System.currentTimeMillis());
        db.insert(TABLE_EVENTS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMAILS);
        onCreate(db);
    }

    public boolean addUser(String username, String email, String password, String role, String studentId, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USERNAME", username);
        values.put("EMAIL", email);
        values.put("PASSWORD", password);
        values.put("ROLE", role);
        values.put("STUDENT_ID", studentId);
        values.put("PHONE", phone);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public Cursor checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE USERNAME=? AND PASSWORD=?", new String[]{username, password});
    }

    public Cursor getUserData(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE USERNAME=?", new String[]{username});
    }

    public boolean updateUserProfile(String oldUser, String newUser, String newEmail, String newPass, String studentId, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("USERNAME", newUser); v.put("EMAIL", newEmail); v.put("PASSWORD", newPass);
        v.put("STUDENT_ID", studentId); v.put("PHONE", phone);
        return db.update(TABLE_USERS, v, "USERNAME=?", new String[]{oldUser}) > 0;
    }

    public boolean addEvent(String title, String desc, String date, String time, String loc, String type, String img, int cap, String cat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TITLE", title); v.put("DESCRIPTION", desc); v.put("DATE", date); v.put("TIME", time);
        v.put("LOCATION", loc); v.put("TYPE", type); v.put("IMAGE", img);
        v.put("CAPACITY", cap); v.put("CATEGORY", cat); v.put("LAST_MODIFIED", System.currentTimeMillis());
        return db.insert(TABLE_EVENTS, null, v) != -1;
    }

    public boolean updateEvent(int id, String title, String desc, String date, String time, String loc, String type, String img, int cap, String cat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TITLE", title); v.put("DESCRIPTION", desc); v.put("DATE", date); v.put("TIME", time);
        v.put("LOCATION", loc); v.put("TYPE", type); v.put("IMAGE", img);
        v.put("CAPACITY", cap); v.put("CATEGORY", cat); v.put("LAST_MODIFIED", System.currentTimeMillis());
        return db.update(TABLE_EVENTS, v, "ID=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteEvent(String id) {
        return this.getWritableDatabase().delete(TABLE_EVENTS, "ID=?", new String[]{id}) > 0;
    }

    public Cursor getAllEvents() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_EVENTS + " ORDER BY LAST_MODIFIED DESC", null);
    }

    public int getEventRegistrationCount(int eventId) {
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLE_REGISTRATIONS + " WHERE EVENT_ID=?", new String[]{String.valueOf(eventId)});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getEventRegistrationCountForUser(String username) {
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLE_REGISTRATIONS + " WHERE USERNAME=?", new String[]{username});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int registerForEventWithStatus(String username, int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor ev = db.rawQuery("SELECT TYPE, CAPACITY FROM " + TABLE_EVENTS + " WHERE ID=?", new String[]{String.valueOf(eventId)});
        if (ev != null && ev.moveToFirst()) {
            String type = ev.getString(0);
            int capacity = ev.getInt(1);
            ev.close();
            if ("Past".equalsIgnoreCase(type)) return 3;
            if (getEventRegistrationCount(eventId) >= capacity) return 2;
        }
        try (Cursor regCursor = db.rawQuery("SELECT * FROM " + TABLE_REGISTRATIONS + " WHERE USERNAME=? AND EVENT_ID=?", new String[]{username, String.valueOf(eventId)})) {
            if (regCursor != null && regCursor.getCount() > 0) return 1;
        }
        ContentValues values = new ContentValues();
        values.put("USERNAME", username);
        values.put("EVENT_ID", eventId);
        return (db.insert(TABLE_REGISTRATIONS, null, values) != -1) ? 0 : -1;
    }

    public boolean unregisterFromEvent(String username, int eventId) {
        return this.getWritableDatabase().delete(TABLE_REGISTRATIONS, "USERNAME=? AND EVENT_ID=?", new String[]{username, String.valueOf(eventId)}) > 0;
    }

    public boolean clearUserRegistrations(String username) {
        return this.getWritableDatabase().delete(TABLE_REGISTRATIONS, "USERNAME=?", new String[]{username}) > 0;
    }

    public Cursor getRegisteredEvents(String username) {
        String query = "SELECT e.* FROM " + TABLE_EVENTS + " e JOIN " + TABLE_REGISTRATIONS + " r ON e.ID = r.EVENT_ID WHERE r.USERNAME = ?";
        return this.getReadableDatabase().rawQuery(query, new String[]{username});
    }

    public Cursor getAllStudents() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE ROLE='Student'", null);
    }

    public boolean sendMessage(String sender, String receiver, String message) {
        ContentValues values = new ContentValues();
        values.put("SENDER", sender); values.put("RECEIVER", receiver);
        values.put("MESSAGE", message); values.put("TIMESTAMP", System.currentTimeMillis());
        return this.getWritableDatabase().insert(TABLE_MESSAGES, null, values) != -1;
    }

    public Cursor getChatHistory(String user1, String user2) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE (SENDER=? AND RECEIVER=?) OR (SENDER=? AND RECEIVER=?) ORDER BY TIMESTAMP ASC", new String[]{user1, user2, user2, user1});
    }

    public boolean sendInAppEmail(String sender, String receiver, String subject, String message) {
        ContentValues values = new ContentValues();
        values.put("SENDER", sender); values.put("RECEIVER", receiver);
        values.put("SUBJECT", subject); values.put("MESSAGE", message);
        values.put("TIMESTAMP", System.currentTimeMillis());
        return this.getWritableDatabase().insert(TABLE_EMAILS, null, values) != -1;
    }

    public Cursor getReceivedEmails(String receiver) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_EMAILS + " WHERE RECEIVER=? ORDER BY TIMESTAMP DESC", new String[]{receiver});
    }
}
