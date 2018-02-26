package com.lodygaj.findmybuddy.async;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Joey Laptop on 2/1/2018.
 */

//    /**
//     * Class used for updating location in database
//     */
public class AsyncSetLocation extends AsyncTask<String, Void, String> {
    private String parameters;
    private Context context;
    private String user, timestamp;
    private Double latitude, longitude;
    private final String serverURL = "http://jlodyga.com/server/setLocation.php";

    public AsyncSetLocation(Context context, String user, Double latitude, Double longitude, String timestamp) {
        this.context = context;
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {
        //android.os.Debug.waitForDebugger();
        parameters = "user=" + user + "&latitude=" + latitude + "&longitude=" + longitude + "&timestamp=" + timestamp;
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

    public void onPostExecute(String value) {
        //android.os.Debug.waitForDebugger();
        if(value.equals("1")) {
            Toast.makeText(context, "Location updated!", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context, "Failed to update location!", Toast.LENGTH_LONG).show();
        }
    }
}