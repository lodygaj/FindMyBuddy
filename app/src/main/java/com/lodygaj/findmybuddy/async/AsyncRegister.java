package com.lodygaj.findmybuddy.async;

/**
 * Created by Joey Laptop on 2/1/2018.
 */

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
 * Async class used to register new user in database
 */
public class AsyncRegister extends AsyncTask<String, Void, String> {
    private String parameters;
    private String username;
    private Context context;
    private String serverURL = "http://jlodyga.com/server/register.php";

    public AsyncRegister(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {
        try {
            username = (String) arg0[0];
            String password = (String) arg0[1];
            String email = (String) arg0[2];
            String firstName = (String) arg0[3];
            String lastName = (String) arg0[4];

            parameters = "username=" + username + "&password=" + password + "&email=" + email +
                    "&firstName=" + firstName + "&lastName=" + lastName;

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

            // Go to Home Activity
            Intent homeStartIntent = new Intent(context, HomeActivity.class);
            context.startActivity(homeStartIntent);

            Toast.makeText(context, "User successfully created!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Username already taken!", Toast.LENGTH_LONG).show();
        }
    }
}