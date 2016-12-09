package com.demo.arctf.arctfdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
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
        mSocket.on("gameStatusPopulate", receiveCapturePoints);
        mSocket.on("wrongPoint", wrongPoint);
        mSocket.on("gameStart", gameStart);
        mSocket.on("win", foundTreasure);
        mSocket.on("gameOver", endGame);
        //mSocket.on("scoreUpdate", updateDigCount);
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


    private Emitter.Listener wrongPoint = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String pointName = (String) args[0];
                    //final String distanceMessage = (String) args[1];
                    final Integer state = (Integer) args[1];
                    // close
                    String distanceMessage = "unable to detect";
                    if (state == 5) {
                        distanceMessage = "really far";
                    } else if (state == 4) {
                        distanceMessage = "far";
                    } else if (state == 3) {
                        distanceMessage = "moderately far";
                    } else if (state==2) {
                        distanceMessage = "close";
                    }else if (state==1) {
                        distanceMessage = "nearby";
                    }
                    final String message = distanceMessage;
                    Log.d("debug", "point is: " + pointName.toString());
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "The treasure is " + message + ".", Toast.LENGTH_LONG).show();
                            NetworkHandler networkHandler = (NetworkHandler) getActivity();
                            networkHandler.setPointColor(pointName,state);
                        }
                    }, 1000);

                }
            });

        }
    };

    private Emitter.Listener foundTreasure = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //final String username = (String) args[0];
                    getActivity().runOnUiThread(new Runnable() {
                        Integer treasureCount = (Integer) args[1];
                        String pointName = (String) args[2];
                        @Override
                        public void run() {
                            final NetworkHandler networkHandler = (NetworkHandler) getActivity();
                            networkHandler.setPointColor(pointName,6);
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Congratulations! Your team has found the treasure.", Toast.LENGTH_LONG).show();
                            EditText scoreText =(EditText) getActivity().findViewById(R.id.team_score);
                            scoreText.setText("" + treasureCount);
                            // pause game here?
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "A new treasure has been detected. Find it before the time runs out!", Toast.LENGTH_LONG).show();
                                    networkHandler.resetCapturePoints();
                                }
                            }, 2000);
                        }
                    });


                }
            });
        }
    };

    private Emitter.Listener endGame = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //final String username = (String) args[0];
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Integer treasureCount = (Integer) args[0];
                            NetworkHandler networkHandler = (NetworkHandler) getActivity();
                            networkHandler.resetCapturePoints();
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Your final score is " + treasureCount + " treasure found.", Toast.LENGTH_LONG).show();
                            /*Intent intent = new Intent(networkHandler,MainActivity.class);
                            intent.putExtra("com.demo.arctf.arctfdemo.NetworkHandler.SCORE", treasureCount);*/

                            /*startActivity(intent);*/
                        }
                    });
                }
            });
        }
    };

    private Emitter.Listener gameStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Integer timeLeft = (Integer) args[0];

                    //TODO: Need to have a start screen - then game start changes the screen
                    //TODO: Remove start button
                    ImageButton startGameButton = (ImageButton) getActivity().findViewById(R.id.start_game_button);
                    ViewGroup layout = (ViewGroup) startGameButton.getParent();
                    if(null!=layout) //for safety only  as you are doing onClick
                        layout.removeView(startGameButton);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Game Start.", Toast.LENGTH_LONG).show();
                        }
                    });
                    NetworkHandler networkHandler = (NetworkHandler) getActivity();
                    networkHandler.setGameStarted();
                    new CountDownTimer(timeLeft, 1000) {

                    public void onTick(long millisUntilFinished) {
                    EditText gameTimeText =(EditText) getActivity().findViewById(R.id.Game_Time);
                    long secondsLeft = millisUntilFinished/1000;
                    long minutesLeft = secondsLeft/60;
                    long secondsRemaining = secondsLeft%60;
                    if(secondsRemaining <10)
                            gameTimeText.setText(""+minutesLeft+":0"+secondsRemaining);
                    else
                        gameTimeText.setText(""+minutesLeft+":"+secondsRemaining);
                }

                    public void onFinish() {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Time's up.", Toast.LENGTH_LONG).show();
                    }
                    }.start();

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


    public void establishSession(String username) {
        Log.d("debug", "establish session called");
        if (mSocket == null) {
            Log.d("debug", "why is msocket null when establishsession is called");
        }
        mSocket.emit("session", username);
        Log.d("debug", "establish session: emitted message");
    }


    public void checkForTreasure(String pointName, String username) {
        Log.d("debug", "capture point called");
        if (mSocket == null) {
            Log.d("debug", "why is msocket null when updatelocation");
        }

        mSocket.emit("check", pointName, username);
        Log.d("debug", "capture message sent");
    }

    public void gameStart() {
        Log.d("debug", "game start");
        if (mSocket == null) {
            Log.d("debug", "why is msocket null when updatelocation");
        }

        mSocket.emit("gameStart");
    }
}
