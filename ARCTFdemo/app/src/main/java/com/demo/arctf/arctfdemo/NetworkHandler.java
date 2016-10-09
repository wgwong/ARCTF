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

/**
 * Created by David on 10/8/2016.
 */
public class NetworkHandler extends AppCompatActivity {
    NetworkClient networkClient;
    String macAddress;
    PlayerMap playerMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectToServer();
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();
        playerMap = (PlayerMap) getSupportFragmentManager().findFragmentById(R.id.map);

        // Start update loop
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 20 seconds
                updateServer();
                handler.postDelayed(this, 10000);
            }
        }, 200000);
    }

    /**
     * @param playerLocations list of latitude and longitude of each player
     */
    public void updateMap(ArrayList<LatLng> playerLocations)
    {
        // TODO(david): Update to take player names as well
        playerMap.updateMap(playerLocations);
    }

    private void updateServer()
    {
        JSONObject jsonObj = new JSONObject();
        LatLng curLoc = playerMap.getLastKnownLatLng();
        try {
            jsonObj.put("address", macAddress);
            jsonObj.put("latitude", curLoc.latitude);
            jsonObj.put("longitude", curLoc.longitude);
        }
        catch(JSONException ex) {
            ex.printStackTrace();
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
}
