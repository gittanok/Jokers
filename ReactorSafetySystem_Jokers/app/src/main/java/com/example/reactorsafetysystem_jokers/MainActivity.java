package com.example.reactorsafetysystem_jokers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DatabaseRFIDRepository db = new DatabaseRFIDRepository();

    private static String check_RFID;
    private static String documentId;
    private static String disByteArray = "A6155549";
    private static Boolean userState;
    private static List<String> recievedBytes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        byte[] bytes = new byte[] {0,0,0,0,0x0A,6,1,5,5,5,4,9};

        for(int i = 0; i < bytes.length; i++) {
            byte byteNumber = Array.getByte(bytes,i);
            recievedBytes.add(String.valueOf(byteNumber));
        }

        db.getUserInfo("qpGCZ9QCMdh4AfwheTy7ShUNF").addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        documentId = document.getId();
                        check_RFID = Objects.requireNonNull(document.getData().get("RFID")).toString();
                        userState = (Boolean) Objects.requireNonNull(document.getData().get("userstate"));
                    }

                    if (check_RFID.equals(disByteArray)) {
                        if (userState) {
                            db.setUserClockInState(false, documentId);
                            //skicka tillbaka lämpligt protokoll till DIS
                        }
                        else {
                            db.setUserClockInState(true, documentId);
                            //skicka tillbaka lämpligt protokoll till DIS
                        }
                    }
                }
                else {
                    Log.w("yeet", "Error getting documents.", task.getException());
                    //skicka tillbaka lämpligt protokoll till DIS INGEN CONNECTIOn typ
                }
            }
        });
    }
}
