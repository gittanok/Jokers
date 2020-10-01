package com.example.reactorsafetysystem_jokers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UserActivity extends AppCompatActivity {




    DatabaseRFIDRepository db = new DatabaseRFIDRepository();

    private static String check_RFID;
    private static String documentId;
    private static String disByteArray = "A6155549";
    private static Boolean userState;
    private static List<String> recievedBytes = new ArrayList<>();
    RadiationActivity radiation = new RadiationActivity();
    private static String byteProtocol;

    private static final int REQUEST_ENABLE_BT = 1;


    TextView textStatus, textByteCnt;

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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        btnTest = findViewById(R.id.btnTest);
        Button btnWarningNotification = findViewById(R.id.button_warning_notification);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_warning_notification)
                .setContentTitle("WARNING DANGER")
                .setContentText("Time is running out run")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Abort reactor boom boom"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        btnWarningNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new Thread(() -> {
                    //Do whatever

                    //radiation.calculateRadiation();

                    boolean warning = false;





                    while(!warning) {

                        warning = radiation.checkRadiationLimit();

                        if (radiation.getValuesChanged()) {

                            radiation.setValuesChanged(false);
                            int timeRemaining = radiation.timeRemaining();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateTimeRemaining(timeRemaining, builder);
                                }
                            });

                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Log.d("sleep","inside while loop for warning");
                    }

                    Log.d("Threaaaaaaad", "doing the notification now");


                }).start();






            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


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

        });

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);



    }

    private void updateTimeRemaining(int timeRemaining, NotificationCompat.Builder builder){

        long time = timeRemaining * 1000;

        TextView timeInfo = findViewById(R.id.textview_time_remaining);

        new CountDownTimer(time, 1000) {

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

            }
        }.start();

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



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // for when we get back to the userActivity from BluetoothActivity
        String address = data.getExtras()
                .getString(BluetoothActivity.DEVICE_ADDRESS);

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
        myThreadConnectBTdevice.start();

        /*
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
        */

    }

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
                textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
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

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText("something wrong bluetoothSocket.connect():startThreadConnectedn" + eMessage);
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;

                /*
                runOnUiThread(new Runnable() {


                    @Override
                    public void run() {
                        textStatus.setText("");
                        textByteCnt.setText("");
                        Toast.makeText(MainActivity.this, msgconnected, Toast.LENGTH_LONG).show();

                        //listViewPairedDevice.setVisibility(View.GONE);
                        //inputPane.setVisibility(View.VISIBLE);
                    }
                });

                 */

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
            byte[] buffer = new byte[1024];
            int bytes;

            String strRx = "";

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);

                    final String strReceived = new String(buffer, 0, bytes);
                    final String strByteCnt = String.valueOf(bytes) + " bytes received.\n";

                    for(int i = 0; i < buffer.length; i++) {
                        byte byteNumber = Array.getByte(bytes,i);
                        recievedBytes.add(String.valueOf(byteNumber));
                    }

                    byteProtocol = "";
                    for(int i = 0; i < 4; i++){
                        byteProtocol += recievedBytes.get(i);
                    }

                    switch(byteProtocol) {
                        case "0000":
                            break;
                        case "0001":
                            break;
                        case "0010":
                            break;
                        case "0011":
                            break;
                        default:
                            // code block
                    }





                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.append(strReceived);
                            textByteCnt.append(strByteCnt);
                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }});
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
