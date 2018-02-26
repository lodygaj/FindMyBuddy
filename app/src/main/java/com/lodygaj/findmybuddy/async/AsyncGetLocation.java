package com.lodygaj.findmybuddy.async;

import com.lodygaj.findmybuddy.MapFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Joey Laptop on 2/1/2018.
 */

//    /**
//     * Class used for retrieving friends location data
//     */
public class AsyncGetLocation extends AsyncTask<String, Void, String> {
    private Context context;
    private String friend;
    private String parameters;
    private final String serverURL = "http://jlodyga.com/server/getLocation.php";

    public AsyncGetLocation(Context context, String friend) {
        this.context = context;
        this.friend = friend;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {
        //android.os.Debug.waitForDebugger();
        parameters = "user=" + friend;
        try {
            URL url = new URL(serverURL);
            URLConnection con = url.openConnection();

            con.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

            wr.write(parameters);
            wr.flush();
            wr.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = "";

            while((line = reader.readLine()) != null) {
                sb.append(line);
                break;
            }

            String result = sb.toString();
            return result;
        }
        catch(Exception e) {
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    public void onPostExecute(String array) {
        //android.os.Debug.waitForDebugger();
        try {
            // Create JSON array from input value
            JSONArray jArray = new JSONArray(array);
            // Create final array to be returned
            String[] friends = new String[jArray.length()];
            // Add each object from JSON Array to final array
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject object = jArray.getJSONObject(i);
                friends[i] = object.getString(Integer.toString(i));
            }

            Double latitude = Double.parseDouble(friends[0]);
            Double longitude = Double.parseDouble(friends[1]);
            String time = friends[2];

            // Add data to bundle
            Bundle b = new Bundle();
            b.putString("Friend", friend);
            b.putDouble("Latitude", latitude);
            b.putDouble("Longitude", longitude);
            b.putString("Time", time);

            Toast.makeText(context, time, Toast.LENGTH_LONG).show();

            // Set map fragment
            MapFragment mapFragment = new MapFragment();
            mapFragment.setArguments(b); // set bundle
            //setFragment(mapFragment);
        }
        catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}