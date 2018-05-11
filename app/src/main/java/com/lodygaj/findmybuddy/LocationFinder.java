package com.lodygaj.findmybuddy;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created on 6/8/2016.
 */
public class LocationFinder implements LocationListener {
    private Context context;
    private Location location;
    private LocationManager locManager;
    private double latitude, longitude;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;

    // The minimum distance to change updates
    private static final long MIN_DISTANCE = 1; // 10 meters

    // The minimum time between updates
    private static final long MIN_TIME = 1; // 1 minute

    public LocationFinder(Context context) {
        this.context = context;
        location = getLocation();
    }

    public Location getLocation() {
        // Get location permissions
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(HomeActivity.activity, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                    255);
        }

        try {
            locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isGpsEnabled) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if(locManager != null) {
                    location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            } else if(isNetworkEnabled) {
                locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if(locManager != null) {
                    location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            } else {
                Toast.makeText(context, "NO GPS OR NETWORK", Toast.LENGTH_SHORT).show();
                // GPS AND NETWORK NOT AVAILABLE
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}