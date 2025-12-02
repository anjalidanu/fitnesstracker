package com.example.fitnesstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int REQ_CODE_ACTIVITY_RECOGNITION = 1001;
    private static final String PREFS = "fitnessData";

    private SensorManager sensorManager;
    private Sensor stepSensor;

    private TextView tvSteps, tvCalories, tvDistance;
    private Button btnOpenBMI, btnOpenHistory, btnOpenWorkouts, btnOpenProfile, btnOpenSettings;

    private boolean isRunning = false;
    private int totalSteps = 0;
    private int previousSteps = 0; // from SharedPreferences (baseline)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ensure this exists

        tvSteps = findViewById(R.id.tv_steps);
        tvCalories = findViewById(R.id.tv_calories);
        tvDistance = findViewById(R.id.tv_distance);

        btnOpenBMI = findViewById(R.id.btn_open_bmi);
        btnOpenHistory = findViewById(R.id.btn_open_history);
        btnOpenWorkouts = findViewById(R.id.btn_open_workouts);
        btnOpenProfile = findViewById(R.id.btn_open_profile);
        btnOpenSettings = findViewById(R.id.btn_open_settings);

        btnOpenBMI.setOnClickListener(v -> startActivity(new Intent(this, BMICalculatorActivity.class)));
        btnOpenHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnOpenWorkouts.setOnClickListener(v -> startActivity(new Intent(this, WorkoutListActivity.class)));
        btnOpenProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnOpenSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        loadData();

        // request permission at runtime if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        REQ_CODE_ACTIVITY_RECOGNITION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            tvSteps.setText("Steps: Sensor not available");
            Toast.makeText(this, "Step sensor not available on this device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        saveData();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isRunning) return;
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // event.values[0] returns total steps since last device boot (float)
            totalSteps = (int) event.values[0];
            int currentSteps = totalSteps - previousSteps;
            if (currentSteps < 0) currentSteps = 0;

            tvSteps.setText("Steps: " + currentSteps);

            // approximate calories and distance
            double calories = currentSteps * 0.04; // rough estimate
            double distanceKm = currentSteps * 0.0008; // 0.8 meter per step

            tvCalories.setText("Calories: " + String.format("%.2f", calories));
            tvDistance.setText("Distance: " + String.format("%.2f", distanceKm) + " km");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not needed for this simple app
    }

    private void saveData() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Save the baseline totalSteps so currentSteps = totalSteps - baseline
        editor.putInt("previousSteps", totalSteps);
        // Additionally store lastSavedTotal for history/summary
        editor.putInt("lastSavedTotalSteps", totalSteps);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        previousSteps = prefs.getInt("previousSteps", 0);
    }

    // handle runtime permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // OK — permission granted. Sensor registration will be done in onResume
                Toast.makeText(this, "Activity recognition permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied — step counter may not work", Toast.LENGTH_LONG).show();
            }
        }
    }
}
