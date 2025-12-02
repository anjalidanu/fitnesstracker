package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private EditText etName, etWeight, etHeight;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);

        etName = findViewById(R.id.et_name);
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        btnSave = findViewById(R.id.btn_save_profile);

        // Load existing profile data
        loadProfile();

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();

            if (name.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double weight = Double.parseDouble(weightStr);
                double height = Double.parseDouble(heightStr);

                if (weight <= 0 || height <= 0) {
                    Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.insertOrUpdateProfile(name, weight, height);
                if (success) {
                    Toast.makeText(this, "Profile saved to database!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        Cursor cursor = dbHelper.getProfile();
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));
            double height = cursor.getDouble(cursor.getColumnIndexOrThrow("height"));

            etName.setText(name);
            etWeight.setText(String.valueOf(weight));
            etHeight.setText(String.valueOf(height));
        }
        cursor.close();
    }
}