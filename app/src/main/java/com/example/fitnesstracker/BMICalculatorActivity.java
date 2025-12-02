package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BMICalculatorActivity extends AppCompatActivity {

    private EditText etWeight, etHeight;
    private Button btnCalc;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi); // ensure this layout exists

        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        btnCalc = findViewById(R.id.btn_calc);
        tvResult = findViewById(R.id.tv_result);

        btnCalc.setOnClickListener(v -> {
            String wStr = etWeight.getText().toString().trim();
            String hStr = etHeight.getText().toString().trim();

            if (wStr.isEmpty() || hStr.isEmpty()) {
                Toast.makeText(this, "Enter weight and height", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double weight = Double.parseDouble(wStr);
                double heightCm = Double.parseDouble(hStr);
                double heightM = heightCm / 100.0;

                if (heightM <= 0) {
                    Toast.makeText(this, "Enter valid height", Toast.LENGTH_SHORT).show();
                    return;
                }

                double bmi = weight / (heightM * heightM);
                String category;
                if (bmi < 18.5) category = "Underweight";
                else if (bmi < 25) category = "Normal";
                else if (bmi < 30) category = "Overweight";
                else category = "Obese";

                tvResult.setText(String.format("BMI: %.2f (%s)", bmi, category));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
