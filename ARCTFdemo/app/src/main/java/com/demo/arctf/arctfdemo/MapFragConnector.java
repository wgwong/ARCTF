package com.demo.arctf.arctfdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wgwong on 10/8/2016.
 */

public class MapFragConnector extends Fragment {

    private PlayerMap mapFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mapfrag, container, false);

        mapFragment = new PlayerMap();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map, mapFragment).commit();

        return rootView;
    }

    public void setUsername(String username)
    {
        mapFragment.setUsername(username);
    }
    public LatLng getLastKnownLatLng()
    {
        return mapFragment.getLastKnownLatLng();
    }

    public void updateMap(HashMap<String, ArrayList<LatLng>> playerLocations)
    {
        mapFragment.updateMap(playerLocations);
    }
}
