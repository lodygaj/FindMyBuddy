package com.lodygaj.findmybuddy;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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

        // Set initial fragment
        fm = getSupportFragmentManager();
        setFragment(new MenuFragment());

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:88f63976-d65f-4215-8dae-f887b0421644", // Identity pool ID
                Regions.US_EAST_1 // Region
        );

        // Initialize Amazon DynamoDB client
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

        // Initialize DynamoDB object mapper
        mapper = new DynamoDBMapper(ddbClient);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Get user from shared preferences
        String user = SaveSharedPreference.getUserName(this);

        // use switch to handle selected items
        switch(id) {
            case R.id.action_my_location:
                getUserLocation(user);
                break;
            case R.id.action_update_location:
                // Get latitude and longitude from location finder
                LocationFinder locationFinder = new LocationFinder(this);
                Double latitude = locationFinder.getLatitude();
                Double longitude = locationFinder.getLongitude();
                // Get timestamp
                String timestamp = getTimestamp();
                // Call async method to update location
                setUserLocation(user, latitude, longitude, timestamp);
                break;
            case R.id.action_logout:
                SaveSharedPreference.setUserName(this, "");
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

    // Method called to get a users location in database
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

         // Add data to bundle
        Bundle b = new Bundle();
        b.putString("Friend", selectedUser.getUsername());
        b.putDouble("Latitude", selectedUser.getLatitude());
        b.putDouble("Longitude", selectedUser.getLongitude());
        b.putString("Time", selectedUser.getTimestamp());

        Toast.makeText(getApplicationContext(), selectedUser.getTimestamp(), Toast.LENGTH_LONG).show();

        // Set map fragment
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(b); // set bundle
        setFragment(mapFragment);
    }

    // Method called to set a users location in database
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
        Toast.makeText(getApplicationContext(), "Location updated!", Toast.LENGTH_LONG).show();
    }

    public String getTimestamp() {
        return new SimpleDateFormat("M/d/yy h:mm a").format(new Date());
    }

    // Method called to update fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }

//    /**
//     * Class used for updating location in database
//     */
//    public class AsyncSetLocation extends AsyncTask<String, Void, String> {
//        private String parameters;
//        private Context context;
//        private String user, timestamp;
//        private Double latitude, longitude;
//        private final String serverURL = "http://jlodyga.com/server/setLocation.php";
//
//        public AsyncSetLocation(Context context, String user, Double latitude, Double longitude, String timestamp) {
//            this.context = context;
//            this.user = user;
//            this.latitude = latitude;
//            this.longitude = longitude;
//            this.timestamp = timestamp;
//        }
//
//        protected void onPreExecute() {}
//
//        @Override
//        protected String doInBackground(String... arg0) {
//            //android.os.Debug.waitForDebugger();
//            parameters = "user=" + user + "&latitude=" + latitude + "&longitude=" + longitude + "&timestamp=" + timestamp;
//            try {
//                URL url = new URL(serverURL);
//                URLConnection con = url.openConnection();
//
//                con.setDoOutput(true);
//                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
//
//                wr.write(parameters);
//                wr.flush();
//                wr.close();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                StringBuilder sb = new StringBuilder();
//                String line = "";
//
//                while((line = reader.readLine()) != null) {
//                    sb.append(line);
//                    break;
//                }
//
//                String result = sb.toString();
//                return result;
//            }
//            catch(Exception e) {
//                return new String("Exception: " + e.getMessage());
//            }
//        }
//
//        public void onPostExecute(String value) {
//            //android.os.Debug.waitForDebugger();
//            if(value.equals("1")) {
//                Toast.makeText(context, "Location updated!", Toast.LENGTH_LONG).show();
//            }
//            else {
//                Toast.makeText(context, "Failed to update location!", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    /**
//     * Class used for retrieving friends location data
//     */
//    public class AsyncGetLocation extends AsyncTask<String, Void, String> {
//        private Context context;
//        private String friend;
//        private String parameters;
//        private final String serverURL = "http://jlodyga.com/server/getLocation.php";
//
//        public AsyncGetLocation(Context context, String friend) {
//            this.context = context;
//            this.friend = friend;
//        }
//
//        @Override
//        protected void onPreExecute() {}
//
//        @Override
//        protected String doInBackground(String... arg0) {
//            //android.os.Debug.waitForDebugger();
//            parameters = "user=" + friend;
//            try {
//                URL url = new URL(serverURL);
//                URLConnection con = url.openConnection();
//
//                con.setDoOutput(true);
//                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
//
//                wr.write(parameters);
//                wr.flush();
//                wr.close();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                StringBuilder sb = new StringBuilder();
//                String line = "";
//
//                while((line = reader.readLine()) != null) {
//                    sb.append(line);
//                    break;
//                }
//
//                String result = sb.toString();
//                return result;
//            }
//            catch(Exception e) {
//                return new String("Exception: " + e.getMessage());
//            }
//        }
//
//        @Override
//        public void onPostExecute(String array) {
//            //android.os.Debug.waitForDebugger();
//            try {
//                // Create JSON array from input value
//                JSONArray jArray = new JSONArray(array);
//                // Create final array to be returned
//                String[] friends = new String[jArray.length()];
//                // Add each object from JSON Array to final array
//                for (int i = 0; i < jArray.length(); i++) {
//                    JSONObject object = jArray.getJSONObject(i);
//                    friends[i] = object.getString(Integer.toString(i));
//                }
//
//                Double latitude = Double.parseDouble(friends[0]);
//                Double longitude = Double.parseDouble(friends[1]);
//                String time = friends[2];
//
//                // Add data to bundle
//                Bundle b = new Bundle();
//                b.putString("Friend", friend);
//                b.putDouble("Latitude", latitude);
//                b.putDouble("Longitude", longitude);
//                b.putString("Time", time);
//
//                Toast.makeText(context, time, Toast.LENGTH_LONG).show();
//
//                // Set map fragment
//                MapFragment mapFragment = new MapFragment();
//                mapFragment.setArguments(b); // set bundle
//                setFragment(mapFragment);
//            }
//            catch(Exception e) {
//                System.out.println("Exception: " + e.getMessage());
//            }
//        }
//    }
}
