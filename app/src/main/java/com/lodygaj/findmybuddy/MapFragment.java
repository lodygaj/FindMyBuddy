package com.lodygaj.findmybuddy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created on 6/30/2017.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String user, friend, time;
    private TextView txtUser, txtTime;
    private double latitude, longitude;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        user = SaveSharedPreference.getUserName(getActivity().getApplicationContext());

        // Get data from bundle
        Bundle b = this.getArguments();
        if(b != null){
            friend = b.getString("Friend");
            latitude = b.getDouble("Latitude");
            longitude = b.getDouble("Longitude");
            time = b.getString("Time");
        }

        // Set text to display user name shown
        txtUser = (TextView) view.findViewById(R.id.txtUser);
        txtUser.setText(friend);

        // Set text to display last known time
        txtTime = (TextView) view.findViewById(R.id.txtTime);
        txtTime.setText(time);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get user location
        LocationFinder locationFinder = new LocationFinder(getActivity().getApplicationContext());
        Double userLat = locationFinder.getLatitude();
        Double userLong = locationFinder.getLongitude();

        LatLng friendPoint = new LatLng(latitude, longitude);

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.addMarker(new MarkerOptions().position(friendPoint).title(friend));

        if(!friend.equals(user)) {
            LatLng userPoint = new LatLng(userLat, userLong);
            mMap.addMarker(new MarkerOptions().position(userPoint).title(user));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(friendPoint, 16.0f));

    }
}