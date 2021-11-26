package com.example.heartstrawngv1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddWorkoutActivity extends AppCompatActivity {
    ArrayList<Exercise> savedExercises;

    public List<Exercise> arm_workouts = new ArrayList<>();
    List<Exercise> back_workouts = new ArrayList<>();
    List<Exercise> cardio_workouts = new ArrayList<>();
    List<Exercise> chest_workouts = new ArrayList<>();
    List<Exercise> core_workouts = new ArrayList<>();
    List<Exercise> legs_workouts = new ArrayList<>();
    List<Exercise> shoulders_workouts = new ArrayList<>();
    List<Exercise> sports_workouts = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
        savedExercises = new ArrayList<>();

        // Gets the data sent from the previous view
        Bundle extras = getIntent().getExtras();
        boolean addWorkout;
        int workoutID;
        int highestOrderNum;
        int userID;
        if (extras != null) {
            addWorkout = extras.getBoolean("addWorkout");
            workoutID = extras.getInt("workoutID");
            highestOrderNum = extras.getInt("highestOrderNum");
            userID = extras.getInt("userID");
        }
        else {
            addWorkout = true;
            workoutID = -1;
            highestOrderNum = -1;
            userID = -1;
        }
        RelativeLayout layout = findViewById(R.id.progress_layout);


        Button goBackBtn = (Button) findViewById(R.id.create_workout_back_button);

        goBackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (addWorkout) {
                    if (savedExercises.size() == 0) {
                        finish();
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("New Workout");

                        LayoutInflater popupInflater = getLayoutInflater();
                        View dialogLayout = popupInflater.inflate(R.layout.finished_workout_popup, null);
                        builder.setView(dialogLayout);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String workoutName = ((EditText) dialogLayout.findViewById(R.id.new_workout_name)).getText().toString();

                                if (workoutName.equals("")) {
                                    ((EditText) dialogLayout.findViewById(R.id.new_workout_name)).setHint("Please enter a workout name");
                                    return;
                                }
                                RadioGroup group = (RadioGroup) dialogLayout.findViewById(R.id.intensity_btns);
                                int intensity = group.getCheckedRadioButtonId();
                                String convertedIntensity;

                                switch(intensity) {
                                    case R.id.easyBtn:
                                        convertedIntensity = "E";
                                        break;
                                    case R.id.moderateBtn:
                                        convertedIntensity = "M";
                                        break;
                                    case R.id.hardBtn:
                                        convertedIntensity = "H";
                                        break;
                                    default:
                                        convertedIntensity = "N";
                                }

                                // Instantiate the RequestQueue.
                                RequestQueue queue = Volley.newRequestQueue(dialogLayout.getContext());
                                String postUrl = "https://heartstrawng.azurewebsites.net/workout";
                                JSONArray exercisesArr = new JSONArray();

                                JSONObject postData = new JSONObject();
                                try {
                                    Log.d("USERID", String.valueOf(userID));

                                    postData.put("userID", userID);
                                    postData.put("workoutName", workoutName);
                                    postData.put("intensity", convertedIntensity);
                                    // Temporary
                                    postData.put("duration", 200);
                                    // End temporary

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                for (int i = 0; i < savedExercises.size(); i++) {
                                    JSONObject o = new JSONObject();
                                    try {
                                        o.put("exerciseID", savedExercises.get(i).id);
                                        o.put("exerciseType", savedExercises.get(i).exerciseType);
                                        o.put("repsTimeOrDistanceVal", savedExercises.get(i).repsTimeOrDistanceVal);
                                        if (savedExercises.get(i).weightUsed) {
                                            o.put("weightValue", savedExercises.get(i).weightVal);
                                        }
                                    } catch (JSONException e) {
                                        Log.d("JSONERR", e.toString());
                                    }
                                    exercisesArr.put(o);
                                }

                                try {
                                    postData.put("exercises", exercisesArr);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("POSTDATA", "EX: " + postData);

                                // Request a string response from the provided URL.
                                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, postUrl,
                                        postData,
                                        response -> {
                                            setResult(Activity.RESULT_OK);
                                            finish();
                                        }, error -> {
                                    Log.d("ERROR", error.toString());

                                });

                                stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                // Add the request to the RequestQueue.
                                queue.add(stringRequest);

                                Intent resultIntent = new Intent();

                                resultIntent.putExtra("exercises", savedExercises);
                                resultIntent.putExtra("workout_name", workoutName);
                                //setResult(Activity.RESULT_OK, resultIntent);
                                //finish();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                }
                else {
                    if (savedExercises.size() != 0) {
                        recursivelyAddExercises(0, workoutID, highestOrderNum, v);
                        String addedExercise = "Exercises added!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(v.getContext(), addedExercise, duration);
                        toast.show();
                    }
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        Button armsBtn = (Button) findViewById(R.id.workouts_arms);
        Button backBtn = (Button) findViewById(R.id.workouts_back);
        Button cardioBtn = (Button) findViewById(R.id.workouts_cardio);
        Button chestBtn = (Button) findViewById(R.id.workouts_chest);
        Button coreBtn = (Button) findViewById(R.id.workouts_core);
        Button legsBtn = (Button) findViewById(R.id.workouts_legs);
        Button shouldersBtn = (Button) findViewById(R.id.workouts_shoulders);
        Button sportsBtn = (Button) findViewById(R.id.workouts_sports);

        //RelativeLayout layout = doLoadingScreen("Loading exercises...");

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(650, 1100, 100, 200);

        TextView loadingText = new TextView(this);
        loadingText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        loadingText.setTextSize(24);
        loadingText.setText("Loading exercises...");
        loadingText.setPadding(375, 1350, 100, 200);
        layout.addView(progressBar);
        layout.addView(loadingText);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://heartstrawng.azurewebsites.net/exercises";

        // Request a string response from the provided URL.
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, url,
                null,
                response -> {
                    // Do something with response
                    //mTextView.setText(response.toString());

                    // Process the JSON
                    try{

                        // Loop through the array elements
                        for(int i = 0; i < response.length(); i++){
                            // Get current json object
                            JSONObject exercise = response.getJSONObject(i);

                            // Get the current exercise (json object) data
                            String name = exercise.getString("name");
                            String category = exercise.getString("category");
                            boolean repsBased = exercise.getBoolean("repsBased");
                            boolean timeBased = exercise.getBoolean("timeBased");
                            boolean distanceBased = exercise.getBoolean("distanceBased");
                            boolean weightUsed = exercise.getBoolean("weightUsed");
                            int id = exercise.getInt("exerciseID");

                            Exercise toAdd = new Exercise(name, repsBased, timeBased, distanceBased, weightUsed, id);

                            switch(category) {
                                case "Arms":
                                    arm_workouts.add(toAdd);
                                    break;
                                case "Back":
                                    back_workouts.add(toAdd);
                                    break;
                                case "Cardio":
                                    cardio_workouts.add(toAdd);
                                    break;
                                case "Chest":
                                    chest_workouts.add(toAdd);
                                    break;
                                case "Core":
                                    core_workouts.add(toAdd);
                                    break;
                                case "Legs":
                                    legs_workouts.add(toAdd);
                                    break;
                                case "Shoulders":
                                    shoulders_workouts.add(toAdd);
                                    break;
                                case "Sports":
                                    sports_workouts.add(toAdd);
                                    break;
                            }


                        }

                        boolean customExercises = false;
                        Map<String, ?> exerciseName = null;
                        try {
                            SharedPreferences settings = getApplicationContext().getSharedPreferences("SHARED_PREFS", 0);
                            exerciseName = settings.getAll();
                            customExercises = true;
                        } catch (NullPointerException e) {

                        }

                        if (customExercises) {
                            int maxID = 0;
                            for (Map.Entry<String, ?> entry : exerciseName.entrySet()) {
                                if (entry.getKey().toString().equals("maxID")) {
                                    maxID = (int) entry.getValue();
                                }
                                else if (entry.getKey().toString().equals("password") || entry.getKey().toString().equals("rememberMe")
                                        || entry.getKey().toString().equals("username")) {
                                    continue;
                                }
                                else {
                                    Iterator<String> iterator = ((HashSet<String>)entry.getValue()).iterator();
                                    boolean weightBased = false;
                                    boolean repsBased = false;
                                    boolean timeBased = false;
                                    boolean distanceBased = false;
                                    String category = "";
                                    while (iterator.hasNext()) {
                                        String next = iterator.next();
                                        if (!next.equals("weightBased") && !next.equals("repsBased") && !next.equals("timeBased") &&
                                                !next.equals("distanceBased")) {
                                            category = next;
                                        }
                                        weightBased = next.equals("weightBased");
                                        repsBased = next.equals("repsBased");
                                        timeBased = next.equals("timeBased");
                                        distanceBased = next.equals("distanceBased");
                                    }
                                    Exercise customExercise;
                                    if (maxID == 0) {
                                        customExercise =
                                                new Exercise(entry.getKey(), repsBased, timeBased, distanceBased, weightBased, 97);
                                    }
                                    else {
                                        customExercise =
                                                new Exercise(entry.getKey(), repsBased, timeBased, distanceBased, weightBased, maxID);
                                    }


                                    switch(category) {
                                        case "Arms":
                                            arm_workouts.add(customExercise);
                                            break;
                                        case "Back":
                                            back_workouts.add(customExercise);
                                            break;
                                        case "Cardio":
                                            cardio_workouts.add(customExercise);
                                            break;
                                        case "Chest":
                                            chest_workouts.add(customExercise);
                                            break;
                                        case "Core":
                                            core_workouts.add(customExercise);
                                            break;
                                        case "Legs":
                                            legs_workouts.add(customExercise);
                                            break;
                                        case "Shoulders":
                                            shoulders_workouts.add(customExercise);
                                            break;
                                        case "Sports":
                                            sports_workouts.add(customExercise);
                                            break;
                                    }
                                }
                            }
                        }

                        arm_workouts.sort(Comparator.comparing(o -> o.name));
                        back_workouts.sort(Comparator.comparing(o -> o.name));
                        cardio_workouts.sort(Comparator.comparing(o -> o.name));
                        chest_workouts.sort(Comparator.comparing(o -> o.name));
                        core_workouts.sort(Comparator.comparing(o -> o.name));
                        legs_workouts.sort(Comparator.comparing(o -> o.name));
                        shoulders_workouts.sort(Comparator.comparing(o -> o.name));
                        sports_workouts.sort(Comparator.comparing(o -> o.name));

                        arm_workouts.add(new Exercise("Add New Exercise"));
                        back_workouts.add(new Exercise("Add New Exercise"));
                        cardio_workouts.add(new Exercise("Add New Exercise"));
                        chest_workouts.add(new Exercise("Add New Exercise"));
                        core_workouts.add(new Exercise("Add New Exercise"));
                        legs_workouts.add(new Exercise("Add New Exercise"));
                        shoulders_workouts.add(new Exercise("Add New Exercise"));
                        sports_workouts.add(new Exercise("Add New Exercise"));


                        setBtnPressed(armsBtn, arm_workouts, "Arm");
                        setBtnPressed(backBtn, back_workouts, "Back");
                        setBtnPressed(cardioBtn, cardio_workouts, "Cardio");
                        setBtnPressed(chestBtn, chest_workouts, "Chest");
                        setBtnPressed(coreBtn, core_workouts, "Core");
                        setBtnPressed(legsBtn, legs_workouts, "Leg");
                        setBtnPressed(shouldersBtn, shoulders_workouts, "Shoulder");
                        setBtnPressed(sportsBtn, sports_workouts, "Sports");

                        ((ViewGroup) layout.getParent()).removeView(layout);


                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("ERROR", error.toString());
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    public void setBtnPressed(Button b, final List<Exercise> workouts, final String type) {
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                // Add list of arm workouts
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                String title = "Select " + type + " Exercise to Add";

                String[] names = new String[workouts.size()];
                for (int i = 0; i < workouts.size(); i++) {
                    names[i] = workouts.get(i).name;
                }

                builder.setTitle(title)
                        .setItems(names, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                if (which == workouts.size() - 1) {
                                    Intent addCustomExercise = new Intent(v.getContext(), AddCustomExercise.class);
                                    addCustomExercise.putExtra("type", type);
                                    startActivityForResult(addCustomExercise, 2);                                }
                                else {
                                    Intent setWorkoutDetailsIntent = new Intent(v.getContext(), setWorkoutDetails.class);
                                    setWorkoutDetailsIntent.putExtra("name", workouts.get(which).name);
                                    setWorkoutDetailsIntent.putExtra("fullDetails", workouts.get(which));
                                    setWorkoutDetailsIntent.putExtra("setInfo", new ArrayList<String>());
                                    startActivityForResult(setWorkoutDetailsIntent, 1);
                                    //startActivity(setWorkoutDetailsIntent);
                                }

                            }
                        });
                builder.create();
                builder.show();

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // add existing exercise
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra("name");
                int id = data.getIntExtra("id", -1);
                boolean repsBased = data.getBooleanExtra("repsBased", false);
                boolean weightBased = data.getBooleanExtra("weightBased", false);
                boolean timeBased = data.getBooleanExtra("timeBased", false);
                boolean distanceBased = data.getBooleanExtra("distanceBased", false);
                ArrayList<Set> sets = (ArrayList<Set>) data.getSerializableExtra("sets");

                for (int i = 0; i < sets.size(); i++) {
                    Exercise toAdd;

                    if ((timeBased && distanceBased) || (repsBased && timeBased)) {
                        if (sets.get(i).timeVal == 0 && (repsBased && timeBased)) {
                            toAdd = new Exercise(name, false, true, false, false, id, "R", sets.get(i).repsVal);
                        }
                        else if (sets.get(i).timeVal == 0 && (timeBased && distanceBased)) {
                            toAdd = new Exercise(name, false, true, false, false, id, "D", sets.get(i).distanceVal);
                        }
                        else {
                            toAdd = new Exercise(name, false, true, false, false, id, "T", sets.get(i).timeVal);
                        }
                        // TODO: add distance/reps and some sort of identifier to user device
                    }
                    else if (repsBased && distanceBased) {
                        if (sets.get(i).distanceVal == 0) {
                            toAdd = new Exercise(name, false, false, true, false, id, "R", sets.get(i).repsVal);
                        }
                        else {
                            toAdd = new Exercise(name, false, false, true, false, id, "D", sets.get(i).distanceVal);
                        }
                    }
                    else if (timeBased){
                        if (sets.get(i).weightVal != 0.0) {
                            toAdd = new Exercise(name, false, true, false, weightBased, id, "T", sets.get(i).timeVal,
                                    sets.get(i).weightVal);
                        }
                        else {
                            toAdd = new Exercise(name, false, true, false, weightBased, id, "T", sets.get(i).timeVal);
                        }
                    }
                    else if (repsBased) {
                        if (sets.get(i).weightVal != 0.0) {
                            toAdd = new Exercise(name, true, false, false, weightBased, id, "R", sets.get(i).repsVal,
                                    sets.get(i).weightVal);
                        }
                        else {
                            toAdd = new Exercise(name, true, false, false, weightBased, id, "R", sets.get(i).repsVal);
                        }
                    }
                    else if (distanceBased) {
                        if (sets.get(i).weightVal != 0.0) {
                            toAdd = new Exercise(name, false, false, true, weightBased, id, "D", sets.get(i).distanceVal,
                                    sets.get(i).weightVal);
                        }
                        else {
                            toAdd = new Exercise(name, false, false, true, weightBased, id, "D", sets.get(i).distanceVal);
                        }
                    }
                    else {
                        toAdd = new Exercise(name);
                    }
                    savedExercises.add(toAdd);
                }

                Button savedButton = findViewById(R.id.create_workout_back_button);
                savedButton.setText("Save");
            }
        }
        // add custom exercise
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                String type = data.getStringExtra("type");
                String name = data.getStringExtra("name");
                boolean wb = data.getBooleanExtra("weightBased", false);
                boolean rb = data.getBooleanExtra("repsBased", false);
                boolean tb = data.getBooleanExtra("timeBased", false);
                boolean db = data.getBooleanExtra("distanceBased", false);
                int maxID = data.getIntExtra("maxID", 97);

                Exercise newExercise = new Exercise(name, rb, tb, db, wb, maxID);
                switch(type) {
                    case "Arms":
                        arm_workouts.remove(arm_workouts.size()-1);
                        arm_workouts.add(newExercise);
                        arm_workouts.sort(Comparator.comparing(o -> o.name));
                        arm_workouts.add(new Exercise("Add New Exercise"));
                        break;
                    case "Back":
                        back_workouts.remove(back_workouts.size()-1);
                        back_workouts.add(newExercise);
                        back_workouts.sort(Comparator.comparing(o -> o.name));
                        back_workouts.add(new Exercise("Add New Exercise"));
                        break;
                    case "Cardio":
                        cardio_workouts.remove(cardio_workouts.size()-1);
                        cardio_workouts.add(newExercise);
                        cardio_workouts.sort(Comparator.comparing(o -> o.name));
                        cardio_workouts.add(new Exercise("Add New Exercise"));
                        break;
                    case "Chest":
                        chest_workouts.remove(chest_workouts.size()-1);
                        chest_workouts.add(newExercise);
                        chest_workouts.sort(Comparator.comparing(o -> o.name));
                        chest_workouts.add(new Exercise("Add New Exercise"));
                        break;
                    case "Core":
                        core_workouts.remove(core_workouts.size()-1);
                        core_workouts.add(newExercise);
                        core_workouts.sort(Comparator.comparing(o -> o.name));
                        core_workouts.add(new Exercise("Add New Exercise"));
                        break;
                    case "Legs":
                        legs_workouts.remove(legs_workouts.size()-1);
                        legs_workouts.add(newExercise);
                        legs_workouts.sort(Comparator.comparing(o -> o.name));
                        legs_workouts.add(new Exercise("Add New Exercise"));
                        break;
                    case "Shoulders":
                        shoulders_workouts.remove(shoulders_workouts.size()-1);
                        shoulders_workouts.add(newExercise);
                        shoulders_workouts.sort(Comparator.comparing(o -> o.name));
                        shoulders_workouts.add(new Exercise("Add New Exercise"));
                        break;
                }
            }
        }
    }
    public RelativeLayout doLoadingScreen(String text) {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.progress_layout);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(650, 1100, 100, 200);

        TextView loadingText = new TextView(this);
        loadingText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        loadingText.setTextSize(24);
        loadingText.setText(text);
        loadingText.setPadding(375, 1350, 100, 200);
        Log.d("PROGRESS", progressBar.toString());
        layout.addView(progressBar);
        layout.addView(loadingText);

        return layout;
    }

    public void recursivelyAddExercises(int index, int workoutID, int highestOrderNum, View v) {

        if (index == savedExercises.size()) {
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(v.getContext());
        String postUrl = "https://heartstrawng.azurewebsites.net/workout/" + workoutID + "/exercise";
        JSONObject o = new JSONObject();
        try {
            o.put("exerciseID", savedExercises.get(index).id);
            o.put("orderNum", (highestOrderNum + index + 1));
            o.put("exerciseType", savedExercises.get(index).exerciseType);
            o.put("repsTimeOrDistanceVal", savedExercises.get(index).repsTimeOrDistanceVal);
            if (savedExercises.get(index).weightUsed) {
                o.put("weightValue", savedExercises.get(index).weightVal);
            }
        } catch (JSONException e) {
            Log.d("JSONERR", e.toString());
        }
        // Request a string response from the provided URL.
        JsonObjectRequest addExerciseRequest = new JsonObjectRequest(Request.Method.POST, postUrl,
                o,
                response -> {
                    recursivelyAddExercises(index + 1, workoutID, highestOrderNum, v);
                }, error -> {
            Log.d("ERROR", o.toString());

        });

        addExerciseRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(addExerciseRequest);
    }
}

