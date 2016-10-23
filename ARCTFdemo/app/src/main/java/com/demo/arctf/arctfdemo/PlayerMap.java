package com.demo.arctf.arctfdemo;

import android.app.Fragment;
import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class PlayerMap extends com.google.android.gms.maps.SupportMapFragment implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String TAG = PlayerMap.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mLastLocation;
    private String username;
    private CapturePoint.State team;
    private HashMap<Marker, CapturePoint> capturePoints;
    private Queue<Marker> playerMarkers;
    private Marker playerLocation;
    private NetworkHandler networkHandler;
    LocationRequest mLocationRequest;
    LocationSettingsRequest.Builder builder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_player_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        try {
           Location location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            if (location == null) {
                //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else {
                handleFirstLocation(location);
            };
        } catch (SecurityException s) {
            //TODO: Handle Security Exception
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        Log.i("Info", "Updating Location");
        LatLng newLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mLastLocation = newLoc;
        if(mMap != null) {
         if (playerLocation != null) {
             playerLocation.remove();
         }
         playerLocation = mMap.addMarker(new MarkerOptions().position(newLoc).title(username));
         mMap.moveCamera(CameraUpdateFactory.newLatLng(newLoc));
        }
    }

    private void handleFirstLocation(Location location) {
        Log.d(TAG, location.toString());
        Log.i("Info", "Updating First Location");
        LatLng newLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mLastLocation = newLoc;
        if(mMap != null) {
            if(playerLocation!=null)
                playerLocation.remove();
            playerLocation = mMap.addMarker(new MarkerOptions().position(newLoc).title(username));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc,14));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public void onStart(){
        Log.i("Test", "Starting...");
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        Log.d(TAG, "Map is Ready");
        mMap = map;
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location is being changed.");
        handleNewLocation(location);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }
    public void setUsername(String name)
    {
        username = name;
    }
    public LatLng getLastKnownLatLng()
    {
        return mLastLocation;
    }


    /*
     * Adds capture points to map
     */
    public void populateCapturePoints(ArrayList<CapturePoint> capturePointList)
    {
        for(CapturePoint point: capturePointList)
        {
            //capturePointList.add(point);
            Marker captureMarker = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(point.getName()));
            capturePoints.put(captureMarker, point);
            Log.d("debug", capturePoints.toString());
        }
    }

    // Set team of player
    public void setTeam(CapturePoint.State teamColor)
    {
        team = teamColor;
    }
    private void clearOtherPlayerMarkers()
    {
        Marker marker = playerMarkers.poll();
        while(marker != null)
        {
            marker.remove();
            marker = playerMarkers.poll();
        }
    }
    public void updateMap(HashMap<String, ArrayList<LatLng>> playerLocations)
    {
        Log.d("Update", "Updating Map");
        clearOtherPlayerMarkers();
        for(String name: playerLocations.keySet())
        {
            if (name != username)
            {
                ArrayList<LatLng> locationArray = playerLocations.get(name);
                LatLng lastLocation = locationArray.get(locationArray.size()-1);
                Marker loc = mMap.addMarker(new MarkerOptions().position(lastLocation).title(name));
                playerMarkers.add(loc);
            }
        }
    }

    public void setNetworkHandler(NetworkHandler handler)
    {
        networkHandler = handler;
    }
    public boolean onMarkerClick(Marker marker) {

        // TODO(david): register listener
        if(capturePoints.keySet().contains(marker))
        {
            // Send message to server requesting capture
            LatLng pointLocation = capturePoints.get(marker).getLocation();
            float distance[] = null;
            Location.distanceBetween(mLastLocation.latitude, pointLocation.longitude,
                    mLastLocation.latitude, pointLocation.longitude, distance);
            if(networkHandler != null) {
                if (distance != null) {
                    if (distance[0] < 50) {
                        // Send the capture message
                        networkHandler.capturePoint(username, capturePoints.get(marker).getName());
                    }

                }
            }
            else
            {
                throw new NullPointerException("Network Handler is undefined in PlayerMap");
            }
        }
        return false;
    }

    public void capturePoint(CapturePoint.State state)
    {
        // Set state of capture point to team who has captured
    }
}
