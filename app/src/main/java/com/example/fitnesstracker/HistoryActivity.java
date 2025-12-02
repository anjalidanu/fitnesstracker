package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class HistoryActivity extends AppCompatActivity {

    private static final String PREFS = "fitnessData";
    private TextView tvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history); // ensure this exists

        tvHistory = findViewById(R.id.tv_history);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        int lastSavedTotal = prefs.getInt("lastSavedTotalSteps", 0);
        int baseline = prefs.getInt("previousSteps", 0);
        int stepsToday = Math.max(0, lastSavedTotal - baseline);

        String text = "Last saved total steps (since boot): " + lastSavedTotal + "\n"
                + "Baseline (saved earlier): " + baseline + "\n"
                + "Estimated steps today: " + stepsToday + "\n\n"
                + "(This simple history shows the most recent saved snapshot. For full daily history use a DB.)";

        tvHistory.setText(text);
    }
}