class Exercise implements Serializable {
    public String name;
    public boolean repsBased;
    public boolean timeBased;
    public boolean distanceBased;
    public boolean weightUsed;
    public int id;
    public double weight;
    public int reps;
    public double distance;
    public String time;
    public String exerciseType;
    public double repsTimeOrDistanceVal;
    public double weightVal;

    Exercise(String n, boolean r, boolean t, boolean d, boolean w, int id, String exerciseType, double repsTimeOrDistanceVal, double weightVal) {
        this.name = n;
        this.repsBased = r;
        this.timeBased = t;
        this.distanceBased = d;
        this.weightUsed = w;
        this.id = id;
        this.repsTimeOrDistanceVal = repsTimeOrDistanceVal;
        this.weight = -1;
        this.reps = -1;
        this.distance = -1;
        this.time = "";
        this.exerciseType = exerciseType;
        this.weightVal = weightVal;
    }

    Exercise(String n, boolean r, boolean t, boolean d, boolean w, int id, String exerciseType, double repsTimeOrDistanceVal) {
        this.name = n;
        this.repsBased = r;
        this.timeBased = t;
        this.distanceBased = d;
        this.weightUsed = w;
        this.id = id;
        this.repsTimeOrDistanceVal = repsTimeOrDistanceVal;
        this.weight = -1;
        this.reps = -1;
        this.distance = -1;
        this.time = "";
        this.exerciseType = exerciseType;
    }

    Exercise(String n, boolean r, boolean t, boolean d, boolean w, int id) {
        this.name = n;
        this.repsBased = r;
        this.timeBased = t;
        this.distanceBased = d;
        this.weightUsed = w;
        this.id = id;
        this.weight = -1;
        this.reps = -1;
        this.distance = -1;
        this.time = "";
    }

    Exercise(String n) {
        this.name = n;
        this.repsBased = false;
        this.timeBased = false;
        this.distanceBased = false;
        this.weightUsed = false;
        this.id = -1;
        this.weight = -1;
        this.reps = -1;
        this.distance = -1;
        this.time = "";
    }
}
