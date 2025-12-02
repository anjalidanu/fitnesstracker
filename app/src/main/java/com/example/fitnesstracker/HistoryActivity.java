package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class HistoryActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private TextView tvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DBHelper(this);
        tvHistory = findViewById(R.id.tv_history);

        loadHistory();
    }

    private void loadHistory() {
        StringBuilder historyText = new StringBuilder();
        historyText.append("=== FITNESS HISTORY ===\n\n");

        // Get last 30 days of step data
        Cursor cursor = dbHelper.getRecentSteps(30);

        if (cursor.getCount() == 0) {
            historyText.append("No history available yet.\nStart walking to track your progress!");
        } else {
            int totalSteps = 0;
            double totalCalories = 0;
            double totalDistance = 0;
            int daysCount = 0;

            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow("calories"));
                double distance = cursor.getDouble(cursor.getColumnIndexOrThrow("distance"));

                totalSteps += steps;
                totalCalories += calories;
                totalDistance += distance;
                daysCount++;

                historyText.append(String.format("📅 %s\n", date));
                historyText.append(String.format("   👟 Steps: %d\n", steps));
                historyText.append(String.format("   🔥 Calories: %.2f\n", calories));
                historyText.append(String.format("   📍 Distance: %.2f km\n\n", distance));
            }

            // Add summary
            historyText.append("\n=== SUMMARY ===\n");
            historyText.append(String.format("Total Days: %d\n", daysCount));
            historyText.append(String.format("Total Steps: %d\n", totalSteps));
            historyText.append(String.format("Total Calories: %.2f\n", totalCalories));
            historyText.append(String.format("Total Distance: %.2f km\n", totalDistance));

            if (daysCount > 0) {
                historyText.append(String.format("\nAverage Steps/Day: %d\n", totalSteps / daysCount));
                historyText.append(String.format("Average Calories/Day: %.2f\n", totalCalories / daysCount));
                historyText.append(String.format("Average Distance/Day: %.2f km\n", totalDistance / daysCount));
            }
        }

        cursor.close();
        tvHistory.setText(historyText.toString());
    }
}