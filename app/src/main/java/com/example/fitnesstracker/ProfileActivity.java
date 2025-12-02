package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS = "fitnessData";
    private EditText etName, etWeight, etHeight;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // ensure this exists

        etName = findViewById(R.id.et_name);
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        btnSave = findViewById(R.id.btn_save_profile);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        etName.setText(prefs.getString("profile_name", ""));
        etWeight.setText(prefs.getString("profile_weight", ""));
        etHeight.setText(prefs.getString("profile_height", ""));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String weight = etWeight.getText().toString().trim();
            String height = etHeight.getText().toString().trim();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("profile_name", name);
            editor.putString("profile_weight", weight);
            editor.putString("profile_height", height);
            editor.apply();

            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        });
    }
}
