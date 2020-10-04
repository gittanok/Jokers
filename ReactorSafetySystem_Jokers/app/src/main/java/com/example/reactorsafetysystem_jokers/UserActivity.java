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
import java.security.cert.TrustAnchor;
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
    private static String disByteArray = "A6155549";
    private static Boolean userState;
    private static List<String> recievedBytes = new ArrayList<>();
    RadiationActivity radiation = new RadiationActivity();
    private static String byteProtocol;
    static Boolean clockedIn;
    private static final int REQUEST_ENABLE_BT = 1;


    TextView textByteCnt;

    Button btnTest;

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
/*
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = getIntent();
        String address = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS);

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
        myThreadConnectBTdevice.start();


 */


        Button changeRadiationButton = findViewById(R.id.button_change_radiation);

        changeRadiationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                byte[] buffer = {(byte)100};
                int operation = 4;

                determineOperation(operation, buffer);
            }
        });

        btnTest = findViewById(R.id.btnTest);
        Button btnWarningNotification = findViewById(R.id.button_warning_notification);


        btnWarningNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO: this should be started when i clock in


            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                determineOperation(Operation.CLOCK_IN_OR_OUT, new byte[] {0,0,0,0,0x0A,6,1,5,5,5,4,9} );


            }

        });





    }

    private void prepareWarning(){

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_warning_notification)
                .setContentTitle("WARNING DANGER")
                .setContentText("Time is running out run")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Abort reactor boom boom"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        new Thread(() -> {

            //TODO: should this thread also handle the update of sending time every minute, and everytime radiation is sent?
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
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendTimeRemaining(int timeRemaining) {
        long time = timeRemaining * 1000;

        if(consoleCountDownTimer != null){
            consoleCountDownTimer.cancel();
        }

        consoleCountDownTimer = new CountDownTimer(time, 60000) {

            public void onTick(long millisUntilFinished) {

                if (!clockedIn) {
                    consoleCountDownTimer.cancel();
                }

                long time = millisUntilFinished/1000;
                long hours = time / 3600;
                long minutes = (time % 3600) / 60;

                byte[] sendTimeToConsole = {Responses.TIME_LEFT, (byte) hours, (byte) minutes};
                sendResponse(sendTimeToConsole);
            }
            public void onFinish() { }
        }.start();
    }


    private void updateTimeRemaining(int timeRemaining, NotificationCompat.Builder builder){
        long time = timeRemaining * 1000;

        TextView timeInfo = findViewById(R.id.textview_time_remaining);

            if(mCountDownTimer != null){
                mCountDownTimer.cancel();
            }

            mCountDownTimer = new CountDownTimer(time, 1000) {

                public void onTick(long millisUntilFinished) {

                    if (!clockedIn) {
                        radiation.setValuesChanged(true);
                        timeInfo.setText("Have a nice day");
                        radiation.totalExposure = 0;
                        mCountDownTimer.cancel();
                    }

                    else {
                        long time = millisUntilFinished/1000;
                        long hours = time / 3600;
                        long minutes = (time % 3600) / 60;
                        long seconds = time % 60;
                        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        timeInfo.setText(timeString);
                    }
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

        builder2.setProgress(100, 40, false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(UserActivity.this);
        notificationManager.notify(5, builder2.build());

    }

    private void clockInOrOut(byte[] RFID){
        //"A6155549";
        byte[] bytes = new byte[] {0,0,0,0,0x0A,6,1,5,5,5,4,9};

        for(int i = 0; i < bytes.length; i++) {
            byte byteNumber = Array.getByte(bytes,i);
            recievedBytes.add(String.valueOf(byteNumber));
        }

        db.getUserInfo(Objects.requireNonNull(currentUser.getCurrentUser()).getUid()).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    documentId = document.getId();
                    check_RFID = Objects.requireNonNull(document.getData().get("RFID")).toString();
                    userState = (Boolean) Objects.requireNonNull(document.getData().get("userstate"));
                }

                if (check_RFID.equals( disByteArray )) {
                    if (userState) {
                        db.setUserClockInState(false, documentId);
                        clockedIn = false;
                        sendResponse(new byte[] {Responses.CLOCK_IN, Responses.CLOCK_OUT_SUCCESSFUL});

                    }
                    else {
                        db.setUserClockInState(true, documentId);
                        clockedIn = true;
                        sendResponse(new byte[] {Responses.CLOCK_IN, Responses.CLOCK_IN_SUCCESSFUL});
                    }
                    prepareWarning();
                }
            }
            else {
                sendResponse(new byte[] {Responses.CLOCK_IN, Responses.REQUEST_FAILED});
                Log.w("Error", "Error getting documents.", task.getException());
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


    private void changeRadiationLevel(int radiationValue){
        radiation.setCurrentRadiation(radiationValue);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        // for when we get back to the userActivity from BluetoothActivity
        String address = data.getExtras()
                .getString(BluetoothActivity.DEVICE_ADDRESS);

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
        myThreadConnectBTdevice.start();


        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
            } else {
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }


    }

    */

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){

                startThreadConnected(bluetoothSocket);

            }else{
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
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
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            int bytes = 0;

            String strRx = "";

            byte[] buffer = new byte[1024];
            int operation;

            while (true) {
                try {

                    byte currentByte;
                    int informationByteSize = 0;


                    connectedInputStream.read(buffer, 0, 1);

                    operation = buffer[0];

                    //TODO: include all operations

                    if(operation == Operation.CLOCK_IN_OR_OUT ){
                        informationByteSize = 4;
                    }
                    if(operation == Operation.NEW_RADIATION_LEVEL || operation == Operation.SET_PROTECTIVE_GEAR || operation == Operation.SET_ROOM){
                        informationByteSize = 1;
                    }

                    for(int i = 0; i < informationByteSize; i++){

                            connectedInputStream.read(buffer, 0, 1);
                            buffer[i] = buffer[0];
                    }
                    determineOperation(operation, buffer);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }
            }
        }

        public void write(byte[] buffer) {
                try {
                    connectedOutputStream.write(buffer);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public interface Operation {
        byte CLOCK_IN_OR_OUT = 0;
        byte NEW_RADIATION_LEVEL = 4;
        byte SET_PROTECTIVE_GEAR = 5;
        byte SET_ROOM = 6;
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

  //      myThreadConnected.write(byteResponse);
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

            case Operation.SET_ROOM:
                int room = buffer[0];
                setRoom(room);

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



        /*
        inputPane = (LinearLayout)findViewById(R.id.inputpane);
        inputField = (EditText)findViewById(R.id.input);

        btnSend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(myThreadConnected!=null){
                    byte[] bytesToSend = inputField.getText().toString().getBytes();
                    myThreadConnected.write(bytesToSend);
                    byte[] NewLine = "\n".getBytes();
                    myThreadConnected.write(NewLine);
                }
            }});




        btnClear = (Button)findViewById(R.id.clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textStatus.setText("");
                textByteCnt.setText("");
            }
        });

        */

        /*
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }


         */

//using the well-known SPP UUID

/*
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

 */
/*
        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();
        Log.d("Device information", stInfo);
        textInfo.setText(stInfo);

 */
