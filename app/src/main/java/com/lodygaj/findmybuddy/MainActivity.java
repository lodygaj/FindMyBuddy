package com.lodygaj.findmybuddy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created on 6/29/2017.
 */
public class MainActivity extends AppCompatActivity {
    private EditText usernameField;
    private EditText passwordField;

    private User user;

    private DDBHelper ddbHelper;

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
        ddbHelper = new DDBHelper();

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
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        // Get user info from users table
        if(username.length() > 0 && password.length() > 0) {
            user = ddbHelper.getUser(username);

            // Verify credentials
            if(user != null && user.getPassword().equals(password)) {
                // Set username in shared preferences
                SaveSharedPreference.setUserName(getApplicationContext(), username);

                // Start menu activity
                Intent homeStartIntent = new Intent(getApplicationContext(), HomeActivity.class);
                getApplicationContext().startActivity(homeStartIntent);
            } else {
                Toast.makeText(getApplicationContext(), "Wrong username or Password!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Must enter a username and Password!", Toast.LENGTH_LONG).show();
        }
    }

    // Method called when "Register" button is clicked
    public void register(View view) {
        Intent registerStartIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerStartIntent);
        this.finish();
    }
}