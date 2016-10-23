package com.demo.arctf.arctfdemo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by David on 10/8/2016.
 */
public class NetworkHandler extends AppCompatActivity {
    NetworkClient networkClient;
    String macAddress;
    MapFragConnector playerMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectToServer();
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();
        playerMap = (MapFragConnector) getSupportFragmentManager().findFragmentById(R.id.map);
        playerMap.setUsername(macAddress);
        /* TODO(david): Async Calls - call handler after address is received and map is created
            must change id from mac address
         */

        // Start update loop
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Handler", "Calling Update Server");
                updateServer();
                handler.postDelayed(this, 5000);
            }
        }, 15000);
    }

    /**
     * @param playerLocations hashmap of mac addresses to list of latitude and longitude of each player
     */
    public void updateMap(HashMap<String, ArrayList<LatLng>> playerLocations)
    {
        playerMap.updateMap(playerLocations);
    }

    private void updateServer()
    {
        JSONObject jsonObj = new JSONObject();
        LatLng curLoc = playerMap.getLastKnownLatLng();
        if(curLoc !=null) {
            try {
                jsonObj.put("address", macAddress);
                jsonObj.put("latitude", curLoc.latitude);
                jsonObj.put("longitude", curLoc.longitude);
                Log.d("Message", "Sending Location to Server");
                networkClient.updateLocation(jsonObj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void connectToServer() {
        Log.d("debug", "connect to server called");

        networkClient = (NetworkClient) getSupportFragmentManager().findFragmentById(R.id.main);

        if (networkClient != null) {
            networkClient.establishSession();
        } else {
            Log.d("debug", "m is null!");
        }

        Log.d("debug", "succeed?");
    }

    /*
     * Adds capture points to map
     */
    public void populateCapturePoints(ArrayList<CapturePoint> capturePoints)
    {

    }
}
