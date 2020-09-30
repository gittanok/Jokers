package com.example.reactorsafetysystem_jokers;

import android.util.Log;

public class RadiationActivity {


    int radiation = 0;
    int pc = 1;


    public void calculateRadiation(){


        while(radiation <200000){
            radiation += 1;
            Log.d("radiation", String.valueOf(radiation));

        }


    }



}
