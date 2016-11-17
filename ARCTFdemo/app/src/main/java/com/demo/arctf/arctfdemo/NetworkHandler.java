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
    public void updateMap(HashMap<String, ArrayList<LatLng>> playerLocations,
                          HashMap<String,CapturePoint.State> playerTeams)
    {
        playerMap.updateMap(playerLocations, playerTeams);
    }

    private void updateServer()
    {
        JSONObject jsonObj = new JSONObject();
        LatLng curLoc = playerMap.getLastKnownLatLng();
        if(curLoc !=null) {
            try {
                jsonObj.put("player", uniqueID);
                jsonObj.put("latitude", curLoc.latitude);
                jsonObj.put("longitude", curLoc.longitude);
                Log.d("Message", "Sending Location to Server");
                networkClient.updateLocation(jsonObj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void capturePoint(final String username, final String pointName,
                             final LatLng captureLocation) {


        //TODO: Figure this out
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Handler", "Calling Update Server");
                if(playerMap.inCaptureRange(captureLocation) /*&& !playerMap.alreadyCaptured(pointName)*/) {
                    sendCaptureRequest(username, pointName);
                    handler.postDelayed(this, 1000);
                }
                else {
                    sendLeavePoint(username, pointName);
                }
            }
        }, 1000);



    }
    private void sendCaptureRequest(String username, String pointName)
    {
        JSONObject jsonObj = new JSONObject();
        Log.d("debug", "INSIDE CAPTURE POINT NETWORK HANDLER");
        try {
            jsonObj.put("player", username);
            jsonObj.put("capturePoint", pointName);
            Log.d("debug", "Sending Capture Request Server");
            networkClient.capturePoint(jsonObj);
        } catch (JSONException ex) {
            Log.d("debug", "Capture Request Exception");
            ex.printStackTrace();
        }
    }

    private void sendLeavePoint(String username, String pointName)
    {
        JSONObject jsonObj = new JSONObject();
        Log.d("debug", "INSIDE CAPTURE POINT NETWORK HANDLER");
        try {
            jsonObj.put("player", username);
            jsonObj.put("capturePoint", pointName);
            Log.d("debug", "Sending Capture Request Server");
            networkClient.leavePoint(jsonObj);
        } catch (JSONException ex) {
            Log.d("debug", "Capture Request Exception");
            ex.printStackTrace();
        }
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

    public void updateGameState(ArrayList<CapturePoint> capturePointList)
    {
        playerMap.updateGameState(capturePointList);
    }

    public void setTeam(String team) {
        //TODO: In the future handle other cases by throwing an error
        CapturePoint.State playerTeam;
        if (team.equals("blue"))
            playerTeam = CapturePoint.State.BLUE;
        else
            playerTeam = CapturePoint.State.RED;
        playerMap.setTeam(playerTeam);
    }

    public void updateCaptureStatus(String pointName, String capturingTeam, Integer redCount, Integer blueCount,
                                    Double timestep){
        playerMap.updateCaptureStatus(pointName, capturingTeam, redCount, blueCount, timestep.intValue());
    }
}
