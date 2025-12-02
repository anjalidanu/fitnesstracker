package com.example.fitnesstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int REQ_CODE_ACTIVITY_RECOGNITION = 1001;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private DBHelper dbHelper;

    private TextView tvSteps, tvCalories, tvDistance, tvSensorStatus;
    private Button btnOpenBMI, btnOpenHistory, btnOpenWorkouts, btnOpenProfile;
    private EditText etManualSteps;
    private Button btnAddSteps;

    private boolean isRunning = false;
    private boolean sensorAvailable = false;
    private int totalSteps = 0;
    private int previousSteps = 0;
    private int manualSteps = 0; // For devices without sensor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        tvSteps = findViewById(R.id.tv_steps);
        tvCalories = findViewById(R.id.tv_calories);
        tvDistance = findViewById(R.id.tv_distance);
        tvSensorStatus = findViewById(R.id.tv_sensor_status);

        btnOpenBMI = findViewById(R.id.btn_open_bmi);
        btnOpenHistory = findViewById(R.id.btn_open_history);
        btnOpenWorkouts = findViewById(R.id.btn_open_workouts);
        btnOpenProfile = findViewById(R.id.btn_open_profile);

        etManualSteps = findViewById(R.id.et_manual_steps);
        btnAddSteps = findViewById(R.id.btn_add_steps);

        btnOpenBMI.setOnClickListener(v -> startActivity(new Intent(this, BMICalculatorActivity.class)));
        btnOpenHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnOpenWorkouts.setOnClickListener(v -> startActivity(new Intent(this, WorkoutListActivity.class)));
        btnOpenProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Check if sensor is available
        if (stepSensor == null) {
            sensorAvailable = false;
            tvSensorStatus.setText("⚠️ Step sensor not available - Using manual input mode");
            etManualSteps.setVisibility(android.view.View.VISIBLE);
            btnAddSteps.setVisibility(android.view.View.VISIBLE);

            // Setup manual step input
            btnAddSteps.setOnClickListener(v -> addManualSteps());
        } else {
            sensorAvailable = true;
            tvSensorStatus.setText("✓ Step sensor active");
            etManualSteps.setVisibility(android.view.View.GONE);
            btnAddSteps.setVisibility(android.view.View.GONE);
        }

        loadData();

        // Request permission at runtime if needed
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
        loadTodayData();

        if (sensorAvailable && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        saveData();
        if (sensorManager != null && sensorAvailable) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isRunning || !sensorAvailable) return;
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = (int) event.values[0];
            int currentSteps = totalSteps - previousSteps;
            if (currentSteps < 0) currentSteps = 0;

            updateDisplay(currentSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not needed
    }

    private void addManualSteps() {
        String stepsStr = etManualSteps.getText().toString().trim();
        if (stepsStr.isEmpty()) {
            Toast.makeText(this, "Please enter steps", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int steps = Integer.parseInt(stepsStr);
            if (steps <= 0) {
                Toast.makeText(this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                return;
            }

            manualSteps += steps;
            updateDisplay(manualSteps);
            saveData();
            etManualSteps.setText("");
            Toast.makeText(this, steps + " steps added!", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDisplay(int currentSteps) {
        tvSteps.setText("Steps: " + currentSteps);

        double calories = currentSteps * 0.04;
        double distanceKm = currentSteps * 0.0008;

        tvCalories.setText("Calories: " + String.format("%.2f", calories));
        tvDistance.setText("Distance: " + String.format("%.2f", distanceKm) + " km");
    }

    private void saveData() {
        if (sensorAvailable) {
            dbHelper.saveSetting("previousSteps", String.valueOf(totalSteps));
            dbHelper.saveSetting("lastSavedTotalSteps", String.valueOf(totalSteps));

            int currentSteps = Math.max(0, totalSteps - previousSteps);
            double calories = currentSteps * 0.04;
            double distanceKm = currentSteps * 0.0008;

            dbHelper.insertOrUpdateSteps(currentSteps, calories, distanceKm);
        } else {
            // Save manual steps
            dbHelper.saveSetting("manualSteps", String.valueOf(manualSteps));

            double calories = manualSteps * 0.04;
            double distanceKm = manualSteps * 0.0008;

            dbHelper.insertOrUpdateSteps(manualSteps, calories, distanceKm);
        }
    }

    private void loadData() {
        if (sensorAvailable) {
            String prevStepsStr = dbHelper.getSetting("previousSteps", "0");
            previousSteps = Integer.parseInt(prevStepsStr);
        } else {
            String manualStepsStr = dbHelper.getSetting("manualSteps", "0");
            manualSteps = Integer.parseInt(manualStepsStr);
        }
    }

    private void loadTodayData() {
        Cursor cursor = dbHelper.getTodaySteps();
        if (cursor.moveToFirst()) {
            int steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
            double calories = cursor.getDouble(cursor.getColumnIndexOrThrow("calories"));
            double distance = cursor.getDouble(cursor.getColumnIndexOrThrow("distance"));

            if (!sensorAvailable) {
                manualSteps = steps;
            }

            tvSteps.setText("Steps: " + steps);
            tvCalories.setText("Calories: " + String.format("%.2f", calories));
            tvDistance.setText("Distance: " + String.format("%.2f", distance) + " km");
        } else {
            updateDisplay(0);
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Activity recognition permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied — step counter may not work", Toast.LENGTH_LONG).show();
            }
        }
    }
}