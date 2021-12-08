package com.example.heartstrawngv1;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.icu.util.Output;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "HeartStrawng";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    //ProgressDialog mProgressDialog;
    private ConnectedThread mConnectedThread;
    private final Handler mHandler;
    private final boolean isWaterIntake;
    private String partialMessage;

    public BluetoothConnectionService(Context context, Handler handler, boolean isWaterIntake) {
        mContext = context;
        mHandler = handler;
        this.isWaterIntake = isWaterIntake;
        partialMessage = "";
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            } catch(IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread running");

            BluetoothSocket socket = null;

            Log.d(TAG, "run: RFCOM server socket start...");

            try {
                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            if (socket != null) {
                connected(socket, mmDevice);
            }

            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread");

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfCommSocket using UUID: " + MY_UUID_INSECURE);
                Log.d(TAG, "ConnectThread: device is " + mmDevice.getName());
                Log.d(TAG, "ConnectThread: UUID is " + deviceUUID);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfCommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected");
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed socket");
                } catch (IOException ioException) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + ioException.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }

            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket");
                mmSocket.close();
            } catch(IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "Start");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started");

        //mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            /*try {
                //mProgressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }*/

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true) {
                try {
                    if (mmInStream.available() > 0) {
                        Log.d(TAG, "Stuff here 1");
                    }
                    bytes = mmInStream.read(buffer);
                    if (mmInStream.available() > 0) {
                        Log.d(TAG, "Stuff here 2");
                    }
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    String newMessage = incomingMessage;
                    //Message m = Message.obtain(mHandler, 1, incomingMessage);
                    //mHandler.sendMessage(m);
                    if (isWaterIntake) {
                        while (true) {
                            if (newMessage.substring(newMessage.length() - 1).equals("d")) {
                                if (partialMessage.length() != 0) {
                                    partialMessage += newMessage;
                                    Log.d(TAG, "Full Message: " + partialMessage);
                                    partialMessage = partialMessage.substring(0, partialMessage.length() - 1);
                                    Message m = Message.obtain(mHandler, 1, partialMessage);
                                    mHandler.sendMessage(m);
                                    partialMessage = "";
                                    break;
                                }
                                else {
                                    Log.d(TAG, "Full Message1: " + newMessage);
                                    newMessage = newMessage.substring(0, newMessage.length() - 1);
                                    Message m = Message.obtain(mHandler, 1, newMessage);
                                    mHandler.sendMessage(m);
                                    break;
                                }

                            } else {
                                Log.d(TAG, "Message Received: " + newMessage);
                                partialMessage += newMessage;
                                Thread.sleep(2000);
                                if (mmInStream.available() > 0) {
                                    Log.d(TAG, "Stuff available");
                                }
                                bytes = mmInStream.read(buffer);
                                Log.d(TAG, "Finished reading again");
                                newMessage = new String(buffer, 0, bytes);
                            }
                        }
                    }
                    else {
                        Message m = Message.obtain(mHandler, 1, incomingMessage);
                        mHandler.sendMessage(m);
                    }
                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "write: Error reading inputstream " + e.getMessage());
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputstream " + e.getMessage());
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {}
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        Log.d(TAG, "write: Write Called");
        mConnectedThread.write(out);
    }
}
