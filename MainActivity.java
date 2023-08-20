package com.example.wifimapping;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast; //this is for showing on-screen messages
import android.widget.Button; //Required for the button class

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Date;


public class MainActivity extends AppCompatActivity {
    //Declare the textviews from activity_main
    private TextView networkID;
    private TextView networkName;
    private TextView networkLevel;
    private  TextView GPSnum;

    //GPS
    private LocationManager locationManager;
    private LocationListener locationListener;

    //Handlers allows you to send and process Message and Runnable objects associated with a thread's MessageQueue
    private Handler wifiStrengthHandler = new Handler();
    private boolean isCheckingWifiStrength = false;

    //Private boolean states
    private boolean startState = false; //track the state of start/stop operation
    private boolean wifiVal = false;
    private boolean gpsVal = false;

    private String filename;
    private String timestamp;

    //GPS Information Handlers
    @Override
    public void  onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && startState == true) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
                }
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the textviews
        networkID = findViewById(R.id.networkID); // Initialize the TextView
        networkName = findViewById(R.id.networkRSSI); // Initialize the TextView
        networkLevel = findViewById(R.id.networkLevel); // Initialize the TextView
        GPSnum = findViewById(R.id.GPS); // Initialize the TextView
        GPSnum.setVisibility(View.INVISIBLE);

        //Initialize GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Initialize the buttons
        Button initiateButton = findViewById(R.id.start1);
        Button stopButton = findViewById(R.id.stop);

        //Attach an OnClickListener to the button for the START
        initiateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Change the button state to true
                startState = true;
                //Upon click, create a new file
                fileCreate();
                //upon clicked, check if there is an established WiFi connection
                if (!isCheckingWifiStrength) {
                    isCheckingWifiStrength = true;
                    //Set GPS text to "Pending"
                    GPSnum.setVisibility(View.VISIBLE);
                    GPSnum.setText("Pending");

                    //If there is WiFi, check the strength
                    checkWifiStrength();

                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
                    } catch (SecurityException e) {
                        Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    }

                    //Make a toast to show the user something happened
                    Toast.makeText(MainActivity.this, "Started WiFi check!", Toast.LENGTH_SHORT).show();


                }

            }
        });
        //Attach an OnClickListener to the button for stop
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startState == true){
                    //Set all readouts to null
                    networkID.setText("null");
                    networkName.setText("null");
                    networkLevel.setText("null");
                    GPSnum.setText("null");
                    wifiVal=false;
                    //Set checking for wifi to false
                    isCheckingWifiStrength = false;
                    //remove the callback
                    wifiStrengthHandler.removeCallbacksAndMessages(null);

                    locationManager.removeUpdates(locationListener);
                    GPSnum.setVisibility(View.INVISIBLE);
                    //Tell the user the process is stopped
                    Toast.makeText(MainActivity.this, "Stopped WiFi check!", Toast.LENGTH_SHORT).show();
                    startState = false;
                }
                else{
                    Toast.makeText(MainActivity.this, "Connect to WiFi First", Toast.LENGTH_SHORT).show();
                }

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

                    wifiVal=true;
                    if(gpsVal==true) {
                        String dataToSave ="SSID: " + ssid + ", RSSI: " + rssi + " dBm, Level: " + level + "\n";
                        saveDataToInternalStorage(dataToSave);
                        gpsVal=false; //measurement is taken, set it back to false
                    }
                }
                if (isCheckingWifiStrength) {
                    checkWifiStrength(); // Call itself again to update after a delay
                }
            }
        }, 500); // Updating every 500 milliseconds

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(startState == true) {
                    //Get Timestamp:
                    Date date = new Date();
                    long timestamp = date.getTime();
                    Log.d("Timestamp", "Current timestamp: " + timestamp);

                    GPSnum.setText("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                    String dataToSave =  "unix time: " + String.valueOf(timestamp) + ", " + "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude() +", ";
                    if(wifiVal==true) {
                        saveDataToInternalStorage(dataToSave);
                        Log.d("Saved Data", "values: " + dataToSave); //debug
                        gpsVal=true; // set GPS value to true since data is measured
                    }
                    wifiVal = false;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }


    //Create the file name
    private void fileCreate(){
        if(startState==true) {
            File directory = new File(getFilesDir(), "WIFI-MAP");
            if (!directory.exists()) {
                directory.mkdir();
                Log.d("directory_State", "Directory Made!");
            }
            Log.d("directory_exists", "Directory Exists");
            Date date2 = new Date();
            long file_timestamp = date2.getTime();
            Log.d("Timestamp", "Current timestamp: " + file_timestamp);
            filename = String.valueOf(file_timestamp);
        }
    }

    private void saveDataToInternalStorage(String data) {
        try {
            // Use MODE_APPEND to append data, or MODE_PRIVATE to overwrite existing data
            FileOutputStream fos = openFileOutput(filename+".txt", MODE_APPEND);
            fos.write(data.getBytes());
            Log.d("directory_File", "Data Added to File");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
