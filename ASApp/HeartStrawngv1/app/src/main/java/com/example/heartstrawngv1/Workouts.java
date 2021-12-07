package com.example.heartstrawngv1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class Workouts extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String name;
    private ArrayList<Exercise> exercises;
    private HashMap<String, ArrayList<String>> fullWorkouts;
    private JSONArray fullResponse;
    private ArrayList<String> workoutIDs;
    private LinearLayout svd;
    private View returnedView;
    private int userID;

    //private OnFragmentInteractionListener mListener;

    public Workouts() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Workouts.
     */
    // TODO: Rename and change types and number of parameters
    public static Workouts newInstance(String param1, String param2) {
        Workouts fragment = new Workouts();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_workouts, container, false);
        Button addWorkoutView = (Button) view.findViewById(R.id.add_workout_button);
        Button showWorkoutView = (Button) view.findViewById(R.id.show_workout_button);
        svd = view.findViewById(R.id.saved);
        returnedView = view;
        fullWorkouts = new HashMap<>();
        workoutIDs = new ArrayList<>();

        Bundle extras = this.getArguments();
        String firstName;
        String lastName;
        String username;
        boolean newUser;
        if (extras != null) {
            userID = extras.getInt("userID");
            firstName = extras.getString("firstName");
            lastName = extras.getString("lastName");
            username = extras.getString("username");
            newUser = extras.getBoolean("newUser");
        }
        else {
            userID = -1;
            firstName = "";
            lastName = "";
            username = "";
            newUser = false;
        }

        if (!newUser) {
            getWorkouts(userID);
        }

        addWorkoutView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent addWorkoutIntent = new Intent(v.getContext(), AddWorkoutActivity.class);
                addWorkoutIntent.putExtra("addWorkout", true);
                addWorkoutIntent.putExtra("userID", userID);
                startActivityForResult(addWorkoutIntent, 1);

            }
        });

        showWorkoutView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(returnedView.getContext());
                String url = "https://heartstrawng.azurewebsites.net/workouts/schedule/" + userID;

                // Request a string response from the provided URL.
                JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, url,
                        null,
                        response -> {
                            Intent showWorkoutIntent = new Intent(v.getContext(), ShowWorkout.class);
                            showWorkoutIntent.putExtra("response", response.toString());
                            showWorkoutIntent.putExtra("workouts", fullWorkouts);
                            showWorkoutIntent.putExtra("workoutIDs", workoutIDs);
                            showWorkoutIntent.putExtra("workoutResponse", fullResponse.toString());
                            startActivityForResult(showWorkoutIntent, 5);
                        },
                        error -> {
                            Log.d("ERROR1", error.toString());
                        });

                queue.add(stringRequest);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || requestCode == 2 || requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {
                getWorkouts(userID);
            }
        }
        if (requestCode == 4) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("scheduled", false)) {
                    getWorkouts(userID);
                }

            }
        }
    }

    public void getWorkouts(int userID) {
        ProgressBar progressBar = new ProgressBar(svd.getContext());
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(100, 100, 100, 100);

        TextView loadingText = new TextView(svd.getContext());
        loadingText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        loadingText.setTextSize(24);
        loadingText.setText("Loading workouts...");
        loadingText.setPadding(350, 100, 200, 100);
        svd.addView(progressBar);
        svd.addView(loadingText);


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(returnedView.getContext());
        String url = "https://heartstrawng.azurewebsites.net/workouts/" + userID;

        // Request a string response from the provided URL.
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, url,
                null,
                response -> {
                    try {
                        if (response.length() != 0) {
                            try {
                                svd.removeView(returnedView.findViewById(R.id.saved_workouts_table));
                            } catch (Exception e) {

                            }
                            TableLayout workoutsTable = new TableLayout(returnedView.getContext());
                            workoutsTable.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                    TableLayout.LayoutParams.MATCH_PARENT));
                            workoutsTable.setGravity(Gravity.START);
                            workoutsTable.setId(R.id.saved_workouts_table);
                            workoutsTable.setStretchAllColumns(true);

                            TableRow heading = new TableRow(returnedView.getContext());
                            heading.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0));
                            heading.setPadding(0, 0, 0, 90);

                            TextView wName = new TextView(returnedView.getContext());
                            wName.setLayoutParams(new TableRow.LayoutParams(0));
                            wName.setTextSize(25);
                            wName.setTypeface(null, Typeface.BOLD);
                            wName.setText("Name");


                            TextView iName = new TextView(returnedView.getContext());
                            iName.setLayoutParams(new TableRow.LayoutParams(1));
                            iName.setTextSize(25);
                            iName.setTypeface(null, Typeface.BOLD);
                            iName.setGravity(Gravity.CENTER);
                            iName.setText("Intensity");

                            heading.addView(wName);
                            heading.addView(iName);

                            workoutsTable.addView(heading);

                            svd.addView(workoutsTable);
                        }
                        TableLayout workoutList = returnedView.findViewById(R.id.saved_workouts_table);
                        fullResponse = response;
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject exercise = response.getJSONObject(i);

                            int workoutID = exercise.getInt("workoutID");
                            String workoutName = exercise.getString("workoutName");
                            String intensity = exercise.getString("intensity");
                            int duration = exercise.getInt("duration");
                            JSONArray exercises = exercise.getJSONArray("exercises");
                            TableRow newRow = new TableRow(returnedView.getContext());
                            newRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0));
                            newRow.setPadding(0, 0, 0, 90);

                            ArrayList<String> exerciseNames = new ArrayList<>();
                            for (int j = 0; j < exercises.length(); j++) {
                                exerciseNames.add(exercises.get(j).toString());
                            }

                            fullWorkouts.put(workoutName, exerciseNames);
                            workoutIDs.add(String.valueOf(workoutID));
                            Log.d("Workouts", "fullWorkouts " + fullWorkouts);
                            Log.d("Workouts", "workoutIds " + workoutIDs);

                            TextView newWrk = new TextView(returnedView.getContext());
                            newWrk.setLayoutParams(new TableRow.LayoutParams(0));
                            newWrk.setTextSize(23);
                            newWrk.setClickable(true);
                            newWrk.setText(workoutName);

                            TextView newWrkInt = new TextView(returnedView.getContext());
                            newWrkInt.setLayoutParams(new TableRow.LayoutParams(1));
                            newWrkInt.setTextSize(23);
                            newWrkInt.setClickable(true);
                            newWrkInt.setGravity(Gravity.CENTER);

                            switch(intensity) {
                                case "E":
                                    intensity = "Easy";
                                    break;
                                case "M":
                                    intensity = "Moderate";
                                    break;
                                case "H":
                                    intensity = "Hard";
                                    break;
                                default:
                                    intensity = "N/A";
                                    break;
                            }
                            newWrkInt.setText(intensity);

                            Button menuButton = new Button(returnedView.getContext());
                            menuButton.setBackgroundResource(R.drawable.menu_options);
                            menuButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                            menuButton.setScaleX(0.2F);
                            menuButton.setScaleY(0.6F);
                            menuButton.setPadding(100, 0, 0, 0);
                            menuButton.setLayoutParams(new TableRow.LayoutParams(2));
                            String finalIntensity = intensity;
                            menuButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    PopupMenu options = new PopupMenu(returnedView.getContext(), menuButton);

                                    // Inflating popup menu from popup_menu.xml file
                                    options.getMenuInflater().inflate(R.menu.popup_menu, options.getMenu());
                                    options.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem menuItem) {
                                            String title = (String) menuItem.getTitle();

                                            if (title.equals("Start")) {
                                                ProgressDialog.show(returnedView.getContext(), "Starting Workout", "Please wait...", true);
                                                Intent startWorkoutIntent = new Intent(view.getContext(), StartWorkout.class);
                                                startWorkoutIntent.putExtra("exercises", exerciseNames);
                                                startWorkoutIntent.putExtra("workoutName", workoutName);
                                                startWorkoutIntent.putExtra("userID", userID);
                                                startActivityForResult(startWorkoutIntent, 5);

                                            } else if (title.equals("Schedule")) {
                                                Intent scheduleIntent = new Intent(view.getContext(), ScheduleWorkout.class);
                                                scheduleIntent.putExtra("workoutName", workoutName);
                                                scheduleIntent.putExtra("workoutID", workoutID);
                                                scheduleIntent.putExtra("userID", userID);
                                                startActivityForResult(scheduleIntent, 4);

                                            } else if (title.equals("Edit")) {
                                                Intent editWorkoutIntent = new Intent(view.getContext(), EditWorkoutActivity.class);
                                                editWorkoutIntent.putExtra("exercises", exercises.toString());
                                                editWorkoutIntent.putExtra("workoutName", workoutName);
                                                editWorkoutIntent.putExtra("intensity", finalIntensity);
                                                editWorkoutIntent.putExtra("workoutID", workoutID);
                                                startActivityForResult(editWorkoutIntent, 3);

                                            } else if (title.equals("Add Exercise")) {
                                                Intent addWorkoutIntent = new Intent(view.getContext(), AddWorkoutActivity.class);
                                                addWorkoutIntent.putExtra("addWorkout", false);
                                                addWorkoutIntent.putExtra("workoutID", workoutID);
                                                addWorkoutIntent.putExtra("highestOrderNum", exercises.length());
                                                startActivityForResult(addWorkoutIntent, 2);

                                            } else if (title.equals("Delete")) {
                                                String deleteUrl = "https://heartstrawng.azurewebsites.net/workout/" + workoutID;
                                                Log.d("URL", deleteUrl);
                                                StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, deleteUrl,
                                                        response1 -> {
                                                            getWorkouts(userID);
                                                            return;
                                                        },
                                                        error -> {
                                                            Log.d("ERROR", error.toString());
                                                        });
                                                deleteRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                                queue.add(deleteRequest);

                                            } else {
                                                Toast.makeText(returnedView.getContext(), "Something odd happened", Toast.LENGTH_LONG).show();
                                            }
                                            return false;
                                        }
                                    });
                                    // Showing the popup menu
                                    options.show();
                                }
                            });

                            /*TextView scheduleWrk = new TextView(returnedView.getContext());
                            scheduleWrk.setBackgroundResource(android.R.drawable.ic_menu_my_calendar);
                            scheduleWrk.setScaleX(0.6F);
                            scheduleWrk.setScaleY(0.9F);
                            scheduleWrk.setGravity(Gravity.CENTER);
                            scheduleWrk.setLayoutParams(new TableRow.LayoutParams(3));

                            TextView addWrk = new TextView(returnedView.getContext());
                            addWrk.setBackgroundResource(android.R.drawable.ic_menu_add);
                            addWrk.setScaleX(0.6F);
                            addWrk.setScaleY(0.9F);
                            addWrk.setGravity(Gravity.CENTER);
                            addWrk.setLayoutParams(new TableRow.LayoutParams(2));

                            addWrk.setLayoutParams(new TableRow.LayoutParams(4));

                            TextView editWrk = new TextView(returnedView.getContext());
                            editWrk.setBackgroundResource(android.R.drawable.ic_menu_edit);
                            editWrk.setScaleX(0.6F);
                            editWrk.setScaleY(0.9F);
                            editWrk.setGravity(Gravity.CENTER);
                            editWrk.setLayoutParams(new TableRow.LayoutParams(3));
                            editWrk.setLayoutParams(new TableRow.LayoutParams(5));

                            TextView deleteWrk = new TextView(returnedView.getContext());
                            deleteWrk.setLayoutParams(new TableRow.LayoutParams(4));
                            deleteWrk.setLayoutParams(new TableRow.LayoutParams(6));
                            deleteWrk.setTextSize(28);
                            deleteWrk.setClickable(true);
                            deleteWrk.setText("x");
                            deleteWrk.setGravity(Gravity.CENTER);

                            deleteWrk.setTextColor(Color.RED);

                            newWrk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    view.setBackgroundColor(Color.LTGRAY);
                                    newWrkInt.setBackgroundColor(Color.LTGRAY);
                            TextView startWrk = new TextView(returnedView.getContext());
                            editWrk.setBackgroundResource(android.R.drawable.ic_media_play);
                            editWrk.setScaleX(0.6F);
                            editWrk.setScaleY(0.9F);
                            editWrk.setGravity(Gravity.CENTER);
                            editWrk.setLayoutParams(new TableRow.LayoutParams(0));

                            startWrk.setOnClickListener(view -> {
                                Intent startWorkoutIntent = new Intent(view.getContext(), StartWorkout.class);
                                startWorkoutIntent.putExtra("exercises", exerciseNames);
                                startActivityForResult(startWorkoutIntent, 5);
                            });*/

                            /*newWrk.setOnClickListener(view -> {
                                view.setBackgroundColor(Color.LTGRAY);
                                newWrkInt.setBackgroundColor(Color.LTGRAY);

                                }
                            });

                            newWrkInt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    view.setBackgroundColor(Color.LTGRAY);
                                    newWrk.setBackgroundColor(Color.LTGRAY);
                            newWrkInt.setOnClickListener(view -> {
                                view.setBackgroundColor(Color.LTGRAY);
                                newWrk.setBackgroundColor(Color.LTGRAY);

                                }
                            });

                            scheduleWrk.setOnClickListener(view -> {
                                Intent scheduleIntent = new Intent(view.getContext(), ScheduleWorkout.class);
                                scheduleIntent.putExtra("workoutName", workoutName);
                                scheduleIntent.putExtra("workoutID", workoutID);
                                scheduleIntent.putExtra("userID", userID);
                                startActivityForResult(scheduleIntent, 4);
                            });

                            deleteWrk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String deleteUrl = "https://heartstrawng.azurewebsites.net/workout/" + workoutID;
                                    Log.d("URL", deleteUrl);
                                    StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, deleteUrl,
                                            response1 -> {

                                            },
                                            error -> {
                                                Log.d("ERROR", error.toString());
                                            });
                                    deleteRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    queue.add(deleteRequest);
                                }
                            deleteWrk.setOnClickListener(view -> {
                                String deleteUrl = "https://heartstrawng.azurewebsites.net/workout/" + workoutID;
                                Log.d("URL", deleteUrl);
                                StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, deleteUrl,
                                        response1 -> {
                                            getWorkouts(userID);
                                            return;
                                        },
                                        error -> {
                                            Log.d("ERROR", error.toString());
                                        });
                                deleteRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                queue.add(deleteRequest);
                            });

                            addWrk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent addWorkoutIntent = new Intent(view.getContext(), AddWorkoutActivity.class);
                                    addWorkoutIntent.putExtra("addWorkout", false);
                                    addWorkoutIntent.putExtra("workoutID", workoutID);
                                    addWorkoutIntent.putExtra("highestOrderNum", exercises.length());
                                    startActivityForResult(addWorkoutIntent, 2);
                            addWrk.setOnClickListener(view -> {
                                Intent addWorkoutIntent = new Intent(view.getContext(), AddWorkoutActivity.class);
                                addWorkoutIntent.putExtra("addWorkout", false);
                                addWorkoutIntent.putExtra("workoutID", workoutID);
                                addWorkoutIntent.putExtra("highestOrderNum", exercises.length());
                                startActivityForResult(addWorkoutIntent, 2);

                                }
                            });

                            String finalIntensity = intensity;
                            editWrk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent editWorkoutIntent = new Intent(view.getContext(), EditWorkoutActivity.class);
                                    editWorkoutIntent.putExtra("exercises", exercises.toString());
                                    editWorkoutIntent.putExtra("workoutName", workoutName);
                                    editWorkoutIntent.putExtra("intensity", finalIntensity);
                                    startActivityForResult(editWorkoutIntent, 3);
                                }
                            });
                            editWrk.setOnClickListener(view -> {
                                Intent editWorkoutIntent = new Intent(view.getContext(), EditWorkoutActivity.class);
                                editWorkoutIntent.putExtra("exercises", exercises.toString());
                                editWorkoutIntent.putExtra("workoutName", workoutName);
                                editWorkoutIntent.putExtra("intensity", finalIntensity);
                                startActivityForResult(editWorkoutIntent, 3);
                            });*/

                            //newRow.addView(startWrk);
                            newRow.addView(newWrk);
                            newRow.addView(newWrkInt);
                            newRow.addView(menuButton);
                            //newRow.addView(scheduleWrk);
                            //newRow.addView(addWrk);
                            //newRow.addView(editWrk);
                            //newRow.addView(deleteWrk);
                            workoutList.addView(newRow);
                        }

                        svd.removeView(progressBar);
                        svd.removeView(loadingText);

                    } catch (Exception e) {
                        Log.d("ERROR2", e.toString());
                    }
                },
                error -> {
                    Log.d("ERROR3", error.toString());
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}

