package com.lodygaj.findmybuddy.async;

/**
 * Created by Joey Laptop on 2/1/2018.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class used for deleting friends from database
 */
public class AsyncDeleteFriend extends AsyncTask<String, Void, String> {
    private String parameters;
    private Context context;
    private String user, friend;
    private final String serverURL = "http://jlodyga.com/server/deleteFriend.php";

    public AsyncDeleteFriend(Context context, String user, String friend) {
        this.context = context;
        this.user = user;
        this.friend = friend;
    }

    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {
        //android.os.Debug.waitForDebugger();
        parameters = "user=" + user + "&friend=" + friend;
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

    public void onPostExecute(String value) {
        //android.os.Debug.waitForDebugger();
        if(value.equals("1")) {
            Toast.makeText(context, "Friend deleted!", Toast.LENGTH_LONG).show();
            new AsyncGetFriends(context, user).execute(); // refresh friends list
            //contactListView.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(context, "Unable to delete!", Toast.LENGTH_LONG).show();
        }
    }
}