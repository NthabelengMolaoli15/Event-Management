package com.example.nthabelengmolaoli2333784;

import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegEmail, etRegPassword, etRegConfirmPassword, etRegStudentID, etRegPhone;
    private RadioGroup rgRole;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Registration");
        }

        db = new DatabaseHelper(this);
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegStudentID = findViewById(R.id.etRegStudentID);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String user = etRegUsername.getText().toString().trim();
            String studentId = etRegStudentID.getText().toString().trim();
            String phone = etRegPhone.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim();
            String pass = etRegPassword.getText().toString().trim();
            String confirmPass = etRegConfirmPassword.getText().toString().trim();

            int selectedRoleId = rgRole.getCheckedRadioButtonId();
            if (selectedRoleId == -1) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton rbSelected = findViewById(selectedRoleId);
            String role = rbSelected.getText().toString();

            if (user.isEmpty() || studentId.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format. Use example@botho.ac.bw", Toast.LENGTH_SHORT).show();
            } else if (pass.length() < 8 || !pass.matches(".*[0-9].*") || !pass.matches(".*[a-zA-Z].*")) {
                Toast.makeText(this, "Password must be 8+ characters with letters and numbers", Toast.LENGTH_LONG).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(RegistrationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Database version 19 requires 6 parameters
                boolean success = db.addUser(user, email, pass, role, studentId, phone);
                if (success) {
                    Toast.makeText(RegistrationActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegistrationActivity.this, "Registration failed. Username might already exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
