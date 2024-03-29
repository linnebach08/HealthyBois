package com.example.heartstrawngv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.graphics.vector.Stroke;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditWorkoutActivity extends AppCompatActivity implements OnStartDragListener{

    private ItemTouchHelper mItemTouchHelper;
    RecyclerListAdapter adapter;
    ArrayList<Spanned> items;
    ArrayList<ExerciseItem> exerciseDetails;
    RadioButton eBtn;
    RadioButton mBtn;
    RadioButton hBtn;
    EditText eName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);
        eBtn = findViewById(R.id.easyBtn);
        mBtn = findViewById(R.id.moderateBtn);
        hBtn = findViewById(R.id.hardBtn);
        eName = findViewById(R.id.edit_workout_name);

        items = new ArrayList<>();
        exerciseDetails = new ArrayList<>();

        // Gets the data sent from the previous view
        Bundle extras = getIntent().getExtras();
        String exerciseStrings;
        String intensity;
        String workoutName;
        int workoutID;
        JSONArray exercises;
        if (extras != null) {
            exerciseStrings = extras.getString("exercises");
            workoutName = extras.getString("workoutName");
            intensity = extras.getString("intensity");
            workoutID = extras.getInt("workoutID");
            try {
                exercises = new JSONArray(exerciseStrings);
            } catch (JSONException e) {
                exercises = new JSONArray();
                e.printStackTrace();
            }
        }
        else {
            exerciseStrings = "";
            exercises = new JSONArray();
            workoutName = "";
            intensity = "";
            workoutID = -1;
        }

        RecyclerView recyclerLayout = findViewById(R.id.saved_exercises);
        adapter = new RecyclerListAdapter(this, items, this::onStartDrag, true, workoutID);

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

        eName.setText(workoutName);

        switch(intensity) {
            case "Easy":
                eBtn.setChecked(true);
                break;
            case "Moderate":
                mBtn.setChecked(true);
                break;
            case "Hard":
                hBtn.setChecked(true);
                break;
        }

        String prevExercise = "";
        String copyStyled = "";
        boolean same = false;
        int count = 1;
        for (int i = 0; i < exercises.length(); i++) {
            try {
                JSONObject o = exercises.getJSONObject(i);
                Log.d("OBJECT", "It's " + o);
                String name = o.getString("exercise");
                int repsTimeOrDistanceVal = o.getInt("repsTimeOrDistanceValue");
                int orderingWithinWorkout = o.getInt("orderingWithinWorkout");
                boolean hasWeight = false;
                int weightVal = 0;
                if (o.has("weightValue") && !o.isNull("weightValue")) {
                    weightVal = o.getInt("weightValue");
                    hasWeight = true;
                }
                String type = o.getString("exerciseType");

                String styledText = "";
                if (prevExercise.equals(name)) {
                    count++;
                    styledText = copyStyled.substring(0, copyStyled.length() - 17) + "<br><br> &emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp&emsp";
                    same = true;
                }
                else {
                    if (same) {
                        exerciseDetails.add(new ExerciseItem(copyStyled, orderingWithinWorkout));
                        Log.d("ADDING", copyStyled);
                        items.add(Html.fromHtml(copyStyled));

                        count = 1;

                        adapter.notifyItemInserted(items.size());
                    }
                    else if (i > 0) {
                        exerciseDetails.add(new ExerciseItem(copyStyled, orderingWithinWorkout - 1));
                        items.add(Html.fromHtml(copyStyled));

                        adapter.notifyItemInserted(items.size());
                    }

                    styledText = "<p><strong><i>" + name;
                    same = false;
                }

                // Converts repsTimeOrDistanceVal into its appropriate value depending on exerciseType
                if (type.equals("D")) {
                    // TODO: I just HAD to allow the user to change the units for the distance val, now I need
                    // TODO: to find a way to pass that info to this view without adding it to the database

                }
                else if (type.equals("T")) {
                    int s = repsTimeOrDistanceVal % 60;
                    int h = repsTimeOrDistanceVal / 60;
                    int m = h % 60;
                    h = h / 60;

                    styledText += " &emsp " + h + ":" + m + ":" + s;
                }
                else {
                    styledText += " &emsp " + repsTimeOrDistanceVal;
                }

                if (hasWeight) {
                    styledText += " @ " + weightVal + " lbs</i></strong></p>";
                }
                else {
                    styledText += "</i></strong></p>";
                }

                prevExercise = name;
                copyStyled = styledText;

                if (!same && i != 0 && i == exercises.length() - 1) {
                    exerciseDetails.add(new ExerciseItem(styledText, orderingWithinWorkout));
                    items.add(Html.fromHtml(styledText));

                    adapter.notifyItemInserted(items.size());
                }

                if ((i == exercises.length() - 1 && count == exercises.length()) ||
                        (i == exercises.length() - 1 && count != 1)) {
                    exerciseDetails.add(new ExerciseItem(copyStyled, orderingWithinWorkout));
                    items.add(Html.fromHtml(copyStyled));

                    adapter.notifyItemInserted(items.size());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Button finished = findViewById(R.id.finished_btn);
        finished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean intensityChanged = false;
                String newIntensity = "";
                if (eBtn.isChecked() && !intensity.equals("Easy")) {
                    intensityChanged = true;
                    newIntensity = "E";
                }
                else if (mBtn.isChecked() && !intensity.equals("Moderate")) {
                    intensityChanged = true;
                    newIntensity = "M";
                }
                else if (hBtn.isChecked() && !intensity.equals("Hard")) {
                    intensityChanged = true;
                    newIntensity = "H";
                }

                String currentName = eName.getText().toString();
                boolean nameChanged = false;
                if (!currentName.equals(workoutName)) {
                    nameChanged = true;
                }

                if (nameChanged || intensityChanged) {
                    JSONObject o = new JSONObject();
                    if (nameChanged) {
                        try {
                            o.put("workoutName", currentName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (intensityChanged) {
                        try {
                            o.put("intensity", newIntensity);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String putUrl = "https://heartstrawng.azurewebsites.net/workout/" + workoutID;

                    // Request a string response from the provided URL.
                    JsonObjectRequest putWorkoutRequest = new JsonObjectRequest(Request.Method.PUT, putUrl,
                            o,
                            response -> {

                            }, error -> {
                        Log.d("ERROR:Request", error.toString());

                    });

                    putWorkoutRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    // Add the request to the RequestQueue.
                    queue.add(putWorkoutRequest);
                }
                else {
                    for (int i = 0; i < exerciseDetails.size(); i++) {

                    }
                }
            }
        });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}

class ExerciseItem {
    public String styledText;
    public int ordering;

    ExerciseItem(String styledText, int ordering) {
        this.styledText = styledText;
        this.ordering = ordering;
    }
}