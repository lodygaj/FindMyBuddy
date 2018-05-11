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
public class RegisterActivity extends AppCompatActivity {
    private EditText firstNameField;
    private EditText lastNameField;
    private EditText usernameField;
    private EditText emailField;
    private EditText emailConfirmField;
    private EditText passwordField;
    private EditText passwordConfirmField;

    private DDBHelper ddbHelper;
    private Boolean userTaken = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get objects from layout
        firstNameField = (EditText) findViewById(R.id.editTextFirstName);
        lastNameField = (EditText) findViewById(R.id.editTextLastName);
        usernameField = (EditText) findViewById(R.id.editTextUsername);
        emailField = (EditText) findViewById(R.id.editTextEmail);
        emailConfirmField = (EditText) findViewById(R.id.editTextEmailConfirm);
        passwordField = (EditText) findViewById(R.id.editTextPassword);
        passwordConfirmField = (EditText) findViewById(R.id.editTextPasswordConfirm);

        // Initialize Amazon DynamoDB client
        ddbHelper = new DDBHelper();
    }

    // Method called when "Register" button is clicked
    public void register(View view) {
        // Get user entries from edit text fields
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String passwordConfirm = passwordConfirmField.getText().toString();
        String email = emailField.getText().toString();
        String emailConfirm = emailConfirmField.getText().toString();
        String firstName = firstNameField.getText().toString();
        String lastName = lastNameField.getText().toString();

        // Verify that password and password confirmation match
        if(password.equals(passwordConfirm) && !password.equals("")) {
            // Verify that email and email confirmation match
            if(email.equals(emailConfirm) && !email.equals("")) {
                if(firstName.equals("") || lastName.equals("") || username.equals("")) {
                    Toast.makeText(getApplicationContext(), "All fields must be filled out!", Toast.LENGTH_LONG).show();
                } else {
                    // Submit new user to database
                    User user = ddbHelper.getUser(username);
                    if(user == null) {
                        userTaken = false;
                    }

                    // Add user to database if username isn't taken
                    if(userTaken == false) {
                        // Create user object
                        user = new User();
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setEmail(email);
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setLatitude(0.0);
                        user.setLongitude(0.0);
                        user.setTimestamp("N/A");

                        // Add to users table
                        ddbHelper.addUser(user);

                        // Set username in shared preferences
                        SaveSharedPreference.setUserName(getApplicationContext(), username);

                        // Go to Home Activity
                        Intent homeStartIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        getApplicationContext().startActivity(homeStartIntent);

                        // Toast user to display success message
                        Toast.makeText(getApplicationContext(), "User created!", Toast.LENGTH_LONG).show();

                        // Clear fields
                        usernameField.setText("");
                        passwordField.setText("");
                        passwordConfirmField.setText("");
                        emailField.setText("");
                        emailConfirmField.setText("");
                        firstNameField.setText("");
                        lastNameField.setText("");
                    } else {
                        // Toast user that username is already taken
                        Toast.makeText(getApplicationContext(), "Username already taken!", Toast.LENGTH_LONG).show();
                        usernameField.setText(""); // Clear username field
                    }
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Emails do not match!", Toast.LENGTH_LONG).show();
                emailField.setText(""); // Clear email field
                emailConfirmField.setText(""); // Clear confirm email field
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_LONG).show();
            passwordField.setText(""); // Clear password field
            passwordConfirmField.setText(""); // Clear confirm password field
        }
    }

    // Method called when "Cancel" button is clicked
    public void cancel(View view) {
        // Go back to login screen
        Intent mainStartIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainStartIntent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        // Go back to login screen
        Intent mainStartIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainStartIntent);
        this.finish();
    }
}