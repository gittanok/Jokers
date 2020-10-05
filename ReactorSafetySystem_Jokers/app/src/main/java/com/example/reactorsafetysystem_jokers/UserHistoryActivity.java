package com.example.reactorsafetysystem_jokers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class UserHistoryActivity extends AppCompatActivity {

    FirebaseAuth currentUser = FirebaseAuth.getInstance();
    DatabaseRFIDRepository db = new DatabaseRFIDRepository();

    private static String documentId;
    private static Timestamp timeStampIn;
    private static Timestamp timeStampOut;
    private static int clockInCounter;
    private static int clockOutCounter;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<String> arrayList2 = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);

        TextView clockInText = findViewById(R.id.textview_counter_clock_in);
        TextView clockOutText = findViewById(R.id.textView_counter_clock_out);
        final ListView list_history = findViewById(R.id.list_history);
        final ListView list_history2 = findViewById(R.id.list_history2);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.row, arrayList);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this,R.layout.row, arrayList2);

        db.getUserClockInHistory(currentUser.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                timeStampIn = (Timestamp) Objects.requireNonNull(document.getData().get("clockInTime"));
                                clockInCounter += 1;


                                clockInText.setText(String.valueOf(clockInCounter));
                                arrayList.add(timeStampIn.toDate().toString());
                            list_history.setAdapter(arrayAdapter);
                            Log.d("clockincounter", String.valueOf(clockInCounter));
                            Log.d("timestamp",timeStampIn.toDate().toString());
                        }

                    } else {
                        Log.d("Error", "Error getting documents: ", task.getException());
                    }

                }
            });

        Log.d("arraylist",arrayList.toString());

        db.getUserClockOutHistory(currentUser.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                        timeStampOut = (Timestamp) Objects.requireNonNull(document.getData().get("clockOutTime"));
                        clockOutCounter += 1;

                        clockOutText.setText(String.valueOf(clockOutCounter));
                        arrayList2.add(timeStampOut.toDate().toString());
                        list_history2.setAdapter(arrayAdapter2);
                        Log.d("clockOutcounter", String.valueOf(clockOutCounter));
                        Log.d("timestamp",timeStampOut.toDate().toString());
                    }

                } else {
                    Log.d("Error", "Error getting documents: ", task.getException());
                }
            }
        });

        };


}
