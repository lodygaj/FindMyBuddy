package com.lodygaj.findmybuddy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;


public class MainActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText passwordField;
    private User user;
    private String username;
    private String password;

    private DynamoDBMapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create objects from layout
        usernameField = (EditText) findViewById(R.id.editTextUsername);
        passwordField = (EditText) findViewById(R.id.editTextPassword);

        // For testing purposes only
        usernameField.setText("vigilante276");
        passwordField.setText("vigilante276");

        // Initialize Amazon DynamoDB client
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.mapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

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

    // Method called when "Login" button is clicked
    public void login(View view) {
        // Get username and password from fields
        username = usernameField.getText().toString();
        password = passwordField.getText().toString();

        // Retrieve user data from database
        Runnable runnable = new Runnable() {
            public void run() {
                user = mapper.load(User.class, username);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();
        // Wait for thread to complete
        try {
            myThread.join();
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
}