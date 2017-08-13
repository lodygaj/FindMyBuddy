package com.lodygaj.findmybuddy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    private Context context;
    private FragmentManager fm;
    public String[] contacts;
    public ContactAdapter cAdapter;
    public ListView contactListView;
    public EditText edtTxtFriend;
    public Button btnAddFriend;
    public String user, friend;
    private DynamoDBMapper mapper;
    private Friends selectedFriend;
    private PaginatedQueryList<Friends> result;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        context = getActivity().getApplicationContext();
        fm = getFragmentManager();

        // Get objects from layout
        contactListView = (ListView) view.findViewById(R.id.listViewContacts);
        edtTxtFriend = (EditText) view.findViewById(R.id.editTextFriend);
        btnAddFriend = (Button) view.findViewById(R.id.buttonAddFriend);

        // Get current user from SharedPreferences
        user = SaveSharedPreference.getUserName(context);

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity().getApplicationContext(),
                "us-east-1:88f63976-d65f-4215-8dae-f887b0421644", // Identity pool ID
                Regions.US_EAST_1 // Region
        );

        // Initialize Amazon DynamoDB client
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

        // Initialize DynamoDB object mapper
        mapper = new DynamoDBMapper(ddbClient);

        // Create contact list adapter
        cAdapter = new ContactAdapter(context, contacts);

        // Retrieve list of friends from database
        cAdapter.contacts = getUserFriends(user);
        //new AsyncGetFriends(context, user).execute();

        // Attach adapter to list view
        contactListView.setAdapter(cAdapter);

        // Set listener to determine what happens when item in list is clicked
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get selected user from list view
                friend = (String) contactListView.getItemAtPosition(position);

                // Add friend data to bundle
                Bundle b = new Bundle();
                b.putString("Friend", friend);

                // Load User Fragment
                UserFragment userFragment = new UserFragment();
                userFragment.setArguments(b); // set bundle
                setFragment(userFragment);
            }
        });

        // Set listener to determine what happens when item in list is long clicked
        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu pm = new PopupMenu(getContext(), view);
                Menu menu = pm.getMenu();
                menu.add("Delete");
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Get selected user
                        friend = (String) contactListView.getItemAtPosition(position);

                        // Delete from database
                        //new AsyncDeleteFriend(context, user, friend).execute();
                        selectedFriend = new Friends();

                        Runnable runnable = new Runnable() {
                            public void run() {
                                // Delete user/friend record from database
                                selectedFriend.setUser(user);
                                selectedFriend.setFriend(friend);
                                mapper.delete(selectedFriend);

                                // Delete friend/user record from database
                                selectedFriend.setUser(friend);
                                selectedFriend.setFriend(user);
                                mapper.delete(selectedFriend);
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

                        // Toast user to show successful deletion
                        Toast.makeText(context, "Friend deleted!", Toast.LENGTH_LONG).show();

                        // Refresh friends list to remove deleted user
                        cAdapter.contacts = getUserFriends(user);
                        cAdapter.notifyDataSetChanged();

                        return true;
                    }
                });
                pm.show();
                return true;
            }
        });

        // Called when add friend button is clicked
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close keyboard
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                String friend = edtTxtFriend.getText().toString();
                if(!friend.equals("")) {
                    //new AsyncAddFriend(context, user, friend).execute();
                    addFriend(friend);
                    edtTxtFriend.setText("");
                } else {
                    Toast.makeText(context, "Must enter a name!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    public String[] getUserFriends(final String user) {
        // Set hash key values
        Friends friendsToFind = new Friends();
        friendsToFind.setUser(user);

        // Create query with hash key values
        final DynamoDBQueryExpression query = new DynamoDBQueryExpression()
                .withHashKeyValues(friendsToFind)
                .withConsistentRead(false);

        // Run query and get result from database
        Runnable runnable = new Runnable() {
            public void run() {
                result = mapper.query(Friends.class, query);
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

        // Add query results to array list of friends
        String[] friends = new String[result.size()];
        int index = 0;
        for(Friends friend: result) {
            friends[index++] = friend.getFriend().toLowerCase();
        }

        return friends;
    }

    public void addFriend(final String friend) {
        // Add friend to database
        Runnable runnable = new Runnable() {
            public void run() {
                Friends friends = new Friends();
                // Add user/friend item
                friends.setUser(user);
                friends.setFriend(friend);
                mapper.save(friends);
                // Add friend/user item
                friends.setUser(friend);
                friends.setFriend(user);
                mapper.save(friends);
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

        Toast.makeText(context, "Friend added!", Toast.LENGTH_LONG).show();
        contactListView.setVisibility(View.VISIBLE);

        // Update list of friends from database
        cAdapter.contacts = getUserFriends(user);
        // Refresh list view to show new friend
        cAdapter.notifyDataSetChanged();
    }

    // Method called to upgrade fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }

//    /**
//     * Class used for retrieving friends from database and storing them in arraylist
//     */
//    public class AsyncGetFriends extends AsyncTask<String, Void, String> {
//        private Context context;
//        private String user;
//        private String parameters;
//        private final String serverURL = "http://jlodyga.com/server/getFriends.php";
//
//        public AsyncGetFriends(Context context, String user) {
//            this.context = context;
//            this.user = user;
//        }
//
//        @Override
//        protected void onPreExecute() {}
//
//        @Override
//        protected String doInBackground(String... arg0) {
//            //android.os.Debug.waitForDebugger();
//            parameters = "user=" + user;
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
//            } catch(Exception e) {
//                return new String("Exception: " + e.getMessage());
//            }
//        }
//
//        @Override
//        public void onPostExecute(String array) {
//            //android.os.Debug.waitForDebugger();
//            try {
//                // Create JSON array from input value
//                JSONArray jArray = new JSONArray(array);
//                // Create final array to be returned
//                String[] friends = new String[jArray.length()];
//                // Add each object from JSON Array to final array
//                for (int i = 0; i < jArray.length(); i++) {
//                    JSONObject object = jArray.getJSONObject(i);
//                    friends[i] = object.getString(Integer.toString(i));
//                }
//
//                if(friends[0].equals("0")) {
//                    contactListView.setVisibility(View.GONE);
//                } else {
//                    // Set contacts
//                    cAdapter.contacts = friends;
//                    cAdapter.notifyDataSetChanged();
//                }
//                // Set contacts
//                cAdapter.contacts = friends;
//                cAdapter.notifyDataSetChanged();
//            } catch(Exception e) {
//                System.out.println("Exception: " + e.getMessage());
//                cAdapter.contacts[0] = "server error";
//                cAdapter.notifyDataSetChanged();
//            }
//        }
//    }
//
//    /**
//     * Class used for adding new friends to database
//     */
//    public class AsyncAddFriend extends AsyncTask<String, Void, String> {
//        private String parameters;
//        private Context context;
//        private String user, friend;
//        private final String serverURL = "http://jlodyga.com/server/addFriend.php";
//
//        public AsyncAddFriend(Context context, String user, String friend) {
//            this.context = context;
//            this.user = user;
//            this.friend = friend;
//        }
//
//        protected void onPreExecute() {}
//
//        @Override
//        protected String doInBackground(String... arg0) {
//            //android.os.Debug.waitForDebugger();
//            parameters = "user=" + user + "&friend=" + friend;
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
//            } catch(Exception e) {
//                return new String("Exception: " + e.getMessage());
//            }
//        }
//
//        public void onPostExecute(String value) {
//            //android.os.Debug.waitForDebugger();
//            if(value.equals("2")) {
//                Toast.makeText(context, "Friend does not exist!", Toast.LENGTH_LONG).show();
//            } else if(value.equals("1")) {
//                Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_LONG).show();
//                new AsyncGetFriends(context, user).execute(); // refresh friends list
//                contactListView.setVisibility(View.VISIBLE);
//            } else {
//                Toast.makeText(context, "Friend already exists!", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    /**
//     * Class used for deleting friends from database
//     */
//    public class AsyncDeleteFriend extends AsyncTask<String, Void, String> {
//        private String parameters;
//        private Context context;
//        private String user, friend;
//        private final String serverURL = "http://jlodyga.com/server/deleteFriend.php";
//
//        public AsyncDeleteFriend(Context context, String user, String friend) {
//            this.context = context;
//            this.user = user;
//            this.friend = friend;
//        }
//
//        protected void onPreExecute() {}
//
//        @Override
//        protected String doInBackground(String... arg0) {
//            //android.os.Debug.waitForDebugger();
//            parameters = "user=" + user + "&friend=" + friend;
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
//            } catch(Exception e) {
//                return new String("Exception: " + e.getMessage());
//            }
//        }
//
//        public void onPostExecute(String value) {
//            //android.os.Debug.waitForDebugger();
//            if(value.equals("1")) {
//                Toast.makeText(context, "Friend deleted!", Toast.LENGTH_LONG).show();
//                new AsyncGetFriends(context, user).execute(); // refresh friends list
//                contactListView.setVisibility(View.VISIBLE);
//            } else {
//                Toast.makeText(context, "Unable to delete!", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}
