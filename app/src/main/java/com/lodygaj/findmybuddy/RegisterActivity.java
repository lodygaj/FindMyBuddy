package com.lodygaj.findmybuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class RegisterActivity extends AppCompatActivity {
    private EditText firstNameField;
    private EditText lastNameField;
    private EditText usernameField;
    private EditText emailField;
    private EditText emailConfirmField;
    private EditText passwordField;
    private EditText passwordConfirmField;

    private DynamoDBMapper mapper;
    private Boolean userTaken = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Create objects from layout
        firstNameField = (EditText) findViewById(R.id.editTextFirstName);
        lastNameField = (EditText) findViewById(R.id.editTextLastName);
        usernameField = (EditText) findViewById(R.id.editTextUsername);
        emailField = (EditText) findViewById(R.id.editTextEmail);
        emailConfirmField = (EditText) findViewById(R.id.editTextEmailConfirm);
        passwordField = (EditText) findViewById(R.id.editTextPassword);
        passwordConfirmField = (EditText) findViewById(R.id.editTextPasswordConfirm);

        firstNameField.setText("Joe");
        lastNameField.setText("Lodyga");
        usernameField.setText("vigilante276");
        emailField.setText("lodygaj@hotmail.com");
        emailConfirmField.setText("lodygaj@hotmail.com");
        passwordField.setText("vigilante");
        passwordConfirmField.setText("vigilante");

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

    // Method called when "Register" button is clicked
    public void register(View view) {
        // Get user entries from edit text fields
        final String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String passwordConfirm = passwordConfirmField.getText().toString();
        String email = emailField.getText().toString();
        String emailConfirm = emailConfirmField.getText().toString();
        String firstName = firstNameField.getText().toString();
        String lastName = lastNameField.getText().toString();

        // Verifiy that password and password confirmation match
        if(password.equals(passwordConfirm) && !password.equals("")) {
            // Verify that email and email confirmation match
            if(email.equals(emailConfirm) && !email.equals("")) {
                if(firstName.equals("") || lastName.equals("") || username.equals("")) {
                    Toast.makeText(getApplicationContext(), "All fields must be filled out!", Toast.LENGTH_LONG).show();
                } else {
//                    // Submit new user to database
//                    new AsyncRegister(this).execute(username, password, email, firstName, lastName);

                    // Determine if username is taken
                    Runnable runnable = new Runnable() {
                        public void run() {
                            User user = mapper.load(User.class, username);
                            if(user == null) {
                                userTaken = false;
                            }
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

                    // Add user to database if username isn't taken
                    if(userTaken == false) {
                        // Create user object
                        final User user = new User();
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setEmail(email);
                        user.setFirstname(firstName);
                        user.setLastName(lastName);
                        user.setLatitude(0.0);
                        user.setLongitude(0.0);
                        user.setTimestamp("N/A");

                        // Add user to database
                        runnable = new Runnable() {
                            public void run() {
                                mapper.save(user);
                            }
                        };
                        mythread = new Thread(runnable);
                        mythread.start();

                        // Set username in shared preferences
                        SaveSharedPreference.setUserName(getApplicationContext(), username);

                        // Go to Home Activity
                        Intent homeStartIntent = new Intent(getApplicationContext(), HomeActivity.class);
                        getApplicationContext().startActivity(homeStartIntent);

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
                        Toast.makeText(getApplicationContext(), "Username already taken!", Toast.LENGTH_LONG).show();
                        usernameField.setText("");
                    }
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Emails do not match!", Toast.LENGTH_LONG).show();
                emailField.setText("");
                emailConfirmField.setText("");
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_LONG).show();
            passwordField.setText("");
            passwordConfirmField.setText("");
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

//    /**
//     * Async class used to register new user in database
//     */
//    public class AsyncRegister extends AsyncTask<String, Void, String> {
//        private String parameters;
//        private String username;
//        private Context context;
//        private String serverURL = "http://jlodyga.com/server/register.php";
//
//        public AsyncRegister(Context context) {
//            this.context = context;
//        }
//
//        protected void onPreExecute() {}
//
//        @Override
//        protected String doInBackground(String... arg0) {
//            try {
//                username = (String) arg0[0];
//                String password = (String) arg0[1];
//                String email = (String) arg0[2];
//                String firstName = (String) arg0[3];
//                String lastName = (String) arg0[4];
//
//                parameters = "username=" + username + "&password=" + password + "&email=" + email +
//                        "&firstName=" + firstName + "&lastName=" + lastName;
//
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
//
//                // Go to Home Activity
//                Intent homeStartIntent = new Intent(context, HomeActivity.class);
//                context.startActivity(homeStartIntent);
//
//                Toast.makeText(context, "User successfully created!", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(context, "Username already taken!", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}
