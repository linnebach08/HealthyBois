package com.example.heartstrawngv1;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.graphics.vector.Stroke;
import com.anychart.scales.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class HeartRate extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    TextView heartRateLabel;
    Spinner datesSpinner;
    Spinner detailedDatesSpinner;
    Context context;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private String bluetoothAddress;
    private BluetoothDevice watch;
    BluetoothConnectionService mBluetoothConnection;
    private static final String TAG = "HeartRateFrag";
    Button measureHeartrate;
    HeartRate.LooperThread newL;
    AnyChartView graph;
    ProgressDialog p;
    boolean clicked = false;
    boolean firstLoad = true;
    int userID;
    Cartesian cartesian;
    String currentChoice = "";
    String currentMonthChoice = "";

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


    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;

    public HeartRate() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HeartRate.
     */
    // TODO: Rename and change types and number of parameters
    public static HeartRate newInstance(String param1, String param2) {
        HeartRate fragment = new HeartRate();
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        FragmentActivity activity = getActivity();

        Bundle extras = this.getArguments();
        if (extras != null) {
            userID = extras.getInt("userID");
        }
        else {
            userID = -1;
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "Bluetooth not available", Toast.LENGTH_LONG).show();
        }


            boolean created = false;
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 101);
            } else {
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("Vapor 2 0846")) {
                        watch = bt;
                        created = true;
                    }
                    Log.d("BT", bt.getName());
                }

            }

            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 102);
            } else {
                if (!created) {
                    pairedDevices = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice bt : pairedDevices) {
                        if (bt.getName().equals("Vapor 2 0846")) {
                            watch = bt;
                        }
                        Log.d("BT", bt.getName());
                    }
                }

            }


        //Wearable.getDataClient(this.getContext()).addListener(this);
    }

    public void startConnection() {
        startBTConnection(watch, MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");

        mBluetoothConnection.startClient(device, uuid);

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] bytes = "Get Heartrate".getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_heart_rate, container, false);
        context = getContext();
        measureHeartrate = view.findViewById(R.id.measure_heartrate_btn);
        heartRateLabel = view.findViewById(R.id.heartrate_label);
        graph = view.findViewById(R.id.heartrate_graph_view);
        datesSpinner = view.findViewById(R.id.heartrate_dates_spinner);
        detailedDatesSpinner = view.findViewById(R.id.heartrate_detailed_dates_spinner);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not available", Toast.LENGTH_LONG).show();
        }
            newL = new LooperThread();
            newL.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mBluetoothConnection = new BluetoothConnectionService(view.getContext(), newL.mHandler, false);

            measureHeartrate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    p = ProgressDialog.show(context, "Getting Heart Rate", "Please wait...", true);
                    if (clicked) {
                        byte[] bytes = "Get Heartrate".getBytes(Charset.defaultCharset());
                        mBluetoothConnection.write(bytes);
                    } else {
                        startConnection();
                        clicked = true;
                    }
                }
            });


        showGraph();

        return view;
    }

    public void showGraph() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String postUrl = "https://heartstrawng.azurewebsites.net/heart-rate/readings/" + userID;

        // Request a string response from the provided URL.
        JsonArrayRequest getHeartRateRequest = new JsonArrayRequest(Request.Method.GET, postUrl,
                null,
                response -> {
                    String[] times = new String[response.length()];
                    ArrayList<String> months = new ArrayList<>();
                    ArrayList<String> days = new ArrayList<>();
                    int[] heartrates = new int[response.length()];
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            int heartRate = o.getInt("heartRate");
                            String date = o.getString("readingTime");

                            String[] fullSplit = date.split("T");
                            String[] dateSplit = fullSplit[0].split("-");

                            String convMonth = convertMonth(dateSplit[1]);

                            months.add(dateSplit[1]);

                            if (dateSplit[2].charAt(0) == '0') {
                                dateSplit[2] = String.valueOf(dateSplit[2].charAt(1));
                            }
                            
                            days.add(convMonth + " " + dateSplit[2]);

                            String[] timeSplit = fullSplit[1].split(":");
                            String timeToAdd = timeSplit[0] + ":" + timeSplit[1];

                            String convTime = convertTime(timeToAdd);

                            heartrates[i] = heartRate;
                            times[i] = convTime;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ArrayList<String> choices = new ArrayList<>();
                    if (days.size() != 0) {
                        choices.add("Daily");
                    }
                    if (months.size() != 0) {
                        choices.add("Monthly");
                    }

                    ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, choices);
                    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    datesSpinner.setAdapter(dateAdapter);
                    datesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            String choice = adapterView.getItemAtPosition(i).toString();

                            ArrayList<Integer> chosenHeartrates = new ArrayList<>();
                            ArrayList<String> chosenTimes = new ArrayList<>();

                            if (choice.equals("Monthly")) {
                                // For monthly, we want to graph all the readings for that month with the dates as the x-axis

                                if (months.size() > 1) {
                                    ArrayList<String> distinctMonths = new ArrayList<>();
                                    String prev = "";
                                    for(int m = 0; m < months.size(); m++) {
                                        if (m == 0) {
                                            prev = months.get(m);
                                            distinctMonths.add(prev);
                                        }
                                        else {
                                            if (prev.equals(months.get(m))) {
                                                continue;
                                            }
                                            else {
                                                distinctMonths.add(months.get(m));
                                                prev = months.get(m);
                                            }
                                        }
                                    }
                                    ArrayList<String> distinctConvMonths = new ArrayList<>();

                                    for (String m : distinctMonths) {
                                        distinctConvMonths.add(convertMonth(m));
                                    }
                                    detailedDatesSpinner.setVisibility(View.VISIBLE);
                                    ArrayAdapter<String> detailedDateAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, distinctConvMonths);
                                    detailedDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    detailedDatesSpinner.setAdapter(detailedDateAdapter);
                                    if (!currentMonthChoice.equals("")) {
                                        detailedDatesSpinner.setSelection(distinctConvMonths.indexOf(currentMonthChoice));
                                    }
                                    detailedDatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            String choice = adapterView.getItemAtPosition(i).toString();
                                            currentChoice = choice;
                                            //Log.d(TAG, "Date selected: " + choice);
                                            //Log.d(TAG, "firstLoad: " + firstLoad);
                                            chosenHeartrates.clear();
                                            chosenTimes.clear();

                                            for (int k = 0; k < months.size(); k++) {
                                                if (months.get(k).equals(convertMonthBack(choice))) {
                                                    Log.d(TAG, "Days found: " + days.get(k));
                                                    Log.d(TAG, "HR found: " + heartrates[k]);
                                                    chosenHeartrates.add(heartrates[k]);
                                                    chosenTimes.add(days.get(k) + " " + times[k]);
                                                }
                                            }

                                            APIlib.getInstance().setActiveAnyChartView(graph);

                                            if (firstLoad) {
                                                cartesian = AnyChart.line();
                                                cartesian.animation(true);
                                                cartesian.padding(10d, 20d, 5d, 20d);
                                                cartesian.crosshair().enabled(true);
                                                cartesian.crosshair().yLabel(true).yStroke((Stroke) null, null, null, (String) null, (String) null);
                                                cartesian.yAxis(0).title("Heart Rate");
                                                cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);
                                            }
                                            else {
                                                cartesian.removeAllSeries();
                                            }
                                            cartesian.title("Heart Rate History (Monthly) " + choice);

                                            List<DataEntry> lineVals = new ArrayList<>();
                                            for (int j = 0; j < chosenTimes.size(); j++) {
                                                ValueDataEntry d = new ValueDataEntry(chosenTimes.get(j), chosenHeartrates.get(j));
                                                lineVals.add(d);
                                                //Log.d(TAG, "ChosenTime: " + chosenTimes.get(j));
                                                //Log.d(TAG, "ChosenHR: " + chosenHeartrates.get(j));
                                                //Log.d(TAG, "Linevals: " + lineVals.get(j).getValue("x"));
                                            }

                                            //set.data(lineVals);
                                            //Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

                                            Line series1 = cartesian.line(lineVals);
                                            series1.name("Heart Rate");
                                            series1.hovered().markers().enabled(true);
                                            series1.hovered().markers()
                                                    .type(MarkerType.CIRCLE)
                                                    .size(4d);
                                            series1.tooltip()
                                                    .position("right")
                                                    .anchor(Anchor.LEFT_CENTER)
                                                    .offsetX(5d)
                                                    .offsetY(5d);

                                            cartesian.xAxis(0).labels().rotation(-45);

                                            if (firstLoad) {
                                                graph.setChart(cartesian);
                                                firstLoad = false;
                                            }

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });
                                }
                                else {
                                    detailedDatesSpinner.setVisibility(View.INVISIBLE);
                                    //Cartesian cartesian = AnyChart.line();
                                    cartesian.animation(true);
                                    cartesian.padding(10d, 20d, 5d, 20d);
                                    cartesian.crosshair().enabled(true);
                                    cartesian.crosshair().yLabel(true).yStroke((Stroke) null, null, null, (String) null, (String) null);
                                    cartesian.title("Heart Rate History (Monthly)");
                                    cartesian.yAxis(0).title("Heart Rate");
                                    cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);
                                    cartesian.xAxis(0).labels().rotation(-45);

                                    List<DataEntry> lineVals = new ArrayList<>();
                                    for (int j = 0; j < response.length(); j++) {
                                        ValueDataEntry d = new ValueDataEntry(times[j], heartrates[j]);
                                        lineVals.add(d);
                                    }

                                    //set.data(lineVals);
                                    //Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

                                    Line series1 = cartesian.line(lineVals);
                                    series1.name("Heart Rate");
                                    series1.hovered().markers().enabled(true);
                                    series1.hovered().markers()
                                            .type(MarkerType.CIRCLE)
                                            .size(4d);
                                    series1.tooltip()
                                            .position("right")
                                            .anchor(Anchor.LEFT_CENTER)
                                            .offsetX(5d)
                                            .offsetY(5d);

                                    cartesian.legend().enabled(false);
                                    cartesian.legend().fontSize(13d);
                                    cartesian.legend().padding(0d, 0d, 10d, 0d);

                                    //if (firstLoad) {
                                    //   graph.setChart(cartesian);
                                    //   firstLoad = false;
                                    //}
                                }
                            }
                            else if (choice.equals("Daily")) {
                                // For daily, we want to graph all the readings for that day with the times they were read as the x-axis

                                if (days.size() > 1) {
                                    ArrayList<String> distinctDays = new ArrayList<>();
                                    String prev = "";
                                    for(int m = 0; m < days.size(); m++) {
                                        if (m == 0) {
                                            prev = days.get(m);
                                            distinctDays.add(prev);
                                        }
                                        else {
                                            if (prev.equals(days.get(m))) {
                                                continue;
                                            }
                                            else {
                                                distinctDays.add(days.get(m));
                                                prev = days.get(m);
                                            }
                                        }
                                    }
                                    detailedDatesSpinner.setVisibility(View.VISIBLE);
                                    ArrayAdapter<String> detailedDateAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, distinctDays);
                                    detailedDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    detailedDatesSpinner.setAdapter(detailedDateAdapter);
                                    if (!currentChoice.equals("")) {
                                        detailedDatesSpinner.setSelection(distinctDays.indexOf(currentChoice));
                                    }
                                    detailedDatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            String choice = adapterView.getItemAtPosition(i).toString();
                                            currentChoice = choice;
                                            //Log.d(TAG, "Date selected: " + choice);
                                            //Log.d(TAG, "firstLoad: " + firstLoad);
                                            chosenHeartrates.clear();
                                            chosenTimes.clear();

                                            for (int k = 0; k < days.size(); k++) {
                                                if (days.get(k).equals(choice)) {
                                                    //Log.d(TAG, "Days found: " + days.get(k));
                                                    chosenHeartrates.add(heartrates[k]);
                                                    chosenTimes.add(times[k]);
                                                }
                                            }

                                            APIlib.getInstance().setActiveAnyChartView(graph);

                                            if (firstLoad) {
                                                cartesian = AnyChart.line();
                                                cartesian.animation(true);
                                                cartesian.padding(10d, 20d, 5d, 20d);
                                                cartesian.crosshair().enabled(true);
                                                cartesian.crosshair().yLabel(true).yStroke((Stroke) null, null, null, (String) null, (String) null);
                                                cartesian.yAxis(0).title("Heart Rate");
                                                cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);
                                            }
                                            else {
                                                cartesian.removeAllSeries();
                                            }
                                            cartesian.title("Heart Rate History (Daily) " + choice);

                                            List<DataEntry> lineVals = new ArrayList<>();
                                            for (int j = 0; j < chosenTimes.size(); j++) {
                                                ValueDataEntry d = new ValueDataEntry(chosenTimes.get(j), chosenHeartrates.get(j));
                                                lineVals.add(d);
                                                //Log.d(TAG, "ChosenTime: " + chosenTimes.get(j));
                                                //Log.d(TAG, "ChosenHR: " + chosenHeartrates.get(j));
                                                //Log.d(TAG, "Linevals: " + lineVals.get(j).getValue("x"));
                                            }

                                            //set.data(lineVals);
                                            //Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

                                            Line series1 = cartesian.line(lineVals);
                                            series1.name("Heart Rate");
                                            series1.hovered().markers().enabled(true);
                                            series1.hovered().markers()
                                                    .type(MarkerType.CIRCLE)
                                                    .size(4d);
                                            series1.tooltip()
                                                    .position("right")
                                                    .anchor(Anchor.LEFT_CENTER)
                                                    .offsetX(5d)
                                                    .offsetY(5d);

                                            cartesian.xAxis(0).labels().rotation(-45);



                                            if (firstLoad) {
                                                graph.setChart(cartesian);
                                                firstLoad = false;
                                            }

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });
                                }
                                else {
                                    //Cartesian cartesian = AnyChart.line();
                                    detailedDatesSpinner.setVisibility(View.INVISIBLE);
                                    cartesian.animation(true);
                                    cartesian.padding(10d, 20d, 5d, 20d);
                                    cartesian.crosshair().enabled(true);
                                    cartesian.crosshair().yLabel(true).yStroke((Stroke) null, null, null, (String) null, (String) null);
                                    cartesian.title("Heart Rate History (Daily)");
                                    cartesian.yAxis(0).title("Heart Rate");
                                    cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

                                    List<DataEntry> lineVals = new ArrayList<>();
                                    for (int j = 0; j < response.length(); j++) {
                                        ValueDataEntry d = new ValueDataEntry(times[j], heartrates[j]);
                                        lineVals.add(d);
                                    }

                                    //set.data(lineVals);
                                    //Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

                                    Line series1 = cartesian.line(lineVals);
                                    series1.name("Heart Rate");
                                    series1.hovered().markers().enabled(true);
                                    series1.hovered().markers()
                                            .type(MarkerType.CIRCLE)
                                            .size(4d);
                                    series1.tooltip()
                                            .position("right")
                                            .anchor(Anchor.LEFT_CENTER)
                                            .offsetX(5d)
                                            .offsetY(5d);

                                    cartesian.legend().enabled(false);
                                    cartesian.legend().fontSize(13d);
                                    cartesian.legend().padding(0d, 0d, 10d, 0d);

                                    //if (firstLoad) {
                                     //   graph.setChart(cartesian);
                                     //   firstLoad = false;
                                    //}
                                }
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                }, error -> {
            Log.d("ERROR", error.toString());

        });

        getHeartRateRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(getHeartRateRequest);

        /*SharedPreferences sharedPref = context.getSharedPreferences("SHARED_PREFS", 0);
        String heartrateVals = sharedPref.getString("HeartrateVals", "");
        String[] vals = heartrateVals.split(", ");
        String[] times = new String[vals.length];
        String[] heartrates = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            String[] temp = vals[i].split(" : ");
            times[i] = temp[0].split(" ")[1];
            heartrates[i] = temp[1];
        }*/
        //graph.setProgressBar();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateHR(String newHR) {
        p.dismiss();
        heartRateLabel.post(new Runnable() {
            @Override
            public void run() {
                heartRateLabel.setText("Heart Rate: " + newHR + " BPM");
            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = formatter.format(now);

        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String postUrl = "https://heartstrawng.azurewebsites.net/heart-rate/readings/" + userID;
            JSONObject o = new JSONObject();
            try {
                o.put("heartRate", Integer.parseInt(newHR));
                o.put("readingTime", formattedDate);

            } catch (JSONException e) {
                Log.d("JSONERR", e.toString());
            }

            JSONArray toSend = new JSONArray();
            JSONArray temp = new JSONArray();
            temp.put("");
            toSend.put(o);
            //Log.d("BODY", "Body is " + o);
            JSONObject o2= toSend.toJSONObject(temp);

            Log.d("BODY", "Body is " + o2);
            // Request a string response from the provided URL.
            JsonArrayRequest postHeartRateRequest = new JsonArrayRequest(Request.Method.POST, postUrl,
                    toSend,
                    response -> {
                        graph.post(new Runnable() {
                            @Override
                            public void run() {
                                showGraph();
                            }
                        });

                    }, error -> {
                graph.post(new Runnable() {
                    @Override
                    public void run() {
                        showGraph();
                    }
                });

            });

            postHeartRateRequest.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // Add the request to the RequestQueue.
            queue.add(postHeartRateRequest);


        } catch (NullPointerException | JSONException e) {
            Log.d(TAG, "UpdateHR: " + e.getMessage());
        }

    }

    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    Log.d("BT", bt.getName());
                }
            }
            else {
                Toast.makeText(context, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 102) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("Vapor 2 0846")) {
                        bluetoothAddress = bt.getAddress();
                        if (bt.getBondState() == BluetoothDevice.BOND_BONDED) {
                            watch = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
                            break;
                        }

                    }
                    Log.d("BT", bt.getName());
                }
            }
            else {
                Toast.makeText(context, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertMonth(String month) {
        switch(month) {
            case "1":
                return "Jan.";
            case "2":
                return "Feb.";
            case "3":
                return "Mar.";
            case "4":
                return "Apr.";
            case "5":
                return "May.";
            case "6":
                return "Jun.";
            case "7":
                return "Jul.";
            case "8":
                return "Aug.";
            case "9":
                return "Sep.";
            case "10":
                return "Oct.";
            case "11":
                return "Nov.";
            case "12":
                return "Dec.";
            default:
                return "";
        }
    }

    private String convertMonthBack(String month) {
        switch(month) {
            case "Jan.":
                return "1";
            case "Feb.":
                return "2";
            case "Mar.":
                return "3";
            case "Apr.":
                return "4";
            case "May.":
                return "5";
            case "Jun.":
                return "6";
            case "Jul.":
                return "7";
            case "Aug.":
                return "8";
            case "Sep.":
                return "9";
            case "Oct.":
                return "10";
            case "Nov.":
                return "11";
            case "Dec.":
                return "12";
            default:
                return "";
        }
    }

    private String convertTime(String time) {
        String[] split = time.split(":");

        if (Integer.parseInt(split[0]) == 0) {
            return "12:" + split[1] + " AM";
        }

        if (Integer.parseInt(split[0]) >= 12) {
            String holder = "";
            switch(split[0]) {
                case "12":
                    holder = "12:" + split[1] + " PM";
                    break;
                case "13":
                    holder = "1:" + split[1] + " PM";
                    break;
                case "14":
                    holder = "2:" + split[1] + " PM";
                    break;
                case "15":
                    holder = "3:" + split[1] + " PM";
                    break;
                case "16":
                    holder = "4:" + split[1] + " PM";
                    break;
                case "17":
                    holder = "5:" + split[1] + " PM";
                    break;
                case "18":
                    holder = "6:" + split[1] + " PM";
                    break;
                case "19":
                    holder = "7:" + split[1] + " PM";
                    break;
                case "20":
                    holder = "8:" + split[1] + " PM";
                    break;
                case "21":
                    holder = "9:" + split[1] + " PM";
                    break;
                case "22":
                    holder = "10:" + split[1] + " PM";
                    break;
                case "23":
                    holder = "11:" + split[1] + " PM";
                    break;
            }
            return holder;
        }
        else {
            return time + " AM";
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    //@Override
    /*public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    //@Override
    /*public void onDetach() {
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
