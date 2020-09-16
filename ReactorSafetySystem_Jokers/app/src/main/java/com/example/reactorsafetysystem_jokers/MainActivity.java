package com.example.reactorsafetysystem_jokers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
