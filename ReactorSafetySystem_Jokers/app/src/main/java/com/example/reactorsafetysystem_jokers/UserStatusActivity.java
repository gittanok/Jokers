package com.example.reactorsafetysystem_jokers;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class UserStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_status);


        TextView checkedIn = (TextView) findViewById(R.id.clockedInStatus);


    /*

        db.checkIfUserIsClockedIn(RFID).addOnSuccessListener { clockedIn ->


         if(clockedIn) {
            checkedIn.setText("yeees");
         }else{
            checkedIn.setText("noooooo");
        }

       }

*/


    }





}
