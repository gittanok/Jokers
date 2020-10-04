package com.example.reactorsafetysystem_jokers;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {


    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    ListView listViewPairedDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    public static String DEVICE_ADDRESS = "device_address";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }



    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();

    }

    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
            }

            pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this,
                    android.R.layout.simple_list_item_1, pairedDeviceArrayList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    BluetoothDevice device =
                            (BluetoothDevice) parent.getItemAtPosition(position);
                    Toast.makeText(BluetoothActivity.this,
                            "Name: " + device.getName() + "\n"
                                    + "Address: " + device.getAddress() + "\n"
                                    + "BondState: " + device.getBondState() + "\n"
                                    + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                                    + "Class: " + device.getClass(),
                            Toast.LENGTH_LONG).show();


                    Intent intent = new Intent(new Intent(getApplicationContext(), UserActivity.class));
                    intent.putExtra(DEVICE_ADDRESS, device.getAddress());
                    Log.d("Device", device.toString());

                    //TODO check if you get to the next activity as intended, should probably be moved to ONCREATE.
                    // Set result and finish this Activity
                    startActivity(intent);

                    //setResult(Activity.RESULT_OK, intent);
                    finish();

                    //textStatus.setText("start ThreadConnectBTdevice");

 //                   startActivity(new Intent(getApplicationContext(), BluetoothActivity.class) );

                }
            });
        }
    }
}
