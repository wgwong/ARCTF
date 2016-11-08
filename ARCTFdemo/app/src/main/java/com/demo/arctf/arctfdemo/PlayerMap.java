package com.demo.arctf.arctfdemo;

import android.app.Fragment;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class PlayerMap extends com.google.android.gms.maps.SupportMapFragment implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Capture Range in Meters
    public static final float CAPTURE_RANGE = 50;
    public static final String TAG = PlayerMap.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mLastLocation;
    private String username;
    private CapturePoint.State team;
    private HashMap<Marker, CapturePoint> capturePoints;
    private HashMap<String, Marker> capturePointMarkers;
    private ArrayList<CapturePoint> capturePointArray;
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
        playerMarkers = new ConcurrentLinkedQueue<Marker>();
        capturePoints = new HashMap<Marker, CapturePoint>();
        capturePointMarkers = new HashMap<String, Marker>();
        capturePointArray = new ArrayList<CapturePoint>();
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
            //int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

            /*
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                //Execute location service call if user has explicitly granted ACCESS_FINE_LOCATION..
            }*/

            Random rn = new Random();
            int requestCode = rn.nextInt(65535);


            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);

            Location location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            if (location == null) {
                //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                Log.d("debug", "location is null"); //debug
            }
            else {
                Log.d("debug", "location is not null");
                Log.d("debug", location.toString());
                handleFirstLocation(location);
            };
        } catch (SecurityException s) {
            //TODO: Handle Security Exception
            Log.d("debug", "security exception: " + s.toString());
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
        BitmapDescriptor icon;
        if(team != null)
        {
            icon = getMarkerIcon(team,false);
        }
        else{
            icon = getMarkerIcon(CapturePoint.State.BLUE, false);
        }
         playerLocation = mMap.addMarker(new MarkerOptions().position(newLoc)
                 .icon(icon).title(username));
         //mMap.moveCamera(CameraUpdateFactory.newLatLng(newLoc));
        }
    }

    private void handleFirstLocation(Location location) {
        Log.d("debug", "handleFirstLocation called");
        Log.d(TAG, location.toString());
        Log.i("Info", "Updating First Location");
        LatLng newLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mLastLocation = newLoc;
        if(mMap != null) {
            if(playerLocation!=null)
                playerLocation.remove();
            BitmapDescriptor icon;
            if(team != null)
            {
                icon = getMarkerIcon(team,false);
            }
            else{
                icon = getMarkerIcon(CapturePoint.State.BLUE, false);
            }
            playerLocation = mMap.addMarker(new MarkerOptions().position(newLoc).title(username)
                    .icon(icon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc,16));
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
        if(capturePoints.isEmpty())
        {
            for (CapturePoint point : capturePointArray) {
                //capturePointList.add(point);
                BitmapDescriptor icon;
                CapturePoint.State team = point.getState();
                icon = getMarkerIcon(team, true);
                Marker captureMarker = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                        icon(icon)
                        .title(point.getName()));
                capturePoints.put(captureMarker, point);
                capturePointMarkers.put(point.getName(), captureMarker);
                Log.d("debug", capturePoints.toString());

            }
        }
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

    /**
     * Updates color of capture points
     */
    public void updateGameState(ArrayList<CapturePoint> capturePointList)
    {
        Log.d("debug", "Update Game State");
        for(CapturePoint point: capturePointList)
        {
            String name = point.getName();
            Log.d("debug", name);

            if (capturePointMarkers.keySet().contains(name))
            {
                Marker tempMarker = capturePointMarkers.get(name);
                CapturePoint tempPoint = capturePoints.get(tempMarker);
                tempMarker.remove();
                tempPoint.setState(point.getState());
                BitmapDescriptor icon = getMarkerIcon(point.getState(), true);
                                tempMarker = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                        icon(icon)
                        .title(point.getName()));
                capturePoints.put(tempMarker, tempPoint);
                capturePointMarkers.put(name, tempMarker);
                Log.d("marker", "New Marker Color contained: " + capturePoints.keySet().contains(tempMarker));
            }
        }
    }
    /*
     * Adds capture points to map
     */
    public void populateCapturePoints(ArrayList<CapturePoint> capturePointList)
    {
        Log.d("debug", "Capture Points Received");
        for (CapturePoint point : capturePointList) {
            capturePointArray.add(point);
        }
        if (mMap != null) {
            for (CapturePoint point : capturePointList) {
                //capturePointList.add(point);
                BitmapDescriptor icon;
                CapturePoint.State team = point.getState();
                icon = getMarkerIcon(team, true);
                Marker captureMarker = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                        icon(icon)
                        .title(point.getName()));
                capturePoints.put(captureMarker, point);
                capturePointMarkers.put(point.getName(), captureMarker);
                Log.d("debug", capturePoints.toString());

            }
        }
    }

    // Set team of player
    public void setTeam(CapturePoint.State teamColor)
    {
        team = teamColor;
    }
    private void clearOtherPlayerMarkers()
    {
        if (playerMarkers != null) {
            Marker marker = playerMarkers.poll();
            while (marker != null) {
                marker.remove();
                marker = playerMarkers.poll();
            }
        }
    }

    private BitmapDescriptor getMarkerIcon(CapturePoint.State team, Boolean isCapturePoint)
    {
        BitmapDescriptor markerIcon;
        if(isCapturePoint) {
            if (team == CapturePoint.State.BLUE)
                markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.blue_capture_point);
            else if (team == CapturePoint.State.RED)
                markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.orange_capture_point);
            else
                markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.neutral_capture_point);
            return markerIcon;
        }
        else {
            if (team == CapturePoint.State.BLUE)
                markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.player_blue_other);
            else
                markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.player_orange_other);
            return markerIcon;
        }
    }

    public void updateMap(HashMap<String, ArrayList<LatLng>> playerLocations,
                          HashMap<String,CapturePoint.State> playerTeams)
    {
        Log.d("Update", "Updating Map");
        clearOtherPlayerMarkers();
        for(String name: playerLocations.keySet())
        {
            if (!name.equals(username))
            {
                ArrayList<LatLng> locationArray = playerLocations.get(name);
                if(locationArray.size() >0) {
                    LatLng lastLocation = locationArray.get(locationArray.size() - 1);
                    BitmapDescriptor icon;
                    CapturePoint.State team = playerTeams.get(name);
                    icon = getMarkerIcon(team, false);
                    Marker loc = mMap.addMarker(new MarkerOptions().position(lastLocation)
                            .icon(icon).title(name));
                    playerMarkers.add(loc);
                }
            }
        }
    }

    public void setNetworkHandler(NetworkHandler handler)
    {
        networkHandler = handler;
    }

    public boolean inCaptureRange(LatLng captureLatLng){
        Location captureLocation = new Location("");
        captureLocation.setLatitude(captureLatLng.latitude);
        captureLocation.setLongitude(captureLatLng.longitude);

        Location playerLocation = new Location("");
        playerLocation.setLatitude(mLastLocation.latitude);
        playerLocation.setLongitude(mLastLocation.longitude);

        float distanceInMeters = playerLocation.distanceTo(captureLocation);
        Log.d("debug", "Distance from point: " + distanceInMeters);
        if(distanceInMeters <= CAPTURE_RANGE)
        {
            return true;
        }
        return false;
    }

    public boolean alreadyCaptured(String pointName)
    {
        Log.d("EndCaptureRequest", "Checking if already captured");
        Marker pointMarker = capturePointMarkers.get(pointName);
        CapturePoint.State owner = capturePoints.get(pointMarker).getState();
        Log.d("EndCaptureRequest", owner.toString());
        Log.d("EndCaptureRequest", team.toString());
        if (owner.equals(team))
        {
            Log.d("EndCaptureRequest", "Owner = team");
            return true;
        }
        return false;
    }
    public boolean onMarkerClick(Marker marker) {
        //TODO(david): IMPORTANT - Multiple clicks should not send a new capture request if it is already going
        Log.d("debug", "On Marker Click Called");
        if(capturePoints.keySet().contains(marker))
        {
            // Send message to server requesting capture
            LatLng pointLatLng = capturePoints.get(marker).getLocation();

            // TODO(david): Calculate distance between points

            if(networkHandler != null) {
                    if (inCaptureRange(pointLatLng)) {
                        // Send the capture message
                        Log.d("MarkerCapture", "Capturing point in On Marker Click");
                        networkHandler.capturePoint(username, capturePoints.get(marker).getName(), pointLatLng);
                    }
                    else
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Too far for capture", Toast.LENGTH_LONG).show();
                    }
            }
            else
            {
                throw new NullPointerException("Network Handler is undefined in PlayerMap");
            }
        }
        Log.d("debug", "Finish capture");
        return true;
    }

}
