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
import android.view.View;
import android.webkit.ConsoleMessage;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

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
