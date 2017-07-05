package com.lodygaj.findmybuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    EditText usernameField;
    EditText passwordField;
    Button loginButton;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create objects from layout
        usernameField = (EditText) findViewById(R.id.editTextUsername);
        passwordField = (EditText) findViewById(R.id.editTextPassword);
        loginButton = (Button) findViewById(R.id.buttonLogin);
        registerButton = (Button) findViewById(R.id.buttonSubmit);

        // For testing purposes only
        usernameField.setText("vigilante276");
        passwordField.setText("vigilante");

        // Check if user is already logged in
        if(SaveSharedPreference.getUserName(MainActivity.this).length() != 0) {
            // Go to main menu
            Intent homeStartIntent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(homeStartIntent);
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Show update alert dialog if intent is true
        if (getIntent().getBooleanExtra("fromnotification", false) == true)
        {
            getIntent().removeExtra("fromnotification");
            startActivity(new Intent(getApplicationContext(), UpdateAlertDialog.class));
        }
    }

    // Method called when "Login" button is clicked
    public void login(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        // Check databse to verify that username and password are correct and login
        new AsyncLogin(this, username, password).execute();
    }

    // Method called when "Register" button is clicked
    public void register(View view) {
        Intent registerStartIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerStartIntent);
        this.finish();
    }

    /**
     * Async Class used to verify login credentials from database
     */
    public class AsyncLogin extends AsyncTask<String, Void, String> {
        private String parameters;
        private Context context;
        private String username, password;
        //private final String serverURL = "http://jlodyga.000webhostapp.com/login.php";
        private final String serverURL = "https://lodygaj.localtunnel.me/login.php";

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
}
