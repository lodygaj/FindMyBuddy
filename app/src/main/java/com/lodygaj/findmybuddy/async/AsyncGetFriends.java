package com.lodygaj.findmybuddy.async;

/**
 * Created by Joey Laptop on 2/1/2018.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 //     * Class used for retrieving friends from database and storing them in arraylist
 //     */
public class AsyncGetFriends extends AsyncTask<String, Void, String> {
    private Context context;
    private String user;
    private String parameters;
    private final String serverURL = "http://jlodyga.com/server/getFriends.php";

    public AsyncGetFriends(Context context, String user) {
        this.context = context;
        this.user = user;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {
        //android.os.Debug.waitForDebugger();
        parameters = "user=" + user;
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
        } catch(Exception e) {
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

            if(friends[0].equals("0")) {
                //contactListView.setVisibility(View.GONE);
            } else {
                // Set contacts
                //cAdapter.contacts = friends;
                //cAdapter.notifyDataSetChanged();
            }
            // Set contacts
            //cAdapter.contacts = friends;
            //cAdapter.notifyDataSetChanged();
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
            //cAdapter.contacts[0] = "server error";
            //cAdapter.notifyDataSetChanged();
        }
    }
}