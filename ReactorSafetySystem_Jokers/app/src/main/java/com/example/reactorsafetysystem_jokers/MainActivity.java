package com.example.reactorsafetysystem_jokers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.ConsoleMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
BluetoothAdapter blueToothAdapter = BluetoothAdapter.getDefaultAdapter();
int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(blueToothAdapter == null) {
            //device does not support bluetooth

        } else {
            if(!blueToothAdapter.isEnabled()) {
                Intent enableBluetoothintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothintent, REQUEST_ENABLE_BT);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (requestCode == RESULT_OK) {
                //bluetooth is enable
            } else if (resultCode == RESULT_CANCELED) {
                //bluetooth is cancelled
            }
        }
    }
Handler handler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
//            case STATE_LISTENING:
//                status.s
        }

        return false;
    }
});
    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private  final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
