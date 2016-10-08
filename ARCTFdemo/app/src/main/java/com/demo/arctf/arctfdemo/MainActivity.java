package com.demo.arctf.arctfdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /*public void connectToServer(View view) {
        Log.d("debug", "connect to server called");

        NetworkClient m = (NetworkClient) getSupportFragmentManager().findFragmentById(R.id.main);

        if (m != null) {
            m.establishSession();
        } else {
            Log.d("debug", "m is null!");
        }

        Log.d("debug", "succeed?");
    }*/
}



