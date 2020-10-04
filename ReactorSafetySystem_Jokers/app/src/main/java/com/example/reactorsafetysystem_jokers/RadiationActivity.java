package com.example.reactorsafetysystem_jokers;

import android.util.Log;

public class RadiationActivity {


    int currentRadiation = 30;
    int protectiveGear = 1;
    int rc = 1;
    int totalExposure = 0;
    int radiationLimit = 5000; //500000
    boolean valuesChanged = true;



    public boolean checkRadiationLimit(){

        totalExposure += currentRadiation;
        Log.d("exposure to radiation:", String.valueOf(totalExposure));
        if(totalExposure <= radiationLimit){
            return false;
        }
        return true;

    }

    public int timeRemaining(){
        int timeRemaining = (radiationLimit - totalExposure) / ((currentRadiation * rc) / protectiveGear);
        Log.d("Time", "Has changed ");

        return  timeRemaining;
    }

    public void setProtectiveGear(int gear){

        if (gear == Gear.clothes){
            protectiveGear = 1;
        }
        else if(gear == Gear.hazmatSuit){
            protectiveGear = 5;
        }

        valuesChanged = true;
    }

    public int getRadiationExposure() {
        return totalExposure;
    }

    public void setCurrentRadiation(int newRadiation){

        currentRadiation = newRadiation;
        valuesChanged = true;
        Log.d("radiation level changed to:", String.valueOf(newRadiation));

    }

    public boolean getValuesChanged(){
        return valuesChanged;
    }

    public void setValuesChanged(boolean valuesChanged) {
        Log.d("values Changed", "this should appear twice");
        this.valuesChanged = valuesChanged;
    }

    public int[] getIntervals() {

        int seventyFive = (int) (radiationLimit * 0.75);
        int fifty = (int) (radiationLimit * 0.50);
        int twentyFive = (int) (radiationLimit * 0.25);

        int[] intervalArray = new int[] {twentyFive, fifty, seventyFive };


        return intervalArray;
    }

    public interface Gear {

        int clothes = 1;
        int hazmatSuit = 2;
    }

    public interface Room {

        int breakRoom = 1;
        int controlRoom = 2;
        int reactorRoom = 3;
    }


}
