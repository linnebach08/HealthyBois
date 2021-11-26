package com.example.heartstrawngv1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class setWorkoutDetails extends AppCompatActivity implements OnStartDragListener{
    int numClicked = 1;
    ArrayList<Spanned> items;
    ArrayList<Set> sets;
    private ItemTouchHelper mItemTouchHelper;
    RecyclerListAdapter adapter;

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

        // For the drag and drop saved workouts view
        items = new ArrayList<android.text.Spanned>();
        sets = new ArrayList<Set>();

        RecyclerView recyclerLayout = findViewById(R.id.saved_workouts);
        adapter = new RecyclerListAdapter(this, items, this::onStartDrag, false);

        recyclerLayout.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerLayout.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerLayout.getContext(),
                layoutManager.getOrientation());
        recyclerLayout.addItemDecoration(dividerItemDecoration);

        recyclerLayout.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerLayout);

        // Gets the data sent from the previous view
        Bundle extras = getIntent().getExtras();
        final String name;
        Exercise e;
        ArrayList<String> vals;
        if (extras != null) {
            name = extras.getString("name");
            e = (Exercise) extras.getSerializable("fullDetails");
            vals = extras.getStringArrayList("setInfo");
        }
        else {
            name = "Error";
            e = null;
            vals = new ArrayList<>();
        }

        TextView title = (TextView) findViewById(R.id.workout_name);

        if (name.equals("Bodyweight Flyes")) {
            title.setText("Bodyweight Flys");
        }
        else {
            title.setText(name);
        }

        Button repsMinusBtn = (Button) findViewById(R.id.workout_reps_minus_btn);
        Button repsPlusBtn = (Button) findViewById(R.id.workout_reps_plus_btn);
        final EditText repsInput = (EditText) findViewById(R.id.workout_reps_input);

        Button weightMinusBtn = (Button) findViewById(R.id.workout_weight_minus_btn);
        Button weightPlusBtn = (Button) findViewById(R.id.workout_weight_plus_btn);
        final EditText weightInput = (EditText) findViewById(R.id.workout_weight_input);

        final Button saveBtn = (Button) findViewById(R.id.workout_save_btn);

        boolean firstParameter = false;
        boolean secondParameter = false;

        if (e.repsBased) {
            if ((!e.name.equals("Arm Circles")) && (!e.name.equals("Counterclockwise Arm Circle"))
                    && (!e.name.equals("Swim"))) {
                TextView reps = (TextView) findViewById(R.id.workout_reps);
                reps.setText("Reps:");
                firstParameter = true;
            }
            else {
                e.repsBased = false;
            }
        }
        if (e.timeBased) {
            if ((!e.name.equals("Straight Bar Wrist Roll-Up")) && (!e.name.equals("Triceps Dip"))
                    && (!e.name.equals("Back Extension")) && (!e.name.equals("Bent-Over Row"))
                    && (!e.name.equals("Lat Pulldown")) && (!e.name.equals("Pull-Up"))
                    && (!e.name.equals("Seated Cable Rows")) && (!e.name.equals("Bodyweight Flyes"))
                    && (!e.name.equals("Chest Dips")) && (!e.name.equals("Low-Cable Crossover"))
                    && (!e.name.equals("Landmine Twist")) && (!e.name.equals("Plate Twist"))
                    && (!e.name.equals("Standing Cable Low-to-High Twist")) && (!e.name.equals("Calf Raises"))
                    && (!e.name.equals("Leg Press")) && (!e.name.equals("Romanian Deadlift"))
                    && (!e.name.equals("Tire Flip")) && (!e.name.equals("Military Press"))
                    && (!e.name.equals("Shrugs")) && (!e.name.equals("Single Arm Kettlebell Push-Press"))
                    && (!e.name.equals("Single-Arm Lateral Raise"))) {

                // Converts pixels to dps, used for setting width and height programmatically
                final float scale = this.getResources().getDisplayMetrics().density;
                int pixels = (int) (50 * scale + 0.5f);
                int pixelsTwo = (int) (10 * scale + 0.5f);

                if (firstParameter) {
                    TextView time = (TextView) findViewById(R.id.workout_weight);
                    time.setText("Time:");
                    time.setLayoutParams(new LinearLayout.LayoutParams(pixels,
                            LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.2));

                    LinearLayout secondRow = findViewById(R.id.second_input);
                    secondRow.removeView(weightMinusBtn);
                    secondRow.removeView(weightInput);
                    secondRow.removeView(weightPlusBtn);

                    EditText hours = new EditText(this);
                    hours.setId(R.id.hours_input);
                    hours.setGravity(Gravity.CENTER);
                    hours.setLayoutParams(new LinearLayout.LayoutParams(pixelsTwo,
                            pixels, (float) 0.6));
                    hours.setInputType(InputType.TYPE_CLASS_NUMBER);
                    hours.setHint("Hours");

                    EditText minutes = new EditText(this);
                    minutes.setId(R.id.minutes_input);
                    minutes.setGravity(Gravity.CENTER);
                    minutes.setLayoutParams(new LinearLayout.LayoutParams(pixelsTwo,
                            pixels, (float) 0.6));
                    minutes.setInputType(InputType.TYPE_CLASS_NUMBER);
                    minutes.setHint("Minutes");

                    EditText seconds = new EditText(this);
                    seconds.setId(R.id.seconds_input);
                    seconds.setGravity(Gravity.CENTER);
                    seconds.setLayoutParams(new LinearLayout.LayoutParams(pixelsTwo,
                            pixels, (float) 0.6));
                    seconds.setInputType(InputType.TYPE_CLASS_NUMBER);
                    seconds.setHint("Seconds");

                    secondRow.addView(hours);
                    secondRow.addView(minutes);
                    secondRow.addView(seconds);

                    secondParameter = true;
                }
                else {
                    TextView time = (TextView) findViewById(R.id.workout_reps);
                    time.setText("Time:");
                    time.setLayoutParams(new LinearLayout.LayoutParams(pixels,
                            LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.2));

                    LinearLayout firstRow = findViewById(R.id.first_input);
                    firstRow.removeView(repsMinusBtn);
                    firstRow.removeView(repsInput);
                    firstRow.removeView(repsPlusBtn);

                    EditText hours = new EditText(this);
                    hours.setId(R.id.hours_input);
                    hours.setGravity(Gravity.CENTER);
                    hours.setLayoutParams(new LinearLayout.LayoutParams(pixelsTwo,
                            pixels, (float) 0.6));
                    hours.setInputType(InputType.TYPE_CLASS_NUMBER);
                    hours.setHint("Hours");

                    EditText minutes = new EditText(this);
                    minutes.setId(R.id.minutes_input);
                    minutes.setGravity(Gravity.CENTER);
                    minutes.setLayoutParams(new LinearLayout.LayoutParams(pixelsTwo,
                            pixels, (float) 0.6));
                    minutes.setInputType(InputType.TYPE_CLASS_NUMBER);
                    minutes.setHint("Minutes");

                    EditText seconds = new EditText(this);
                    seconds.setId(R.id.seconds_input);
                    seconds.setGravity(Gravity.CENTER);
                    seconds.setLayoutParams(new LinearLayout.LayoutParams(pixelsTwo,
                            pixels, (float) 0.6));
                    seconds.setInputType(InputType.TYPE_CLASS_NUMBER);
                    seconds.setHint("Seconds");

                    firstRow.addView(hours);
                    firstRow.addView(minutes);
                    firstRow.addView(seconds);

                    firstParameter = true;
                }

            }
            else {
                e.timeBased = false;
            }

        }
        if (e.distanceBased) {
            if (firstParameter) {
                TextView distance = (TextView) findViewById(R.id.workout_weight);
                distance.setText("Distance:");

                final float scale = this.getResources().getDisplayMetrics().density;
                int pixels = (int) (50 * scale + 0.5f);

                distance.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        pixels, (float)0.8));

                LinearLayout secondRow = findViewById(R.id.second_input);
                secondRow.removeView(weightMinusBtn);
                secondRow.removeView(weightPlusBtn);

                weightInput.setGravity(Gravity.CENTER);
                weightInput.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        pixels, (float)0.8));
                weightInput.setHint("0.0");

                ArrayList<String> spinnerArray = new ArrayList<String>();
                spinnerArray.add("mi        ");
                spinnerArray.add("ft        ");
                spinnerArray.add("in        ");
                spinnerArray.add("km        ");

                Spinner spinner = new Spinner(this);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setId(R.id.distance_choice);

                secondRow.addView(spinner);

                secondParameter = true;
            }
            else {
                TextView distance = (TextView) findViewById(R.id.workout_reps);
                distance.setText("Distance:");

                final float scale = this.getResources().getDisplayMetrics().density;
                int pixels = (int) (50 * scale + 0.5f);

                distance.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        pixels, (float)0.8));

                LinearLayout firstRow = findViewById(R.id.first_input);
                firstRow.removeView(repsMinusBtn);
                firstRow.removeView(repsPlusBtn);

                repsInput.setGravity(Gravity.CENTER);
                repsInput.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        pixels, (float)0.8));
                repsInput.setHint("0.0");

                ArrayList<String> spinnerArray = new ArrayList<String>();
                spinnerArray.add("mi        ");
                spinnerArray.add("ft        ");
                spinnerArray.add("in        ");
                spinnerArray.add("km        ");

                Spinner spinner = new Spinner(this);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setId(R.id.distance_choice);

                firstRow.addView(spinner);

                firstParameter = true;
            }
        }
        if (e.weightUsed || e.name.equals("Triceps Dip") || e.name.equals("Back Extension")
                || e.name.equals("Bodyweight Flyes") || e.name.equals("Chest Dips") || e.name.equals("Calf Raises")) {
            if (!e.name.equals("Tire Flip")) {
                if (firstParameter) {
                    TextView weight = (TextView) findViewById(R.id.workout_weight);
                    weight.setText("Weight:");
                    final float scale = this.getResources().getDisplayMetrics().density;
                    int pixels = (int) (50 * scale + 0.5f);

                    LinearLayout secondRow = findViewById(R.id.second_input);

                    TextView unit = new TextView(this);
                    unit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, pixels, (float) 0.2));
                    unit.setTextSize(20);
                    unit.setGravity(Gravity.CENTER);
                    unit.setPadding(10, 0, 10, 0);
                    unit.setText("lbs");

                    secondRow.addView(unit);
                    secondParameter = true;
                }
                else {
                    TextView weight = (TextView) findViewById(R.id.workout_reps);
                    weight.setText("Weight:");

                    final float scale = this.getResources().getDisplayMetrics().density;
                    int pixels = (int) (50 * scale + 0.5f);

                    LinearLayout firstRow = findViewById(R.id.first_input);

                    TextView unit = new TextView(this);
                    unit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, pixels, (float) 0.2));
                    unit.setTextSize(20);
                    unit.setGravity(Gravity.CENTER);
                    unit.setText("lb");

                    firstRow.addView(unit);
                    firstParameter = true;
                }
            }
            else {
                e.weightUsed = false;
            }

        }

        if (firstParameter && !secondParameter) {
            LinearLayout secondRow = findViewById(R.id.second_input);
            secondRow.removeAllViews();
        }

        repsMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repsInput.getText().toString().equals("0")) {
                    String text = "How the hell do you do a negative rep";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(view.getContext(), text, duration);
                    toast.show();
                }
                else {
                    int currNum;
                    try {
                        currNum = Integer.parseInt(repsInput.getText().toString());
                    } catch (NumberFormatException e) {
                        String text = "How the hell do you do a negative rep";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(view.getContext(), text, duration);
                        toast.show();
                        return;
                    }
                    int newNum = currNum - 1;
                    String s = String.valueOf(newNum);
                    repsInput.setText(s);
                }
            }
        });

        repsPlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currNum;
                try {
                    currNum = Integer.parseInt(repsInput.getText().toString());
                } catch(NumberFormatException e) {
                    currNum = 0;
                }
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
                    toast.show();
                }
                else {
                    double currNum;
                    try {
                        currNum = Double.parseDouble(weightInput.getText().toString());
                    } catch(NumberFormatException e) {
                        String text = "How the hell do you lift negative weight";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(view.getContext(), text, duration);
                        toast.show();
                        return;
                    }
                    double newNum = currNum - 5;
                    String s = String.valueOf(newNum);
                    weightInput.setText(s);
                }
            }
        });

        weightPlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double currNum;
                try {
                    currNum = Double.parseDouble(weightInput.getText().toString());
                } catch (NumberFormatException e) {
                    currNum = 0;
                }
                double newNum = currNum + 5;
                String s = String.valueOf(newNum);
                weightInput.setText(s);
            }
        });

        final Button finishedBtn = findViewById(R.id.finished_btn);

        if (vals.size() != 0) {
            finishedBtn.setText("Finished");
            finishedBtn.setTextColor(getResources().getColor(R.color.black));
            finishedBtn.setBackgroundResource(R.color.colorAccent);
            finishedBtn.setClickable(true);
            for (String set : vals) {
                String styledText = "<strong> " + set + " </strong>";

                items.add(Html.fromHtml(styledText));
                adapter.notifyItemInserted(items.size());
            }
        }
        finishedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                ArrayList<String> result = new ArrayList<>();
                for (int i = 0; i < items.size(); i++) {
                    Spanned temp = items.get(i);
                    String strTemp = temp.toString();
                    result.add(strTemp);
                }
                // TODO: (From editWorkout) allow user to only change ordering of workout and
                // TODO: have that be updated in the database and in the editWorkout screen
                resultIntent.putExtra("sets", sets);
                resultIntent.putExtra("name", name);
                resultIntent.putExtra("repsBased", e.repsBased);
                resultIntent.putExtra("weightBased", e.weightUsed);
                resultIntent.putExtra("timeBased", e.timeBased);
                resultIntent.putExtra("distanceBased", e.distanceBased);
                resultIntent.putExtra("id", e.id);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                //resultIntent.putExtra("sets", items);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText hours = findViewById(R.id.hours_input);
                EditText minutes = findViewById(R.id.minutes_input);
                EditText seconds = findViewById(R.id.seconds_input);

                try {
                    hours.clearFocus();
                    minutes.clearFocus();
                    seconds.clearFocus();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(hours.getWindowToken(), 0);
                } catch (NullPointerException e) {

                }

                String styledText = "";
                double weightVal = 0;
                int repsVal = 0;
                int timeVal = 0;
                double distanceVal = 0;

                /*********** Formats all combinations of parameters for drag/drop list ***********/

                String secondRowText;
                if (findViewById(R.id.workout_weight) != null) {
                    secondRowText = (String) ((TextView) findViewById(R.id.workout_weight)).getText();
                }
                else {
                    secondRowText = "";
                }
                String firstRowText = (String) ((TextView) findViewById(R.id.workout_reps)).getText();

                // 1. Time 2. Weight
                if (firstRowText.equals("Time:") && secondRowText.equals("Weight:")) {
                    String hInput = hours.getText().toString();
                    String mInput = minutes.getText().toString();
                    String sInput = seconds.getText().toString();

                    boolean noHours = hInput.equals("");
                    boolean noMinutes = mInput.equals("");
                    boolean noSeconds = sInput.equals("");

                    // This if/else checks for and handles distance as the second field
                    if (weightInput.getText().toString().equals("")) {
                        if (noHours && noMinutes &&
                                noSeconds) {
                            String notSavedNotification = "Please enter exercise info";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                            toast.show();
                            return;
                        }
                        else if (noHours && noMinutes
                                && !noSeconds) {
                            styledText = "<strong>" + "0:0:" + sInput + " </strong>";
                            timeVal = Integer.parseInt(sInput);
                        }
                        else if (noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + "0:" + mInput + ":0" + " </strong>";
                            timeVal = Integer.parseInt(mInput) * 60;

                        }
                        else if (!noHours && noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:0" + " </strong>";
                            timeVal = Integer.parseInt(hInput) * 3600;

                        }
                        else if (noHours && !noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +"0:" + mInput + ":" + sInput + " </strong>";
                            timeVal = (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);

                        }
                        else if (!noHours && noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:" + sInput + " </strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + Integer.parseInt(sInput);

                        }
                        else if (!noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":0 </strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60);

                        }
                        else {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":" + sInput + "</strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);

                        }
                    }
                    else {
                        if (noHours && noMinutes &&
                                noSeconds) {
                            String notSavedNotification = "Please enter time info";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                            toast.show();
                            return;
                        }
                        else if (noHours && noMinutes
                                && !noSeconds) {
                            styledText = "<strong>" + "0:0:" + sInput + " @ " +
                                    weightInput.getText() + " lbs</strong>";
                            timeVal = Integer.parseInt(sInput);
                            weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                        }
                        else if (noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + "0:" + mInput + ":0" + " @ " +
                                    weightInput.getText() + " lbs</strong>";
                            timeVal = Integer.parseInt(mInput) * 60;
                            weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                        }
                        else if (!noHours && noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:0" + " @ " +
                                    weightInput.getText() + " lbs</strong>";
                            timeVal = Integer.parseInt(hInput) * 3600;
                            weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                        }
                        else if (noHours && !noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +"0:" + mInput + ":" + sInput + " @ " +
                                    weightInput.getText() + " lbs</strong>";
                            timeVal = (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                            weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                        }
                        else if (!noHours && noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:" + sInput + " @ " +
                                    weightInput.getText() + " lbs</strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + Integer.parseInt(sInput);
                            weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                        }
                        else if (!noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":0 @ " +
                                    weightInput.getText() + " lbs</strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60);
                            weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                        }
                        else {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":" + sInput + " @ " +
                                    weightInput.getText() + " lbs</strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                            weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                        }
                    }
                }
                // 1. Reps 2. Weight
                else if (firstRowText.equals("Reps:") && secondRowText.equals("Weight:")) {
                    if (repsInput.getText().toString().equals("") && weightInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter exercise info";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else if (repsInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter number of reps";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else if (weightInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter weight";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else {
                        styledText = "<strong>" +
                                repsInput.getText() + " x " + weightInput.getText() +
                                " lbs</strong>";
                        repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                        weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                    }
                }
                // 1. Distance 2. Weight
                else if (firstRowText.equals("Distance:") && secondRowText.equals("Weight:")) {
                    if (repsInput.getText().toString().equals("") && weightInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter exercise info";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else if (repsInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter distance";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else if (weightInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter weight";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else {
                        Spinner unit = findViewById(R.id.distance_choice);
                        styledText = "<strong>" +
                                repsInput.getText() + " " + unit.getSelectedItem().toString() + " @ " + weightInput.getText() + " lbs" +
                                "</strong>";
                        switch(unit.getSelectedItem().toString()) {
                            case "mi":
                                distanceVal = Double.parseDouble(String.valueOf(repsInput.getText())) * 5280;
                                break;
                            case "ft":
                                distanceVal = Double.parseDouble(String.valueOf(repsInput.getText()));
                                break;
                            case "in":
                                distanceVal = Double.parseDouble(String.valueOf(repsInput.getText())) / 12;
                                break;
                            case "km":
                                distanceVal = Double.parseDouble(String.valueOf(repsInput.getText())) * 3280.84;
                        }
                        weightVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                    }
                }
                // 1. Time 2. Distance
                else if (firstRowText.equals("Time:") && secondRowText.equals("Distance:")) {
                    String hInput = hours.getText().toString();
                    String mInput = minutes.getText().toString();
                    String sInput = seconds.getText().toString();

                    boolean noHours = hInput.equals("");
                    boolean noMinutes = mInput.equals("");
                    boolean noSeconds = sInput.equals("");

                    // This if/else checks for and handles distance as the second field field
                    if (weightInput.getText().toString().equals("")) {
                        if (noHours && noMinutes &&
                                noSeconds) {
                            String notSavedNotification = "Must enter exercise info to save";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                            toast.show();
                            return;
                        }
                        else if (noHours && noMinutes
                                && !noSeconds) {
                            styledText = "<strong>" + "0:0:" + sInput + " </strong>";
                            timeVal = Integer.parseInt(sInput);
                        }
                        else if (noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + "0:" + mInput + ":0" + " </strong>";
                            timeVal = Integer.parseInt(mInput) * 60;
                        }
                        else if (!noHours && noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:0" + " </strong>";
                            timeVal = Integer.parseInt(hInput) * 3600;
                        }
                        else if (noHours && !noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +"0:" + mInput + ":" + sInput + " </strong>";
                            timeVal = (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                        }
                        else if (!noHours && noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:" + sInput + " </strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + Integer.parseInt(sInput);
                        }
                        else if (!noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":0 </strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60);
                        }
                        else {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":" + sInput + "</strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                        }
                    }
                    else {
                        Spinner unit = findViewById(R.id.distance_choice);
                        String choice = unit.getSelectedItem().toString();
                        if (noHours && noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    weightInput.getText() + " " + choice + "</strong>";
                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                        }
                        else if (noHours && noMinutes
                                && !noSeconds) {
                            styledText = "<strong>" + "0:0:" + sInput + " / " +
                                    weightInput.getText() + " " + choice + "</strong>";
                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                            timeVal = Integer.parseInt(sInput);
                        }
                        else if (noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + "0:" + mInput + ":0" + " / " +
                                    weightInput.getText() + " " + choice + "</strong>";

                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                            timeVal = Integer.parseInt(mInput) * 60;
                        }
                        else if (!noHours && noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:0" + " / " +
                                    weightInput.getText() + " " + choice + "</strong>";

                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                            timeVal = Integer.parseInt(hInput) * 3600;
                        }
                        else if (noHours && !noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +"0:" + mInput + ":" + sInput + " / " +
                                    weightInput.getText() + " " + choice + "</strong>";

                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                            timeVal = (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                        }
                        else if (!noHours && noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:" + sInput + " / " +
                                    weightInput.getText() + " " + choice + "</strong>";

                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                            timeVal = (Integer.parseInt(hInput) * 3600) + Integer.parseInt(sInput);
                        }
                        else if (!noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":0 / " +
                                    weightInput.getText() + " " + choice + "</strong>";

                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60);
                        }
                        else {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":" + sInput + " / " +
                                    weightInput.getText() + " " + choice + "</strong>";

                            switch(choice) {
                                case "mi":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                    break;
                                case "ft":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                    break;
                                case "in":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                    break;
                                case "km":
                                    distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                            }
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                        }
                    }
                }
                // 1. Reps 2. Time
                else if (firstRowText.equals("Reps:") && secondRowText.equals("Time:")) {
                    String hInput = hours.getText().toString();
                    String mInput = minutes.getText().toString();
                    String sInput = seconds.getText().toString();

                    boolean noHours = hInput.equals("");
                    boolean noMinutes = mInput.equals("");
                    boolean noSeconds = sInput.equals("");

                    if (repsInput.getText().toString().equals("") && weightInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter exercise info";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else if (repsInput.getText().toString().equals("")) {
                        if (noHours && noMinutes
                                && !noSeconds) {
                            styledText = "<strong>" + "0:0:" + sInput + " </strong>";
                            timeVal = Integer.parseInt(sInput);
                        }
                        else if (noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + "0:" + mInput + ":0" + " </strong>";
                            timeVal = Integer.parseInt(mInput) * 60;
                        }
                        else if (!noHours && noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:0" + " </strong>";
                            timeVal = Integer.parseInt(hInput) * 3600;
                        }
                        else if (noHours && !noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +"0:" + mInput + ":" + sInput + " </strong>";
                            timeVal = (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                        }
                        else if (!noHours && noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":0:" + sInput + " </strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + Integer.parseInt(sInput);
                        }
                        else if (!noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":0 </strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60);
                        }
                        else {
                            styledText = "<strong>" +
                                    hInput + ":" + mInput + ":" + sInput + "</strong>";
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                        }
                    }
                    else if (weightInput.getText().toString().equals("")) {
                        styledText = "<strong>" + repsInput.getText() + "</strong>";
                        repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                    }
                    else {
                        if (noHours && noMinutes
                                && !noSeconds) {
                            styledText = "<strong>" + repsInput.getText() + " / " + "0:0:" + sInput +
                                    "</strong>";
                            repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                            timeVal = Integer.parseInt(sInput);
                        }
                        else if (noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + repsInput.getText() + " / " + "0:" + mInput + ":0" +
                                    "</strong>";
                            repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                            timeVal = Integer.parseInt(mInput) * 60;

                        }
                        else if (!noHours && noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + repsInput.getText() + " / " +
                                    hInput + ":0:0" + "</strong>";
                            repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                            timeVal = Integer.parseInt(hInput) * 3600;

                        }
                        else if (noHours && !noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" + repsInput.getText() + " / " +"0:" + mInput + ":" + sInput + "</strong>";
                            repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                            timeVal = (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);

                        }
                        else if (!noHours && noMinutes &&
                                !noSeconds) {
                            styledText = "<strong>" + repsInput.getText() + " / " +
                                    hInput + ":0:" + sInput + "</strong>";
                            repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                            timeVal = (Integer.parseInt(hInput) * 3600) + Integer.parseInt(sInput);

                        }
                        else if (!noHours && !noMinutes &&
                                noSeconds) {
                            styledText = "<strong>" + repsInput.getText() + " / " +
                                    hInput + ":" + mInput + ":0</strong>";
                            repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60);

                        }
                        else {
                            styledText = "<strong>" + repsInput.getText() + " / " +
                                    hInput + ":" + mInput + ":" + sInput + "</strong>";
                            repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                            timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);

                        }
                    }
                }
                // 1. Reps 2. Distance
                else if (firstRowText.equals("Reps:") && secondRowText.equals("Distance:")) {
                    Spinner unit = findViewById(R.id.distance_choice);
                    String choice = unit.getSelectedItem().toString();
                    if (repsInput.getText().toString().equals("") && weightInput.getText().toString().equals("")) {
                        String notSavedNotification = "Please enter exercise info";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else if (repsInput.getText().toString().equals("")) {
                        styledText = "<strong>" +
                                weightInput.getText() + " " + choice +
                                "</strong>";

                        switch(choice) {
                            case "mi":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                break;
                            case "ft":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                break;
                            case "in":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                break;
                            case "km":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                        }
                    }
                    else if (weightInput.getText().toString().equals("")) {
                        styledText = "<strong>" +
                                repsInput.getText() +
                                "</strong>";
                        repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                    }
                    else {
                        styledText = "<strong>" +
                                repsInput.getText() + " / " + weightInput.getText() + " " + choice +
                                "</strong>";

                        switch(choice) {
                            case "mi":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 5280;
                                break;
                            case "ft":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText()));
                                break;
                            case "in":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) / 12;
                                break;
                            case "km":
                                distanceVal = Double.parseDouble(String.valueOf(weightInput.getText())) * 3280.84;
                        }
                        repsVal = Integer.parseInt(String.valueOf(repsInput.getText()));
                    }
                }
                // 1. Time
                else {
                    String hInput = hours.getText().toString();
                    String mInput = minutes.getText().toString();
                    String sInput = seconds.getText().toString();

                    boolean noHours = hInput.equals("");
                    boolean noMinutes = mInput.equals("");
                    boolean noSeconds = sInput.equals("");

                    if (noHours && noMinutes &&
                            noSeconds) {
                        String notSavedNotification = "Please enter time";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(view.getContext(), notSavedNotification, duration);
                        toast.show();
                        return;
                    }
                    else if (noHours && noMinutes
                            && !noSeconds) {
                        styledText = "<strong>" + "0:0:" + sInput + " </strong>";
                        timeVal = Integer.parseInt(sInput);
                    }
                    else if (noHours && !noMinutes &&
                            noSeconds) {
                        styledText = "<strong>" + "0:" + mInput + ":0" + " </strong>";
                        timeVal = Integer.parseInt(mInput) * 60;
                    }
                    else if (!noHours && noMinutes &&
                            noSeconds) {
                        styledText = "<strong>" +
                                hInput + ":0:0" + " </strong>";
                        timeVal = Integer.parseInt(hInput) * 3600;
                    }
                    else if (noHours && !noMinutes &&
                            !noSeconds) {
                        styledText = "<strong>" +"0:" + mInput + ":" + sInput + " </strong>";
                        timeVal = (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                    }
                    else if (!noHours && noMinutes &&
                            !noSeconds) {
                        styledText = "<strong>" +
                                hInput + ":0:" + sInput + " </strong>";
                        timeVal = (Integer.parseInt(hInput) * 3600) + Integer.parseInt(sInput);
                    }
                    else if (!noHours && !noMinutes &&
                            noSeconds) {
                        styledText = "<strong>" +
                                hInput + ":" + mInput + ":0 </strong>";
                        timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60);
                    }
                    else {
                        styledText = "<strong> " +
                                hInput + ":" + mInput + ":" + sInput + "</strong>";
                        timeVal = (Integer.parseInt(hInput) * 3600) + (Integer.parseInt(mInput) * 60) + Integer.parseInt(sInput);
                    }
                }

                /**********************************************************************************/

                Set newSet = new Set(styledText, weightVal, repsVal, timeVal, distanceVal);
                sets.add(newSet);

                items.add(Html.fromHtml(styledText));
                adapter.notifyItemInserted(items.size());

                numClicked += 1;

                String savedNotification = "Saved!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(view.getContext(), savedNotification, duration);
                toast.show();

                finishedBtn.setText("Finished");
                finishedBtn.setTextColor(getResources().getColor(R.color.black));
                finishedBtn.setBackgroundResource(R.color.colorAccent);
                finishedBtn.setClickable(true);
            }
        });

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

}

class Set implements Serializable {
    public String styledText;
    public double weightVal;
    public int repsVal;
    public int timeVal;
    public double distanceVal;

    Set(String styled, double weight, int reps, int time, double distance) {
        this.styledText = styled;
        this.weightVal = weight;
        this.repsVal = reps;
        this.timeVal = time;
        this.distanceVal = distance;
    }
}
