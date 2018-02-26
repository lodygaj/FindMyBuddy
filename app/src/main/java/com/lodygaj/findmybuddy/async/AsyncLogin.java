package com.lodygaj.findmybuddy.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.lodygaj.findmybuddy.HomeActivity;
import com.lodygaj.findmybuddy.SaveSharedPreference;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Joey Laptop on 2/1/2018.
 */

//
// Async Class used to verify login credentials from database
//
public class AsyncLogin extends AsyncTask<String, Void, String> {
    private String parameters;
    private Context context;
    private String username, password;
    private final String serverURL = "http://jlodyga.com/server/login.php";

    public AsyncLogin(Context context, String username, String password) {
        this.context = context;
        this.username = username;
        this.password = password;
    }

    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {
        parameters = "username=" + username + "&password=" + password;
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
        if (value.equals("1")) {
            // Set username in shared preferences
            SaveSharedPreference.setUserName(context, username);
            // Start menu activity
            Intent homeStartIntent = new Intent(context, HomeActivity.class);
            context.startActivity(homeStartIntent);
        } else if (value.equals("0")) {
            Toast.makeText(context, "Wrong username or Password!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Cannot connect to server!", Toast.LENGTH_LONG).show();
        }
    }
}