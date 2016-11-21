package com.demo.arctf.arctfdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView img= (ImageView) findViewById(R.id.Towers_Controlled);
        img.setImageResource(R.drawable.blue_capture_point-web);
        ImageView img= (ImageView) findViewById(R.id.Opponent_Towers_Controlled);
        img.setImageResource(R.drawable.orange_capture_point-web);
        chronometer = (Chronometer) findViewById(R.id.Game_Time);
        chronometer.setBase(300000);
        chronometer.start();
    }

}



