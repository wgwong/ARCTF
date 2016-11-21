package com.demo.arctf.arctfdemo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Use the {@link NetworkClient#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NetworkClient extends Fragment {

    private String mUsername;
    private Socket mSocket;

    private Boolean isConnected = true;


    public NetworkClient() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NetworkClient.
     */
    // TODO: Rename and change types and number of parameters
    public static NetworkClient newInstance() {
        NetworkClient fragment = new NetworkClient();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("debug","NetworkClient frag oncreate called");

        ServerConnector app = (ServerConnector) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("session", onEstablishSession);
        mSocket.on("playerUpdate_confirm", onUpdate);
        mSocket.on("gameStatusPopulate", receiveCapturePoints);
        mSocket.on("gameStatusUpdate", updateGameStatus);
        mSocket.on("scoreUpdate", updateScore);
        mSocket.on("teamPopulate", setTeam);
        mSocket.on("underCaptureUpdate", updateCaptureStatus);
        mSocket.connect();

        Log.d("debug", "mf oncreate finish");
        if (mSocket == null) {
            Log.d("debug", "but msocket still null?!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("session", onEstablishSession);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_client, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        if(null!=mUsername)
                            mSocket.emit("add user", mUsername);
                        Toast.makeText(getActivity().getApplicationContext(),
                                /*R.string.connect*/ "connect", Toast.LENGTH_LONG).show();
                        isConnected = true;
                    }
                }
            });
        }
    };


    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    Toast.makeText(getActivity().getApplicationContext(),
                            /*R.string.disconnect*/ "disconnect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            Log.d("debug", "error args");
            for (int i = 0; i < args.length; i++) {
                Log.d("debug", args[i].toString());
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            /*R.string.error_connect*/ "error connect", Toast.LENGTH_LONG).show();


                }
            });
        }
    };

    private Emitter.Listener onEstablishSession = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String) args[0];
                    Log.d("debug", "data is: " + data.toString());
                }
            });
        }
    };

    private Emitter.Listener setTeam = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String) args[0];
                    final NetworkHandler networkHandler = (NetworkHandler) getActivity();
                    networkHandler.setTeam(data);

                    new CountDownTimer(300000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            EditText gameTimeText =(EditText) networkHandler.findViewById(R.id.Game_Time);
                            long secondsLeft = millisUntilFinished/1000;
                            long minutesLeft = secondsLeft/60;
                            long secondsRemaining = secondsLeft%60;
                            if(secondsRemaining <10)
                                gameTimeText.setText(""+minutesLeft+":0"+secondsRemaining);
                            else
                                gameTimeText.setText(""+minutesLeft+":"+secondsRemaining);
                        }

                        public void onFinish() {
                        }
                    }.start();
                }
            });
        }
    };
    //TODO(david): update score json
    private Emitter.Listener updateScore = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("JSON", args[0].toString());
                    JSONObject data = (JSONObject) args[0];
                    String blue = "blue";
                    String red = "red";
                    try {
                        Integer redScore = (Integer) data.get(red);
                        Integer blueScore = (Integer) data.get(blue);
                        EditText blueScoreText = (EditText)getActivity().findViewById(R.id.Towers_Controlled);
                        EditText redScoreText = (EditText)getActivity().findViewById(R.id.Opponent_Towers_Controlled);
                        blueScoreText.setText(""+blueScore);
                        redScoreText.setText(""+redScore);

                        Toast.makeText(getActivity().getApplicationContext(),
                            "Blue Score: " + blueScore + " Red Score: " + redScore, Toast.LENGTH_LONG).show();
                    }
                    catch(JSONException ex){
                        ex.printStackTrace();
                    }

                }
            });
        }
    };

    private Emitter.Listener updateCaptureStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CaptureUpdate", "UpdateCaptureStatus");
                    String pointName = args[0].toString();
                    String startedBy = args[1].toString();
                    Integer redCount = Integer.parseInt(args[2].toString());
                    Integer blueCount = Integer.parseInt(args[3].toString());
                    Double timestep = Double.parseDouble(args[4].toString());
                    NetworkHandler networkHandler = (NetworkHandler) getActivity();
                    networkHandler.updateCaptureStatus(pointName, startedBy,redCount,blueCount,timestep);

                }
            });
        }
    };


    private Emitter.Listener onUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Update", "RECEIVING PLAYER LOCATIONS");
                    Log.d("JSON", args[0].toString());
                    NetworkHandler networkHandler = (NetworkHandler) getActivity();
                    JSONObject data = (JSONObject) args[0];
                    Iterator<String> keys = data.keys();
                    HashMap<String,ArrayList<LatLng>> playerLocations = new HashMap<String, ArrayList<LatLng>>();
                    HashMap<String,CapturePoint.State> playerTeams = new HashMap<String,CapturePoint.State>();
                    while( keys.hasNext() ) {
                        String key = (String)keys.next();
                        try
                        {
                            JSONObject player = (JSONObject) data.get(key);
                            JSONArray coordinates = (JSONArray) player.get("coordinates");
                            ArrayList<LatLng> locations = new ArrayList<LatLng>();
                            // TODO(david): Handle team colors - player.get("team")
                            String color =(String) player.get("team");
                            for(int i=0; i < coordinates.length(); i++)
                            {
                                JSONArray latlng = (JSONArray) coordinates.get(i);
                                // TODO(david): Handle null case
                                //if(latlng.get(0) == null || latlng.get(1) == null)
                                locations.add(i,new LatLng((double)latlng.get(0),(double)latlng.get(1)) );
                            }
                            playerLocations.put(key, locations);
                            CapturePoint.State team = CapturePoint.State.NEUTRAL;
                            if(color.equals("blue"))
                                team = CapturePoint.State.BLUE;
                            else if(color.equals("red"))
                                team = CapturePoint.State.RED;
                            playerTeams.put(key, team);
                        }
                        catch(JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    networkHandler.updateMap(playerLocations, playerTeams);
                   // {"undefined":{"name":"test","team":"gray","coordinates":[[42.357999,-71.0933371],

                }
            });
        }
    };

    private Emitter.Listener receiveCapturePoints = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("debug", "receiving capture points");
                    Log.d("JSON", args[0].toString());
                    NetworkHandler networkHandler = (NetworkHandler) getActivity();
                    JSONObject data = (JSONObject) args[0];
                    Iterator<String> keys = data.keys();
                    ArrayList<CapturePoint> capturePoints = new ArrayList<CapturePoint>();
                    while( keys.hasNext() ) {
                        String key = (String)keys.next();
                        try
                        {
                            JSONObject point = (JSONObject) data.get(key);
                            String owner = point.get("ownedBy").toString();
                            CapturePoint.State currentState = CapturePoint.State.NEUTRAL;
                            if(owner.equals("blue"))
                                currentState = CapturePoint.State.BLUE;
                            else if (owner.equals("red"))
                                currentState = CapturePoint.State.RED;
                            JSONArray coordinates = (JSONArray) point.get("coordinates");
                            LatLng latlng = new LatLng((double)coordinates.get(0),(double)coordinates.get(1));

                            CapturePoint pt = new CapturePoint(latlng, key);
                            pt.setState(currentState);
                            capturePoints.add(pt);
                        }
                        catch(JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    // SEND list of points to network handler which has function to update map
                    networkHandler.populateCapturePoints(capturePoints);

                }
            });
        }
    };

    private Emitter.Listener updateGameStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("debug", "receiving capture points");
                    Log.d("JSON", args[0].toString());
                    NetworkHandler networkHandler = (NetworkHandler) getActivity();
                    String pointName = args[0].toString();
                    JSONObject point = (JSONObject) args[1];
                    ArrayList<CapturePoint> capturePoints = new ArrayList<CapturePoint>();
                    try
                        {
                            String owner = point.get("ownedBy").toString();
                            Log.d("debug", "Owned by "+ owner);
                            CapturePoint.State currentState = CapturePoint.State.NEUTRAL;
                            if(owner.equals("blue"))
                                currentState = CapturePoint.State.BLUE;
                            else if (owner.equals("red"))
                                currentState = CapturePoint.State.RED;
                            JSONArray coordinates = (JSONArray) point.get("coordinates");
                            LatLng latlng = new LatLng((double)coordinates.get(0),(double)coordinates.get(1));

                            CapturePoint pt = new CapturePoint(latlng, pointName);
                            pt.setState(currentState);
                            capturePoints.add(pt);
                        }
                        catch(JSONException ex){
                            ex.printStackTrace();
                        }

                    // SEND list of points to network handler which has function to update map
                    // TODO(david): call new function to change capture point colors
                    networkHandler.updateGameState(capturePoints);

                }
            });
        }
    };



    public void establishSession(String username) {
        Log.d("debug", "establish session called");
        if (mSocket == null) {
            Log.d("debug", "why is msocket null when establishsession is called");
        }
        mSocket.emit("session", username);
        Log.d("debug", "establish session: emitted message");
    }

    public void updateLocation(JSONObject playerLoc) {
        Log.d("debug", "update location called");
        if (mSocket == null) {
            Log.d("debug", "why is msocket null when updatelocation");
        }
        mSocket.emit("playerUpdate", playerLoc);
        Log.d("debug", "playerUpdate sent");
    }

    public void capturePoint(JSONObject captureRequest) {
        Log.d("debug", "capture point called");
        if (mSocket == null) {
            Log.d("debug", "why is msocket null when updatelocation");
        }

        mSocket.emit("capture", captureRequest);
        Log.d("debug", "capture message sent");
    }

    public void leavePoint(JSONObject leavePointMessage) {
        Log.d("debug", "leave point called");
        if (mSocket == null) {
            Log.d("debug", "why is msocket null when updatelocation");
        }

        mSocket.emit("leavePoint", leavePointMessage);
        Log.d("debug", "leave message sent");
    }

}
