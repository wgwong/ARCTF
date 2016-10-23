package com.demo.arctf.arctfdemo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by David on 10/21/2016.
 */
public class CapturePoint {
    public enum State{
        RED, BLUE, NEUTRAL
    }
    private String name;
    private LatLng location;
    private State currentState;

    public CapturePoint(LatLng loc, String pointName){
        this.name = pointName;
        this.location = loc;
        this.currentState = State.NEUTRAL;
    }

    public State getState()
    {
        return currentState;
    }

    public LatLng getLocation(){
        return location;
    }

    public String getName(){
        return name;
    }

    public void setState(State state)
    {
        currentState = state;
    }
}
