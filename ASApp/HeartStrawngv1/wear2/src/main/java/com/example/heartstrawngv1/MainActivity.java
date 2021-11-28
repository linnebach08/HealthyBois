package com.example.heartstrawngv1;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.wear.ambient.AmbientModeSupport;

import com.example.heartstrawngv1.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements SensorEventListener{

    private Button mBtn;
    private ActivityMainBinding binding;
    private SensorManager mSensorManager;
    private Sensor mHeartSensor;

    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private String bluetoothAddress;
    private BluetoothDevice phone;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    BluetoothConnectionService mBluetoothConnection;
    private static final String TAG = "Watch";
    LooperThread newL;

    class LooperThread extends Thread {
        public Handler mHandler;

        public void run() {
            Log.d(TAG, "LooperThread: Start");
            Looper.prepare();

            Log.d(TAG, "LooperThread: Handler creation");
            mHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(@NonNull Message message) {
                    //Bundle stuff = message.getData();
                    //String requested = stuff.getString("HRRequested");
                    if (message.what == 1) {
                        Log.d(TAG, "In handler");
                        startMeasure();
                    }

                }
            };
            Log.d(TAG, "LooperThread: Looping");
            Looper.loop();
        }
    }


    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    private static final int SENSOR_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mBtn = binding.button;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.setTurnScreenOn(true);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

             ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.BODY_SENSORS }, SENSOR_PERMISSION_CODE);
        }
        else {
            mBtn.setText("Clicked");
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean created = false;

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d("BLUE", "daboodee");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.BLUETOOTH }, 102);
        }
        else {
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bt : pairedDevices) {
                Log.d("BT", bt.getName());
                if (bt.getName().equals("Pixel 4 XL")) {
                    bluetoothAddress = bt.getAddress();
                    phone = bt;
                    newL = new LooperThread();
                    newL.start();
                    Log.d(TAG, "Handler " + newL.mHandler);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBluetoothConnection = new BluetoothConnectionService(MainActivity.this, newL.mHandler);
                    created = true;
                }
            }
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 101);
        }
        else {
            if (!created) {
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    Log.d("BT", bt.getName());

                    if (bt.getName().equals("Pixel 4 XL")) {
                        bluetoothAddress = bt.getAddress();
                        phone = bt;
                        newL = new LooperThread();
                        newL.start();
                        Log.d(TAG, "Handler " + newL.mHandler);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this, newL.mHandler);
                        created = true;
                    }
                    Log.d("BT", bt.getName());
                }
            }

        }

        //LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();

            }
        });

        if (mBluetoothAdapter == null && MainActivity.this != null) {
            Toast.makeText(MainActivity.this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        }


        //Wearable.getDataClient(this.getContext()).addListener(this);
    }

    public void startConnection() {
        startBTConnection(phone, MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");

        mBluetoothConnection.startClient(device, uuid);

        //startMeasure();
    }

    public void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float mHeartRateFloat = sensorEvent.values[0];

        int mHeartRate = Math.round(mHeartRateFloat);

        //Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage()

        Log.d(TAG, "HR is " + Integer.toString(mHeartRate));
        mBtn.setText(Integer.toString(mHeartRate));
        byte[] bytes = Integer.toString(mHeartRate).getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
        newL.interrupt();
/*
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            Log.d("BT", bt.getName());
            if (bt.getName().equals("Pixel 4 XL")) {
                bluetoothAddress = bt.getAddress();
                phone = bt;
                LooperThread newL = new LooperThread();
                newL.start();
                Log.d(TAG, "Handler " + newL.mHandler);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this, newL.mHandler);
            }
        }
*/
        stopMeasure();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
    }

    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SENSOR_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mBtn.setText("Clicked");
            }
            else {
                Toast.makeText(MainActivity.this, "Sensor Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

        else if (requestCode == 101) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    Log.d("BT", bt.getName());
                }
            }
            else {
                Toast.makeText(MainActivity.this, "Bluetooth Permission Denied1", Toast.LENGTH_SHORT).show();
            }
        }

        else if (requestCode == 102) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 101);
                }
                else {
                    pairedDevices = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice bt : pairedDevices) {
                        if (bt.getBondState() == BluetoothDevice.BOND_BONDED) {
                            bluetoothAddress = bt.getAddress();
                            phone = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
                            newL = new LooperThread();
                            newL.start();
                            Log.d(TAG, "Handler " + newL.mHandler);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mBluetoothConnection = new BluetoothConnectionService(this, newL.mHandler);
                            break;
                        }
                        Log.d("BT", bt.getName());
                    }

                }
            }
            else {
                Toast.makeText(MainActivity.this, "Bluetooth Permission Denied2", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            //Bundle stuff = message.getData();
            //String requested = stuff.getString("HRRequested");
            Log.d(TAG, "In handler");
            startMeasure();
            return false;
        }
    });*/
}