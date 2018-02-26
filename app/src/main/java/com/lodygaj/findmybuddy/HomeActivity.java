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

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    private FragmentManager fm;
    private DynamoDBMapper mapper;
    private User selectedUser;

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
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.mapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
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
                // Show users last known location
                getUserLocation(user);
                break;
            case R.id.action_update_location:
                // Get current latitude and longitude
                LocationFinder locationFinder = new LocationFinder(this);
                Double latitude = locationFinder.getLatitude();
                Double longitude = locationFinder.getLongitude();
                // Get timestamp
                String timestamp = getTimestamp();
                // Call method to update location
                setUserLocation(user, latitude, longitude, timestamp);
                break;
            case R.id.action_logout:
                // Log out
                SaveSharedPreference.setUserName(this, "");
                // Go to login screen
                Intent mainStartIntent = new Intent(this, MainActivity.class);
                startActivity(mainStartIntent);
                this.finish();
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
    public void getUserLocation(final String user) {
        // Get user info from database
        Runnable runnable = new Runnable() {
            public void run() {
                selectedUser = mapper.load(User.class, user);
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
        // Wait for thread to complete
        try {
            mythread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Add user data to bundle
        Bundle b = new Bundle();
        b.putString("Friend", selectedUser.getUsername());
        b.putDouble("Latitude", selectedUser.getLatitude());
        b.putDouble("Longitude", selectedUser.getLongitude());
        b.putString("Time", selectedUser.getTimestamp());

        // Set map fragment
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(b); // set bundle
        setFragment(mapFragment);
    }

    // Method called to update a users location in database
    public void setUserLocation(final String user, final Double latitude, final Double longitude, final String timestamp) {
        // Update user in database
        Runnable runnable = new Runnable() {
            public void run() {
                selectedUser = mapper.load(User.class, user);
                selectedUser.setLatitude(latitude);
                selectedUser.setLongitude(longitude);
                selectedUser.setTimestamp(timestamp);
                mapper.save(selectedUser);
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
        // Wait for thread to complete
        try {
            mythread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Toast user that update was successful
        Toast.makeText(getApplicationContext(), "Location updated!", Toast.LENGTH_LONG).show();
    }

    // Method gets a timestamp
    public String getTimestamp() {
        return new SimpleDateFormat("M/d/yy h:mm a").format(new Date());
    }

    // Method called to update fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }
}