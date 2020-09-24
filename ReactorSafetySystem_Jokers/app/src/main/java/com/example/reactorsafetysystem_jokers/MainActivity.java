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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DatabaseRFIDRepository db = new DatabaseRFIDRepository();

    private static final int RC_SIGN_IN = 123;
    private static String check_RFID;
    private static String documentId;
    private static String disByteArray = "A6155549";
    private static Boolean userState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createSignInIntent();

        db.getUserInfo("qpGCZ9QCMdh4AfwheTy7ShUNF").addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentId = document.getId();

                        check_RFID = Objects.requireNonNull(document.getData().get("RFID")).toString();
                        userState = (Boolean) Objects.requireNonNull(document.getData().get("userstate"));

                        Log.d("test", check_RFID);
                        Log.d("test", String.valueOf(userState));

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

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    // [END auth_fui_result]
}
