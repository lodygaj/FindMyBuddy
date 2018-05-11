package com.lodygaj.findmybuddy;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 5/10/2018.
 */
public class HomeActivity extends AppCompatActivity {
    private FragmentManager fm;
    private DDBHelper ddbHelper;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the toolbar view inside the activity layout
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Set initial menu fragment
        fm = getSupportFragmentManager();
        setFragment(new MenuFragment());

        // Initialize Amazon DynamoDB client
        ddbHelper = new DDBHelper();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    // Handle action bar item clicks here.
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get user from shared preferences
        String user = SaveSharedPreference.getUserName(this);

        // Get id for action bar
        int id = item.getItemId();

        // Use switch to handle selected items
        switch(id) {
            case R.id.action_my_location:
                // Get and show users last known location
                getUserLocation(user);
                break;
            case R.id.action_update_location:
                // Set new user location
                setUserLocation(user);
                break;
            case R.id.action_logout:
                // Logout of application
                logout();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            this.finish();
        }
    }

    // Method called to show users last known location
    public void getUserLocation(String username) {
        // Get user location from users table
        user = ddbHelper.getUser(username);
        // Switch to map fragment
        setMapFragment(user);
    }

    // Method called to update a users location in database
    public void setUserLocation(String username) {
        // Get current latitude, longitude and timestamp
        LocationFinder locationFinder = new LocationFinder(this);
        User user = new User();
        user.setUsername(username);
        user.setLatitude(locationFinder.getLatitude());
        user.setLongitude(locationFinder.getLongitude());
        user.setTimestamp(getTimestamp());

        // Update user location in users table
        ddbHelper.setUserLocation(user);

        // Toast user that update was successful
        Toast.makeText(getApplicationContext(), "Location updated!", Toast.LENGTH_LONG).show();

        // Display updated location in map fragment
        setMapFragment(user);
    }

    // Method gets a timestamp
    public String getTimestamp() {
        return new SimpleDateFormat("M/d/yy h:mm a").format(new Date());
    }

    // Method called to update and set Map Fragment
    public void setMapFragment(User user) {
        // Add user data to bundle
        Bundle b = new Bundle();
        b.putString("Friend", user.getUsername());
        b.putDouble("Latitude", user.getLatitude());
        b.putDouble("Longitude", user.getLongitude());
        b.putString("Time", user.getTimestamp());

        // Set map fragment
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(b); // set bundle
        setFragment(mapFragment);
    }

    // Method called to update fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }

    public void logout() {
        // Log out
        SaveSharedPreference.setUserName(this, "");

        // Go to login screen
        Intent mainStartIntent = new Intent(this, MainActivity.class);
        startActivity(mainStartIntent);
        this.finish();
    }
}