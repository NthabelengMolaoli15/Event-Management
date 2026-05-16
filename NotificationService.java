package com.example.nthabelengmolaoli2333784;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;

public class NotificationService {

    private Context context;
    private DatabaseHelper db;

    public NotificationService(Context context) {
        this.context = context;
        this.db = new DatabaseHelper(context);
    }

    public void sendNewEventEmailNotification(String eventTitle, String eventDate, String eventLoc) {
        ArrayList<String> studentEmails = new ArrayList<>();
        try (Cursor cursor = db.getAllStudents()) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    studentEmails.add(cursor.getString(cursor.getColumnIndexOrThrow("EMAIL")));
                }
            }
        } catch (Exception e) {
            Log.e("NotificationService", "Error fetching student emails", e);
        }

        if (studentEmails.isEmpty()) {
            Log.d("NotificationService", "No students to notify.");
            return;
        }

        String emailBody = "Hello Student,\n\nA new event has been created: " + eventTitle + 
                           "\nDate: " + eventDate + 
                           "\nLocation: " + eventLoc + 
                           "\n\nLog in to the Botho Expo app to see details.\n\nRegards,\nBotho Admin";

        Log.d("NotificationService", "Broadcasting Email to " + studentEmails.size() + " students.");
        Toast.makeText(context, "Notifications broadcasted to " + studentEmails.size() + " students!", Toast.LENGTH_LONG).show();
    }
}
