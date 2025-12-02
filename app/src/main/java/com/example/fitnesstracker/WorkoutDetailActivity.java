package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class WorkoutDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail); // ensure this layout exists

        tvTitle = findViewById(R.id.tv_workout_title);
        tvDesc = findViewById(R.id.tv_workout_desc);

        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("desc");

        if (title != null) tvTitle.setText(title);
        if (desc != null) tvDesc.setText(desc);
    }
}
