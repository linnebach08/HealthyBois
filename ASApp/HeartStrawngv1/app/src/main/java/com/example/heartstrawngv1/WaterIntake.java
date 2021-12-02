package com.example.heartstrawngv1;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class WaterIntake extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    TextView waterIntakeLabel;
    Context context;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private String bluetoothAddress;
    private BluetoothDevice watch;
    BluetoothConnectionService mBluetoothConnection;
    private static final String TAG = "WaterIntakeFrag";
    Button measureWaterIntake;
    AnyChartView graph;
    ProgressDialog p;
    LooperThread newL;
    com.anychart.data.Set set;
    boolean clicked = false;
    boolean firstLoad = true;

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

                        updateWI(message.obj.toString());
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
    private String mParam3;

    //private OnFragmentInteractionListener mListener;

    public WaterIntake() {
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
    public static WaterIntake newInstance(String param1, String param2, String param3) {
        WaterIntake fragment = new WaterIntake();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        FragmentActivity activity = getActivity();
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

        if (mBluetoothAdapter == null && activity != null) {
            Toast.makeText(activity, "Bluetooth not available", Toast.LENGTH_LONG).show();
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

        byte[] bytes = "Get Water Intake".getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_water_intake, container, false);
        context = getContext();
        measureWaterIntake = view.findViewById(R.id.measure_waterintake_btn);
        waterIntakeLabel = view.findViewById(R.id.waterintake_label);
        graph = view.findViewById(R.id.waterintake_graph_view);
        set = com.anychart.data.Set.instantiate();


        newL = new LooperThread();
        newL.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mBluetoothConnection = new BluetoothConnectionService(view.getContext(), newL.mHandler);

        measureWaterIntake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p = ProgressDialog.show(context, "Getting Heart Rate", "Please wait...", true);
                if (clicked) {
                    byte[] bytes = "Get Water Intake".getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                }
                else {
                    startConnection();
                    clicked = true;
                }
            }
        });

        SharedPreferences sharedPref = context.getSharedPreferences("SHARED_PREFS", 0);
        if (sharedPref.contains("WaterIntakeVals")) {
            showGraph();
        }

        return view;
    }

    public void showGraph() {
        SharedPreferences sharedPref = context.getSharedPreferences("SHARED_PREFS", 0);
        String waterIntakeVals = sharedPref.getString("WaterIntakeVals", "");
        String[] vals = waterIntakeVals.split(", ");
        String[] times = new String[vals.length];
        String[] waterIntakes = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            String[] temp = vals[i].split(" : ");
            times[i] = temp[0].split(" ")[1];
            waterIntakes[i] = temp[1];
            Log.d(TAG, "ShowGraph: Time: " + times[i]);
            Log.d(TAG, "ShowGraph: WI: " + waterIntakes[i]);
        }
        //graph.setProgressBar();

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);

        cartesian.crosshair().yLabel(true).yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.title("Water Intake History");

        cartesian.yAxis(0).title("Water Drank (fl. oz)");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        List<DataEntry> lineVals = new ArrayList<>();
        for (int i = 0; i < vals.length; i++) {

            ValueDataEntry d = new ValueDataEntry(times[i], Double.parseDouble(waterIntakes[i]));
            lineVals.add(d);
        }

        set.data(lineVals);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("Water Drank");
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

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateWI(String newWI) {
        p.dismiss();
        try {
            waterIntakeLabel.setText("Water Intake: " + newWI + " fl oz");
        } catch(Exception e) {

        }
        try {
            SharedPreferences sharedPref = context.getSharedPreferences("SHARED_PREFS", 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            if (sharedPref.contains("WaterIntakeVals")) {
                String currWaterIntakes = sharedPref.getString("WaterIntakeVals", "");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                LocalDateTime now = LocalDateTime.now();
                String formattedDate = dtf.format(now);

                String toAdd = currWaterIntakes + ", " + formattedDate + " : " + newWI;

                editor.remove("WaterIntakeVals");
                editor.putString("WaterIntakeVals", toAdd);
            }
            else {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                LocalDateTime now = LocalDateTime.now();
                String formattedDate = dtf.format(now);
                String toAdd = formattedDate + " : " + newWI;
                editor.putString("WaterIntakeVals", toAdd);
            }
            editor.apply();
            Log.d(TAG, "WaterIntake: Prefs: " + sharedPref.getString("WaterIntake", "J"));

            graph.post(new Runnable() {
                @Override
                public void run() {
                    showGraph();
                }
            });

        } catch (NullPointerException e) {
            Log.d(TAG, "UpdateWI: " + e.getMessage());
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
