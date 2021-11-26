package com.example.heartstrawngv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class StartWorkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_workout);

        FloatingActionButton fab = findViewById(R.id.start_workout_back_btn);
        fab.setOnClickListener(view -> finish());

        ConstraintLayout layout = findViewById(R.id.start_workout_layout);
        TableLayout table = findViewById(R.id.start_workout_table);
        Button doneBtn = findViewById(R.id.start_workout_done_btn);

        Bundle extras = getIntent().getExtras();

        ArrayList<String> exerciseNames;
        String workoutName;
        try {
            exerciseNames = extras.getStringArrayList("exercises");
            workoutName = extras.getString("workoutName");
        } catch (Exception e) {
            exerciseNames = new ArrayList<>();
            workoutName = "";
        }

        AtomicInteger numChecked = new AtomicInteger();

        Context thisContext = this;
        ArrayList<String> finalExerciseNames = exerciseNames;
        String finalWorkoutName = workoutName;
        ArrayList<String> finalExerciseNames1 = exerciseNames;
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numChecked.get() < finalExerciseNames.size()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Workout Unfinished");

                    LayoutInflater popupInflater = getLayoutInflater();
                    View dialogLayout = popupInflater.inflate(R.layout.unfinished_workout_popup, null);
                    builder.setView(dialogLayout);

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //TODO: save what the user has completed
                            finish();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builder.show();
                }
                else {
                    File file = new File(thisContext.getFilesDir(), "FinishedWorkouts");
                    try {
                        if (file.createNewFile()) {
                            FileWriter writer = new FileWriter(file);
                            String toWrite = finalWorkoutName + ":" + finalExerciseNames1;
                            writer.write(toWrite);
                            writer.close();
                        } else {
                            FileWriter writer = new FileWriter(file);
                            String toWrite = "\r\n" + finalWorkoutName + ":" + finalExerciseNames1;
                            writer.write(toWrite);
                            writer.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }
        });

        for (int i = 0; i < exerciseNames.size(); i++) {
            String name = "";
            int repsTimeOrDistanceVal = -1;
            int weight = -1;
            try {
                JSONObject o = new JSONObject(exerciseNames.get(i));
                name = o.getString("exercise");
                repsTimeOrDistanceVal = o.getInt("repsTimeOrDistanceValue");
                try {
                    weight = o.getInt("weightValue");
                } catch (Exception n) {
                    weight = -1;
                }
                Log.d("EXERCISEIS", "It's: " + o);
            } catch (Exception e) {
                Log.d("CONVERROR", "Can't convert to JSON object");
            }

            TableRow newRow = new TableRow(this);
            newRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0));
            newRow.setPadding(0, 45, 0, 45);

            TextView exercise = new TextView(this);
            exercise.setLayoutParams(new TableRow.LayoutParams(1));
            exercise.setTextSize(23);
            exercise.setText(name);

            TextView repsTimeOrDist = new TextView(this);
            if (repsTimeOrDistanceVal != -1) {
                repsTimeOrDist.setLayoutParams(new TableRow.LayoutParams(2));
                repsTimeOrDist.setTextSize(23);
                if (weight == -1) {
                    repsTimeOrDist.setText(String.valueOf(repsTimeOrDistanceVal));
                }
                else {
                    String fullText = String.valueOf(repsTimeOrDistanceVal) + " @ " + weight + " lbs";
                    repsTimeOrDist.setText(fullText);
                }
            }


            CheckBox done = new CheckBox(this);
            done.setLayoutParams(new TableRow.LayoutParams(3));
            done.setChecked(false);

            if (i == 0) {
                exercise.setClickable(true);
                repsTimeOrDist.setClickable(true);
                done.setEnabled(true);
                done.setClickable(true);
            }
            else {
                exercise.setClickable(false);
                exercise.setEnabled(false);

                repsTimeOrDist.setClickable(false);
                repsTimeOrDist.setEnabled(false);

                done.setEnabled(false);
                done.setClickable(false);
            }

            int finalI = i;
            done.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked) {
                    View view = table.getChildAt(finalI + 5);
                    Log.d("COUNT", "Its " + table.getChildCount());

                    if (view instanceof TableRow) {
                        numChecked.getAndIncrement();
                        TableRow next = (TableRow) view;

                        next.setEnabled(true);
                        next.setAlpha(1F);
                        next.setBackgroundColor(Color.WHITE);

                        Log.d("COUNT2", "Its " + next.getChildCount());

                        for (int j = 0; j < next.getChildCount(); j++) {
                            Log.d("CHILD", "Child is " + next.getChildAt(j));
                            next.getChildAt(j).setEnabled(true);
                            next.getChildAt(j).setClickable(true);
                        }
                    }
                    else {
                        Toast.makeText(thisContext, "Workout finished! Nice Job!", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    numChecked.getAndDecrement();
                    View view = table.getChildAt(finalI + 5);

                    if (view instanceof TableRow) {
                        TableRow next = (TableRow) view;

                        next.setEnabled(false);
                        next.setAlpha(0.75F);
                        next.setBackgroundColor(Color.LTGRAY);

                        Log.d("COUNT2", "Its " + next.getChildCount());

                        for (int j = 0; j < next.getChildCount(); j++) {
                            Log.d("CHILD", "Child is " + next.getChildAt(j));
                            next.getChildAt(j).setEnabled(false);
                            next.getChildAt(j).setClickable(false);
                        }
                    }
                    else {
                        Toast.makeText(thisContext, "W", Toast.LENGTH_LONG).show();
                    }
                }
            });

            exercise.setOnClickListener(view -> done.setChecked(true));

            repsTimeOrDist.setOnClickListener(view -> done.setChecked(true));

            newRow.addView(exercise);
            newRow.addView(repsTimeOrDist);
            newRow.addView(done);

            if (i != 0) {
                newRow.setBackgroundColor(Color.LTGRAY);
                newRow.setAlpha(0.75F);
            }
            table.addView(newRow);
        }
    }
}