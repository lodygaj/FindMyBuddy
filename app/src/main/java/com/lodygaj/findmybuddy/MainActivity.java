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

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText passwordField;

    private DynamoDBMapper mapper;

    private User user;

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create objects from layout
        usernameField = (EditText) findViewById(R.id.editTextUsername);
        passwordField = (EditText) findViewById(R.id.editTextPassword);

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

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Method called when "Login" button is clicked
    public void login(View view) {
        // Get username and pasword from fields
        username = usernameField.getText().toString();
        password = passwordField.getText().toString();

//        // Check databse to verify that username and password are correct and login
//        new AsyncLogin(this, username, password).execute();

        // Retrieve user data from database
        Runnable runnable = new Runnable() {
            public void run() {
                user = mapper.load(User.class, username);
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

        // Credentials successfully verified
        if(user != null && user.getPassword().equals(password)) {
            // Set username in shared preferences
            SaveSharedPreference.setUserName(getApplicationContext(), username);
            // Start menu activity
            Intent homeStartIntent = new Intent(getApplicationContext(), HomeActivity.class);
            getApplicationContext().startActivity(homeStartIntent);
        } else {
            Toast.makeText(getApplicationContext(), "Wrong username or Password!", Toast.LENGTH_LONG).show();
        }
    }

    // Method called when "Register" button is clicked
    public void register(View view) {
        Intent registerStartIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerStartIntent);
        this.finish();
    }

//    /**
//     * Async Class used to verify login credentials from database
//     */
//    public class AsyncLogin extends AsyncTask<String, Void, String> {
//        private String parameters;
//        private Context context;
//        private String username, password;
//        private final String serverURL = "http://jlodyga.com/server/login.php";
//
//        public AsyncLogin(Context context, String username, String password) {
//            this.context = context;
//            this.username = username;
//            this.password = password;
//        }
//
//        protected void onPreExecute() {}
//
//        @Override
//        protected String doInBackground(String... arg0) {
//            parameters = "username=" + username + "&password=" + password;
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
//            if (value.equals("1")) {
//                // Set username in shared preferences
//                SaveSharedPreference.setUserName(context, username);
//                // Start menu activity
//                Intent homeStartIntent = new Intent(context, HomeActivity.class);
//                context.startActivity(homeStartIntent);
//            } else if (value.equals("0")) {
//                Toast.makeText(context, "Wrong username or Password!", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(context, "Cannot connect to server!", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}
