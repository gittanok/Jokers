package com.example.reactorsafetysystem_jokers;

import android.util.Log;

public class RadiationActivity {


    int radiation = 30;
    int pc = 1;
    int rc = 1;
    int exposure = 0;
    int radiationLimit = 800; //500000
    boolean valuesChanged = true;



    public boolean checkRadiationLimit(){

        exposure += radiation;
        Log.d("radiation", String.valueOf(exposure));
        if(exposure <= radiationLimit){
            return false;
        }
        return true;

    }

    public int timeRemaining(){

        int timeRemaining = (radiationLimit - exposure) / ((radiation * rc) / pc);
        Log.d("Time", "Has changed ");
        return  timeRemaining;

    }

    public void setPc(int newPc){

        pc = newPc;
        valuesChanged = true;

    }

    public void setRadiation(int newRadiation){

        radiation = newRadiation;
        valuesChanged = true;
        Log.d("radiation", "radiation level changed");

    }

    public boolean getValuesChanged(){


        return valuesChanged;


    }

    public void setValuesChanged(boolean valuesChanged) {
        Log.d("values Changed", "this should appear twice");
        this.valuesChanged = valuesChanged;
    }
}
