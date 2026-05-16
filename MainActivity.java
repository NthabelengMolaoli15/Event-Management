package com.example.nthabelengmolaoli2333784;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgLoginRole;
    private DatabaseHelper db;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgLoginRole = findViewById(R.id.rgLoginRole);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            int selectedRoleId = rgLoginRole.getCheckedRadioButtonId();
            if (selectedRoleId == -1) {
                Toast.makeText(MainActivity.this, "Please select a login role", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rbSelected = findViewById(selectedRoleId);
            String selectedRole = rbSelected.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                try (Cursor cursor = db.checkUser(user, pass)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        String dbRole = cursor.getString(cursor.getColumnIndexOrThrow("ROLE"));

                        if (dbRole.equalsIgnoreCase(selectedRole)) {
                            // Save session
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("USERNAME", user);
                            editor.putString("ROLE", dbRole);
                            editor.apply();

                            Toast.makeText(MainActivity.this, "Login Successful as " + dbRole, Toast.LENGTH_SHORT).show();
                            Intent intent;
                            if (dbRole.equalsIgnoreCase("Admin")) {
                                intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                            } else {
                                intent = new Intent(MainActivity.this, StudentDashBoardActivity.class);
                                intent.putExtra("USERNAME", user);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid role for this user", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        EditText etEmail = dialogView.findViewById(R.id.etForgotEmail);
        EditText etNewPass = dialogView.findViewById(R.id.etForgotNewPass);
        Button btnReset = dialogView.findViewById(R.id.btnResetPassword);

        AlertDialog dialog = builder.create();

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String newPass = etNewPass.getText().toString().trim();

            if (email.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Please enter email and new password", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            } else if (newPass.length() < 8 || !newPass.matches(".*[0-9].*") || !newPass.matches(".*[a-zA-Z].*")) {
                Toast.makeText(this, "Password must be 8+ chars with letters and numbers", Toast.LENGTH_LONG).show();
            } else {
                // Check if user exists with this email
                try (Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM users WHERE EMAIL=?", new String[]{email})) {
                    if (cursor != null && cursor.moveToFirst()) {
                        String username = cursor.getString(cursor.getColumnIndexOrThrow("USERNAME"));
                        String studentId = cursor.getString(cursor.getColumnIndexOrThrow("STUDENT_ID"));
                        String phone = cursor.getString(cursor.getColumnIndexOrThrow("PHONE"));

                        boolean updated = db.updateUserProfile(username, username, email, newPass, studentId, phone);
                        if (updated) {
                            Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Reset failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog.show();
    }
}
