package com.example.heartstrawngv1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.heartstrawngv1.databinding.ActivityAddCustomExerciseBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddCustomExercise extends AppCompatActivity {

    // Keeps track of how many checkboxes are currently checked
    // App currently only supports up to two descriptors
    int numChecked = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom_exercise);

        // Gets the data sent from the previous view
        Bundle extras = getIntent().getExtras();
        String type;
        if (extras != null) {
            type = extras.getString("type");
        }
        else {
            type = "Error";
        }

        if (type.equals("Arm") || type.equals("Leg") || type.equals("Shoulder")) {
            type = type + "s";
        }
        // Views
        FloatingActionButton fab = findViewById(R.id.add_custom_exercise_back_btn);
        CheckBox weightBased = findViewById(R.id.weight_based_checkbox);
        CheckBox repsBased = findViewById(R.id.reps_based_checkbox);
        CheckBox timeBased = findViewById(R.id.time_based_checkbox);
        CheckBox distanceBased = findViewById(R.id.distance_based_checkbox);
        Button finished = findViewById(R.id.custom_exercise_finished_btn);
        EditText nameInput = findViewById(R.id.new_exercise_name_input);
        Spinner categoryChoice = findViewById(R.id.category_spinner);


        String[] types = new String[]{"Arms", "Back", "Cardio", "Chest", "Core", "Legs", "Shoulders"};
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(type);

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(type)) {
                continue;
            }
            spinnerArray.add(types[i]);
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, spinnerArray);
        categoryChoice.setAdapter(spinnerArrayAdapter);

        // Click listeners
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Weird thing that apparently needs to be done
        // Final keyword is weird
        String finalType = type;
        finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numChecked == 0) {
                    String text = "Please select at least one exercise descriptor";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(view.getContext(), text, duration);
                    toast.show();
                    return;
                }
                if (nameInput.getText().toString().equals("")) {
                    String text = "Please provide a name for the new exercise";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(view.getContext(), text, duration);
                    toast.show();
                    return;
                }
                // Save new exercise on user's device
                SharedPreferences settings = getApplicationContext().getSharedPreferences("SHARED_PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                Set<String> details = new HashSet<String>();
                Intent resultIntent = new Intent();
                boolean wb = false;
                boolean rb = false;
                boolean tb = false;
                boolean db = false;

                int currentMax = settings.getInt("maxID", 96);

                editor.putInt("maxID", currentMax + 1);

                if (weightBased.isChecked()) {
                    details.add("weightBased");
                    wb = true;
                }
                if (repsBased.isChecked()) {
                    details.add("repsBased");
                    rb = true;
                }
                if (timeBased.isChecked()) {
                    details.add("timeBased");
                    tb = true;
                }
                if (distanceBased.isChecked()) {
                    details.add("distanceBased");
                    db = true;
                }
                details.add(categoryChoice.getSelectedItem().toString());

                editor.putStringSet(nameInput.getText().toString(), details);

                // Apply the edits
                editor.apply();

                resultIntent.putExtra("type", categoryChoice.getSelectedItem().toString());
                resultIntent.putExtra("name", nameInput.getText().toString());
                resultIntent.putExtra("weightBased", wb);
                resultIntent.putExtra("repsBased", rb);
                resultIntent.putExtra("timeBased", tb);
                resultIntent.putExtra("distanceBased", db);
                resultIntent.putExtra("maxID", currentMax + 1);
                setResult(Activity.RESULT_OK, resultIntent);

                finish();
            }
        });

        setCheckedListener(this, weightBased);
        setCheckedListener(this, repsBased);
        setCheckedListener(this, timeBased);
        setCheckedListener(this, distanceBased);

    }

    public void setCheckedListener(Context context, CheckBox c) {
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    numChecked++;
                    if (numChecked > 2) {
                        String text = "Maximum of two choices allowed";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                        compoundButton.setChecked(false);
                        numChecked--;
                    }
                }
                else {
                    numChecked--;
                }
            }
        });

    }
}