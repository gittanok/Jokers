package com.example.reactorsafetysystem_jokers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserActivity extends AppCompatActivity {
    FirebaseAuth currentUser = FirebaseAuth.getInstance();
    DatabaseRFIDRepository db = new DatabaseRFIDRepository();

    private CountDownTimer mCountDownTimer;
    private CountDownTimer consoleCountDownTimer;
    private static String check_RFID;
    private static String documentId;
    private static Boolean userState;
    RadiationActivity radiation = new RadiationActivity();
    static Boolean clockedIn;
    private static final int REQUEST_ENABLE_BT = 1;


    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        createNotificationChannel();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = getIntent();
        String address = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS);

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
        myThreadConnectBTdevice.start();

        Button userHistoryButton = findViewById(R.id.button_user_history);

        userHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UserHistoryActivity.class) );
            }
        });
    }

    private void prepareWarning() {

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_warning_notification)
                .setContentTitle("WARNING DANGER")
                .setContentText("Time is running out run")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Abort reactor boom boom"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        new Thread(() -> {

            int[] intervals = radiation.getIntervals();

            boolean warning = false;
            int i = 0;

            while(!warning && clockedIn) {

                warning = radiation.checkRadiationLimit();
                int currentRadiationExposure = radiation.getRadiationExposure();


                if ( i < intervals.length && currentRadiationExposure > intervals[i]) {
                    i+=1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyWarningByInterval();
                        }
                    });
                }

                if (radiation.getValuesChanged()) {
                    radiation.setValuesChanged(false);
                    int timeRemaining = radiation.timeRemaining();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTimeRemaining(timeRemaining, builder);
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendTimeRemaining(timeRemaining);
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUiForTest();
                        }
                    });
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateUiForTest() {
        int rad = radiation.getCurrentRadiation();
        double rc = radiation.getRoomCoefficient();
        int pc = radiation.getProtectiveGear();

        TextView pcView = findViewById(R.id.textview_protective_gear);
        if(pc == 5) {
            pcView.setText("ON");
        }
        else {
            pcView.setText("OFF");
        }

        TextView rcView = findViewById(R.id.textview_rc);
        if(rc == 0.1) {
            rcView.setText("Break room");
        }
        else if(rc == 0.5) {
            rcView.setText("Control room");
        }
        else if(rc == 1.6) {
            rcView.setText("Reactor room");
        }
        TextView radView = findViewById(R.id.textview_radiation);
        radView.setText(Integer.toString(rad));
    }

    private void cancelTimerThreads() {
        if(consoleCountDownTimer != null) {
            consoleCountDownTimer.cancel();
        }
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        radiation.totalExposure = 0;
        radiation.setValuesChanged(true);
    }

    private void sendTimeRemaining(int timeRemaining) {
        long time = timeRemaining * 1000;

        if(consoleCountDownTimer != null) {
            consoleCountDownTimer.cancel();
        }

        consoleCountDownTimer = new CountDownTimer(time, 60000) {

            public void onTick(long millisUntilFinished) {
                long time = millisUntilFinished/1000;
                long hours = time / 3600;
                long minutes = (time % 3600) / 60;

                byte[] sendTimeToConsole = {Responses.TIME_LEFT, (byte) hours, (byte) minutes};
                sendResponse(sendTimeToConsole);
            }
            public void onFinish() { }
        }.start();
    }


    private void updateTimeRemaining(int timeRemaining, NotificationCompat.Builder builder) {

        long time = timeRemaining * 1000;
        TextView timeInfo = findViewById(R.id.textview_time_remaining);

            if(mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }

            mCountDownTimer = new CountDownTimer(time, 1000) {

                public void onTick(long millisUntilFinished) {
                    long time = millisUntilFinished/1000;
                    long hours = time / 3600;
                    long minutes = (time % 3600) / 60;
                    long seconds = time % 60;
                    String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    timeInfo.setText(timeString);
                }

                public void onFinish() {
                    timeInfo.setText("Evacuate!!!");
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(UserActivity.this);
                    notificationManager.notify(5, builder.build());

                    sendResponse(Responses.WARNING);
                }
            }.start();
    }

    private void notifyWarningByInterval() {
        final NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_warning_notification)
                .setContentTitle("WARNING CHECK TIME")
                .setContentText("Time is about to run out")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Be careful"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(UserActivity.this);
        notificationManager.notify(5, builder2.build());
    }



    private void clockInOrOut(byte[] RFID) {

        String stringRFID = "";

        for (int i = 0; i<4; i++) {
            String eachByteValue = String.format("%02X", RFID[i]);
            stringRFID += eachByteValue;
        }

        String finalStringRFID = stringRFID;

        db.getUserInfo(Objects.requireNonNull(currentUser.getCurrentUser()).getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        documentId = document.getId();
                        check_RFID = Objects.requireNonNull(document.getData().get("RFID")).toString();
                        userState = (Boolean) Objects.requireNonNull(document.getData().get("userstate"));
                    }

                    if (check_RFID.equals(finalStringRFID)) {
                        if (userState) {
                            db.addClockOutHistory(currentUser.getCurrentUser().getUid(), userState);
                            db.setUserClockInState(false, documentId);
                            clockedIn = false;
                            sendResponse(new byte[] {Responses.CLOCK_IN, Responses.CLOCK_OUT_SUCCESSFUL});
                            cancelTimerThreads();
                        }
                        else {
                            db.addClockInHistory(currentUser.getCurrentUser().getUid(), userState);
                            db.setUserClockInState(true, documentId);
                            clockedIn = true;
                            sendResponse(new byte[] {Responses.CLOCK_IN, Responses.CLOCK_IN_SUCCESSFUL});
                            prepareWarning();
                        }
                    }
                }
                else {
                    sendResponse(new byte[] {Responses.CLOCK_IN, Responses.REQUEST_FAILED});
                    Log.w("Error", "Error getting documents.", task.getException());
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "warning notification";
            String description = "warning notfication alert";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("channel1", name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void changeRadiationLevel(int radiationValue) {
        radiation.setCurrentRadiation(radiationValue);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myThreadConnectBTdevice!=null) {
            myThreadConnectBTdevice.cancel();
        }
    }

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket) {
        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;

        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;

            try {
                bluetoothSocket.connect();
                success = true;
            }
            catch (IOException e) {
                e.printStackTrace();

                try {
                    bluetoothSocket.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if(success){
                startThreadConnected(bluetoothSocket);
            }
            else{
                //fail
            }
        }

        public void cancel() {
            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            int operation;

            while (true) {
                try {
                    byte[] buffer = new byte[4];

                    int informationByteSize = 0;

                    connectedInputStream.read(buffer, 0, 1);

                    operation = buffer[0];

                    if(operation == Operation.CLOCK_IN_OR_OUT ){
                        informationByteSize = 4;
                    }

                    if(operation == Operation.NEW_RADIATION_LEVEL || operation == Operation.SET_PROTECTIVE_GEAR || operation == Operation.SET_ROOM){
                        informationByteSize = 1;
                    }

                    //first bit is always the same as the last.
                    for(int i = 0; i < informationByteSize; i++){
                            connectedInputStream.read(buffer, 0, 1);
                            buffer[i] = buffer[0];
                    }
                    determineOperation(operation, buffer);

                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] buffer) {
                try {
                    connectedOutputStream.write(buffer);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface Operation {
        byte CLOCK_IN_OR_OUT = 0;
        byte NEW_RADIATION_LEVEL = 4;
        byte SET_ROOM = 5;
        byte SET_PROTECTIVE_GEAR = 6;
    }

    public interface Responses {
        byte CLOCK_IN = 1;
        byte CLOCK_IN_SUCCESSFUL = 2;
        byte CLOCK_OUT_SUCCESSFUL = 1;
        byte REQUEST_FAILED = 0;
        byte[] WARNING = {(byte)2};
        byte TIME_LEFT = 3;
    }

    private void sendResponse(byte[] byteResponse){
        myThreadConnected.write(byteResponse);
    }

    private void determineOperation(int operation, byte[] buffer){

        switch(operation) {
            case Operation.CLOCK_IN_OR_OUT:
                clockInOrOut(buffer);
                break;

            case Operation.NEW_RADIATION_LEVEL:
                int radiationValue = buffer[0];
                changeRadiationLevel(radiationValue);
                break;

            case Operation.SET_PROTECTIVE_GEAR:
                int gear = buffer[0];
                setProtectiveGear(gear);
                break;

            case Operation.SET_ROOM:
                int room = buffer[0];
                setRoom(room);
                break;

            default:
                // code block
                break;
        }
    }

    private void setProtectiveGear(int gear) {
        radiation.setProtectiveGear(gear);
    }

    private void setRoom(int room){
        radiation.setRoom(room);
    }
}


