package com.example.heartstrawngv1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class HeartRate extends Fragment implements DataClient.OnDataChangedListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    protected Handler myHandler;
    TextView heartRateLabel;
    Context context;
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 3;
    private Set<BluetoothDevice> pairedDevices;
    private String bluetoothAddress;
    private BluetoothDevice watch;
    BluetoothConnectionService mBluetoothConnection;
    private static final String TAG = "HeartRateFrag";
    Button measureHeartrate;


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
        if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 101);
        }
        else {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_heart_rate, container, false);
        context = getContext();
        measureHeartrate = view.findViewById(R.id.measure_heartrate_btn);
        heartRateLabel = view.findViewById(R.id.heartrate_label);

        mBluetoothConnection = new BluetoothConnectionService(view.getContext());

        measureHeartrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                Bundle stuff = message.getData();
                messageText(stuff.getString("messageText"));
                return false;
            }
        });

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(messageReceiver, messageFilter);

       // measureHeartrate.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        talkClick(view);
        //    }
        //});

        return view;
    }


    public void messageText(String newInfo) {
        if (newInfo.compareTo("") != 0) {
            String newHR = "Heart Rate: " + newInfo;
            heartRateLabel.setText(newHR);
        }
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();
        }
    }

    public void talkClick(View v) {
        Toast.makeText(v.getContext(), "Sending message...", Toast.LENGTH_LONG).show();

        new NewThread("/heartRateRequest", "Sending message...").start();
    }

    public void sendMessage(String messageText) {
        Bundle bundle = new Bundle();
        bundle.putString("messageText", messageText);
        Message msg = myHandler.obtainMessage();
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    class NewThread extends Thread {
        String path;
        String message;

        NewThread(String p, String m) {
            path = p;
            message = m;
        }

        public void run() {
            Task<List<Node>> wearableList = Wearable.getNodeClient(context).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {
                    Log.d("NODE", "It's " + node);
                        Task<Integer> sendMessageTask = Wearable.getMessageClient(context).sendMessage(node.getId(), path, message.getBytes());
                        try {
                            int result = Tasks.await(sendMessageTask);
                            Log.d("done", "Sent to " + node.getDisplayName());
                            sendMessage("I just sent the wearable a message");
                        } catch (InterruptedException | ExecutionException e) {
                            Log.d("ERROR", e.toString());
                        }
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.d("ERROR", e.toString());
            }
        }
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("DELETED", "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("CHANGED", "DataItem changed: " + event.getDataItem().getUri());
            }
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
