package com.demo.arctf.arctfdemo;

import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class PlayerMap extends com.google.android.gms.maps.SupportMapFragment implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Capture Range in Meters
    public static final float CAPTURE_RANGE = 9000;//30;
    public static final String TAG = PlayerMap.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mLastLocation;
    private String username;
    private HashMap<Marker, String> capturePointMarkers;
    private HashMap<String, Marker> capturePointColorMarkers;
    private ArrayList<CapturePoint> capturePointArray;

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
        capturePointMarkers = new HashMap<Marker, String>();
        capturePointColorMarkers = new HashMap<String, Marker>();
        capturePointArray = new ArrayList<CapturePoint>();
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval( 1000);
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
        icon = getPlayerIcon();
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
            icon = getPlayerIcon();
            playerLocation = mMap.addMarker(new MarkerOptions().position(newLoc).title(username)
                    .icon(icon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLoc,16));
        }
    }

    public BitmapDescriptor getPlayerIcon()
    {
        BitmapDescriptor markerIcon;
        markerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.player_blue_you);
        return markerIcon;
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
        if(!capturePointArray.isEmpty())
        {
            Log.d("debug", "Icon in OnMapReady");
            for (CapturePoint point : capturePointArray) {
                //capturePointList.add(point);
                BitmapDescriptor icon;
                BitmapDescriptor icon2;
                CapturePoint.State team = point.getState();
                icon = BitmapDescriptorFactory.fromResource(R.mipmap.neutral_capture_point);
                icon2 = BitmapDescriptorFactory.fromResource(R.mipmap.blue_capture_point);
                Marker captureMarker = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                        icon(icon)
                        .title(point.getName()));
                Marker captureMarkerColor = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                        icon(icon2)
                        .title(point.getName()));
                captureMarkerColor.setVisible(false);
                capturePointMarkers.put(captureMarker, point.getName());
                capturePointColorMarkers.put(point.getName(), captureMarkerColor);

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
            Log.d("drawIcon", "Drawing Icons in populate.");
            for (CapturePoint point : capturePointList) {
                //capturePointList.add(point);
                BitmapDescriptor icon;
                BitmapDescriptor icon2;
                CapturePoint.State team = point.getState();
                icon = BitmapDescriptorFactory.fromResource(R.mipmap.neutral_capture_point);
                icon2 = BitmapDescriptorFactory.fromResource(R.mipmap.blue_capture_point);
                Marker captureMarker = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                        icon(icon)
                        .title(point.getName()));
                Marker captureMarkerColor = mMap.addMarker(new MarkerOptions().position(point.getLocation()).
                        icon(icon2)
                        .title(point.getName()));
                captureMarkerColor.setVisible(false);
                capturePointMarkers.put(captureMarker, point.getName());
                capturePointColorMarkers.put(point.getName(), captureMarkerColor);

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

    public boolean onMarkerClick(Marker marker) {
        //TODO(david): IMPORTANT - Multiple clicks should not send a new capture request if it is already going
        Log.d("debug", "On Marker Click Called");
        if(capturePointMarkers.keySet().contains(marker))
        {
            // Send message to server requesting capture
            LatLng pointLatLng = marker.getPosition();

            // TODO(david): Calculate distance between points

            if(networkHandler != null) {
                    if (inCaptureRange(pointLatLng)) {
                        // Send the capture message
                        marker.setVisible(false);
                        String pointName = capturePointMarkers.get(marker);
                        capturePointColorMarkers.get(pointName).setVisible(true);
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Digging...", Toast.LENGTH_LONG).show();
                        networkHandler.checkForTreasure(capturePointMarkers.get(marker), username);
                    }
                    else
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Get closer to check this point!", Toast.LENGTH_LONG).show();
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
