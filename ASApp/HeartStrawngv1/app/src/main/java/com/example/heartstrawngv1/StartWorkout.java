package com.example.heartstrawngv1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class StartWorkout extends AppCompatActivity {

    BluetoothDevice watch;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final String TAG = "StartWorkout";
    BluetoothConnectionService mBluetoothConnection;
    StartWorkout.LooperThread newL;
    Context context;
    HashMap<String, Integer> tempHRs;
    int userID;
    boolean finished = false;

    class LooperThread extends Thread {
        public Handler mHandler;

        public void run() {
            Log.d(TAG, "LooperThread: Start");
            Looper.prepare();

            Log.d(TAG, "LooperThread: Handler creation");
            mHandler = new Handler(Looper.myLooper()) {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void handleMessage(@NonNull Message message) {
                    if (message.what == 1) {
                        Log.d(TAG, "In handler");
                        //measureHeartrate.post(new Runnable() {

                        //   @Override
                        //   public void run() {
                        updateHR(message.obj.toString());

                        //    }
                        //});
                    }

                }
            };
            Log.d(TAG, "LooperThread: Looping");
            Looper.loop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_workout);
        context = this;

        FloatingActionButton fab = findViewById(R.id.start_workout_back_btn);
        fab.setOnClickListener(view -> finish());

        ConstraintLayout layout = findViewById(R.id.start_workout_layout);
        TableLayout table = findViewById(R.id.start_workout_table);
        Button doneBtn = findViewById(R.id.start_workout_done_btn);
        tempHRs = new HashMap<>();

        Bundle extras = getIntent().getExtras();

        ArrayList<String> exerciseNames;
        String workoutName;
        try {
            exerciseNames = extras.getStringArrayList("exercises");
            workoutName = extras.getString("workoutName");
            userID = extras.getInt("userID");
        } catch (Exception e) {
            exerciseNames = new ArrayList<>();
            workoutName = "";
            userID = -1;
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices;
        boolean created = false;
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.BLUETOOTH_CONNECT }, 101);
        }
        else {
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bt : pairedDevices) {
                if (bt.getName().equals("Vapor 2 0846")) {
                    watch = bt;
                    created = true;
                }
                Log.d("BT", bt.getName());
            }

        }

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BLUETOOTH_SCAN }, 102);
        }
        else {
            if (!created) {
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("Vapor 2 0846")) {
                        watch = bt;
                        created = true;
                    }
                    Log.d("BT", bt.getName());
                }
            }

        }

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Smartwatch detected");

        LayoutInflater popupInflater = getLayoutInflater();
        View dialogLayout = popupInflater.inflate(R.layout.start_workout_smartwatch_detected, null);
        builder.setView(dialogLayout);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();*/

        newL = new StartWorkout.LooperThread();
        newL.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mBluetoothConnection = new BluetoothConnectionService(this, newL.mHandler ,false);

        AtomicInteger numChecked = new AtomicInteger();

        Context thisContext = this;
        ArrayList<String> finalExerciseNames = exerciseNames;
        String finalWorkoutName = workoutName;
        ArrayList<String> finalExerciseNames1 = exerciseNames;
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!finished) {
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
                    try {
                        RequestQueue queue = Volley.newRequestQueue(context);
                        String postUrl = "https://heartstrawng.azurewebsites.net/heart-rate/readings/" + userID;
                        JSONArray toSend = new JSONArray();

                        for (int i = 0; i < tempHRs.size(); i++) {
                            JSONObject o = new JSONObject();
                            try {
                                o.put("heartRate", tempHRs.get(tempHRs.keySet().toArray()[i]));
                                o.put("readingTime", tempHRs.keySet().toArray()[i]);
                                toSend.put(o);

                            } catch (JSONException e) {
                                Log.d("JSONERR", e.toString());
                            }
                        }
                        Log.d("BODY", "Body is " + toSend);
                        // Request a string response from the provided URL.
                        JsonArrayRequest postHeartRateRequest = new JsonArrayRequest(Request.Method.POST, postUrl,
                                toSend,
                                response -> {

                                    finish();

                                }, error -> {
                            newL.interrupt();
                            finish();
                        });

                        postHeartRateRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        // Add the request to the RequestQueue.
                        queue.add(postHeartRateRequest);
                    }
                    catch (Exception e) {
                        Log.d("ERRORR", e.toString());
                    }
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
                        finished = true;
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

        startConnection();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateHR(String newHR) {
        try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    LocalDateTime now = LocalDateTime.now();
                    String formattedDate = dtf.format(now);

                    tempHRs.put(formattedDate, Integer.parseInt(newHR));
            if (!finished) {
                    byte[] bytes = "Get Heartrate".getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                }


        } catch (NullPointerException e) {
            Log.d(TAG, "UpdateHR: " + e.getMessage());
        }

    }

    public void startConnection() {
        startBTConnection(watch, MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");

        mBluetoothConnection.startClient(device, uuid);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            byte[] bytes = "Get Heartrate".getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(bytes);
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

    }
}