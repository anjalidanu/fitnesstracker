package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WorkoutListActivity extends AppCompatActivity {

    private ListView listView;
    private String[] workouts = {
            "Running - 20 mins",
            "Push Ups - 3 x 12",
            "Squats - 3 x 15",
            "Plank - 3 x 60s",
            "Jumping Jacks - 3 x 50",
            "Lunges - 3 x 12"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list); // ensure this exists

        listView = findViewById(R.id.list_workouts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, workouts);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            Intent i = new Intent(WorkoutListActivity.this, WorkoutDetailActivity.class);
            i.putExtra("title", workouts[position]);
            // pass a simple description
            i.putExtra("desc", "Details and tips for: " + workouts[position]);
            startActivity(i);
        });
    }
}
