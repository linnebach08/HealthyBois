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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements SensorEventListener,
        DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

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

        //Bundle extras = getIntent().getExtras();
        //String message;
        //try {
        //    message = extras.getString("message");
        //} catch (Exception e) {
        //    message = "Nope";
        //}

        //mBtn.setText(message);

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.BLUETOOTH }, 102);
        }
        else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
                        break;
                    }
                    Log.d("BT", bt.getName());
                }

            }
        }

        if (mBluetoothAdapter == null && MainActivity.this != null) {
            Toast.makeText(MainActivity.this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        }

        mBluetoothConnection = new BluetoothConnectionService(this);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startConnection();
                //if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BODY_SENSORS)
                //        != PackageManager.PERMISSION_GRANTED) {
                //     ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.BODY_SENSORS }, SENSOR_PERMISSION_CODE);
                // }
                //else {
                //    mBtn.setText("Clicked");
                //    startMeasure();
                //}
            }
        });


        //Wearable.getDataClient(this.getContext()).addListener(this);
    }

    public void startConnection() {
        startBTConnection(phone, MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");

        mBluetoothConnection.startClient(device, uuid);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this).addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d("DATA", "onDataChanged(): " + dataEventBuffer);

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (MessageService.COUNT_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    // Loads image on background thread.

                } else {
                    Log.d("BAD", "Unrecognized path: " + path);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("DELETED", "deleted");

            } else {
                Log.d(
                        "Unknown data event type", "Type = " + event.getType());
            }
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d("MessageReceived", "OnReceived: " + messageEvent);
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mBtn.setText(intent.getStringExtra("message"));
            //mBtn.setText("I just received a message from the handheld");
        }
    }

    class SendMessage extends Thread {
        String path;
        String message;

        SendMessage(String p, String m) {
            path = p;
            message = m;
        }

        public void run() {
            Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    //mBtn.setText(node.getDisplayName());
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        int result = Tasks.await(sendMessageTask);
                        mBtn.setText("sent to " + node.getDisplayName());
                    } catch (ExecutionException | InterruptedException e) {
                        Log.d("ERROR", e.toString());
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                mBtn.setText(e.toString());
                Log.d("ERROR", e.toString());

            }
        }
    }

    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float mHeartRateFloat = sensorEvent.values[0];

        int mHeartRate = Math.round(mHeartRateFloat);

        //Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage()

        mBtn.setText(Integer.toString(mHeartRate));
        stopMeasure();
    }

    /*private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
            }

        } catch (ExecutionException exception) {
            Log.e(TAG, "Task failed: " + exception);

        } catch (InterruptedException exception) {
            Log.e(TAG, "Interrupt occurred: " + exception);
        }

        return results;
    }*/

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
                startMeasure();
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
                Toast.makeText(MainActivity.this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

        else if (requestCode == 102) {
            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
                            break;
                        }
                        Log.d("BT", bt.getName());
                    }

                }
            }
            else {
                Toast.makeText(MainActivity.this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {

    }
}