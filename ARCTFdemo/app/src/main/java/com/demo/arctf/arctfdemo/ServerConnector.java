package com.demo.arctf.arctfdemo;

/**
 * Created by wgwong on 10/6/2016.
 */
import android.app.Application;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ServerConnector extends Application{

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.SERVER_URL);


            Log.d("debug", "success init socket without oncreate");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {

        Log.d("debug", "getting socket from server connector");
        return mSocket;
    }

}
