package com.lodygaj.findmybuddy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joey Laptop on 6/29/2017.
 */

public class UserFragment extends Fragment {
    private Context context;
    private FragmentManager fm;
    private Button btnLastKnown, btnSendRequest;
    private double latitude, longitude;
    private String user, friend, time;
    private TextView txtUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        context = getActivity().getApplicationContext();
        fm = getFragmentManager();

        // Create objects
        txtUser = (TextView) view.findViewById(R.id.txtUsername);
        btnLastKnown = (Button) view.findViewById(R.id.btnLastKnown);
        btnSendRequest = (Button) view.findViewById(R.id.btnLocRequest);

        // Get friend from bundle
        Bundle b = this.getArguments();
        if(b != null){
            friend = b.getString("Friend");
        }

        //  Set friend title
        txtUser.setText(friend);

        // Called when add friend button is clicked
        btnLastKnown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncGetLocation(context, friend).execute();
            }
        });

        // Called when add friend button is clicked
        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    // Method called to upgrade fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }


    /**
     * Class used for retrieving friends location data
     */
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

                latitude = Double.parseDouble(friends[0]);
                longitude = Double.parseDouble(friends[1]);
                time = friends[2];

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
                setFragment(mapFragment);
            }
            catch(Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }
    }

}
