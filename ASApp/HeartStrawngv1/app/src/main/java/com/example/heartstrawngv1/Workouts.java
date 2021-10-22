package com.example.heartstrawngv1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


public class Workouts extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button addWorkoutView;
    private Button showWorkoutView;
    private String name;
    private ArrayList<Exercise> exercises;
    private LinearLayout svd;
    private View returnedView;

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
        addWorkoutView = (Button) view.findViewById(R.id.add_workout_button);
        showWorkoutView = (Button) view.findViewById(R.id.show_workout_button);
        svd = view.findViewById(R.id.saved);
        returnedView = view;

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(returnedView.getContext());
        String url = "https://heartstrawng.azurewebsites.net/workouts/1";

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
                            workoutsTable.setGravity(Gravity.CENTER);
                            workoutsTable.setId(R.id.saved_workouts_table);

                            TableRow heading = new TableRow(returnedView.getContext());
                            heading.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 30, 1));

                            TextView wName = new TextView(returnedView.getContext());
                            wName.setLayoutParams(new TableRow.LayoutParams(0));
                            wName.setTextSize(18);
                            wName.setTypeface(null, Typeface.BOLD);
                            wName.setText("Name");

                            TextView iName = new TextView(returnedView.getContext());
                            iName.setLayoutParams(new TableRow.LayoutParams(1));
                            iName.setTextSize(18);
                            iName.setTypeface(null, Typeface.BOLD);
                            iName.setText("Intensity");

                            heading.addView(wName);
                            heading.addView(iName);

                            workoutsTable.addView(heading);

                            svd.addView(workoutsTable);
                        }
                        TableLayout workoutList = returnedView.findViewById(R.id.saved_workouts_table);
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject exercise = response.getJSONObject(i);

                            int workoutID = exercise.getInt("workoutID");
                            String workoutName = exercise.getString("workoutName");
                            String intensity = exercise.getString("intensity");
                            int duration = exercise.getInt("duration");
                            JSONArray exercises = exercise.getJSONArray("exercises");

                            TableRow newRow = new TableRow(returnedView.getContext());
                            newRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 30, 1));

                            TextView newWrk = new TextView(returnedView.getContext());
                            newWrk.setLayoutParams(new TableRow.LayoutParams(0));
                            newWrk.setTextSize(15);
                            newWrk.setClickable(true);
                            newWrk.setText(workoutName);

                            newWrk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    view.setBackgroundColor(Color.LTGRAY);

                                }
                            });

                            newRow.addView(newWrk);
                            workoutList.addView(newRow);

                        }
                    } catch (Exception e) {
                        Log.d("ERROR", e.toString());
                    }
                },
                error -> {
                    Log.d("ERROR", error.toString());
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        addWorkoutView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent addWorkoutIntent = new Intent(v.getContext(), AddWorkoutActivity.class);
                startActivityForResult(addWorkoutIntent, 1);
                /*URL serverURL = null;
                HttpsURLConnection conn = null;
                try {
                    serverURL = new URL("http", "www.google.com", "/post_workout/");
                    conn = (HttpsURLConnection)serverURL.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "heart-strawng-v0.1");

                    // Server responded OK, do whatever with datas
                    if (conn.getResponseCode() == 200) {
                        InputStream responseBody = conn.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                    }
                    else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }
        });

        showWorkoutView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                /*URL serverURL = null;
                HttpsURLConnection conn = null;
                try {
                    serverURL = new URL("http", "www.google.com", "/get_workout/");
                    conn = (HttpsURLConnection)serverURL.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "heart-strawng-v0.1");

                    // Server responded OK, do whatever with datas
                    if (conn.getResponseCode() == 200) {
                        InputStream responseBody = conn.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                    }
                    else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(returnedView.getContext());
                String url = "https://heartstrawng.azurewebsites.net/workouts/1";

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
                                    workoutsTable.setGravity(Gravity.CENTER);
                                    workoutsTable.setId(R.id.saved_workouts_table);

                                    TableRow heading = new TableRow(returnedView.getContext());
                                    heading.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 30, 1));

                                    TextView wName = new TextView(returnedView.getContext());
                                    wName.setLayoutParams(new TableRow.LayoutParams(0));
                                    wName.setPadding(10, 10, 10, 10);
                                    wName.setTextSize(25);
                                    wName.setTypeface(null, Typeface.BOLD);
                                    wName.setText("Name");

                                    TextView iName = new TextView(returnedView.getContext());
                                    iName.setLayoutParams(new TableRow.LayoutParams(1));
                                    iName.setPadding(10, 10, 10, 10);
                                    wName.setTypeface(null, Typeface.BOLD);
                                    wName.setTextSize(25);
                                    iName.setText("Intensity");

                                    heading.addView(wName);
                                    heading.addView(iName);

                                    workoutsTable.addView(heading);

                                    svd.addView(workoutsTable);
                                }
                                TableLayout workoutList = returnedView.findViewById(R.id.saved_workouts_table);
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject exercise = response.getJSONObject(i);

                                    int workoutID = exercise.getInt("workoutID");
                                    String workoutName = exercise.getString("workoutName");
                                    String intensity = exercise.getString("intensity");
                                    int duration = exercise.getInt("duration");
                                    JSONArray exercises = exercise.getJSONArray("exercises");

                                    TableRow newRow = new TableRow(returnedView.getContext());
                                    newRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 30, 1));

                                    TextView newWrk = new TextView(returnedView.getContext());
                                    newWrk.setLayoutParams(new TableRow.LayoutParams(0));
                                    newWrk.setPadding(10, 10, 10, 10);
                                    newWrk.setTextSize(20);
                                    newWrk.setClickable(true);
                                    newWrk.setText(workoutName);

                                    newWrk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            view.setBackgroundColor(Color.LTGRAY);

                                        }
                                    });

                                    newRow.addView(newWrk);
                                    workoutList.addView(newRow);

                                }

                                svd.addView(workoutList);
                            } catch (Exception e) {
                                Log.d("ERROR", e.toString());
                            }
                        },
                        error -> {
                            Log.d("ERROR", error.toString());
                        });

                stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        }
    }



    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
