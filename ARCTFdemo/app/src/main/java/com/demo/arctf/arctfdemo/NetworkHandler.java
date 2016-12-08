package com.demo.arctf.arctfdemo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by David on 10/8/2016.
 */
public class NetworkHandler extends AppCompatActivity {
    NetworkClient networkClient;
    String uniqueID;
    MapFragConnector playerMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sending reference to PlayerMap
        setContentView(R.layout.activity_frame);
        uniqueID = UUID.randomUUID().toString();
        connectToServer();
        Log.d("debug", "networkhandler oncreate called");

        playerMap = (MapFragConnector) getSupportFragmentManager().findFragmentById(R.id.map);
        playerMap.setUsername(uniqueID);
        playerMap.setNetworkHandler(this);
        /* TODO(david): Async Calls - call handler after address is received and map is created
            must change id from mac address
         */
        Button startGameButton = (Button) findViewById(R.id.start_game_button);
        startGameButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                networkClient.gameStart();

            }
        });
    }


    public void checkForTreasure(String pointName, String username) {
        networkClient.checkForTreasure(pointName, username);
    }


    public void connectToServer() {
        Log.d("debug", "connect to server called");

        networkClient = (NetworkClient) getSupportFragmentManager().findFragmentById(R.id.main);

        if (networkClient != null) {
            networkClient.establishSession(uniqueID);
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
        playerMap.populateCapturePoints(capturePoints);
    }

    public void resetCapturePoints()
    {
        playerMap.resetCapturePoints();
    }

    public void setPointColor(String name, Integer state)
    {
        playerMap.setPointColor(name, state);
    }

    public void setGameStarted()
    {
        playerMap.setGameStarted();
    }
}
