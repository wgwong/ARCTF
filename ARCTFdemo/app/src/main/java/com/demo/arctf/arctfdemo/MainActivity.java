package com.demo.arctf.arctfdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Intent intent = getIntent();
        EditText finalScore = (EditText) findViewById(R.id.finalMessage);
        String message = intent.getStringExtra("com.demo.arctf.arctfdemo.NetworkHandler.SCORE");
        finalScore.setText("You found " + message + " treasures!");

    }

}



