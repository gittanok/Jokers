package com.example.reactorsafetysystem_jokers;

import android.util.Log;

public class RadiationActivity {

    int currentRadiation = 30;
    int protectiveGear = 1;
    double roomCoefficient = 1;
    int totalExposure = 0;
    int radiationLimit = 5000; //500000
    boolean valuesChanged = true;


    public boolean checkRadiationLimit() {
        totalExposure += currentRadiation;

        if(totalExposure <= radiationLimit){
            return false;
        }
        return true;

    }

    public int timeRemaining() {
        int timeRemaining = (int) ((radiationLimit - totalExposure) / ((currentRadiation * roomCoefficient) / protectiveGear));

        return  timeRemaining;
    }



    public int getRadiationExposure() {
        return totalExposure;
    }

    public void setCurrentRadiation(int newRadiation){

        currentRadiation = newRadiation;
        valuesChanged = true;

    }

    public boolean getValuesChanged(){
        return valuesChanged;
    }

    public void setValuesChanged(boolean valuesChanged) {
        this.valuesChanged = valuesChanged;
    }

    public int[] getIntervals() {
        int seventyFive = (int) (radiationLimit * 0.75);
        int fifty = (int) (radiationLimit * 0.50);
        int twentyFive = (int) (radiationLimit * 0.25);

        int[] intervalArray = new int[] {twentyFive, fifty, seventyFive };

        return intervalArray;
    }

    public void setRoom(int room) {

        if (room == Room.breakRoom){
            roomCoefficient = 0.1;
        }
        else if(room == Room.controlRoom){
            roomCoefficient = 0.5;
        }
        else if(room == Room.reactorRoom){
            roomCoefficient = 1.6;
        }

        valuesChanged = true;

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

    public double getRoomCoefficient() {

        return  roomCoefficient;
    }

    public int getProtectiveGear() {
        return protectiveGear;
    }

    public int getCurrentRadiation() {
        return currentRadiation;
    }

    public interface Gear {

        int clothes = 0;
        int hazmatSuit = 1;
    }

    public interface Room {

        int breakRoom = 0;
        int controlRoom = 1;
        int reactorRoom = 2;
    }


}
