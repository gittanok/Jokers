package com.example.reactorsafetysystem_jokers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DatabaseRFIDRepository {

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public Task<QuerySnapshot> getUserInfo(String userId) {

        return db.collection("users").whereEqualTo("userId",userId).get();

    }

    public void setUserClockInState(Boolean flag, String documentId) {
        db.collection("users").document(documentId).update("userstate", flag);
    }

    public String getShit(){

        return "shit";
    }

    public void addClockInHistory(String userId, Boolean flag){

        Map<String, Object> values = new HashMap<>();
        values.put("clockInTime",FieldValue.serverTimestamp());
        values.put("clockIn", flag);
        values.put("userId", userId);

        db.collection("userHistoryClockIn").document().set(values);

    }
    public void addClockOutHistory(String userId, Boolean flag){

        Map<String, Object> values = new HashMap<>();
        values.put("clockOutTime",FieldValue.serverTimestamp());
        values.put("clockOut", flag);
        values.put("userId", userId);

        db.collection("userHistoryClockOut").document().set(values);

    }

    public Task<QuerySnapshot> getUserClockInHistory(String userId) {

        return db.collection("userHistoryClockIn").whereEqualTo("userId",userId).get();

    }
    public Task<QuerySnapshot> getUserClockOutHistory(String userId) {

        return db.collection("userHistoryClockOut").whereEqualTo("userId",userId).get();

    }

}
