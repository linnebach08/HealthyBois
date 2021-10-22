package com.example.heartstrawng;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class setWorkoutDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_workout_details);

        FloatingActionButton fab = findViewById(R.id.set_workout_details_back_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        final String name;
        String type;
        if (extras != null) {
            name = extras.getString("name");
            type = extras.getString("type");
        }
        else {
            name = "Error";
            type = "Error";
        }

        TextView title = (TextView) findViewById(R.id.workout_name);

        title.setText(name);

        Button repsMinusBtn = (Button) findViewById(R.id.workout_reps_minus_btn);
        Button repsPlusBtn = (Button) findViewById(R.id.workout_reps_plus_btn);
        final EditText repsInput = (EditText) findViewById(R.id.workout_reps_input);

        Button weightMinusBtn = (Button) findViewById(R.id.workout_weight_minus_btn);
        Button weightPlusBtn = (Button) findViewById(R.id.workout_weight_plus_btn);
        final EditText weightInput = (EditText) findViewById(R.id.workout_weight_input);

        final Button saveBtn = (Button) findViewById(R.id.workout_save_btn);

        if (type.equals("Cardio")) {
            TextView time = (TextView) findViewById(R.id.workout_reps);
            time.setText("Time:");
            // 24 corresponds to a time input type
            repsInput.setInputType(24);

       }
        else {
            TextView reps = (TextView) findViewById(R.id.workout_reps);
            reps.setText("Reps:");

            TextView weight = (TextView) findViewById(R.id.workout_weight);
            weight.setText("Weight:");
        }

        repsMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repsInput.getText().toString().equals("0")) {
                    String text = "How the hell do you do a negative rep";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(view.getContext(), text, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    int currNum = Integer.parseInt(repsInput.getText().toString());
                    int newNum = currNum - 1;
                    String s = String.valueOf(newNum);
                    repsInput.setText(s);
                }
            }
        });

        repsPlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currNum = Integer.parseInt(repsInput.getText().toString());
                int newNum = currNum + 1;
                String s = String.valueOf(newNum);
                repsInput.setText(s);
            }
        });

        weightMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (weightInput.getText().toString().equals("0.0")) {
                    String text = "How the hell do you lift negative weight";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(view.getContext(), text, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    double currNum = Double.parseDouble(weightInput.getText().toString());
                    double newNum = currNum - 5;
                    String s = String.valueOf(newNum);
                    weightInput.setText(s);
                }
            }
        });

        weightPlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double currNum = Double.parseDouble(weightInput.getText().toString());
                double newNum = currNum + 5;
                String s = String.valueOf(newNum);
                weightInput.setText(s);
            }
        });

        final Context c = this;
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = new LinearLayout(c);
                setContentView(linearLayout);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1));
                linearLayout.setPadding(10, 100, 10, 0);

                TextView savedWorkout = new TextView(c);
                savedWorkout.setText(name);
                linearLayout.addView(savedWorkout);
            }
        });
    }

}
