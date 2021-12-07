package com.example.heartstrawngv1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.anychart.core.ui.Label;
import com.anychart.data.Mapping;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.graphics.vector.text.HAlign;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.graphics.vector.SolidFill;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ShowWorkout extends AppCompatActivity {
    AnyChartView chartView;
    //Marker marker;
    Scatter scatter;
    Spinner dateSpinner;
    ConstraintLayout layout;
    boolean firstLoad = true;
    private static final String TAG = "ShowWorkout";
    final String[] selectedDate = {""};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_workout);

        FloatingActionButton fab = findViewById(R.id.show_workout_back_btn);
        fab.setOnClickListener(view -> finish());

        Spinner workoutNames = findViewById(R.id.workout_list);
        Spinner exerciseNamesSpinner = findViewById(R.id.exercise_list);
        dateSpinner = findViewById(R.id.workout_history_dates);
        chartView = findViewById(R.id.show_workout_graph);
        layout = findViewById(R.id.workout_history_layout);

        Bundle extras = getIntent().getExtras();
        HashMap<String, ArrayList<String>> fullWorkouts;
        String response;
        ArrayList<String> workoutIDs;
        String workoutResponse;
        try {
            fullWorkouts = (HashMap<String, ArrayList<String>>) extras.getSerializable("workouts");
            response = extras.getString("response");
            workoutIDs = extras.getStringArrayList("workoutIDs");
            workoutResponse = extras.getString("workoutResponse");
        } catch (Exception e) {
            fullWorkouts = new HashMap<>();
            response = "";
            workoutIDs = new ArrayList<>();
            workoutResponse = "";
        }

        JSONArray responseArray;
        JSONArray workoutResponseArray;
        try {
            responseArray = new JSONArray(response);
            workoutResponseArray = new JSONArray(workoutResponse);
        } catch (JSONException e) {
            responseArray = new JSONArray();
            workoutResponseArray = new JSONArray();
            e.printStackTrace();
        }
        Log.d(TAG, "FullWorkouts " + fullWorkouts);
        Log.d(TAG, "WorkoutResponse: " + workoutResponseArray);
        ArrayList<String> workoutNamesList = new ArrayList<>(); // = new ArrayList<>(fullWorkouts.keySet());
        for (int i = 0; i < workoutResponseArray.length(); i++) {
            try {
                JSONObject o = workoutResponseArray.getJSONObject(i);
                workoutNamesList.add(o.getString("workoutName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        JSONArray finalWorkoutResponseArray = workoutResponseArray;

        JSONArray finalWorkoutResponseArray1 = workoutResponseArray;
        workoutNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String workoutName = parent.getItemAtPosition(position).toString();
                JSONObject currentWorkout = new JSONObject();
                try {
                    currentWorkout = finalWorkoutResponseArray1.getJSONObject(position);
                    currWorkoutId[0] = currentWorkout.getInt("workoutID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //currWorkoutId[0] = Integer.parseInt(finalWorkoutIDs.get(position));
                //Log.d(TAG, "FinalWorkoutIds: " + finalWorkoutIDs);
                Log.d(TAG, "Position: " + position);
                Log.d(TAG, "CurrWorkoutId: " + currWorkoutId[0]);

                weightVals.clear();
                repsVals.clear();
                currentWeightVals.clear();
                currentRepsVals.clear();

                ArrayList<String> exerciseNames = new ArrayList<>();
                String prev = "";
                JSONArray currentExercises = new JSONArray();
                try {
                    currentExercises = (JSONArray) currentWorkout.get("exercises");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < currentExercises.length(); i++) {
                    try {
                        JSONObject o = (JSONObject) currentExercises.get(i);
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

                        if (i == currentExercises.length() - 1) {
                            currentWeightVals.put(prev, weightVals);
                            currentRepsVals.put(prev, repsVals);
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

                        ArrayList<String> dates = new ArrayList<>();
                        ArrayList<String> formattedDates = new ArrayList<>();
                        boolean workoutFound = false;
                        //IT"S IN THE RESPONSE, GET WORKOUTID FROM FULLEXERCISES
                        for (int i = 0; i < finalResponseArray.length(); i++) {
                            try {
                                JSONObject o = finalResponseArray.getJSONObject(i);
                                JSONObject o2 = o.getJSONObject("workoutID");
                                if (currWorkoutId[0] == o2.getInt("workoutId")) {
                                    Log.d(TAG, "Scheduled workout found");
                                    workoutFound = true;
                                    String formattedDate = (o.getString("date")).replace('T', ' ');
                                    String[] splitFull = formattedDate.split(" ");
                                    String[] splitDate = splitFull[0].split("-");
                                    String[] splitTime = splitFull[1].split(":");
                                    int month = Integer.parseInt(splitDate[1]);
                                    int day = Integer.parseInt(splitDate[2]);
                                    int hours = Integer.parseInt(splitTime[0]);
                                    int minutes = Integer.parseInt(splitTime[1]);
                                    String convertedDate = convertDate(month, day, hours, minutes);
                                    dates.add(formattedDate);
                                    formattedDates.add(convertedDate);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (dates.size() == 0) {
                            layout.removeView(dateSpinner);
                        }
                        else {
                            selectedDate[0] = formattedDates.get(0);
                            ArrayAdapter<String> exerciseDatesAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, formattedDates);
                            exerciseDatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            dateSpinner.setAdapter(exerciseDatesAdapter);
                            dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selectedDate[0] = adapterView.getItemAtPosition(i).toString();

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                        }
                        Log.d(TAG, "SelectedDate: " + selectedDate[0]);
                        if (workoutFound) {
                            showGraph(firstLoad, exerciseName, selectedDate[0], currentRepsVals.get(exerciseName), currentWeightVals.get(exerciseName));
                        }
                        else {
                            chartView.clear();
                            Scatter newScatter = AnyChart.scatter();
                            newScatter.title("No Data");
                            newScatter.noData().label().enabled(true);
                            chartView.setChart(newScatter);
                            //noData.enabled(true);
                            //noData.text("No exercise history found");
                            //noData.background().enabled(true);
                            //noData.background().fill("White 0.5");
                            //noData.padding(20, 20, 20, 20);
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
    public void showGraph(boolean firstSelected, String title, String date, ArrayList<String> repsString, ArrayList<String> weightsString) {
        Log.d(TAG, "ShowGraph: Title: " + title);
        Log.d(TAG, "ShowGraph: reps: " + repsString);
        Log.d(TAG, "ShowGraph: weight: " + weightsString);
        Log.d(TAG, "ShowGraph: first: " + firstSelected);
        Log.d(TAG, "ShowGraph: date: " + date);

        ArrayList<Integer> reps = new ArrayList<>();
        for(String rep : repsString) {
            reps.add(Integer.parseInt(rep));
        }
        if (firstLoad) {
            scatter = AnyChart.scatter();
            scatter.animation(true);
            scatter.padding(10d, 20d, 5d, 20d);
        }

        scatter.title(title);

        scatter.tooltip().displayMode(TooltipDisplayMode.UNION);

        boolean weightBased = false;
        List<DataEntry> lineVals = new ArrayList<>();
        ArrayList<Double> weights = new ArrayList<>();
        ArrayList<Double> vols = new ArrayList<>();
        for (int i = 0; i < reps.size(); i++) {
            if (weightsString.get(i).equals("")) {
                scatter.yAxis(0).title("Time (min)");
                scatter.xAxis(0).title("Distance (mi)");
                int formattedTime = reps.get(i) / 60;
                //Log.d(TAG, "formatted: " + formattedDate);
                ValueDataEntry d = new ValueDataEntry((i + 1) * 5, formattedTime);
                lineVals.add(d);
            }
            else {
                scatter.yAxis(0).title("Reps");
                scatter.yAxis(0).labels().format("{%Value}{numDecimals:0}");
                scatter.yAxis(0).minorLabels().format("{%Value}{numDecimals:0}");
                scatter.yAxis(0).minorTicks().enabled(true);
                scatter.yAxis(0).ticks().enabled(true);
                scatter.yAxis(0).minorLabels().enabled(true);
                scatter.xAxis(0).title("Weight");
                scatter.xAxis(0).minorTicks().enabled(true);
                scatter.xAxis(0).ticks().enabled(true);
                scatter.xAxis(0).minorLabels().enabled(true);
                weights.add(Double.parseDouble(weightsString.get(i)));
                vols.add((Double.parseDouble(weightsString.get(i)) * reps.get(i)));
                ValueDataEntry d = new ValueDataEntry(Double.parseDouble(weightsString.get(i)), reps.get(i));
                lineVals.add(d);
                weightBased = true;
            }
        }

        if (weightBased) {
            Log.d(TAG, "ShowGraph: MaxWeight: " + Collections.max(weights));
            Log.d(TAG, "ShowGraph: Weights: " + weights);

            int roundedDown = ((int)(Collections.min(weights) / 100) * 100);
            int roundedUp = ((int)(Collections.max(weights) + 99) / 100) * 100;
            Log.d(TAG, "ShowGraph: roundedDown: " + roundedDown);
            Log.d(TAG, "ShowGraph: roundedUp: " + roundedUp);
            scatter.yScale()
                    .minimum(0d)
                    .maximum(Collections.max(reps) + 5d);
            scatter.xScale()
                    .minimum(roundedDown)
                    .maximum(roundedUp);
        }
        else {
            scatter.yScale()
                    .minimum(0d)
                    .maximum((Collections.max(reps) / 60) + 50d);
            scatter.xScale()
                    .minimum(0d)
                    .maximum(50d);
        }

        Log.d(TAG, "ShowGraph: Linevals: " + lineVals.get(0).getValue("x"));
        scatter.interactivity()
                .hoverMode(HoverMode.BY_SPOT)
                .spotRadius(15d);

        Marker marker = scatter.marker(lineVals);
        marker.type(MarkerType.TRIANGLE_UP)
                .size(8d);
        marker.hovered()
                .size(7d)
                .fill(new SolidFill("#ed5a5a", 0.5d))
                .stroke("anychart.color.darken(#ed5a5a)");

        if (weightBased) {
            marker.tooltip()
                    .hAlign(HAlign.START)
                    .format("Weight: {%X} lbs.\\nReps: {%Value}.");
        }
        else {
            marker.tooltip()
                    .hAlign(HAlign.START)
                    .format("Distance: {%X} miles.\\nTime: {%Value} minutes.");
        }


        if (firstLoad) {
            chartView.setChart(scatter);
            firstLoad = false;
        }

    }

    public String convertDate(int month, int day, int hours, int minutes) {
        String convMonth = "";
        String amPm = "";
        String convHour = "";

        switch(month) {
            case 1:
                convMonth = "Jan.";
                break;
            case 2:
                convMonth = "Feb.";
                break;
            case 3:
                convMonth = "Mar.";
                break;
            case 4:
                convMonth = "Apr.";
                break;
            case 5:
                convMonth = "May";
                break;
            case 6:
                convMonth = "June";
                break;
            case 7:
                convMonth = "July";
                break;
            case 8:
                convMonth = "Aug.";
                break;
            case 9:
                convMonth = "Sept.";
                break;
            case 10:
                convMonth = "Oct.";
                break;
            case 11:
                convMonth = "Nov.";
                break;
            case 12:
                convMonth = "Dec.";
                break;
        }

        switch(hours) {
            case 0:
                convHour = "12";
                amPm = "AM";
                break;
            case 1:
                convHour = "1";
                amPm = "AM";
                break;
            case 2:
                convHour = "2";
                amPm = "AM";
                break;
            case 3:
                convHour = "3";
                amPm = "AM";
                break;
            case 4:
                convHour = "4";
                amPm = "AM";
                break;
            case 5:
                convHour = "5";
                amPm = "AM";
                break;
            case 6:
                convHour = "6";
                amPm = "AM";
                break;
            case 7:
                convHour = "7";
                amPm = "AM";
                break;
            case 8:
                convHour = "8";
                amPm = "AM";
                break;
            case 9:
                convHour = "9";
                amPm = "AM";
                break;
            case 10:
                convHour = "10";
                amPm = "AM";
                break;
            case 11:
                convHour = "11";
                amPm = "AM";
                break;
            case 12:
                convHour = "12";
                amPm = "PM";
                break;
            case 13:
                convHour = "1";
                amPm = "PM";
                break;
            case 14:
                convHour = "2";
                amPm = "PM";
                break;
            case 15:
                convHour = "3";
                amPm = "PM";
                break;
            case 16:
                convHour = "4";
                amPm = "PM";
                break;
            case 17:
                convHour = "5";
                amPm = "PM";
                break;
            case 18:
                convHour = "6";
                amPm = "PM";
                break;
            case 19:
                convHour = "7";
                amPm = "PM";
                break;
            case 20:
                convHour = "8";
                amPm = "PM";
                break;
            case 21:
                convHour = "9";
                amPm = "PM";
                break;
            case 22:
                convHour = "10";
                amPm = "PM";
                break;
            case 23:
                convHour = "11";
                amPm = "PM";
                break;
        }
        String convMin = String.valueOf(minutes);
        if (minutes < 10) {
            convMin = "0" + convMin;
        }

        return convMonth + " " + day + "   " + convHour + ":" + convMin + " " + amPm;
    }
}