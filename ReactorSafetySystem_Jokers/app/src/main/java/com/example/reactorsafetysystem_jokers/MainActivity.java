package com.example.reactorsafetysystem_jokers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.provider.Settings.NameValueTable.NAME;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final Intent myIntent = new Intent(MainActivity.this, DeviceListActivity.class);

        BluetoothActivity bluetoothActivity = new BluetoothActivity();

        Button btButton = findViewById(R.id.buttonBt);

        btButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                MainActivity.this.startActivity(myIntent);


            }
        });






    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, DEFAULT_UUID);
            } catch (IOException e) {
                Log.e("TAG", "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {

            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e("TAG", "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e("TAG", "Could not close the connect socket", e);
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {


    }


    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

/*

    boolean clockInClockOut(string RFID) {

         db.checkIfUserIsClockedIn(RFID).addOnSuccessListener { clockedIn ->

            if(!clockedIn){

                db.clockInUser(RFID).addOnSuccessListener {

                    return true //skicka med bluetooth tillbaka true för att indikera lyckad inloggning

                }.addOnFailureListener {

                    return false

                }

            }else{

                db.clockOutUser(RFID).addOnSuccessListener {

                    return true //skicka med bluetooth tillbaka true för att indikera lyckad inloggning

                }.addOnFailureListener {

                    return false

                }

            }
         }.addOnFailureListener {

            return false

         }

     }


         */












}
