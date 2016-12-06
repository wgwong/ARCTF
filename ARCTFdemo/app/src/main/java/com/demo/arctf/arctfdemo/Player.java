package com.demo.arctf.arctfdemo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by David on 10/29/2016.
 */
public class Player {

    private String name;
    private CapturePoint.State team;
    private LatLng location;
    public Player(String username, CapturePoint.State state, LatLng loc) {
        name = username;
        team = state;
        location = loc;
    }

    public String getName(){
        return name;
    }

    public CapturePoint.State getTeam()
    {
        return team;
    }

    public LatLng getLocation()
    {
        return location;
    }
}
