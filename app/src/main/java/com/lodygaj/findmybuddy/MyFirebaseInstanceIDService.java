/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lodygaj.findmybuddy;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Update token in databse
        sendRegistrationToServer(refreshedToken);
    }

    // Method called to update token in database
    private void sendRegistrationToServer(String token) {
        // Submit new token to database
        new AsyncUpdateFcm(this).execute(SaveSharedPreference.getUserName(getApplicationContext()), token);
    }

    /**
     * Async class used to register new user in database
     */
    public class AsyncUpdateFcm extends AsyncTask<String, Void, String> {
        private String parameters;
        private String username;
        private String fcmToken;
        private Context context;
        //private String serverURL = "http://jlodyga.000webhostapp.com/updateFcmToken.php";
        private String serverURL = "https://lodygaj.localtunnel.me/updateFcmToken.php";

        public AsyncUpdateFcm(Context context) {
            this.context = context;
        }

        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0) {
            try {
                username = (String) arg0[0];
                fcmToken = (String) arg0[1];

                parameters = "username=" + username + "&fcmToken=" + fcmToken;

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
                Toast.makeText(context.getApplicationContext(),
                        "Token Updated!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context.getApplicationContext(),
                        "Token Failed To Update!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
