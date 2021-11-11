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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EditWorkoutActivity extends AppCompatActivity implements OnStartDragListener{

    private ItemTouchHelper mItemTouchHelper;
    RecyclerListAdapter adapter;
    ArrayList<Spanned> items;
    ArrayList<ExerciseItem> exerciseDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        items = new ArrayList<>();
        exerciseDetails = new ArrayList<>();

        RecyclerView recyclerLayout = findViewById(R.id.saved_exercises);
        adapter = new RecyclerListAdapter(this, items, this::onStartDrag, true);

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
        String exerciseStrings;
        String intensity;
        String workoutName;
        JSONArray exercises;
        if (extras != null) {
            exerciseStrings = extras.getString("exercises");
            workoutName = extras.getString("workoutName");
            intensity = extras.getString("intensity");
            try {
                exercises = new JSONArray(exerciseStrings);
            } catch (JSONException e) {
                exercises = new JSONArray();
                e.printStackTrace();
            }
        }
        else {
            exercises = new JSONArray();
            workoutName = "";
            intensity = "";
        }

        EditText eName = findViewById(R.id.edit_workout_name);
        eName.setText(workoutName);

        switch(intensity) {
            case "Easy":
                RadioButton eBtn = findViewById(R.id.easyBtn);
                eBtn.setChecked(true);
                break;
            case "Moderate":
                RadioButton mBtn = findViewById(R.id.moderateBtn);
                mBtn.setChecked(true);
                break;
            case "Hard":
                RadioButton hBtn = findViewById(R.id.hardBtn);
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

                        adapter.notifyItemInserted(items.size());
                    }

                    styledText = "<p><strong><i>" + name;
                    same = false;
                }

                // Converts repsTimeOrDistanceVal into its appropriate value depending on exerciseType
                if (type.equals("D")) {
                    // TODO: I just HAD to allow the user to change the units for the distance val, now I need
                    // TODO: to find a way to pass that info to this view without adding it to the database
                    // Fuck

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

                if (!same && i != 0) {
                    exerciseDetails.add(new ExerciseItem(styledText, orderingWithinWorkout));
                    items.add(Html.fromHtml(styledText));

                    adapter.notifyItemInserted(items.size());
                }

                if (i == exercises.length() - 1 && count == exercises.length()) {
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
                for (int i = 0; i < exerciseDetails.size(); i++) {

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