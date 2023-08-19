package com.example.wifimapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast; //this is for showing on-screen messages
import android.widget.Button; //Required for the button class

public class MainActivity extends AppCompatActivity {
    //Declare the textviews from activity_main
    private TextView networkID;
    private TextView networkName;
    private TextView networkLevel;

    //Handlers allows you to send and process Message and Runnable objects associated with a thread's MessageQueue
    private Handler wifiStrengthHandler = new Handler();
    private boolean isCheckingWifiStrength = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the textviews
        networkID = findViewById(R.id.networkID); // Initialize the TextView
        networkName = findViewById(R.id.networkRSSI); // Initialize the TextView
        networkLevel = findViewById(R.id.networkLevel); // Initialize the TextView

        //Initialize the buttons
        Button initiateButton = findViewById(R.id.start1);
        Button stopButton = findViewById(R.id.stop);

        //Attach an OnClickListener to the button for the START
        initiateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //upon clicked, check if there is an established WiFi connection
                if (!isCheckingWifiStrength) {
                    isCheckingWifiStrength = true;
                    //If there is WiFi, check the strength
                    checkWifiStrength();
                    //Make a toast to show the user something happened
                    Toast.makeText(MainActivity.this, "Started WiFi check!", Toast.LENGTH_SHORT).show();

                }

            }
        });
        //Attach an OnClickListener to the button for stop
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set all readouts to null
                networkID.setText("null");
                networkName.setText("null");
                networkLevel.setText("null");
                //Set checking for wifi to false
                isCheckingWifiStrength = false;
                //remove the callback
                wifiStrengthHandler.removeCallbacksAndMessages(null);
                //Tell the user the process is stopped
                Toast.makeText(MainActivity.this, "Stopped WiFi check!", Toast.LENGTH_SHORT).show();
            }
        });
        }
    //Check the WiFi Strength
    private void checkWifiStrength() {
        wifiStrengthHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //If WiFi info is not nothing
                if (wifiInfo != null) {
                    int rssi = wifiInfo.getRssi();
                    String ssid = wifiInfo.getSSID();
                    networkID.setText(String.valueOf(ssid));
                    networkName.setText(String.valueOf(rssi) + " dBm");
                    int level = WifiManager.calculateSignalLevel(rssi, 5);
                    networkLevel.setText(String.valueOf(level));
                }
                if (isCheckingWifiStrength) {
                    checkWifiStrength(); // Call itself again to update after a delay
                }
            }
        }, 500); // Updating every 500 milliseconds
    }
    }
