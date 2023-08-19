package com.example.wifimapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast; //this is for showing on-screen messages
import android.widget.Button; //Required for the button class

public class MainActivity extends AppCompatActivity {
    private TextView networkID; //Declare the textview
    private TextView networkName; //Declare the textview
    private TextView networkLevel; //Declare the textview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the textviews
        networkID = findViewById(R.id.networkID); // Initialize the TextView
        networkName = findViewById(R.id.networkRSSI); // Initialize the TextView
        networkLevel = findViewById(R.id.networkLevel); // Initialize the TextView

        //Locate the button
        Button initiateButton = findViewById(R.id.start1);

        //Attach an OnClickListener to the button
        initiateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //This will execute when teh button is clicked
                //Toast.makeText(MainActivity.this, "Initiated!", Toast.LENGTH_SHORT).show();
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null){
                    int rssi = wifiInfo.getRssi();
                    String ssid =  wifiInfo.getSSID();
                    //update the textview with the ssid value
                    networkID.setText(String.valueOf(ssid));

                    // Update the TextView with the rssi value
                    networkName.setText(String.valueOf(rssi)+" dBm");

                    int level = WifiManager.calculateSignalLevel(rssi,5); //the level will be between 0 and 4, where 0 is worst and 4 is best
                    networkLevel.setText(String.valueOf(level));
                }
                Toast.makeText(MainActivity.this, "Complete!", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
