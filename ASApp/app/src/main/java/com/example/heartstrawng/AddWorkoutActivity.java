package com.example.heartstrawng;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.Arrays;

public class AddWorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        Button goBackBtn = (Button) findViewById(R.id.create_workout_back_button);

        goBackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }});

        Button armsBtn = (Button) findViewById(R.id.workouts_arms);
        Button backBtn = (Button) findViewById(R.id.workouts_back);
        Button cardioBtn = (Button) findViewById(R.id.workouts_cardio);
        Button chestBtn = (Button) findViewById(R.id.workouts_chest);
        Button coreBtn = (Button) findViewById(R.id.workouts_core);
        Button legsBtn = (Button) findViewById(R.id.workouts_legs);
        Button shouldersBtn = (Button) findViewById(R.id.workouts_shoulders);

        final String[] arm_workouts = new String[]{ "Bicep Curls", "Farmer's Walk", "Incline Hammer Curls", "Rickshaw Carry",
                "Skullcrusher", "Straight Bar Wrist Roll-Up", "Triceps Dip", "Wrist Curls", "Add New Exercise"};
        final String[] back_workouts = new String[]{"Atlas Stones", "Back Extension", "Bent-Over Row", "Deadlift",
                "Lat Pulldown", "One-Arm Dumbbell Row", "Pull-Up", "Seated Cable Rows", "T-Bar Row", "Add New Exercise"};
        final String[] cardio_workouts = new String[]{"Bike", "Burpees", "Dance", "Jump Rope", "Jumping Jacks",
                "Pilates", "Run", "Stationary Bike", "Swim", "Walk", "Yoga", "Add New Exercise"};
        final String[] chest_workouts = new String[]{"Barbell Bench Press", "Bodyweight Flys", "Chest Dips",
                "Close-Grip Bench Press", "Decline Barbell Bench Press", "Dumbbell Bench Press",
                "Dumbbell Flys", "Incline Dumbbell Bench Press", "Low-Cable Crossover", "Pushups",
                "Wide-Grip Bench Press", "Add New Exercise"};
        final String[] core_workouts = new String[]{"Ab Roller", "Cross-Body Crunch", "Crunches",
                "Decline Crunch", "Decline Reverse Crunch", "Landmine Twist", "Mountain Climber",
                "Plank", "Plate Twist", "Side Plank", "Spider Crawl",
                "Standing Cable Low-to-High Twist", "Add New Exercise"};
        final String[] legs_workouts = new String[]{"Barbell Deadlift", "Barbell Glute Bridge", "Barbell Squat",
                "Calf Raises", "Clean Deadlift", "Hang Clean", "Leg Press", "Power Snatch",
                "Romanian Deadlift", "Squats", "Tire Flip", "Add New Exercise"};
        final String[] shoulders_workouts = new String[]{"Clean and Jerk", "Clean and Press",
                "Military Press", "Seated Barbell Shoulder Press", "Seated Dumbbell Press", "Shrugs",
                "Single Arm Kettlebell Push-Press", "Single-Arm Lateral Raise", "Standing Dumbbell Shoulder Press",
                "Add New Exercise"};

        setBtnPressed(armsBtn, arm_workouts, "Arm");
        setBtnPressed(backBtn, back_workouts, "Back");
        setBtnPressed(cardioBtn, cardio_workouts, "Cardio");
        setBtnPressed(chestBtn, chest_workouts, "Chest");
        setBtnPressed(coreBtn, core_workouts, "Core");
        setBtnPressed(legsBtn, legs_workouts, "Leg");
        setBtnPressed(shouldersBtn, shoulders_workouts, "Shoulder");
    }

    public void setBtnPressed(Button b, final String[] workouts, final String type) {
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                // Add list of arm workouts
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                String title = "Select " + type + " Exercise to Add";
                builder.setTitle(title)
                        .setItems(workouts, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                Intent setWorkoutDetailsIntent = new Intent(v.getContext(), setWorkoutDetails.class);
                                setWorkoutDetailsIntent.putExtra("name", workouts[which]);
                                setWorkoutDetailsIntent.putExtra("type", type);
                                startActivity(setWorkoutDetailsIntent);
                            }
                        });
                builder.create();
                builder.show();

            }
        });
    }

}
