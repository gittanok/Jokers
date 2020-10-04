package com.example.reactorsafetysystem_jokers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DatabaseRFIDRepository {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<QuerySnapshot> getUserInfo(String userId) {
        return db.collection("users").whereEqualTo("userId",userId).get();
    }

    public void setUserClockInState(Boolean flag, String documentId) {
        db.collection("users").document(documentId).update("userstate", flag);
    }
}
