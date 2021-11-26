package com.example.heartstrawngv1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;

public class ShowWorkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workout);

        Spinner workoutNames = findViewById(R.id.workout_list);
        Spinner exerciseNamesSpinner = findViewById(R.id.exercise_list);
        AnyChartView chartView = findViewById(R.id.graph_view);

        Bundle extras = getIntent().getExtras();
        HashMap<String, ArrayList<String>> fullWorkouts;
        try {
            fullWorkouts = (HashMap<String, ArrayList<String>>) extras.getSerializable("workouts");
        } catch (Exception e) {
            fullWorkouts = new HashMap<>();
        }

        ArrayList<String> workoutNamesList = new ArrayList<>(fullWorkouts.keySet());

        ArrayAdapter<String> workoutNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, workoutNamesList);
        workoutNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutNames.setAdapter(workoutNameAdapter);
        HashMap<String, ArrayList<String>> finalFullWorkouts = fullWorkouts;
        workoutNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String workoutName = parent.getItemAtPosition(position).toString();

                ArrayList<String> exerciseNames = new ArrayList<>();
                for (String s : finalFullWorkouts.get(workoutName)) {
                    try {
                        JSONObject o = new JSONObject(s);
                        exerciseNames.add(o.getString("exercise"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ArrayAdapter<String> exerciseNameAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, exerciseNames);
                exerciseNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                exerciseNamesSpinner.setAdapter(exerciseNameAdapter);
                exerciseNamesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String tutorialsName = parent.getItemAtPosition(position).toString();
                        Toast.makeText(parent.getContext(), "Selected: " + tutorialsName, Toast.LENGTH_LONG).show();


                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}