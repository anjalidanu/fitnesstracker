package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS = "fitnessData";
    private Switch swNotifications;
    private Button btnResetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // ensure this exists

        swNotifications = findViewById(R.id.switch_notifications);
        btnResetData = findViewById(R.id.btn_reset_data);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean notifEnabled = prefs.getBoolean("notifications", true);
        swNotifications.setChecked(notifEnabled);

        swNotifications.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notifications", isChecked);
            editor.apply();
            Toast.makeText(this, "Notifications: " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        btnResetData.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("previousSteps");
            editor.remove("lastSavedTotalSteps");
            editor.apply();
            Toast.makeText(this, "Saved data reset", Toast.LENGTH_SHORT).show();
        });
    }
}
