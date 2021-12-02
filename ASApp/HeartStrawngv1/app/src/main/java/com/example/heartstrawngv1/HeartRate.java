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

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
    com.anychart.data.Set set;
    boolean clicked = false;
    boolean firstLoad = true;
    int userID;

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
        if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 101);
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

        if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.BLUETOOTH_SCAN }, 102);
        }
        else {
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
            Thread.sleep(4000);
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
        set = com.anychart.data.Set.instantiate();


        newL = new LooperThread();
        newL.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mBluetoothConnection = new BluetoothConnectionService(view.getContext(), newL.mHandler);

        measureHeartrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p = ProgressDialog.show(context, "Getting Heart Rate", "Please wait...", true);
                if (clicked) {
                    byte[] bytes = "Get Heartrate".getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                }
                else {
                    startConnection();
                    clicked = true;
                }
            }
        });

        //SharedPreferences sharedPref = context.getSharedPreferences("SHARED_PREFS", 0);
        //if (sharedPref.contains("HeartrateVals")) {
            showGraph();
        //}

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
                    int[] heartrates = new int[response.length()];
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            int heartRate = o.getInt("heartRate");
                            String date = o.getString("readingTime");

                            String[] timeSplit = date.split("T")[1].split(":");
                            String timeToAdd = timeSplit[0] + ":" + timeSplit[1];

                            heartrates[i] = heartRate;
                            times[i] = timeToAdd;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Cartesian cartesian = AnyChart.line();
                    cartesian.animation(true);
                    cartesian.padding(10d, 20d, 5d, 20d);
                    cartesian.crosshair().enabled(true);
                    cartesian.crosshair().yLabel(true).yStroke((Stroke) null, null, null, (String) null, (String) null);
                    cartesian.title("Heart Rate History");
                    cartesian.yAxis(0).title("Heart Rate");
                    cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

                    List<DataEntry> lineVals = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {

                        ValueDataEntry d = new ValueDataEntry(times[i], heartrates[i]);
                        lineVals.add(d);
                    }

                    set.data(lineVals);
                    Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

                    Line series1 = cartesian.line(series1Mapping);
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

                    if (firstLoad) {
                        graph.setChart(cartesian);
                        firstLoad = false;
                    }
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
        try {
            heartRateLabel.setText("Heart Rate: " + newHR + " BPM");
        } catch(Exception e) {

        }

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
