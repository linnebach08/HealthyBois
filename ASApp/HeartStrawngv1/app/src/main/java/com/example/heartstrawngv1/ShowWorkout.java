package com.example.heartstrawngv1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.core.scatter.series.Marker;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Scatter;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.graphics.vector.Stroke;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.lang.reflect.Array;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ShowWorkout extends AppCompatActivity {
    AnyChartView chartView;
    Marker marker;
    boolean firstLoad = true;
    private static final String TAG = "ShowWorkout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workout);

        FloatingActionButton fab = findViewById(R.id.show_workout_back_btn);
        fab.setOnClickListener(view -> finish());

        Spinner workoutNames = findViewById(R.id.workout_list);
        Spinner exerciseNamesSpinner = findViewById(R.id.exercise_list);
        chartView = findViewById(R.id.show_workout_graph);

        Bundle extras = getIntent().getExtras();
        HashMap<String, ArrayList<String>> fullWorkouts;
        String response;
        ArrayList<String> workoutIDs;
        try {
            fullWorkouts = extras.getSerializable("workouts");
            response = extras.getString("response");
            workoutIDs = extras.getStringArrayList("workoutIDs");
        } catch (Exception e) {
            fullWorkouts = new HashMap<>();
            response = "";
            workoutIDs = new ArrayList<>();
        }

        JSONArray responseArray;
        try {
            responseArray = new JSONArray(response);
        } catch (JSONException e) {
            responseArray = new JSONArray();
            e.printStackTrace();
        }
        Log.d(TAG, "FullWorkoutsKeys " + fullWorkouts.keySet());
        Log.d(TAG, "FullWorkouts " + fullWorkouts);
        ArrayList<String> workoutNamesList = new ArrayList<>(fullWorkouts.keySet());
        Log.d(TAG, "WorkoutNamesList " + workoutNamesList);
        final int[] currWorkoutId = new int[1];
        HashMap<String, ArrayList<String>> currentWeightVals = new HashMap<>();
        HashMap<String, ArrayList<String>> currentRepsVals = new HashMap<>();
        ArrayList<String> weightVals = new ArrayList<>();
        ArrayList<String> repsVals = new ArrayList<>();

        ArrayAdapter<String> workoutNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, workoutNamesList);
        workoutNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutNames.setAdapter(workoutNameAdapter);
        HashMap<String, ArrayList<String>> finalFullWorkouts = fullWorkouts;
        ArrayList<String> finalWorkoutIDs = workoutIDs;
        JSONArray finalResponseArray = responseArray;

        workoutNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String workoutName = parent.getItemAtPosition(position).toString();
                currWorkoutId[0] = Integer.parseInt(finalWorkoutIDs.get(position));
                Log.d(TAG, "FinalWorkoutIds: " + finalWorkoutIDs);
                Log.d(TAG, "Position: " + position);
                Log.d(TAG, "CurrWorkoutId: " + currWorkoutId[0]);

                weightVals.clear();
                repsVals.clear();
                currentWeightVals.clear();
                currentRepsVals.clear();

                ArrayList<String> exerciseNames = new ArrayList<>();
                String prev = "";
                for (int i = 0; i < finalFullWorkouts.get(workoutName).size(); i++) {
                    try {
                        JSONObject o = new JSONObject(finalFullWorkouts.get(workoutName).get(i));
                        Log.d("ShowWorkout", "Current Object: " + o);
                        if (i == 0) {
                            exerciseNames.add(o.getString("exercise"));
                            prev = o.getString("exercise");
                            String weight;
                            try {
                                weight = String.valueOf(o.getInt("weightValue"));
                            } catch (JSONException e) {
                                weight = "";
                            }
                            weightVals.add(weight);
                            repsVals.add(String.valueOf(o.getInt("repsTimeOrDistanceValue")));
                        }
                        else {
                            if (!prev.equals(o.getString("exercise"))) {
                                ArrayList<String> tempWeights = new ArrayList<>(weightVals);
                                ArrayList<String> tempReps = new ArrayList<>(repsVals);
                                currentWeightVals.put(prev, tempWeights);
                                currentRepsVals.put(prev, tempReps);
                                Log.d(TAG, "CurrentWeightVals1 " + currentWeightVals);
                                Log.d(TAG, "CurrentRepsVals1 " + currentRepsVals);
                                weightVals.clear();
                                repsVals.clear();
                                exerciseNames.add(o.getString("exercise"));
                                prev = o.getString("exercise");
                                String weight;
                                try {
                                    weight = String.valueOf(o.getInt("weightValue"));
                                } catch (JSONException e) {
                                    weight = "";
                                }
                                weightVals.add(weight);
                                repsVals.add(String.valueOf(o.getInt("repsTimeOrDistanceValue")));
                            }
                            else {
                                String weight;
                                try {
                                    weight = String.valueOf(o.getInt("weightValue"));
                                } catch (JSONException e) {
                                    weight = "";
                                }
                                weightVals.add(weight);
                                repsVals.add(String.valueOf(o.getInt("repsTimeOrDistanceValue")));
                            }
                        }

                        if (i == finalFullWorkouts.get(workoutName).size() - 1) {
                            currentWeightVals.put(prev, weightVals);
                            currentRepsVals.put(prev, repsVals);
                            Log.d(TAG, "CurrentWeightVals " + currentWeightVals);
                            Log.d(TAG, "CurrentRepsVals " + currentRepsVals);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ArrayAdapter<String> exerciseNameAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, exerciseNames);
                exerciseNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                exerciseNamesSpinner.setAdapter(exerciseNameAdapter);
                exerciseNamesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String exerciseName = parent.getItemAtPosition(position).toString();

                        //IT"S IN THE RESPONSE, GET WORKOUTID FROM FULLEXERCISES
                        for (int i = 0; i < finalResponseArray.length(); i++) {
                            try {
                                JSONObject o = finalResponseArray.getJSONObject(i);
                                JSONObject o2 = o.getJSONObject("workoutID");
                                Log.d("SHOWWORKOUT", "name: " + exerciseName);
                                Log.d("SHOWWORKOUT", "reps: " + currentRepsVals);
                                Log.d("SHOWWORKOUT", "weight: " + currentWeightVals);
                                Log.d(TAG, "Object workoutID: " + o2.getInt("workoutId"));
                                Log.d(TAG, "Current workoutID: " + currWorkoutId[0]);
                                if (currWorkoutId[0] == o2.getInt("workoutId")) {
                                    Log.d(TAG, "Scheduled workout found");
                                    showGraph(exerciseName, o.getString("date"),currentRepsVals.get(exerciseName), currentWeightVals.get(exerciseName));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showGraph(String title, String date, ArrayList<String> reps, ArrayList<String> weights) {
        Scatter scatter = AnyChart.scatter();

        scatter.animation(true);

        scatter.padding(10d, 20d, 5d, 20d);

        scatter.title(title);

        scatter.xScale()
                .minimum(1.5d)
                .maximum(5.5d);
        scatter.yScale()
                .minimum(40d)
                .maximum(100d);

        scatter.tooltip().displayMode(TooltipDisplayMode.UNION);

        List<DataEntry> lineVals = new ArrayList<>();
        for (int i = 0; i < reps.size(); i++) {
            if (weights.get(i).equals("")) {
                scatter.yAxis(0).title("Reps/Time/Distance");
                scatter.xAxis(0).title("Date");
                ValueDataEntry d = new ValueDataEntry(date, Integer.parseInt(reps.get(i)));
                lineVals.add(d);
            }
            else {
                scatter.yAxis(0).title("Weight");
                scatter.xAxis(0).title("Reps");
                ValueDataEntry d = new ValueDataEntry(Integer.parseInt(reps.get(i)), Integer.parseInt(weights.get(i)));
                lineVals.add(d);
            }
        }

        marker = scatter.marker(lineVals);
        marker.type(MarkerType.TRIANGLE_UP)
                .size(4d);

        if (firstLoad) {
            chartView.setChart(scatter);
            firstLoad = false;
        }

    }
}