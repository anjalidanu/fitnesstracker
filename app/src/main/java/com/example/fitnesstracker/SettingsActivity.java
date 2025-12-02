package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private Switch swNotifications;
    private Button btnResetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DBHelper(this);

        swNotifications = findViewById(R.id.switch_notifications);
        btnResetData = findViewById(R.id.btn_reset_data);

        // Load notification setting from database
        String notifEnabled = dbHelper.getSetting("notifications", "true");
        swNotifications.setChecked(notifEnabled.equals("true"));

        swNotifications.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            dbHelper.saveSetting("notifications", String.valueOf(isChecked));
            Toast.makeText(this, "Notifications: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        btnResetData.setOnClickListener(v -> {
            // Reset all data from database
            dbHelper.resetAllData();
            Toast.makeText(this, "All fitness data has been reset!", Toast.LENGTH_SHORT).show();
        });
    }
}